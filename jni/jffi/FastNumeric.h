#ifndef JFFI_FASTNUMERIC_H
#define JFFI_FASTNUMERIC_H

#if (defined(__i386__) || defined(__x86_64__)) && defined(__GNUC__)
# define INT_BYPASS_FFI
#endif

#if defined(__x86_64__) && defined(__GNUC__)
# define LONG_BYPASS_FFI
#endif


# if defined(__x86_64) || defined(__amd64)
#  define CLEAR_VARARGS ({__asm__ __volatile__("xorq %%rax, %%rax" ::: "rax");})
# else
#  define CLEAR_VARARGS do { } while(0)
# endif

#if defined(INT_BYPASS_FFI)

# define invokeI0(ctx, fn, retval) do { \
            CLEAR_VARARGS; *(retval) = ((jint (*)()) (fn))(); \
    } while (0)

# define invokeI1(ctx, fn, retval, arg1) do { \
            CLEAR_VARARGS; *(retval) = ((jint (*)(jint)) (fn))(arg1); \
    } while (0)

# define invokeI2(ctx, fn, retval, arg1, arg2) do { \
            CLEAR_VARARGS; *(retval) = ((jint (*)(jint, jint)) (fn))((arg1), (arg2)); \
    } while (0)

# define invokeI3(ctx, fn, retval, arg1, arg2, arg3) do { \
            CLEAR_VARARGS; *(retval) = ((jint (*)(jint, jint, jint)) (fn))(arg1, arg2, arg3); \
    } while (0)

# define invokeI4(ctx, fn, retval, arg1, arg2, arg3, arg4) do { \
            CLEAR_VARARGS; *(retval) = ((jint (*)(jint, jint, jint, jint)) (fn))(arg1, arg2, arg3, arg4); \
    } while (0)

# define invokeI5(ctx, fn, retval, arg1, arg2, arg3, arg4, arg5) do { \
            CLEAR_VARARGS; *(retval) = ((jint (*)(jint, jint, jint, jint, jint)) (fn))(arg1, arg2, arg3, arg4, arg5); \
    } while (0)

# define invokeI6(ctx, fn, retval, arg1, arg2, arg3, arg4, arg5, arg6) do { \
            CLEAR_VARARGS; *(retval) = ((jint (*)(jint, jint, jint, jint, jint, jint)) (fn))(arg1, arg2, arg3, arg4, arg5, arg6); \
    } while (0)

#else /* non-i386, non-x86_64 */

# define invokeI0 ffi_call0
# define invokeI1 ffi_call1
# define invokeI2 ffi_call2
# define invokeI3 ffi_call3
# define invokeI4 ffi_call4
# define invokeI5 ffi_call5
# define invokeI6 ffi_call6

#endif



#if defined(LONG_BYPASS_FFI)

# define invokeL0(ctx, fn, retval) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)()) (fn))(); \
    } while (0)

# define invokeL1(ctx, fn, retval, arg1) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)(jlong)) (fn))(arg1); \
    } while (0)

# define invokeL2(ctx, fn, retval, arg1, arg2) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)(jlong, jlong)) (fn))((arg1), (arg2)); \
    } while (0)

# define invokeL3(ctx, fn, retval, arg1, arg2, arg3) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)(jlong, jlong, jlong)) (fn))(arg1, arg2, arg3); \
    } while (0)

# define invokeL4(ctx, fn, retval, arg1, arg2, arg3, arg4) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)(jlong, jlong, jlong, jlong)) (fn))(arg1, arg2, arg3, arg4); \
    } while (0)

# define invokeL5(ctx, fn, retval, arg1, arg2, arg3, arg4, arg5) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)(jlong, jlong, jlong, jlong, jlong)) (fn))(arg1, arg2, arg3, arg4, arg5); \
    } while (0)

# define invokeL6(ctx, fn, retval, arg1, arg2, arg3, arg4, arg5, arg6) do { \
            CLEAR_VARARGS; (retval)->j = ((jlong (*)(jlong, jlong, jlong, jlong, jlong, jlong)) (fn))(arg1, arg2, arg3, arg4, arg5, arg6); \
    } while (0)

#else /* non-i386, non-x86_64 */

# define invokeL0 ffi_call0
# define invokeL1 ffi_call1
# define invokeL2 ffi_call2
# define invokeL3 ffi_call3
# define invokeL4 ffi_call4
# define invokeL5 ffi_call5
# define invokeL6 ffi_call6

#endif

#endif /* JFFI_FASTNUMERIC_H */