#ifdef __linux__
#include <ares.h>
#include <stdint.h>
#include <stdbool.h>
#include <pthread.h>
#include <unistd.h>
#include "../headers/logging.h"
#include "../headers/api.h"

#ifndef ARES_OPT_EVENT_THREAD
#define USE_DISPATCH_THREAD 1
#endif

typedef struct prepared_query {
    ares_channel channel;
    const char *domain;
    struct ares_addrinfo_hints *hints;
    void *params;
} prepared_query_t;

#ifdef USE_DISPATCH_THREAD
#include <poll.h>

typedef struct {
    ares_channel   channel;
    struct pollfd *fds;
    size_t         nfds;
} dnsstate_t;

typedef struct dispatch_queue_node {
    prepared_query_t *query;
    struct dispatch_queue_node *next;
} dispatch_queue_node_t;
#endif

typedef struct params {
    uint_fast8_t               init;
    pthread_mutex_t            dispatch_lock;
    ares_channel               channel;
    struct ares_addrinfo_hints hints;
#ifdef USE_DISPATCH_THREAD
    pthread_t                  dispatch_thread;
    dispatch_queue_node_t     *dispatch_queue;
    dnsstate_t                 dispatch_sock_state;
#endif
} params_t;

static params_t PARAMS = { 0 };

static void api_query_callback(void *arg, int status, int timeouts, struct ares_addrinfo *result) {
    dnsbench_query_callback_t cb = *((dnsbench_query_callback_t*) arg);
    (cb)->open(cb, status == ARES_SUCCESS ? E_OK : E_ARES(status), timeouts);

    if (result) {
        struct ares_addrinfo_node *node;
        char buf[64];
        uint_fast8_t v6;
        for (node = result->nodes; node != NULL; node = node->ai_next) {
            const void *ptr = NULL;
            if (node->ai_family == AF_INET) {
                v6 = 0;
                const struct sockaddr_in *in_addr = (const struct sockaddr_in *)((void *)node->ai_addr);
                ptr = &in_addr->sin_addr;
            } else if (node->ai_family == AF_INET6) {
                v6 = 1;
                const struct sockaddr_in6 *in_addr =
                        (const struct sockaddr_in6 *)((void *)node->ai_addr);
                ptr = &in_addr->sin6_addr;
            } else {
                continue;
            }
            ares_inet_ntop(node->ai_family, ptr, buf, sizeof(buf));
            if (v6) {
                cb->addV6(cb, buf, INET6_ADDRSTRLEN);
            } else {
                cb->addV4(cb, buf, INET_ADDRSTRLEN);
            }
        }
    }

    cb->close(cb);
    ares_freeaddrinfo(result);
    free(arg);
}

void api_query_dispatch_sync(prepared_query_t *query) {
    ares_getaddrinfo(query->channel, query->domain, NULL, query->hints, api_query_callback, query->params);
}

#ifdef USE_DISPATCH_THREAD
void api_query_dispatch_async(dispatch_queue_node_t *node) {
    dispatch_queue_node_t *head = PARAMS.dispatch_queue;
    if (head == NULL) {
        PARAMS.dispatch_queue = node;
    } else {
        dispatch_queue_node_t *next = head->next;
        while (next != NULL) {
            head = next;
            next = head->next;
        }
        head->next = node;
    }
}
#endif

void api_query_dispatch(
        const char *server,
        const char *domain,
        dnsbench_query_callback_t callback,
        ares_channel channel,
        struct ares_addrinfo_hints *hints,
        uint64_t *err
) {
    int estat;

    estat = ares_set_servers_csv(channel, server);
    if (estat != ARES_SUCCESS) {
        *err = E_ARES(estat);
        return;
    }

    size_t domain_l = strlen(domain);
    void *params = malloc(sizeof(void*) + domain_l + 1);
    if (params == NULL) {
        *err = E_NOMEM;
        return;
    }
    *((dnsbench_query_callback_t*) params) = callback;
    char* moved_domain = (char*) (params + sizeof(void*));
    memcpy(moved_domain, domain, domain_l); // NOLINT(bugprone-not-null-terminated-result)
    moved_domain[domain_l] = '\0';

    prepared_query_t prepared = { channel, (const char*) moved_domain, hints, params };
#ifdef USE_DISPATCH_THREAD
    prepared_query_t *prepared_moved = (prepared_query_t*) malloc(sizeof(prepared_query_t));
    if (prepared_moved == NULL) {
        *err = E_NOMEM;
        free(params);
        return;
    }
    memcpy(prepared_moved, &prepared, sizeof(prepared_query_t));

    dispatch_queue_node_t *node = (dispatch_queue_node_t*) malloc(sizeof(dispatch_queue_node_t));
    if (node == NULL) {
        *err = E_NOMEM;
        free(prepared_moved);
        free(params);
        return;
    }
    node->query = prepared_moved;
    node->next = NULL;

    api_query_dispatch_async(node);
#else
    api_query_dispatch_sync(&prepared);
#endif
}

#ifdef USE_DISPATCH_THREAD
void sock_state_cb(void *data, ares_socket_t socket_fd, int readable, int writable) {
    dnsstate_t *state = data;
    size_t      idx;

    for (idx=0; idx<state->nfds; idx++) {
        if (state->fds[idx].fd == socket_fd) {
            break;
        }
    }

    if (idx >= state->nfds) {
        if (!readable && !writable) {
            return;
        }

        state->nfds++;
        state->fds = realloc(state->fds, sizeof(*state->fds) * state->nfds);
    } else {
        if (!readable && !writable) {
            memmove(&state->fds[idx], &state->fds[idx+1],
                    sizeof(*state->fds) * (state->nfds - idx - 1));
            state->nfds--;
            return;
        }
    }

    state->fds[idx].fd     = socket_fd;
    state->fds[idx].events = 0;
    if (readable) {
        state->fds[idx].events |= POLLIN;
    }
    if (writable) {
        state->fds[idx].events |= POLLOUT;
    }
}

void sock_state_process(dnsstate_t *state) {
    struct timeval tv;

    while (1) {
        int            rv;
        int            timeout;
        size_t         i;
        struct pollfd *fds;
        size_t         nfds;

        if (ares_timeout(state->channel, NULL, &tv) == NULL) {
            break;
        }

        timeout = tv.tv_sec * 1000 + tv.tv_usec / 1000; // NOLINT(cppcoreguidelines-narrowing-conversions)

        rv = poll(state->fds, state->nfds, timeout);
        if (rv < 0) {
            continue;
        } else if (rv == 0) {
            ares_process_fd(state->channel, ARES_SOCKET_BAD, ARES_SOCKET_BAD);
            continue;
        }

        nfds = state->nfds;
        fds  = malloc(sizeof(*fds) * nfds);
        memcpy(fds, state->fds, sizeof(*fds) * nfds);

        for (i=0; i<nfds; i++) {
            if (fds[i].revents == 0) {
                continue;
            }

            ares_process_fd(state->channel,
                            (fds[i].revents & (POLLERR|POLLHUP|POLLIN))?fds[i].fd:ARES_SOCKET_BAD,
                            (fds[i].revents & POLLOUT)?fds[i].fd:ARES_SOCKET_BAD);
        }

        free(fds);
    }
}

static void *dispatch_thread_loop(void* arg) {
    params_t *params = (params_t*) arg;
    dnsstate_t *sock_state = &params->dispatch_sock_state;
    bool empty;
    bool loop = true;

    while (loop) {
        pthread_mutex_lock(&params->dispatch_lock);
        dispatch_queue_node_t *node = params->dispatch_queue;
        empty = (node == NULL);
        if (!empty) {
            params->dispatch_queue = NULL;
        }
        pthread_mutex_unlock(&params->dispatch_lock);

        if (empty) goto wait;

        prepared_query_t *query;
        do {
            query = node->query;
            if (query == NULL) {
                loop = false;
            } else {
                api_query_dispatch_sync(query);
                free(query);
            }

            dispatch_queue_node_t *temp = node->next;
            free(node);
            node = temp;
        } while (node != NULL);
        sock_state_process(sock_state);

        wait:
        usleep(100);
    }
}
#endif

uint64_t api_query(const char *server, const char *domain, dnsbench_query_callback_t callback) {
    if (!PARAMS.init) return E_STATE;
    uint64_t err = E_OK;
    int estat;

    estat = pthread_mutex_lock(&PARAMS.dispatch_lock);
    if (estat != 0) {
        return E_UNIX(estat);
    }

    api_query_dispatch(server, domain, callback, PARAMS.channel, &PARAMS.hints, &err);

    estat = pthread_mutex_unlock(&PARAMS.dispatch_lock);
    if (estat != 0) {
        if (err != E_OK) return err;
        return E_UNIX(estat);
    }

    return err;
}

uint64_t api_init() {
    if (PARAMS.init) return E_STATE;
    uint64_t err = E_OK;
    int estat;

#ifdef USE_DISPATCH_THREAD
    dnsstate_t *sock_state = &PARAMS.dispatch_sock_state;
    memset(sock_state, 0, sizeof(dnsstate_t));

    PARAMS.dispatch_queue = NULL;
    pthread_t dispatch_thread;
    estat = pthread_create(&dispatch_thread, NULL, dispatch_thread_loop, &PARAMS);
    if (estat != 0) {
        err = E_UNIX(estat);
        goto exit_final;
    }
    PARAMS.dispatch_thread = dispatch_thread;

    ares_channel *channel_ptr = &sock_state->channel;
#else
    ares_channel channel = NULL;
    ares_channel *channel_ptr = &channel;
#endif

    pthread_mutex_t            *dispatch_lock = &PARAMS.dispatch_lock;
    struct ares_options         options;
    int                         optmask = 0;
    struct ares_addrinfo_hints *hints = &PARAMS.hints;

    pthread_mutex_t initializer = PTHREAD_MUTEX_INITIALIZER;
    memcpy(dispatch_lock, &initializer, sizeof(initializer));
    estat = pthread_mutex_init(dispatch_lock, NULL);
    if (estat != 0) {
        err = E_UNIX(estat);
        goto exit_exceptionally_1;
    }

    ares_library_init(ARES_LIB_INIT_ALL);
#ifndef USE_DISPATCH_THREAD
    if (!ares_threadsafety()) {
        err = E_ARES(ARES_ENOTIMP);
        goto exit_exceptionally_2;
    }
#endif

    memset(&options, 0, sizeof(options));
#ifdef USE_DISPATCH_THREAD
    optmask |= ARES_OPT_SOCK_STATE_CB;
    options.sock_state_cb = sock_state_cb;
    options.sock_state_cb_data = sock_state;
#else
    optmask |= ARES_OPT_EVENT_THREAD;
    options.evsys = ARES_EVSYS_DEFAULT;
#endif

    estat = ares_init_options(channel_ptr, &options, optmask);
    if (estat != ARES_SUCCESS) {
        err = E_ARES(estat);
        goto exit_exceptionally_2;
    }

    memset(hints, 0, sizeof(struct ares_addrinfo_hints));
    hints->ai_family = AF_UNSPEC;
    hints->ai_flags  = ARES_AI_CANONNAME;

    PARAMS.channel = *channel_ptr;
    PARAMS.init = 1;

    goto exit_final;
    exit_exceptionally_2:
    pthread_mutex_destroy(dispatch_lock);
    exit_exceptionally_1:
#ifdef USE_DISPATCH_THREAD
    pthread_cancel(dispatch_thread);
#endif
    exit_final:
    return err;
}

uint64_t api_destroy() {
    if (!PARAMS.init) return E_STATE;
    PARAMS.init = 0;
    int estat;

#ifdef USE_DISPATCH_THREAD
    dispatch_queue_node_t *exit_node = (dispatch_queue_node_t*) malloc(sizeof(dispatch_queue_node_t));
    if (exit_node == NULL) return E_NOMEM;

    exit_node->query = NULL;
    exit_node->next = NULL;

    pthread_mutex_lock(&PARAMS.dispatch_lock);
    api_query_dispatch_async(exit_node);
    pthread_mutex_unlock(&PARAMS.dispatch_lock);
    pthread_join(PARAMS.dispatch_thread, NULL);
#endif

    ares_channel channel = PARAMS.channel;
#ifndef USE_DISPATCH_THREAD
    ares_queue_wait_empty(channel, -1);
#endif
    ares_destroy(channel);
    ares_library_cleanup();

    estat = pthread_mutex_destroy(&PARAMS.dispatch_lock);
    if (estat != 0) return E_UNIX(estat);

    return E_OK;
}

#endif