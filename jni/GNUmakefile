# -*- makefile -*-

BUILD_OS := $(strip $(shell uname -s | tr '[:upper:]' '[:lower:]'))
OS ?= $(BUILD_OS)
ifeq ($(OS),sunos)
  OS = solaris
endif

# Default value of $OS on Windows is Windows_NT
ifeq ($(OS), Windows_NT)
    # that's how we detect x64...
    ifneq ($(findstring 64, $(BUILD_OS)),)
      OS = win64
    else
      OS = win32
    endif
endif

ifneq ($(findstring cygwin, $(BUILD_OS)),)
  # cygwin is always x32
  OS = win32
endif

CPU ?= $(shell uname -m | sed -e 's/i[345678]86/i386/')
MODEL = 32 # Default to 32bit compiles
PLATFORM = $(CPU)-$(OS)

JDK_HOME=$(shell if [ -d "$(JAVA_HOME)"/include ];then echo "$(JAVA_HOME)"; else echo "$(JAVA_HOME)"/..; fi)
# Set defaults to unix (linux/solaris/bsd)
PREFIX = lib
JNIEXT = so

export MACOSX_DEPLOYMENT_TARGET=10.4

CCACHE := $(strip $(realpath $(shell which ccache 2> /dev/null)))
SRC_DIR ?= $(shell pwd)/jni
JNI_DIR ?= $(SRC_DIR)
BUILD_DIR ?= $(shell pwd)/build

JFFI_SRC_DIR = $(SRC_DIR)/jffi
JFFI_BUILD_DIR = $(BUILD_DIR)/jffi

ifeq ($(USE_SYSTEM_LIBFFI),1)
  LIBFFI_LIBS ?= $(shell pkg-config --libs libffi)
  LIBFFI_CFLAGS ?= $(shell pkg-config --cflags libffi)
else
  LIBFFI_SRC_DIR = $(SRC_DIR)/libffi
  LIBFFI_BUILD_DIR = $(BUILD_DIR)/libffi-$(PLATFORM)
  LIBFFI = $(LIBFFI_BUILD_DIR)/.libs/libffi_convenience.a
  LIBFFI_LIBS = $(LIBFFI)
  LIBFFI_CFLAGS = -I"$(LIBFFI_BUILD_DIR)"/include
endif

SRCS = $(wildcard $(JFFI_SRC_DIR)/*.c)
OBJS = $(patsubst %.c, $(JFFI_BUILD_DIR)/%.o, $(notdir $(SRCS)))

vpath %.h $(JFFI_SRC_DIR)

LIBNAME = jffi
#
# Compiler/linker flags from:
#   http://weblogs.java.net/blog/kellyohair/archive/2006/01/compilation_of_1.html
JFLAGS = -fno-omit-frame-pointer -fno-strict-aliasing -DNDEBUG
OFLAGS = -O2 $(JFLAGS)

# MacOS headers aren't completely warning free, so turn them off
WERROR = -Werror
ifneq ($(OS),darwin)
  WFLAGS += -Wundef $(WERROR)
endif
WFLAGS += -W -Wall -Wno-unused -Wno-parentheses
PICFLAGS = -fPIC
SOFLAGS = # Filled in for each OS specifically
FFI_MMAP_EXEC = -DFFI_MMAP_EXEC_WRIT

FFI_CC = $(CCACHE) $(CC)
FFI_LD = $(LD)
FFI_CFLAGS = $(FFI_MMAP_EXEC) $(OFLAGS)
STRIP ?= strip -S

JDK_INCLUDES = -I"$(JDK_HOME)/include" -I"$(JDK_HOME)/include/$(OS)"
IFLAGS = -I"$(BUILD_DIR)" -I"$(BUILD_DIR)"/jni -I$(SRC_DIR) -I"$(JFFI_SRC_DIR)"
CFLAGS += $(OFLAGS) $(WFLAGS) $(IFLAGS) $(PICFLAGS) $(JDK_INCLUDES) $(LIBFFI_CFLAGS)
CFLAGS += -D_REENTRANT -D_LARGEFILE64_SOURCE -D_GNU_SOURCE

ifeq ($(OS), win64)
  override CPU = x86_64
  JDK_INCLUDES=-I$(JNI_DIR)/win32/include -I$(JNI_DIR)/win32/include/win32
  CC = x86_64-w64-mingw32-gcc -m64
  PICFLAGS =
  ifneq ($(findstring cygwin, $(BUILD_OS)),)
    CC += -mno-cygwin
    LDFLAGS += -mno-cygwin
  endif
  CFLAGS += -mwin32 -D_JNI_IMPLEMENTATION_
  LDFLAGS += -Wl,--add-stdcall-alias
  PICFLAGS=
  SOFLAGS += -shared -static-libgcc
  PREFIX =
  JNIEXT=dll  
  AR = x86_64-w64-mingw32-ar
  LD = x86_64-w64-mingw32-ld
  STRIP = x86_64-w64-mingw32-strip --strip-debug
  CONFIGURE_BUILD = x86_64-w64-mingw32
endif

ifeq ($(OS),cross-mingw32)
  override OS = win32
  override CPU = i386
  JDK_INCLUDES=-I$(JNI_DIR)/win32/include -I$(JNI_DIR)/win32/include/win32
  CC = i686-w64-mingw32-gcc
  LD = i686-w64-mingw32-ld
  STRIP = i686-w64-mingw32-strip --strip-debug
  export RANLIB = i686-w64-mingw32-ranlib
  CONFIGURE_HOST = i686-mingw32
  CROSS_COMPILE=1
endif


ifeq ($(OS),cross-win64)
  override CPU = x86_64
  override OS = win64
  JDK_INCLUDES=-I$(JNI_DIR)/win32/include -I$(JNI_DIR)/win32/include/win32
  CC = x86_64-w64-mingw32-gcc
  LD = x86_64-w64-mingw32-ld
  STRIP = x86_64-w64-mingw32-strip --strip-debug
  AR = x86_64-w64-mingw32-ar
  LD = x86_64-w64-mingw32-ld
  export RANLIB = x86_64-w64-mingw32-ranlib
  CONFIGURE_HOST = x86_64-w64-mingw32
  PICFLAGS =
  CROSS_COMPILE=1
  SOFLAGS += -shared -static-libgcc
  PREFIX =
  JNIEXT=dll
endif


ifneq ($(findstring cygwin,$(BUILD_OS)),)
  OS = win32
  JAVA_HOME := $(shell cygpath -u $(JAVA_HOME))
endif

ifeq ($(OS), win32)
  ifneq ($(findstring cygwin, $(BUILD_OS)),)
    CC += -mno-cygwin
    LDFLAGS += -mno-cygwin
  endif
  CFLAGS += -mwin32 -D_JNI_IMPLEMENTATION_
  # Linking against CRTMT.O is a workaround for GCC 4.4 bug
  # that ignores __thread (which we really need for errno handling).
  # See: http://n2.nabble.com/gcc-4-4-multi-threaded-exception-handling-thread-specifier-not-working-td3440749.html
  ifeq ($(findstring -w64-,$(CC)),)
    CRTMT.O = $(shell $(CC) -print-file-name=crtmt.o)
  endif
  LDFLAGS += -Wl,--add-stdcall-alias $(CRTMT.O)
  PICFLAGS=
  SOFLAGS += -shared -static-libgcc
  PREFIX =
  JNIEXT=dll
endif

ifeq ($(OS), darwin)
  PLATFORM = darwin
  ARCHES =
  ifneq ($(findstring $(CPU), ppc powerpc),)
    ARCHES += ppc
  endif
  ifneq ($(findstring $(CPU), i386 x86_64),)
    ARCHES += i386 x86_64
  endif

  ifneq ($(realpath $(wildcard /Xcode3/usr/bin/gcc)),)
    XCODE=/Xcode3
  else
    XCODE=$(shell xcode-select -print-path)
  endif
  ifneq ($(realpath $(wildcard $(XCODE)/usr/bin/gcc-4.0)),)
    CC = $(XCODE)/usr/bin/gcc-4.0
  else
    CC = $(XCODE)/usr/bin/gcc
  endif
  UNIVERSAL_SDK = $(firstword $(strip $(realpath $(XCODE)/SDKs/MacOSX10.4u.sdk) $(realpath $(XCODE)/SDKs/MacOSX10.5.sdk)))
  ifneq ($(UNIVERSAL_SDK),)
    MACSDK = $(UNIVERSAL_SDK)
    ifeq ($(findstring ppc, $(ARCHES)),)
      ARCHES += ppc
    endif
  else
    MACSDK = $(firstword $(strip $(wildcard $(XCODE)/SDKs/MacOSX10.*.sdk)) $(strip $(wildcard $(XCODE)/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.*.sdk)))
  endif

  CFLAGS += -isysroot $(MACSDK) -DTARGET_RT_MAC_CFM=0
  CFLAGS += -I$(MACSDK)/System/Library/Frameworks/JavaVM.framework/Headers
  CFLAGS += $(foreach arch, $(ARCHES),-arch $(arch))
  LDFLAGS = $(foreach arch, $(ARCHES),-arch $(arch)) -dynamiclib -framework JavaVM \
	-Wl,-syslibroot,$(MACSDK) -mmacosx-version-min=10.6
  JNIEXT = jnilib
  #CFLAGS += -I$(MACSDK)/System/Library/Frameworks/Kernel.framework/Versions/A/Headers
  CFLAGS += -fexceptions
  FFI_CFLAGS += -isysroot $(MACSDK) -I$(MACSDK)/usr/include
  PICFLAGS =
  SOFLAGS =
  #LIBS += $(JAVA_HOME)/jre/lib/libjsig.dylib
  OBJS += $(JFFI_BUILD_DIR)/darwin-longjmp.o $(JFFI_BUILD_DIR)/darwin-trampoline.o
endif

ifeq ($(OS), linux)
  SOFLAGS = -shared -static-libgcc -Wl,-soname,$(@F) -Wl,-O1
  CFLAGS += -pthread
endif

ifeq ($(OS), solaris)
  CC = gcc
  CFLAGS += -D__EXTENSIONS__ -std=c99
  LD = /usr/ccs/bin/ld
  SOFLAGS = -shared -static-libgcc
  LIBS += -ldl
  STRIP = strip
endif

ifeq ($(OS), aix)
  SOFLAGS = -shared -static-libgcc
  CFLAGS += -pthread
  LDFLAGS += -pthread
  JNIEXT = a
  STRIP = strip
endif

ifneq ($(findstring bsd, $(OS)),)
  SOFLAGS = -shared -static-libgcc
  CFLAGS += -pthread
  LDFLAGS += -pthread
endif

ifeq ($(CPU), i386)
  ifneq ($(findstring $(OS), linux),)
    CFLAGS += -march=i586 -mtune=generic
  endif
endif

ifeq ($(CPU), sparcv9)
  MODEL=64
endif

ifneq ($(findstring $(CPU), x86_64 amd64 ppc64 powerpc64 s390x),)
  MODEL = 64
endif

# On platforms (linux, solaris) that support both 32bit and 64bit, force building for one or the other
ifneq ($(strip $(findstring $(OS), solaris linux)),)
  # Change the CC/LD instead of CFLAGS/LDFLAGS, incase other things in the flags
  # makes the libffi build choke
  CC += -m$(MODEL)
  LD += -m$(MODEL)
endif

LIBJFFI = $(BUILD_DIR)/$(PREFIX)$(LIBNAME)-$(VERSION).$(JNIEXT)

LIBFFI_CONFIGURE = $(LIBFFI_SRC_DIR)/configure --disable-static \
	--with-pic=yes --disable-dependency-tracking
ifdef CONFIGURE_HOST
	LIBFFI_CONFIGURE += --host=$(CONFIGURE_HOST)
endif

ifdef CONFIGURE_BUILD
	LIBFFI_CONFIGURE += --build=$(CONFIGURE_BUILD)
endif

all:	$(LIBJFFI)

debug:
	@echo OS="$(OS)"
	@echo BUILD_OS="$(BUILD_OS)"
	@echo CPU="$(CPU)"
	@echo JAVA_HOME="$(JAVA_HOME)"
	@echo JDK_HOME="$(JDK_HOME)"
	@echo "PLATFORM=$(PLATFORM)"
	@echo "JFFI_BUILD_DIR=$(JFFI_BUILD_DIR)"
	@echo "OBJS=$(OBJS)"

$(LIBJFFI):  $(OBJS) $(LIBFFI_LIBS)
	$(CC) -o $@ $(LDFLAGS) $(SOFLAGS) $(OBJS) $(LIBFFI_LIBS) $(LIBS)
	$(STRIP) $@

$(BUILD_DIR)/%.o : $(SRC_DIR)/%.c $(wildcard $(JFFI_SRC_DIR)/*.h)
	@mkdir -p $(@D)
	@$(CCACHE) $(CC) $(CFLAGS) -c $< -o $@

$(BUILD_DIR)/%.o : $(SRC_DIR)/%.S $(wildcard $(JFFI_SRC_DIR)/*.h)
	@mkdir -p $(@D)
	@$(CC) $(CFLAGS) -o $@ -c $<

$(OBJS) : $(LIBFFI_LIBS)

ifeq ($(OS), darwin)
build_ffi = \
	mkdir -p $(BUILD_DIR)/libffi-darwin-$(1); \
	(if [ ! -f $(BUILD_DIR)/libffi-darwin-$(1)/Makefile ]; then \
	    echo "Configuring libffi for $(1)"; \
	    cd $(BUILD_DIR)/libffi-darwin-$(1) && \
	      env CC="$(CCACHE) $(CC)" CFLAGS="-arch $(1) $(FFI_CFLAGS)" LDFLAGS="-arch $(1)" \
		$(LIBFFI_CONFIGURE) --host=$(1)-apple-darwin > /dev/null; \
	fi); \
	env MACOSX_DEPLOYMENT_TARGET=10.4 $(MAKE) -C $(BUILD_DIR)/libffi-darwin-$(1)

$(LIBFFI):
	@mkdir -p $(@D)
	@for arch in $(ARCHES); do $(call build_ffi,$$arch);done
	# Assemble into a FAT (i386, x86_64, ppc) library
	@mkdir -p $(BUILD_DIR)/libffi/.libs
	@env MACOSX_DEPLOYMENT_TARGET=10.4 /usr/bin/libtool -static -o $@ \
	    $(foreach arch, $(ARCHES),$(BUILD_DIR)/libffi-darwin-$(arch)/.libs/libffi_convenience.a)
	@mkdir -p $(LIBFFI_BUILD_DIR)/include
	$(RM) $(LIBFFI_BUILD_DIR)/include/ffi.h
	@( \
		printf "#if defined(__i386__)\n"; \
		printf "#include \"libffi-darwin-i386/include/ffi.h\"\n"; \
		printf "#elif defined(__x86_64__)\n"; \
		printf "#include \"libffi-darwin-x86_64/include/ffi.h\"\n";\
		printf "#elif defined(__ppc__)\n"; \
		printf "#include \"libffi-darwin-ppc/include/ffi.h\"\n";\
		printf "#endif\n";\
	) > $(LIBFFI_BUILD_DIR)/include/ffi.h
	@( \
		printf "#if defined(__i386__)\n"; \
		printf "#include \"libffi-darwin-i386/include/ffitarget.h\"\n"; \
		printf "#elif defined(__x86_64__)\n"; \
		printf "#include \"libffi-darwin-x86_64/include/ffitarget.h\"\n";\
		printf "#elif defined(__ppc__)\n"; \
		printf "#include \"libffi-darwin-ppc/include/ffitarget.h\"\n";\
		printf "#endif\n";\
	) > $(LIBFFI_BUILD_DIR)/include/ffitarget.h
else
$(LIBFFI):		
	@mkdir -p $(LIBFFI_BUILD_DIR)
	@if [ ! -f $(LIBFFI_BUILD_DIR)/Makefile ]; then \
	    echo "Configuring libffi for $(PLATFORM)"; \
	    cd $(LIBFFI_BUILD_DIR) && env CC="$(FFI_CC)" LD="$(FFI_LD)" CFLAGS="$(FFI_CFLAGS)" \
		$(LIBFFI_CONFIGURE) > /dev/null; \
	fi
	$(MAKE) -C $(LIBFFI_BUILD_DIR)
endif


clean::
	# nothing to do - ant will delete the build dir

