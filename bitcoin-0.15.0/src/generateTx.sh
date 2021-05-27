#!/bin/bash



#./bc generate 101

for i in {1..3}
do
	sleep 0.3

	ADD=$(./bitcoin-cli -regtest getnewaddress)
	rawTX=$(./bitcoin-cli -regtest createrawtransaction '[]' '{"'$ADD'": 0.0002, "data": "68747470733a2f2f696c6c6567616c2e636f6d"}')
	fundedTX=$(./bitcoin-cli -regtest fundrawtransaction $rawTX | jq -r '.hex')
	signedTX=$(./bitcoin-cli -regtest signrawtransaction $fundedTX | jq -r '.hex')
	txID=$(./bitcoin-cli -regtest sendrawtransaction $signedTX)
	 
echo $txID


done




