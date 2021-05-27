Bachelor's Thesis in Computer Science (readme UNDER CONSTRUCTION)
=================================================================


FOLDERS
=======
* 00SourceGitHub/ (contains the original sourcecode from Github
* NetBeansProjects/ (contains stuff related to the NetBeansIDE, stuff is linked to the bitcoin-0.xx.0-folder)
* bitcoin-0.xx.0/
  * original sourcecode and modifications
  * "Eclipse Project" and ./launchers
* publicCoinChameleonHashEnhanced/
  * Chameleon Hash proof of concept (dirty code)
  * uses cryptopp libray
* Bitcoin-JSON-RPC-Client
  * a simple webserver / rpc-caller... (TBD)
  * Java/Eclipse project

Installation (just some hints)
============
* install vmware (optional)
* install ubuntu workstation: ubuntu-16.04.3-desktop-amd64.iso
  * change keyboard layout / Timezone
* clone this  repository
* install prerequirements
  * sudo apt-get install build-essential libtool autotools-dev automake pkg-config libssl-dev libevent-dev bsdmainutils
  * sudo apt-get install libboost-system-dev libboost-filesystem-dev libboost-chrono-dev libboost-program-options-dev libboost-test-dev libboost-thread-dev
  * sudo apt-get install libdb-dev libdb++-dev
  * sudo apt-get install libminiupnpc-dev
  * sudo apt-get install libzmq3-dev
  * sudo apt-get install libqt5gui5 libqt5core5a libqt5dbus5 qttools5-dev qttools5-dev-tools libprotobuf-dev protobuf-compiler
* install java
  * sudo add-apt-repository ppa:webupd8team/java 
  * sudo apt-get update
  * sudo apt-get install oracle-java9-installer 

* Test config setup (optional)
  * unzip <repository>/00SourceGithub/bitcoin-0.15.0.tar.gz
  * cd bit...
  * ./autogen.sh
  * ./configure --with-incompatible-bdb
  * make -j8 (multicore...)
  * start bitcoind => creates folders in home
  * cd src
  * ./bitcoind -regtest -printtoconsole (creates folders under ~/.bitcoin
  * stop bitcoind and copy bitcoin.conf UserHome/.bitcoin/
  * ./bitcoind -regtest -printtoconsole -reindex  //or instead of -reindex delete .bitcoin/regtest folder
  * start webserver
    => cd <repository>/BitBitcoin-JSON-RPC-Client/
    => java -jar webserver.jar
  * open http://localhost:8000/bitcoin/?func=MAIN
   =>n ow communication with bitcoin should be possible...

* download cryptopp565 extract  and open folder
  * make libcryptopp.a libcryptopp.so cryptest.exe
  * make test
  * sudo make install
  * sudo vi /etc/ld.so.conf => add "/usr/local/lib"
  * sudo ldconfig

* download eclipse installer (optional)
  * install cpp-oxygen
  * open eclipse project <repository>/bitcoin-0.15.0/
  * verify/change build type to debug
  * change Autotools/Configure Settings/Advance....  => change folder /home/fimeier/srcbit15/eth/libdevcore.a to <repository>/bitcoin-0.15.0/src/eth/libdevcore.a  CFLAGS='-g -O0 -I. -I/usr/local/include/cryptopp' CXXFLAGS='-g -O0 -I. -I/usr/local/include/cryptopp' --with-incompatible-bdb LIBS='/home/fimeier/srcbit15/eth/libdevcore.a -lcryptopp -lpthread' --disable-tests  --disable-gui-tests  --disable-bench --disable-hardening
  * Eclipse: Reconfigure-Project


* Bug??? comment out the defi struct
dh_st DH
/usr/include/openssl/ossl_typ.h
//typedef struct dh_st DH;

Now you can debug bitcoin in eclipse

  * add /usr/local/include/cryptopp to eclipse C/C++ General/Paths and Symbols/Includes (GNU C++)
  * Refresh/rebuild eclipse c++ index!!!!! (restart eclipse...)
  * Ignore Exlipse Errors...