#include <sys/types.h>
#include <stdio.h>
#include <stdint.h>
#if defined(_WIN32) || defined(__WIN32__)
# include <windows.h>
#else
# include <dlfcn.h>
#endif
#include <ffi.h>

#if defined(_WIN32) || defined(__WIN32__)
static void* dl_open(const char* name, int flags);
static void dl_error(char* buf, int size);
#define dl_sym(handle, name) GetProcAddress(handle, name)
#define dl_close(handle) FreeLibrary(handle)
enum { RTLD_LAZY=1, RTLD_NOW, RTLD_GLOBAL, RTLD_LOCAL };
#else
# define dl_open(name, flags) dlopen(name, flags != 0 ? flags : RTLD_LAZY)
# define dl_error(buf, size) do { snprintf(buf, size, "%s", dlerror()); } while(0)
# define dl_sym(handle, name) dlsym(handle, name)
# define dl_close(handle) dlclose(handle)
#ifndef RTLD_LOCAL
# define RTLD_LOCAL 8
#endif
#endif

#ifdef notyet
static VALUE
library_open(VALUE klass, VALUE libname, VALUE libflags)
{
    VALUE retval;
    Library* library;
    int flags;

    Check_Type(libflags, T_FIXNUM);

    retval = Data_Make_Struct(klass, Library, NULL, library_free, library);
    flags = libflags != Qnil ? NUM2UINT(libflags) : 0;

    library->handle = dl_open(libname != Qnil ? StringValueCStr(libname) : NULL, flags);
    if (library->handle == NULL) {
        char errmsg[1024];
        dl_error(errmsg, sizeof(errmsg));
        rb_raise(rb_eLoadError, "Could not open library '%s': %s",
                libname != Qnil ? StringValueCStr(libname) : "[current process]",
                errmsg);
    }
    rb_iv_set(retval, "@name", libname != Qnil ? libname : rb_str_new2("[current process]"));
    return retval;
}

static VALUE
library_dlsym(VALUE self, VALUE name)
{
    Library* library;
    void* address = NULL;
    Check_Type(name, T_STRING);

    Data_Get_Struct(self, Library, library);
    address = dl_sym(library->handle, StringValueCStr(name));
    return address != NULL ? rb_FFI_Pointer_new(address) : Qnil;
}

static VALUE
library_dlerror(VALUE self)
{
    char errmsg[1024];
    dl_error(errmsg, sizeof(errmsg));
    return rb_tainted_str_new2(errmsg);
}

static void
library_free(Library* library)
{
    if (library != NULL) {
        // dlclose() on MacOS tends to segfault - avoid it
#ifndef __APPLE__
        if (library->handle != NULL) {
            dl_close(library->handle);
        }
#endif
        library->handle = NULL;
        xfree(library);
    }
}

#if defined(_WIN32) || defined(__WIN32__)
static void*
dl_open(const char* name, int flags)
{
    if (name == NULL) {
        return GetModuleHandle(NULL);
    } else {
        return LoadLibraryEx(name, NULL, LOAD_WITH_ALTERED_SEARCH_PATH);
    }
}

static void
dl_error(char* buf, int size)
{
    FormatMessageA(FORMAT_MESSAGE_FROM_SYSTEM, NULL, GetLastError(),
            0, buf, size, NULL);
}
#endif
#endif // notyet