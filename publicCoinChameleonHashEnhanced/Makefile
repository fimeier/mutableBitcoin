CXX = c++
CXX_FLAGS = -std=c++14  -O0 -ggdb3 -g3
CXX_CRYPTOFLAGS = -I. -I/usr/local/include/cryptopp
LDFLAGS = -lcryptopp -lpthread
all: ChameleonHash

ChameleonHash: ChameleonHash.cpp chameleon.h Makefile
	$(CXX) $(CXX_FLAGS) $(CXX_CRYPTOFLAGS) ChameleonHash.cpp  -o ChameleonHash  $(LDFLAGS)
