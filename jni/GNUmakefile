# -*- makefile -*-
BUILD_OS = $(shell uname -s | tr '[:upper:]' '[:lower:]')
OS = $(BUILD_OS)
ifeq ($(OS),sunos)
  OS = solaris
endif

CPU ?= $(shell uname -m | sed -e 's/i[345678]86/i386/')
MODEL = 32 # Default to 32bit compiles
PLATFORM = $(CPU)-$(OS)


# Set defaults to unix (linux/solaris/bsd)
PREFIX = lib
LIBEXT = so
JNIEXT = so

export MACOSX_DEPLOYMENT_TARGET=10.4

#CCACHE=$(shell /usr/bin/which ccache)
SRC_DIR ?= $(shell pwd)/jni
JNI_DIR ?= $(SRC_DIR)
BUILD_DIR ?= $(shell pwd)/build

JFFI_SRC_DIR = $(SRC_DIR)/jffi
JFFI_BUILD_DIR = $(BUILD_DIR)/jffi
LIBFFI_SRC_DIR = $(SRC_DIR)/libffi
LIBFFI_BUILD_DIR = $(BUILD_DIR)/libffi-$(PLATFORM)

LIBFFI = $(LIBFFI_BUILD_DIR)/.libs/libffi_convenience.a

SRCS = $(wildcard $(JFFI_SRC_DIR)/*.c)
OBJS = $(patsubst %.c, $(JFFI_BUILD_DIR)/%.o, $(notdir $(SRCS)))

vpath %.h $(JFFI_SRC_DIR)

LIBNAME = jffi
#
# Compiler/linker flags from:
#   http://weblogs.java.net/blog/kellyohair/archive/2006/01/compilation_of_1.html
JFLAGS = -fno-omit-frame-pointer -fno-strict-aliasing -DNDEBUG
OFLAGS = -O2 $(JFLAGS)
WFLAGS = -W -Werror -Wall -Wno-unused -Wno-parentheses -Wundef
PICFLAGS = -fPIC
SOFLAGS = # Filled in for each OS specifically
LDFLAGS += $(SOFLAGS)
FFI_MMAP_EXEC = -DFFI_MMAP_EXEC_WRIT

FFI_CC=$(CCACHE) $(CC)
FFI_LD=$(LD)
FFI_CFLAGS = $(FFI_MMAP_EXEC) $(OFLAGS)

JDK_INCLUDES= -I"$(JDK_HOME)/include" -I"$(JDK_HOME)/include/$(OS)"
IFLAGS = -I"$(BUILD_DIR)" -I"$(JFFI_SRC_DIR)" -I"$(LIBFFI_BUILD_DIR)/include"
CFLAGS = $(OFLAGS) $(WFLAGS) $(IFLAGS) $(PICFLAGS) $(JDK_INCLUDES)
CFLAGS += -D_REENTRANT -D_LARGEFILE64_SOURCE -D_GNU_SOURCE

ifeq ($(OS),mingw32)
  OS = win32
  CPU = i386
  JDK_INCLUDES=-I$(JNI_DIR)/include -I$(JNI_DIR)/include/win32
  ifneq ($(OS),$(BUILD_OS))
    CC ?= i386-mingw32-gcc
    LD ?= i386-mingw32-ld
  else
    CC = gcc
    LD = ld
  endif
endif
ifneq ($(findstring cygwin,$(BUILD_OS)),)
  OS = win32
  JAVA_HOME := $(shell cygpath -u \""$(JAVA_HOME)"\")
endif
ifeq ($(OS), win32)
  CC += -mno-cygwin
  LDFLAGS += -mno-cygwin -Wl,--add-stdcall-alias
  PICFLAGS=
endif
ifeq ($(OS), darwin)
  PLATFORM = darwin
  ARCHES = i386 x86_64 ppc
  CFLAGS += -isysroot /Developer/SDKs/MacOSX10.4u.sdk -DTARGET_RT_MAC_CFM=0
  CFLAGS += $(foreach arch, $(ARCHES),-arch $(arch))
  LDFLAGS = $(foreach arch, $(ARCHES),-arch $(arch)) -dynamiclib -framework JavaVM \
	-Wl,-syslibroot,$(SDKROOT) -mmacosx-version-min=10.4
  LIBEXT = dylib
  JNIEXT = jnilib
  FFI_CFLAGS += -isysroot /Developer/SDKs/MacOSX10.4u.sdk
  PICFLAGS =
  SOFLAGS =
endif

ifeq ($(OS), linux)
  SOFLAGS = -shared -mimpure-text -static-libgcc -Wl,-soname,$(@F) -Wl,-O1
  CFLAGS += -pthread
endif
ifeq ($(OS), solaris)
  CC = /usr/sfw/bin/gcc -std=c99
  CFLAGS += -D__EXTENSIONS__
  LD = /usr/ccs/bin/ld
  CCACHE=
  SOFLAGS = -shared -static-libgcc -mimpure-text
  LIBS += -ldl
endif

ifeq ($(OS), aix)
  SOFLAGS = -shared -static-libgcc
  CFLAGS += -pthread
  LDFLAGS += -pthread
  LIBEXT = a
  JNIEXT = a
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

# On platforms (linux, solaris) that support both 32bit and 64bit, force building for one or the other
ifneq ($(or $(findstring linux, $(OS)), $(findstring solaris, $(OS))),)
  # Change the CC/LD instead of CFLAGS/LDFLAGS, incase other things in the flags
  # makes the libffi build choke
  CC += -m$(MODEL)
  LD += -m$(MODEL)
endif

JDK_HOME=$(shell if [ -d $(JAVA_HOME)/include ];then echo $(JAVA_HOME); else echo $(JAVA_HOME)/..; fi)
LIBJFFI = $(BUILD_DIR)/$(PREFIX)$(LIBNAME).$(JNIEXT)
FFI_CONFIGURE = $(LIBFFI_SRC_DIR)/configure --disable-static \
	--with-pic=yes --disable-dependency-tracking
ifneq ($(MINGW32),)
	FFI_CONFIGURE += --host=i386-mingw32
endif
all:	$(LIBJFFI)

debug:
	@echo OS="$(OS)"
	@echo BUILD_OS="$(BUILD_OS)"
	@echo JAVA_HOME="$(JAVA_HOME)"
	@echo JDK_HOME="$(JDK_HOME)"
	@echo "OBJS=$(OBJS) JFFI_BUILD_DIR=$(JFFI_BUILD_DIR) PLATFORM=$(PLATFORM)"

$(LIBJFFI):  $(LIBFFI) $(OBJS)
	$(CC) -o $@ $(LDFLAGS) $(SOFLAGS) $(OBJS) $(LIBFFI) $(LIBS)

$(BUILD_DIR)/%.o : $(SRC_DIR)/%.c jffi.h
	@mkdir -p $(@D)
	$(CCACHE) $(CC) $(CFLAGS) -c $< -o $@

ifeq ($(OS), darwin)
build_ffi = \
	mkdir -p $(BUILD_DIR)/libffi-darwin-$(1); \
	(if [ ! -f $(BUILD_DIR)/libffi-darwin-$(1)/Makefile ]; then \
	    echo "Configuring libffi for $(1)"; \
	    cd $(BUILD_DIR)/libffi-darwin-$(1) && \
	      env CC="$(CCACHE) $(CC)" CFLAGS="-arch $(1) $(FFI_CFLAGS)" LDFLAGS="-arch $(1)" \
		$(FFI_CONFIGURE) --host=$(1)-apple-darwin > /dev/null; \
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
		$(FFI_CONFIGURE) > /dev/null; \
	fi
	$(MAKE) -C $(LIBFFI_BUILD_DIR)
endif
clean::
	# nothing to do - ant will delete the build dir

