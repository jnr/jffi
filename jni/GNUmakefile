# -*- makefile -*-
OS ?= $(shell uname -s | tr '[A-Z]' '[a-z]')
CPU ?= $(shell uname -m | sed -e 's/i[345678]86/i386/')
MODEL = 32 # Default to 32bit compiles
PLATFORM = $(CPU)-$(OS)
JDK_HOME=$(shell if [ -d $(JAVA_HOME)/include ];then echo $(JAVA_HOME); else echo $(JAVA_HOME)/..; fi)




# Set defaults to unix (linux/solaris/bsd)
PREFIX = lib
LIBEXT = so
JNIEXT = so

export MACOSX_DEPLOYMENT_TARGET=10.4

CCACHE=$(shell /usr/bin/which ccache)
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

TEST_SRCS = $(wildcard $(SRC_DIR)/libtest/*.c)
TEST_OBJS := $(patsubst %.c, $(BUILD_DIR)/libtest/%.o, $(notdir $(TEST_OBJS)))
	
LIBNAME = jffi
#
# Compiler/linker flags from:
#   http://weblogs.java.net/blog/kellyohair/archive/2006/01/compilation_of_1.html
JFLAGS = -fno-omit-frame-pointer -fno-strict-aliasing -DNDEBUG
OFLAGS = -O2 $(JFLAGS)
WFLAGS = -W -Werror -Wall -Wno-unused -Wno-parentheses
PICFLAGS = -fPIC -pthread
SOFLAGS = -shared -static-libgcc -mimpure-text -Wl,-O1
LDFLAGS += $(SOFLAGS)
FFI_MMAP_EXEC = -DFFI_MMAP_EXEC_WRIT

FFI_CC=$(CCACHE) $(CC)
FFI_LD=$(LD)
FFI_CFLAGS = $(FFI_MMAP_EXEC) $(OFLAGS)

JDK_INCLUDES= -I"$(JDK_HOME)/include" -I"$(JDK_HOME)/include/$(OS)"
IFLAGS = -I"$(BUILD_DIR)" -I"$(JFFI_SRC_DIR)" -I"$(LIBFFI_BUILD_DIR)/include"
CFLAGS = $(OFLAGS) $(WFLAGS) $(IFLAGS) $(PICFLAGS) $(JDK_INCLUDES)
CFLAGS += -D_REENTRANT -D_LARGEFILE64_SOURCE -D_GNU_SOURCE

ifneq ($(MINGW32),)
	OS=win32
	CPU=i386
	CC=i386-mingw32-gcc
	LD=i386-mingw32-ld
	PICFLAGS=
	JDK_INCLUDES=-I$(JNI_DIR)/include -I$(JNI_DIR)/include/win32
endif
ifeq ($(OS), win32)
	CC += -mno-cygwin
	LDFLAGS += -mno-cygwin -Wl,--add-stdcall-alias
endif
ifeq ($(OS), darwin)
  CFLAGS += -isysroot /Developer/SDKs/MacOSX10.4u.sdk -DTARGET_RT_MAC_CFM=0
  LDFLAGS = -arch i386 -arch x86_64 -dynamiclib -framework JavaVM \
	-Wl,-syslibroot,$(SDKROOT) -mmacosx-version-min=10.4
  LIBEXT = dylib
  JNIEXT = jnilib
  FFI_CFLAGS += -isysroot /Developer/SDKs/MacOSX10.4u.sdk
  PICFLAGS =
  SOFLAGS =
endif

ifeq ($(OS), linux)
  SOFLAGS += -Wl,-soname,$(@F)
endif

ifeq ($(CPU), sparcv9)
  MODEL = 64
endif

ifeq ($(CPU), amd64)
  MODEL = 64
endif

ifeq ($(MODEL), 64)
  # Change the CC/LD instead of CFLAGS/LDFLAGS, incase other things in the flags
  # makes the libffi build choke
  CC += -m64
  LD += -m64
endif

LIBJFFI = $(BUILD_DIR)/$(PREFIX)$(LIBNAME).$(JNIEXT)
LIBTEST = $(BUILD_DIR)/$(PREFIX)$(LIBNAME)test.$(LIBEXT)
FFI_CONFIGURE = $(LIBFFI_SRC_DIR)/configure --disable-static \
	--with-pic=yes --disable-dependency-tracking
ifneq ($(MINGW32),)
	FFI_CONFIGURE += --host=i386-mingw32
endif
all:	$(LIBJFFI) $(LIBTEST)

debug:
	@echo "OBJS=$(OBJS) JFFI_BUILD_DIR=$(JFFI_BUILD_DIR) PLATFORM=$(PLATFORM)"

$(LIBJFFI):  $(LIBFFI) $(OBJS)
	$(CC) -o $@ $(LDFLAGS) $(JFFI_SOFLAGS) $(OBJS) $(LIBFFI) $(LIBS)

$(LIBTEST):  $(TEST_OBJS)
	$(CC) -o $@ $(LDFLAGS) $(TEST_SOFLAGS) $(TEST_OBJS) -lm
	
ifeq ($(OS), darwin)
$(BUILD_DIR)/%.o:	$(SRC_DIR)/%.c $(JFFI_SRC_DIR)/jffi.h
	@mkdir -p $(@D)
	$(CCACHE) $(CC) -arch i386 -I$(BUILD_DIR)/libffi-i386/include $(CFLAGS) -c $< -o $@.i386
	$(CCACHE) $(CC) -arch x86_64 -I$(BUILD_DIR)/libffi-x86_64/include $(CFLAGS) -c $< -o $@.x86_64
	lipo -create -output $@ -arch x86_64 $@.x86_64 -arch i386 $@.i386 -arch ppc $@.ppc
else
$(BUILD_DIR)/%.o : $(SRC_DIR)/%.c jffi.h
	@mkdir -p $(@D)
	$(CCACHE) $(CC) $(CFLAGS) -c $< -o $@
endif

ifeq ($(OS), darwin)
build_ffi = \
	mkdir -p $(BUILD_DIR)/libffi-$(1); \
	(if [ ! -f $(BUILD_DIR)/libffi-$(1)/Makefile ]; then \
	    echo "Configuring libffi for $(1)"; \
	    cd $(BUILD_DIR)/libffi-$(1) && \
	      env CFLAGS="-arch $(1) $(FFI_CFLAGS)" LDFLAGS="-arch $(1)" \
		$(FFI_CONFIGURE) --host=$(1)-apple-darwin > /dev/null; \
	fi); \
	env MACOSX_DEPLOYMENT_TARGET=10.4 $(MAKE) -C $(BUILD_DIR)/libffi-$(1)
	
$(LIBFFI):
	@echo "JNI_DIR=$(JNI_DIR) MAKE=$(MAKE) FFI_CONFIGURE=$(FFI_CONFIGURE)"
	@$(call build_ffi,i386)
	@$(call build_ffi,x86_64)
	
	# Assemble into a FAT (i386, x86_64, ppc) library
	@mkdir -p $(LIBFFI_BUILD_DIR)/.libs
	env MACOSX_DEPLOYMENT_TARGET=10.4 /usr/bin/libtool -static -o $@ \
            $(BUILD_DIR)/libffi-i386/.libs/libffi_convenience.a \
	    $(BUILD_DIR)/libffi-x86_64/.libs/libffi_convenience.a
else
$(LIBFFI):		
	@mkdir -p $(LIBFFI_BUILD_DIR)
	if [ ! -f $(LIBFFI_BUILD_DIR)/Makefile ]; then \
	    echo "Configuring libffi for $(PLATFORM)"; \
	    cd $(LIBFFI_BUILD_DIR) && env CC="$(FFI_CC)" LD="$(FFI_LD)" CFLAGS="$(FFI_CFLAGS)" \
		$(FFI_CONFIGURE) > /dev/null; \
	fi
	$(MAKE) -C $(LIBFFI_BUILD_DIR)
endif
clean::
	# nothing to do - ant will delete the build dir
	
