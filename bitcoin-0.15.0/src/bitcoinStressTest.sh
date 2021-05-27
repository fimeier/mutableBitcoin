#!/bin/bash



#./bc generate 101

for i in {1..100000}
do
        for tx in {1..2}
	do
		ADD=$(./bc getnewaddress)
        	./bc sendtoaddress $ADD 0.00005	
	done
	./bc generate 1	
	echo "block $i generated"
	sleep 5
done




