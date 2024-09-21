#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>

#ifndef JDNSBENCH_LOGGING_H
#define JDNSBENCH_LOGGING_H

#define E_OK 0UL
#define E_LO 0x00000000FFFFFFFFUL
#define E_HI 0xFFFFFFFF00000000UL

#define EFLAG_NATIVE ((uint64_t) (1ULL << 32))
#define E_NATIVE(c) (EFLAG_NATIVE | (((uint64_t) (c)) & E_LO))
#define E_LINKAGE E_NATIVE(1)
#define E_STATE E_NATIVE(2)
#define E_NOMEM E_NATIVE(3)

#define EFLAG_UNIX ((uint64_t) (1ULL << 33))
#define E_UNIX(c) (EFLAG_UNIX | (((uint64_t) (c)) & E_LO))
#define E_ERRNO E_UNIX(errno)

#ifdef _WIN32
#include <windows.h>
#define EFLAG_WIN32 ((uint64_t) (1ULL << 34))
#define E_WIN32(c) (EFLAG_WIN32 | (((uint64_t) (c)) & E_LO))
#define E_WIN_LAST E_WIN32(GetLastError())
#endif

#ifdef __linux__
#define EFLAG_ARES ((uint64_t) (1UL << 35UL))
#define E_ARES(c) (EFLAG_ARES | (((uint64_t) (c)) & E_LO))
#endif

const char *logging_strerror(uint64_t code);

#endif //JDNSBENCH_LOGGING_H
