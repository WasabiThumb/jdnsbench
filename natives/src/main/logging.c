#include "../headers/logging.h"

#define DYN_MSG_CAPACITY 256

static const char EMSG_OK[] = "OK";
static char EMSG_UNKNOWN[] = "Unknown error (0x0000000000000000)";

// EFLAG_NATIVE
static const char EMSG_LINKAGE[] = "Linkage error";
static const char EMSG_STATE[] = "Illegal state";
static const char EMSG_NOMEM[] = "Out of memory";

// EFLAG_UNIX
#define EMSG_UNIX_S "System error: "
static char EMSG_UNIX[DYN_MSG_CAPACITY] = EMSG_UNIX_S;
#define EMSG_UNIX_L ((sizeof EMSG_UNIX_S) - ((size_t) 1))

#ifdef _WIN32
#include <windows.h>
// EFLAG_WIN32
#define EMSG_WIN32_S "Win32 System error: "
static char EMSG_WIN32[DYN_MSG_CAPACITY] = EMSG_WIN32_S;
#define EMSG_WIN32_L ((sizeof EMSG_WIN32_S) - ((size_t) 1))
#endif

#ifdef __linux__
#include <ares.h>
// EFLAG_ARES
#define EMSG_ARES_S "libcares error: "
static char EMSG_ARES[DYN_MSG_CAPACITY] = EMSG_ARES_S;
#define EMSG_ARES_L ((sizeof EMSG_ARES_S) - ((size_t) 1))
#endif

//

const char *logging_strerror_unknown(uint64_t code) {
    sprintf(&EMSG_UNKNOWN[17], "%llx)", code);
    return EMSG_UNKNOWN;
}

const char *logging_strerror_native(uint64_t code) {
    switch (code & E_LO) {
        case 1:
            return EMSG_LINKAGE;
        case 2:
            return EMSG_STATE;
        case 3:
            return EMSG_NOMEM;
    }
    return logging_strerror_unknown(code);
}

const char *logging_strerror_substitution(char* buf, size_t lbuf, const char* msg) {
    size_t lmsg = strlen(msg);

    size_t i = lbuf;
    size_t z = 0;
    for (; i < (DYN_MSG_CAPACITY - 1); i++) {
        if (z == lmsg) break;
        buf[i] = msg[z];
        z++;
    }
    buf[i] = '\0';

    return buf;
}

const char *logging_strerror_unix(uint64_t code) {
    return logging_strerror_substitution(EMSG_UNIX, EMSG_UNIX_L, strerror((int) (code & E_LO)));
}

#ifdef _WIN32
const char *logging_strerror_win32(uint64_t code) {
    char err[256];
    FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM, NULL, (DWORD) (code & E_LO), MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), err, 255, NULL);
    return logging_strerror_substitution(EMSG_WIN32, EMSG_WIN32_L, err);
}
#endif

#ifdef __linux__
const char *logging_strerror_ares(uint64_t code) {
    return logging_strerror_substitution(EMSG_ARES, EMSG_ARES_L, ares_strerror((int) (code & E_LO)));
}
#endif

const char *logging_strerror(uint64_t code) {
    switch (code & E_HI) {
        case 0UL:
            if (code == E_OK) return EMSG_OK;
            break;
        case EFLAG_NATIVE:
            return logging_strerror_native(code);
        case EFLAG_UNIX:
            return logging_strerror_unix(code);
#ifdef _WIN32
        case EFLAG_WIN32:
            return logging_strerror_win32(code);
#endif
#ifdef __linux__
        case EFLAG_ARES:
            return logging_strerror_ares(code);
#endif
    }
    return logging_strerror_unknown(code);
}
