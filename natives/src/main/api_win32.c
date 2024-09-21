#ifdef _WIN32
#include <malloc.h>
#include <string.h>
#include <WinSock2.h>
#include <Windows.h>
#include <WinDNS.h>
#include <VersionHelpers.h>

#include "../headers/logging.h"
#include "../headers/api.h"

// START Utilities

PCWSTR strdup_u8_u16(const char* u8, uint64_t *err) {
    int size = MultiByteToWideChar(CP_UTF8, MB_PRECOMPOSED, u8, -1, NULL, 0);
    if (size == 0) {
        *err = E_WIN_LAST;
        return NULL;
    }
    WCHAR *u16 = malloc(sizeof(WCHAR) * size);
    if (u16 == NULL) {
        *err = E_NOMEM;
        return NULL;
    }
    if (MultiByteToWideChar(CP_UTF8, MB_PRECOMPOSED, u8, -1, u16, size) == 0) {
        *err = E_WIN_LAST;
        free(u16);
        return NULL;
    }
    return (PCWSTR) u16;
}

uint64_t parse_addr(const char* addr, DNS_ADDR *data) {
    uint64_t err = E_OK;
    PCWSTR addr_wide = strdup_u8_u16(addr, &err);
    if (addr_wide == NULL) return err;

    SOCKADDR_STORAGE sock_addr = { 0 };
    int address_length = sizeof(sock_addr);
    int wsa_err = WSAStringToAddressW((PWSTR) addr_wide, AF_INET, NULL, (LPSOCKADDR) &sock_addr, &address_length);
    free((void*) addr_wide);
    if (wsa_err != 0) {
        return E_WIN32(WSAGetLastError());
    }

    memcpy(data->MaxSa, &sock_addr, DNS_ADDR_MAX_SOCKADDR_LENGTH);

    return err;
}

// END Utilities

uint64_t api_init() {
    if (!IsWindows8OrGreater()) return E_STATE;
    return E_OK;
}

uint64_t api_destroy() {
    return E_OK;
}

typedef struct QUERY_STATE {
    DNS_QUERY_REQUEST request;
    DNS_ADDR_ARRAY addrs;
    DNS_QUERY_RESULT results;
    dnsbench_query_callback_t cb;
    PCWSTR domain;
} STATE;

void api_query_complete(STATE *state, PDNS_QUERY_RESULT results) {

    DNS_STATUS stat = results->QueryStatus;
    dnsbench_query_callback_t cb = state->cb;
    cb->open(cb, stat == ERROR_SUCCESS ? E_OK : E_WIN32(stat), 0);

    PDNS_RECORD records = results->pQueryRecords;
    char addr_buf[16];
    while (records != NULL) {
        if (records->wType == DNS_TYPE_A) {
            DNS_A_DATA *data = &(&records->Data)->A;
            IP4_ADDRESS address = data->IpAddress;
            size_t size = snprintf(
                    addr_buf,
                    16,
                    "%u.%u.%u.%u",
                    (unsigned int) (address & 0xFF),
                    (unsigned int) ((address >> 8) & 0xFF),
                    (unsigned int) ((address >> 16) & 0xFF),
                    (unsigned int) ((address >> 24) & 0xFF)
            );
            cb->addV4(cb, addr_buf, size);
        }
        records = records->pNext;
    }

    DnsRecordListFree(records, DnsFreeRecordList);
    free((void*) state->domain);
    free(state);
    cb->close(cb);
}

static void __stdcall api_query_callback(PVOID pQueryContext, PDNS_QUERY_RESULT pQueryResults) {
    api_query_complete((STATE*) pQueryContext, pQueryResults);
}

// IMPORTANT! Move server string
// Also, the callback should be opened and closed regardless of whether or not any IPs are added.
uint64_t api_query(const char *server, const char *domain, dnsbench_query_callback_t callback) {
    uint64_t err = E_OK;

    STATE *state = (STATE*) malloc(sizeof(struct QUERY_STATE));
    if (state == NULL) return E_NOMEM;
    state->cb = callback;

    PCWSTR domain_w = strdup_u8_u16(domain, &err);
    if (domain_w == NULL) {
        free(state);
        return err;
    }
    state->domain = domain_w;

    PDNS_ADDR_ARRAY addrs = &state->addrs;
    memset(addrs, 0, sizeof(DNS_ADDR_ARRAY));
    addrs->MaxCount = 1;
    addrs->AddrCount = 1;
    err = parse_addr(server, &addrs->AddrArray[0]);
    if (err != E_OK) {
        goto ex;
    }

    PDNS_QUERY_REQUEST request = &state->request;
    memset(request, 0, sizeof(DNS_QUERY_REQUEST));
    request->Version = DNS_QUERY_REQUEST_VERSION1;
    request->QueryName = domain_w;
    request->QueryType = DNS_TYPE_A;
    request->QueryOptions = (ULONG64) DNS_QUERY_BYPASS_CACHE;
    request->pDnsServerList = addrs;
    request->pQueryCompletionCallback = api_query_callback;
    request->pQueryContext = (PVOID) state;

    PDNS_QUERY_RESULT results = &state->results;
    memset(results, 0, sizeof(DNS_QUERY_RESULT));
    results->Version = DNS_QUERY_RESULTS_VERSION1;

    DNS_STATUS stat = DnsQueryEx(request, results, NULL);
    if (stat == DNS_REQUEST_PENDING) {
        return err;
    }
    if (stat == ERROR_SUCCESS) {
        api_query_complete(state, results);
        return err;
    }
    callback->open(callback, E_WIN32(stat), 0);
    callback->close(callback);

    ex:
    free((void*) domain_w);
    free(state);
    return err;
}

#endif