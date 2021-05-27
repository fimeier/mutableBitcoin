/*
 * some notes.... ignore them
 * Ideen
 * -DH stuff in class, damit nur diese übergeben werden muss wenn p,q,g,tk(?),hk abfragen
 * --zudem verschiedene representationen von keys (z.B. Integer, unsigned char....)
 * -class welche konvertierungen macht, dann später auch einfacher um es besser zu machen
 * -lokale var exportieren... was überlebt fnk call
 *
 */


#include <stdio.h>
#include <string>
#include <assert.h>

#include <sstream>  // For std::ostringstream


#include "modarith.h"
using CryptoPP::ModularArithmetic;

#include <iostream>
using std::cout;
using std::cerr;
using std::endl;

#include <string>
using std::string;

#include <stdexcept>
using std::runtime_error;

#include <sstream>
using std::istringstream;

#include <osrng.h>
using CryptoPP::AutoSeededRandomPool;

#include <integer.h>
using CryptoPP::Integer;

#include <nbtheory.h>
using CryptoPP::ModularExponentiation;

#include <secblock.h>
using CryptoPP::SecByteBlock;

#include <dh.h>
using CryptoPP::DH;

#include <hex.h>
using CryptoPP::HexEncoder;

#include <filters.h>
using CryptoPP::StringSink;

#include <sha.h>
using CryptoPP::SHA256;

/** A hasher class for chameleon hash */
class CChamHash {

public:

	CChamHash() {
		Integer p1("0x14d2b679d4bdbfdcd1f366e796d9e7f164a6b5a34383a29ddaf0fdc3011fb583383285dd60234732df194c3fbb76d2e096af464ad5bdcdae9ff6d3e761cd7f746474dc990c51207d12212258dc5db6136e83f374ba35b08a10313d7e8011c0f18a3e96cee5e5bc4aff2fc660c743356945257525505ac5bfb0f4e71fd2130bacf");
		Integer q1("0xa695b3cea5edfee68f9b373cb6cf3f8b2535ad1a1c1d14eed787ee1808fdac19c1942eeb011a3996f8ca61fddbb69704b57a3256adee6d74ffb69f3b0e6bfba323a6e4c8628903e8910912c6e2edb09b741f9ba5d1ad84508189ebf4008e078c51f4b6772f2de257f97e33063a19ab4a292ba92a82d62dfd87a738fe90985d67");
		Integer g1("0x2");
		Integer tk1("0x0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000089f7e594261a017ad7e4317603437dbbca02f0caf");
		Integer hk1("0x005f61b408f891e280f08294e9280559006023668ab06c37b69c66e0e727891940057635a8610ffa6c82cdd7f6f01e13bad62c412fd07fb70608e8e32bbacb532895bbd83a8e79cd7aea0a9cf17d5318949ff3c0b51d68f38e193f593f64249a3df8aab6a543462e79e13faa93b6d3d6669c972b58eb3b41084d77715590fd5b0a");

		p = p1 ;

		q = q1;
		g = g1;
		tk = tk1;
		hk = hk1;

		assert (p == 2*q + 1); // safe primes IFF p && q prime asd

	}

	CChamHash(unsigned int bits){

		hGen(bits);

	}


	//private:

	//TODO define key length
	Integer p, q, g, tk, hk;

	/*
	 * generates safe prime numers/group generator/PK and PK
	 */
	void hGen(unsigned int PBITS){

		//void HGen(Integer& p, Integer& q, Integer& g, Integer& tk, Integer& hk){
		//#define PBITS 1025
		AutoSeededRandomPool rnd;
		unsigned int bits = PBITS;

		cout << "Generating prime of size " << bits << " and generator (hex representation)" << endl;

		DH dh;
		dh.AccessGroupParameters().GenerateRandomWithKeySize(rnd, bits);

		if(!dh.GetGroupParameters().ValidateGroup(rnd, 3))
			throw runtime_error("Failed to validate prime and generator");

		size_t count = 0;

		p = dh.GetGroupParameters().GetModulus();
		count = p.BitCount();
		cout << "P (" << std::dec << count << "): " << std::hex << p << endl;

		q = dh.GetGroupParameters().GetSubgroupOrder();
		count = q.BitCount();
		cout << "Q (" << std::dec << count << "): " << std::hex << q << endl;

		g = dh.GetGroupParameters().GetGenerator();
		count = g.BitCount();
		cout << "G (" << std::dec << count << "): " << std::dec << g << endl;

		// http://groups.google.com/group/sci.crypt/browse_thread/thread/7dc7eeb04a09f0ce
		Integer v = ModularExponentiation(g, q, p);
		if(v != Integer::One())
			throw runtime_error("Failed to verify order of the subgroup");

		SecByteBlock privKey(dh.PrivateKeyLength());
		SecByteBlock pubKey(dh.PublicKeyLength());

		//dh.GeneratePublicKey(RandomNumberGenerator &rng, const byte *privateKey, byte *publicKey) const
		//dh.GeneratePublicKey(rng, const byte *privateKey, byte *publicKey) const

		dh.GenerateKeyPair(rnd, privKey, pubKey);
		tk = byteArraytoInteger(privKey.BytePtr(), privKey.SizeInBytes());
		hk = byteArraytoInteger(pubKey.BytePtr(), pubKey.SizeInBytes());

		cout << "Private key: " << byteArraytoString(privKey.BytePtr(), privKey.SizeInBytes()) << endl;
		cout << "Public key: " << byteArraytoString(pubKey.BytePtr(), pubKey.SizeInBytes()) << endl;

		cout <<"\n(Integer representation)" << endl;
		cout <<"P: "<<p<<endl;
		cout <<"Q: "<<q<<endl;
		cout <<"G: "<<g<<endl;
		cout <<"Private key (tk): " << tk << endl;
		cout <<"Public key (hk): " << hk << endl<< endl;
	}



	string byteArraytoString(byte* ba, int size, int mode = 1){

		//HexEncoder(BufferedTransformation *attachment = NULL, bool uppercase = true, int groupSize = 0, const std::string &separator = ":", const std::string &terminator = "")
		string temp;
		HexEncoder hex(new StringSink(temp), false);

		if (mode) temp = "0x";
		hex.Put(ba, size);
		hex.MessageEnd();

		return temp;
	}

	Integer byteArraytoInteger(byte* ba, int size){
		return Integer((char *)byteArraytoString(ba,size).c_str());
	}

	byte* integertoByteArray(Integer i){
		std::ostringstream os;
		os << i;
		return(byte *) os.str().c_str();
	}

	string integertoHexString(Integer i){
		std::ostringstream os;
		os << std::hex<< i;
		//os << std::hex<< std::showbase << i;
		return os.str();
	}


	//TODO Hex to string converter...
	string integertoString(Integer i){
		string hS = integertoHexString(i);

		const char *str = hS.c_str();
		int len = (hS.size()-1)/2;

		char out[len+1];
		const uint16_t *str_p = (const uint16_t *)str;
		uint32_t b;
		uint16_t *bb = (uint16_t*)&b;
		int c;
		for (c=0; c<len; c++) {
			*bb = *str_p++;
			out[c] = strtol((char*)&b,NULL,16);
		}
		out[c] = 0;
		return string(out);


		//	cout <<"idirekt:"<<i<<endl;
		//
		//	int n;
		//	 //std::istringstream("48656c6c6f207761732067656874") >> std::hex >> n;
		//	 std::istringstream("48656c6c6f207761732067656874") >> std::hex>> n;
		//
		//	 cout << "n="<<n<<endl;
		//	    std::cout << std::dec << "Parsing \"2A\" as hex gives " << n << '\n';
		//
		//	    std::ostringstream os;
		//	    os << std::dec << n << '\n';
		//	return os.str();

	}

	string integerToSHA256(const Integer& mTemp){
		std::ostringstream os;
		os << mTemp;
		string ss = os.str();
		int size = os.str().size();

		byte digest[SHA256::DIGESTSIZE];

		//CalculateDigest(byte *digest, const byte *input, size_t length)
		SHA256().CalculateDigest(digest, (byte *)ss.c_str(), size);



		/*
		 * Integer m(sha256(mTemp||r))
		 */
		return byteArraytoString(digest, SHA256::DIGESTSIZE, 1);

	}


	/*
	 * TODO: ev hier direkt Strings/Hex-Strings als Input???
	 * TODO: variable many Inputs
	 * Arguments: Integer m, Integer r,...
	 * concatenates all the inputs and calculates the sha256 hash of it
	 * return sha256 hash as Integer
	 */
	Integer hashInput(const Integer& mTemp, const Integer& r){

		//TODO ev direkt über integertoByteArray... dieses dann mit Variable length input...
		//concatenate inputs and stores them as byte array (unsigned char *)
		std::ostringstream os;
		os << mTemp << r;
		string ss = os.str();
		int size = os.str().size();

		byte digest[SHA256::DIGESTSIZE];

		//CalculateDigest(byte *digest, const byte *input, size_t length)
		SHA256().CalculateDigest(digest, (byte *)ss.c_str(), size);



		/*
		 * Integer m(sha256(mTemp||r))
		 */
		return byteArraytoInteger(digest, SHA256::DIGESTSIZE);

		/*
		 * mm-OLD=0x88af705e98dae8163e0527931031fb2cf5442fcaef6ebc705f419f52138f2e
		 * mm-NEW=0x88AF705E980DAE8163E05279310031FB2CF5442FCAEF6EBC705F419F52138F2E
		std::stringstream temp;
		temp << "0x";
		for (int i=0;i<SHA256::DIGESTSIZE;i++){
			temp << std::hex << (int)digest[i];
		}
		string mm = temp.str();
		cout << "mm-OLD="<<mm << endl;
		string mmnew = byteArraytoString(digest, SHA256::DIGESTSIZE);
		cout << "mm-NEW="<<mmnew<<endl;


		Integer m((char *)mm.c_str());
		cout << "m-old="<<m << endl;
		cout << "m-new="<<byteArraytoInteger(digest, SHA256::DIGESTSIZE) << endl;
		 */
	}


	void cHash (string& sh, Integer& h, const Integer& mTemp, const Integer& r, const Integer& s){


		Integer m = hashInput(mTemp, r);

		assert (0<=r);
		assert (r < q);
		assert (0<=r && r < q); // r in Zq
		assert (0<=s && s < q); // s in Zq
		assert (0<=m && m < q); // m1 in Zq


		ModularArithmetic maP(p);
		ModularArithmetic maQ(q);


		Integer t1 = maP.Exponentiate(hk, m); //y^H(m') (mod p)
		assert(0<=t1<p);
		Integer t2 = maP.Exponentiate(g, s); //g^s  (mod p)
		assert(0<=t2<p);

		//TODO
		Integer t3 = maP.Multiply(t1, t2); // (y^H(m')(mod p)) * (g^s(mod p)) (mod p)
		//assert(t3<p);

		//???
		//Integer t3 = maQ.Multiply(t1, t2); // (y^H(m')(mod p)) * (g^s(mod p)) (mod q?????) Implementierung funktioniert sonst nicht.... stimmt das mathematisch. Stimmt so implememntierung??
		//assert(0<=t3<q);

		h = maQ.Subtract(r,t3);
		/*
		 * TODO
		 * negative Integer????
		 */
		if (h<0){
			//cout << "ERROR: negative hash value... applying fix h.SetPositive();"<<endl;
			h.SetPositive();
		}

		sh = integerToSHA256(h);


	}

	//benötigt die meistenAngaben garnicht
	void hCol(Integer& r2, Integer& s2, const Integer& h, const Integer& m1, const Integer& r1, const Integer& s1, const Integer& m2){


		ModularArithmetic maP(p);
		ModularArithmetic maQ(q);

		//TODO: muss das rand sein?
		int k_ = 1;
		Integer k(k_);
		assert (0<=k && k < q); // k in Zq

		//TODO Implementierung vs math?
		Integer r = maQ.Exponentiate(g,k); //g^k(mod Q???) vs p
		assert (0<=r<p);
		r = maQ.Reduce(r,q);
		r2 = maQ.Add(h,r);

		Integer m = hashInput(m2, r2);

		Integer t1 = maQ.Multiply(m, tk);
		s2 = maQ.Subtract(k,t1);

		//cout << "r2="<<r2<<endl;
		//cout << "s2="<<s2<<endl;
	}

	void hVer (const Integer& h1,
			const Integer& m1, const Integer& r1, const Integer& s1,
			const Integer& m2, const Integer& r2, const Integer& s2){

		string sh1;
		Integer h2 = Integer();
		cHash (sh1, h2, m2, r2, s2);
		assert(h2==h1);

		string m1Hex = integertoHexString(m1);
		string m2Hex = integertoHexString(m2);
		string m1String = integertoString(m1);
		string m2String = integertoString(m2);
		string hHex = integertoHexString(h1);
		//cout << "message m1 was: "<<m1Hex<<endl<<"Integer-Format: "<<m1<<endl<<"String-Format: "<<m1String<<endl<<endl;
		//cout << "message m2 was: "<<m2Hex<<endl<<"Integer-Format: "<<m2<<endl<<"String-Format: "<<m2String<<endl<<endl;
		//cout << "they both have the same ChameleonHash: "<<hHex<<endl<<"Integer-Format: "<<h1<<endl<<endl;
	}



};


