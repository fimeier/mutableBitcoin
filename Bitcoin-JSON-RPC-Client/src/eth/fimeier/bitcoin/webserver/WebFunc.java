package eth.fimeier.bitcoin.webserver;

public enum WebFunc {


	//
	callMethodWithArgs(2), //func=callmethod&name=<value> ... name=getblockhash 0
	callMethod(2), //func=callmethod&name=<value>
	getblockchaininfo(1), //func
	getnetworkinfo(1), //func

	
	get_statistics(1),	//func=get_statistics
	PAGE(2),	//func=PAGE&name=<value>
	MAIN(1), //func
	ERROR(1),
	
	createrawtransaction(4),
	
	
	add_inproc(10),
	update_inproc(10),
	
	add_proc(14),
	update_proc(14),
	;

	public final int arg_count;

	WebFunc(int arg_count) {
		this.arg_count = arg_count;
	}

	private static final WebFunc[] values = WebFunc.values();

	static public WebFunc fromInt(int type) {
		return values[type];
	}


	public static WebFunc fromString(String type) {
		try {
			return WebFunc.valueOf(type);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
