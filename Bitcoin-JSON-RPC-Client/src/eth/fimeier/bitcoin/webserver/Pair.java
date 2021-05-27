package eth.fimeier.bitcoin.webserver;

public class Pair<K,V>{
	public
	Pair(K key_, V value_) {
		key=key_;
		value=value_;
	};
	K getKey() {
		return key;
	}
	V getValue() {
		return value;
	}


	private
	K key;
	V value;

};
