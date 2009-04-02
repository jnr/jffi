#ifndef JFFI_ENDIAN_H
#define JFFI_ENDIAN_H

#include <sys/param.h>
#include <sys/types.h>

#ifdef __linux__
#  include_next <endian.h>
#endif

#ifdef __sun
# include <sys/byteorder.h>
# define LITTLE_ENDIAN 1234
# define BIG_ENDIAN 4321
# ifdef _BIG_ENDIAN
#  define BYTE_ORDER BIG_ENDIAN
# else
#  define BYTE_ORDER LITTLE_ENDIAN
# endif
#endif

#endif /* JFFI_ENDIAN_H */
