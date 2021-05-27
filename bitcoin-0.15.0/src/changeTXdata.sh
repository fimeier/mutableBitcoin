
for txid in `cat $1`
do


	rawTX=$(./bitcoin-cli -regtest getrawtransaction $txid)
	decodedTX=$(./bitcoin-cli -regtest decoderawtransaction $rawTX | jq -r '.vout |.[]|.scriptPubKey|select(.asm | contains("OP_RETURN"))|.asm')
	echo "$txid : $decodedTX <- delete this stuff...."

	dataDD="6341"$txid

	rawTX=$(./bitcoin-cli -regtest createrawtransaction '[]' '{"data": "'$dataDD'"}')
	fundedTX=$(./bitcoin-cli -regtest fundrawtransaction $rawTX | jq -r '.hex')
	signedTX=$(./bitcoin-cli -regtest signrawtransaction $fundedTX | jq -r '.hex')
	txID=$(./bitcoin-cli -regtest sendrawtransaction $signedTX)
	#echo "sending deleteTX is disabled...."
	echo $txID

	#| jq -r '.vout |.[]|.scriptPubKey|select(.asm | contains("OP_RETURN"))|.asm'
done


