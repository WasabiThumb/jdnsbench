#include <stdint.h>
#include <stdlib.h>

#ifndef JDNSBENCH_API_H
#define JDNSBENCH_API_H

struct dnsbench_query_callback {
    void *reserved1;
    void *reserved2;
    void *reserved3;
    void (*open)(struct dnsbench_query_callback *self, uint64_t status, int32_t timeouts);
    void (*addV4)(struct dnsbench_query_callback *self, const char *v4, size_t v4len);
    void (*addV6)(struct dnsbench_query_callback *self, const char *v6, size_t v6len);
    void (*close)(struct dnsbench_query_callback *self);
};

typedef struct dnsbench_query_callback *dnsbench_query_callback_t;

//

uint64_t api_init();

uint64_t api_destroy();

uint64_t api_query(const char *server, const char *domain, dnsbench_query_callback_t callback);

#endif //JDNSBENCH_API_H
