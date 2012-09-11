# -*- makefile -*-
BUILD_OS := $(strip $(shell uname -s | tr '[:upper:]' '[:lower:]'))
OS ?= $(BUILD_OS)

# Default value of $OS on Windows is Windows_NT
ifeq ($(OS), Windows_NT)
    # that's how we detect x64...
    ifneq ($(findstring 64, $(BUILD_OS)),)
      OS = win64
    else
      OS = win32
    endif
endif

CPU = $(shell uname -m | sed -e 's/i[345678]86/i386/')
MODEL = 32 # Default to 32bit compiles
PLATFORM = $(CPU)-$(OS)

ifeq ($(OS), sunos)
  OS = solaris
endif

ifneq ($(findstring cygwin,$(BUILD_OS)),)
  # cygwin is always x32 for now
  OS = win32
endif

SRC_DIR = libtest
BUILD_DIR ?= build
TEST_BUILD_DIR = $(BUILD_DIR)/libtest
# Set defaults to unix (linux/solaris/bsd)
PREFIX = lib
LIBEXT = so
LIBNAME = $(PREFIX)test.$(LIBEXT)

export MACOSX_DEPLOYMENT_TARGET=10.4

CCACHE := $(strip $(realpath $(shell which ccache 2> /dev/null)))

TEST_SRCS = $(wildcard $(SRC_DIR)/*.c)
TEST_OBJS := $(patsubst $(SRC_DIR)/%.c, $(TEST_BUILD_DIR)/%.o, $(TEST_SRCS))

#
# Compiler/linker flags from:
#   http://weblogs.java.net/blog/kellyohair/archive/2006/01/compilation_of_1.html
JFLAGS = -fno-omit-frame-pointer -fno-strict-aliasing
OFLAGS = -O2 $(JFLAGS)
WFLAGS = -W -Werror -Wall -Wno-unused -Wno-parentheses
PICFLAGS = -fPIC
SOFLAGS = -shared -Wl,-O1
LDFLAGS += $(SOFLAGS)

IFLAGS = -I"$(BUILD_DIR)"
CFLAGS = $(OFLAGS) $(WFLAGS) $(IFLAGS) $(PICFLAGS) -D_REENTRANT

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
  LIBEXT = dll
endif

ifeq ($(OS), win32)
  CC += -mno-cygwin -mwin32
  LDFLAGS += -mno-cygwin -Wl,--add-stdcall-alias
  PREFIX =
  PICFLAGS =
  LIBEXT = dll
endif

ifeq ($(OS), darwin)
  ARCHFLAGS = 
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

  ifneq ($(realpath $(wildcard $(XCODE)/SDKs/MacOSX10.4?.sdk)),)
    MACSDK = $(XCODE)/SDKs/MacOSX10.4u.sdk
    ifeq ($(findstring ppc, $(ARCHES)),)
      ARCHES += ppc
    endif
  else
    MACSDK = $(firstword $(strip $(wildcard $(XCODE)/SDKs/MacOSX10.*.sdk)) $(strip $(wildcard $(XCODE)/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.*.sdk)))
  endif
  ARCHFLAGS = $(foreach arch, $(ARCHES),-arch $(arch))
  CFLAGS += $(ARCHFLAGS) -isysroot $(MACSDK) -DTARGET_RT_MAC_CFM=0
  CFLAGS += -fno-common
  LDFLAGS = $(ARCHFLAGS) -dynamiclib -Wl,-syslibroot,$(MACSDK) -mmacosx-version-min=10.4
  LDFLAGS += $(foreach arch, $(ARCHES),-arch $(arch))
  # link against the universal libraries on ppc machines
  LDFLAGS += -L$(MACSDK)/usr/lib
  LIBEXT = dylib
  FFI_CFLAGS += -isysroot $(MACSDK)
  PICFLAGS =
  SOFLAGS =
endif

ifeq ($(OS), linux)
  SOFLAGS += -Wl,-soname,$(LIBNAME)
endif

ifeq ($(OS), solaris)
  CC = gcc
  CFLAGS += -std=c99
  LD = /usr/ccs/bin/ld
  SOFLAGS = -shared -static-libgcc 
endif

ifeq ($(OS), aix)
  LIBEXT = a
  SOFLAGS = -shared -static-libgcc
  PICFLAGS += -pthread
endif

ifneq ($(findstring bsd, $(OS)),)
  SOFLAGS = -shared -static-libgcc
  CFLAGS += -pthread
  LDFLAGS += -pthread
endif

ifneq ($(findstring cygwin, $(OS)),)
  CFLAGS += -mno-cygwin -mwin32
  LDFLAGS += -mno-cygwin -Wl,--add-stdcall-alias
  LIBEXT = dll
  PREFIX =
  PICFLAGS =
endif
ifneq ($(findstring mingw, $(OS)),)
  LIBEXT = dll
  PICFLAGS=
endif
ifeq ($(CPU), sparcv9)
  MODEL = 64
endif

ifeq ($(CPU), amd64)
  MODEL = 64
endif

ifeq ($(CPU), x86_64)
  MODEL = 64
endif

ifeq ($(CPU), s390x)
  MODEL = 64
endif

ifeq ($(CPU), ppc64)
  MODEL = 64
endif

# On platforms (linux, solaris) that support both 32bit and 64bit, force building for one or the other
ifneq ($(strip $(findstring $(OS), solaris)),)
  # Change the CC/LD instead of CFLAGS/LDFLAGS, incase other things in the flags
  # makes the libffi build choke
  CC += -m$(MODEL)
  LD += -m$(MODEL)
endif

LIBTEST = $(BUILD_DIR)/$(LIBNAME)

all:	$(LIBTEST)

$(TEST_BUILD_DIR)/%.o : $(SRC_DIR)/%.c
	@mkdir -p $(@D)
	$(CCACHE) $(CC) $(CFLAGS) -c $< -o $@

$(LIBTEST):  $(TEST_OBJS)
	$(CC) -o $@ $(LDFLAGS) $(TEST_OBJS) -lm

clean::
	# nothing to do - ant will delete the build dir

debug::
	@echo OS="$(OS)"
	@echo BUILD_OS="$(BUILD_OS)"
	@echo JAVA_HOME="$(JAVA_HOME)"
	@echo JDK_HOME="$(JDK_HOME)"
	@echo "SRCS=$(TEST_SRCS)"
