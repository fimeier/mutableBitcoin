#include <stdio.h>
#include <string>
#include <assert.h>

#include "chameleon.h"

int main(int argc, char** argv)
{
#define INPUTM1 42
#define INPUTM2 33
#define R1 1
#define S1 1

	//TODO define key length
	/*
	Integer p("0x1c31f6057060d5b6a642b4e37f5900bf17ab1e51bc93839f9f537e6fe4d71502eaa784adcddc9cf218b6a175741bf0f7bff6197374dea53b15cddde5cb0ff8553");
	Integer q("0xe18fb02b8306adb53215a71bfac805f8bd58f28de49c1cfcfa9bf37f26b8a817553c256e6ee4e790c5b50baba0df87bdffb0cb9ba6f529d8ae6eef2e587fc2a9");
	Integer g("0x3");
	Integer tk("0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000745407AEB4627A64EBA8ABF541EEBE");
	Integer hk("0x00A1689B3E5C7704250C2C53EF575E3B910F4E12C61B1671423930D014CFE9780F5ACA306C4A57032C661D9E84B2F331D681AC41CB8CBA6B77BF5006C7CB3721C2");
*/

	/*
	std::cout <<"*********************** call: HGen() ***********************"<<std::endl;
	Integer p,q,g,tk,hk;
	HGen(p,q,g,tk,hk);
	*/

	//CChamHash ch(1025);
	CChamHash ch;
	CChamHash ch2;




	try {

		/*
		 * Calculate first Hash h1 for message m1
		 */

		string sh1;
		Integer h1 = Integer();
		Integer r1(R1);
		Integer s1(S1);

		//Integer m1(INPUTM1);
		//Integer m1()
		string m1temp = "1234";
		string mess1 = "0x"; mess1.append(m1temp);
	//	byte message1[]={mess1};
		//message1
		//Integer m1(ch.byteArraytoInteger(message1, 14));
		Integer m1(mess1.c_str());


		cout <<"*********************** call: CHash() ***********************"<<endl;
		ch.cHash (sh1, h1, m1, r1, s1);

		std::cout << "h1="<<h1 <<std::endl;
		std::cout << "sh1="<<sh1 <<std::endl;


		cout <<"*********************** call: HCol() ***********************"<<endl;

		/*
		 * Inputs
		 * tk, (h,m1,r1,s1), m2
		 *
		 * Outputs
		 * r2, s2
		 *
		 */
		Integer r2 = Integer();
		Integer s2 = Integer();

		string mess2 = "Hello was geht Jojo kollision";
		byte message2[]={"Hello was geht Jojo kollision"};
				//message1
		Integer m2(ch.byteArraytoInteger(message2, 29));

		//Integer m2(INPUTM2);

		ch.hCol (r2,s2,h1, m1, r1, s1, m2);

		//TODO new function HVer
		cout <<"*********************** Verify collision ***********************"<<endl;
		ch.hVer (h1, m1, r1, s1, m2, r2, s2);

	}

	catch(const CryptoPP::Exception& e)
	{
		cerr << e.what() << endl;
	}

	catch(const std::exception& e)
	{
		cerr << e.what() << endl;
	}

	return 0;
}

