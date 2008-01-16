
CADP        ?= /usr/local/cadp

CADP_INCL    = $(CADP)/incl
# this may have to be adjusted
CADP_LIB     = $(CADP)/bin.iX86

CXX          = g++

CXX_INCL     = $(foreach inc,$(wildcard /usr/lib/sun*jdk*/include) \
                             $(wildcard /usr/lib/sun*jdk*/include/linux) \
                             $(wildcard /opt/sun*jdk*/include) \
                             $(wildcard /opt/sun*jdk*/include/linux) \
                 ,-I$(inc))

# you may want to set another jdk path here (if jni.h is not found)
CXX_INCL    += -I/cygdrive/e/Java/jdk1.6.0_01/include
CXX_INCL    += -I/cygdrive/e/Java/jdk1.6.0_01/include/win32

CXXFLAGS     = -fPIC -DPIC

# and another cygwin workaround:
CXXFLAGS    += "-D__int64=long long"


# default target: all
all: libBCGWriter.so

%.o: %.cc %.h
	$(CXX) $(CXXFLAGS) $(CXX_INCL) -I$(CADP_INCL) -o $@ -c $<

lib%.so: %.o
	$(CXX) $(CXXFLAGS) -shared -o $@ $^ -L$(CADP_LIB) -lBCG_IO -lBCG -lm

clean:
	rm -f libBCGwrite.so BCGwrite.o

