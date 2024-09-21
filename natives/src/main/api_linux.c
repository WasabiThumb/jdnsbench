#ifdef __linux__
#include <ares.h>
#include <stdint.h>
#include <pthread.h>
#include "../headers/logging.h"
#include "../headers/api.h"

typedef struct params {
    uint_fast8_t               init;
    pthread_mutex_t            dispatch_lock;
    ares_channel               channel;
    struct ares_addrinfo_hints hints;
} params_t;

static params_t PARAMS = { 0 };

uint64_t api_init() {
    if (PARAMS.init) return E_STATE;

    pthread_mutex_t            *dispatch_lock = &PARAMS.dispatch_lock;
    ares_channel                channel = NULL;
    struct ares_options         options;
    int                         optmask = 0;
    struct ares_addrinfo_hints *hints = &PARAMS.hints;
    int                         estat;

    pthread_mutex_t initializer = PTHREAD_MUTEX_INITIALIZER;
    memcpy(dispatch_lock, &initializer, sizeof(initializer));
    estat = pthread_mutex_init(dispatch_lock, NULL);
    if (estat != 0) {
        return E_UNIX(estat);
    }

    ares_library_init(ARES_LIB_INIT_ALL);
    if (!ares_threadsafety()) {
        return E_ARES(ARES_ENOTIMP);
    }

    memset(&options, 0, sizeof(options));
    optmask |= ARES_OPT_EVENT_THREAD;
    options.evsys = ARES_EVSYS_DEFAULT;

    estat = ares_init_options(&channel, &options, optmask);
    if (estat != ARES_SUCCESS) {
        return E_ARES(estat);
    }

    memset(hints, 0, sizeof(struct ares_addrinfo_hints));
    hints->ai_family = AF_UNSPEC;
    hints->ai_flags  = ARES_AI_CANONNAME;

    PARAMS.channel = channel;
    PARAMS.init = 1;
    return E_OK;
}

uint64_t api_destroy() {
    if (!PARAMS.init) return E_STATE;
    PARAMS.init = 0;

    int estat;

    ares_channel channel = PARAMS.channel;
    ares_queue_wait_empty(channel, -1);
    ares_destroy(channel);
    ares_library_cleanup();

    estat = pthread_mutex_destroy(&PARAMS.dispatch_lock);
    if (estat != 0) return E_UNIX(estat);

    return E_OK;
}

static void api_query00(void *arg, int status, int timeouts, struct ares_addrinfo *result) {
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

void api_query0(
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

    ares_getaddrinfo(channel, (const char*) moved_domain, NULL, hints, api_query00, params);
}

// IMPORTANT! Move server string
// Also, the callback should be opened and closed regardless of whether or not any IPs are added.
uint64_t api_query(const char *server, const char *domain, dnsbench_query_callback_t callback) {
    if (!PARAMS.init) return E_STATE;
    uint64_t err = E_OK;
    int estat;

    estat = pthread_mutex_lock(&PARAMS.dispatch_lock);
    if (estat != 0) {
        return E_UNIX(estat);
    }

    api_query0(server, domain, callback, PARAMS.channel, &PARAMS.hints, &err);

    estat = pthread_mutex_unlock(&PARAMS.dispatch_lock);
    if (estat != 0) {
        if (err != E_OK) return err;
        return E_UNIX(estat);
    }

    return err;
}

#endif