package eth.fimeier.bitcoin.webserver;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.net.ssl.HttpsURLConnection;



import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoinRPCException;
import wf.bitcoin.javabitcoindrpcclient.BitcoinRpcException;
import wf.bitcoin.javabitcoindrpcclient.BitcoinUtil;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.BasicTxOutput;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.TxInput;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.TxOutput;
import wf.bitcoin.krotjson.Base64Coder;


public class Webserver {

	private String bitcoinVersion = null;
	private String pathStaticPages = null;
	private String serverName = null;

	private BitcoinJSONRPCClient bc; 

	private HttpServer server;

	private URL rpcUrl;
	private String authStr;



	public Webserver(int port, URL rpcUrl, String bitcoinVersion, String pathStaticPages, String serverName) throws IOException {

		this.bitcoinVersion = bitcoinVersion;
		this.pathStaticPages = pathStaticPages;
		this.serverName = serverName;
		this.bc  = new BitcoinJSONRPCClient(rpcUrl);

		this.rpcUrl = rpcUrl;
		this.authStr = rpcUrl.getUserInfo() == null ? null : String.valueOf(Base64Coder.encode(rpcUrl.getUserInfo().getBytes(Charset.forName("ISO8859-1"))));


		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/bitcoin", new MyHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
	}

	class MyHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			System.out.println("\nuser_input : "+t.getRequestURI().getQuery());
			String user_input = java.net.URLDecoder.decode(t.getRequestURI().getQuery(), "UTF-8");
			System.out.println("user_input after decoding: "+user_input);

			//System.out.println("call backend...");
			String response = "";
			try {
				response = backend(user_input);

			}
			catch(Exception e) {
				e.printStackTrace();
				response = backend("func=MAIN");
			}

			//System.out.println("returned from backend...\n");
			byte[] resp = response.getBytes();
			t.sendResponseHeaders(200, resp.length);
			OutputStream os = t.getResponseBody();
			os.write(resp);
			os.close();
		}

	}


	String readFile(String path, Charset encoding) 
			throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}


	private static Pair<String,String> parse_name_value(String arg) {
		final Pattern pattern = Pattern.compile("(?<name>^.+)=(?<value>.+)");
		Matcher matcher = pattern.matcher(arg);

		if(matcher.find()) {
			return new Pair<String,String>(matcher.group("name"),matcher.group("value"));
		} else {
			return null;
		}
	} 


	private static HashMap<String,String> get_args(String arg_in) {
		HashMap<String,String> args_out = new HashMap<String,String>();
		if (arg_in==null){
			args_out.put("func", "ERROR");
			args_out.put("error_message", "arg_count");	
			return args_out;
		}
		String[] args = arg_in.split("\\&");
		int arg_count = args.length;


		Pair<String,String> name_value = parse_name_value(args[0]);

		//func
		if ((name_value==null) || (!name_value.getKey().equals("func")) || (WebFunc.fromString(name_value.getValue())==null) ){
			args_out.put("func", "ERROR");
			args_out.put("error_message", "not_a_func");	
			return args_out;
		}

		if (arg_count != WebFunc.fromString(name_value.getValue()).arg_count ){
			args_out.put("func", "ERROR");
			args_out.put("error_message", "arg_count_missmatch");	
			return args_out;
		}


		for (int i=0; i< arg_count; i++){
			name_value = parse_name_value(args[i]);

			/*
			//ignore empty parameters for add/update
			if ( args[0].equals("func=add_inproc") || args[0].equals("func=update_inproc") || args[0].equals("func=add_proc") || args[0].equals("func=update_proc")){
				if ( name_value == null ){
					continue;
				}
			}	*/

			if ( name_value == null ){
				args_out.put("func", "ERROR");
				args_out.put("error_message", "wrong_argument_syntax");				
				return args_out;
			}

			args_out.put(name_value.getKey(), name_value.getValue());
		}
		return args_out;
	}


	private String backend(String user_input) {

		HashMap<String,String> args = get_args(user_input);




		String output = "";
		String func = args.get("func");
		String order_by = ""; //default order



		WebFunc wf = WebFunc.fromString(func);

		switch (wf) {
		/**
		 * data manipulation
		 * 
		 */
		/*
		case add_inproc:
			output += create_header(wf);
			output += add_inproc(args);
			output += create_footer(wf);
			break;*/


		/**
		 * Gui-Stuff
		 * 
		 * 
		 */
		case callMethod:
		{
			if ( !args.containsKey("method") ){
				output += create_header(wf);
				output += get_error("wrong_args: method=xyz missing...");
				output += create_footer_simple(wf);
				break;
			}
			String method = args.get("method");

			output += create_header(wf);
			output += "<br><div><pre>calling rpc server with method="+method+"</pre></div>";
			output += callMethod(method);
			output += create_footer_close_script(wf);
			break;
		}

		/*
		 * create a TX like: createrawtransaction [] '{"mx4VtgYRZ8qJQfvdsfsdfsdfgCD5YHZobhnZSY27u1n": 2, "data":"42"}'
		 */
		case createrawtransaction:
		{
			output = create_header(wf);

			String method = "createrawtransaction";
			if ( !args.containsKey("address") || !args.containsKey("bitcoin-value")  ){
				output += create_header(wf);
				output += get_error("wrong_args: address or bitcoin-value missing");
				output += create_footer_simple(wf);
				break;
			}

			String addr = args.get("address");
			String bv = args.get("bitcoin-value");
			Double val;
			try {
				val = Double.valueOf(bv);
			} catch(NumberFormatException ex) {
				output += create_header(wf);
				output += get_error("wrong_args: bitcoin-value NumberFormatException");
				output += create_footer_simple(wf);
				break;
			}

			//no inputs
			List<Map> pInputs = new ArrayList<>();
			//outputs
			Map<String, Object> pOutputs = new LinkedHashMap();

			//add add/value
			//pOutputs.put(addr, (Double) BitcoinUtil.normalizeAmount(val));
			pOutputs.put(addr, val);


			/*OP_RETURN*/
			if ( args.containsKey("hex")){
				String data = args.get("hex");
				if (!data.equals("null")) {
					//add OP_Return
					pOutputs.put("data", data);
					output += "<br><div><pre>calling rpc server with createrawtransaction \"[]\" \"{\""+addr+"\":"+val+", \"data\":\""+data+"\"}\"</pre></div>";
				} else {
					output += "<br><div><pre>calling rpc server with createrawtransaction \"[]\" \"{\""+addr+"\":"+val+"}\"</pre></div>";
				}
			} else {
				output += "<br><div><pre>calling rpc server with createrawtransaction \"[]\" \"{\""+addr+"\":"+val+"}\"</pre></div>";
			}


			String raw;
			try {
				raw = myQuery(method, pInputs, pOutputs);

			} catch (BitcoinRPCException ex) {
				output += get_error(ex.getMessage());
				output += create_footer_simple(wf);
				return output;
			}

			JsonReader jsonReader = Json.createReader(new StringReader(raw));
			JsonObject jobjRawTX = jsonReader.readObject();
			String res = jobjRawTX.get("result").toString().replace("\"", "");

			//System.out.println(res);

			//decoderawtransaction
			raw = myQuery("decoderawtransaction", res);

			jsonReader = Json.createReader(new StringReader(raw));
			JsonObject jobjTXDetails = jsonReader.readObject();

			//System.out.println(jobjTXDetails);

			JsonObject json = Json.createObjectBuilder()
					.add("result", jobjRawTX)
					.add("details",jobjTXDetails)
					.build();
			String result = json.toString();

			//System.out.println(result);

			raw=result;

			output += "<div><pre id=\"rawResponse\"></pre></div>";
			output += "<script>var data ="+ raw+"\n";
			output += create_footer_close_script(wf);

			return output;
		}

		case callMethodWithArgs:
		{
			//String test = bc.getBlockHash(0);
			if ( !args.containsKey("method") ){
				output += create_header(wf);
				output += get_error("wrong_args: method=xyz missing...");
				output += create_footer_simple(wf);
				break;
			}
			String methodArgs = args.get("method");

			Object[] splitArray = methodArgs.split("\\s+");
			String method = (String) splitArray[0];
			//-1, da erstes Argument method-name
			int argCount = splitArray.length -1;

			try {

				// Object[] splitArgs = Arrays.copyOfRange(splitArray, 1, splitArray.length-1);
				// List<Integer> splitArgs = new ArrayList<Integer>();
				List<Object> splitArgs = new ArrayList<Object>();
				for (int i=1; i<splitArray.length; i++) {
					Object arg = splitArray[i];
					if (isStringObject(arg)) {
						splitArgs.add((Object) removeQuotes((String) arg));
						//splitArgs.add((Object) "{\"n431ENcQr9tarvuYCkkpu6oMA9m451xbMM\":2}");

					} else if (isSStringObject(arg)) {
						splitArgs.add((Object) removeSQuotes((String) arg));
						//splitArgs.add((Object) "[]");
					}
					else {
						String num = (String) splitArray[i];
						
						double d = Double.valueOf(num);
						int in = (int) d;
						double din = Double.valueOf(in);
						//ok?
						if (d-din<0.00000001) {
							splitArgs.add(in);
						} else {
							splitArgs.add(d);
						}

						//splitArgs.add(Integer.valueOf((String)splitArray[i]));
					}

				}

				output += create_header(wf);
				output += "<br><div><pre>calling rpc server with method(args)="+method+" "+splitArgs.toString()+"</pre></div>";
				try {
					output += callMethod(method, splitArgs.toArray());
					output += create_footer_close_script(wf);
				} catch (BitcoinRPCException ex) {
					output += get_error(ex.getMessage());
					output += create_footer_simple(wf);
					return output;
				}
				break;	

			} catch (PatternSyntaxException ex) {
				// 
			}

		}


		/*
		case getnetworkinfo:
			output += create_header(wf);
			output += "<br><div><pre>calling rpc server with method=getnetworkinfo</pre></div>";
			output += getnetworkinfo();
			output += create_footer(wf);
			break;

		case getblockchaininfo:
			output += create_header(wf);
			output += "<br><div><pre>calling rpc server with method=getblockchaininfo</pre></div>";
			output += getblockchaininfo();
			output += create_footer(wf);
			break;
		 */



		/**
		 * Navigation and other stuff	
		 */

		case PAGE:
			if ( !args.containsKey("name")){
				output += create_header(wf);
				output += get_error("wrong_args: number missing...");
				output += create_footer_simple(wf);
				break;
			}
			output += create_header(wf);
			output += get_page(args.get("name"));
			output += create_footer_simple(wf);
			break;

		case MAIN:
			output += create_header(WebFunc.MAIN);
			output += get_main();
			output += create_footer_simple(WebFunc.MAIN);
			break;
		case ERROR:
			output += create_header(WebFunc.ERROR);
			output += get_error(args.get("error_message"));
			output += create_footer_simple(WebFunc.MAIN);
			break;

		default:
			output += create_header(WebFunc.MAIN);
			output += get_main();
			output += "<br>default case<br>";
			output += create_footer_simple(WebFunc.MAIN);

		}
		//System.out.println(output);
		return output;
	}

	/*
    public static String stringify(List<String> inList) {
		return name;	
    }

    public static String stringify(String name, String value) {
        StringBuilder b = new StringBuilder();
        b.append('{');
        boolean first = true;
        for (Map.Entry e : ((Map<Object, Object>)m).entrySet()) {
            if (first)
                first = false;
            else
                b.append(",");
            b.append(stringify(e.getKey().toString()));
            b.append(':');
            b.append(stringify(e.getValue()));

        }
        b.append('}');
        return b.toString();
    }*/

	//use regex.... ;-)
	private boolean isStringObject(Object arg) {
		String rem = removeQuotes((String) arg);
		return !rem.equals(arg); 
	}
	private boolean isSStringObject(Object arg) {
		String rem = removeSQuotes((String) arg);
		return !rem.equals(arg); 
	}

	private String removeSQuotes(String arg) {
		String rem = arg.replaceAll("'", "");
		return rem;
	}

	private String removeQuotes(String arg) {
		String rem = arg.replaceAll("\"", "");
		//System.out.println("arg="+arg);
		//System.out.println("rem="+rem);
		return rem;
	}


	private String callMethod(String method, Object... o) {
		String output = "";
		switch (method) {
		case "GETblockfromheight":
			//float f = (Float) o[0];
			//int bh = (int) f;
			//String blockHash = bc.getBlockHash(bh);
			String blockHash = bc.getBlockHash((int) o[0]);
			output += callMethod("getblock", blockHash);
			break;
		default:
			output += getRawResponseJson(method, o);
		}
		return output;
	}

	private String getRawResponseJson(String method, Object... o) {
		//try {
		String output = "";

		String raw;

		//String raw = bc.getBlockHash(0);
		//int height = 0;
		raw = myQuery(method, o);
		switch (method) {
		case "help":
			//	System.out.printf("%s", raw);
			String replace1 = raw.replaceAll("\\\\n", "<br>");
			String replace2 = replace1.replaceAll("\\\\", "");
			//System.out.println(replace2);
			output += "<div><pre>"+replace2+"</pre><div><script>";
			break;
		default:
			output += "<div><pre id=\"rawResponse\"></pre></div>";
			output += "<script>var data ="+ raw+"\n";
		}


		return output;
		//	} catch (BitcoinRPCException ex) {
		//	return get_error(ex.getMessage());
		//	}

	}

	private String getRawResponseJsonMap(String method, Object... o) {

		try {
			Map response = (Map) bc.query(method, o);
			return prettyPrintJsonMap(response);
		} catch (BitcoinRPCException ex) {
			//return get_error(String.valueOf(ex.getResponseCode()));
			return get_error(String.valueOf(ex.getMessage()));

		}
	}

	private String prettyPrintJsonMap(Map jsonMap) {
		String Output ="<div><pre>";
		Set<String> keys = jsonMap.keySet();

		for (String key: keys) {
			String value = jsonMap.get(key).toString();
			Output +=key+ " : "+value+"<br>";
		}
		Output += "</div></pre>";
		return Output;
	}

	public String myQuery(String method, Object... o) throws BitcoinRpcException {
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) rpcUrl.openConnection();

			conn.setDoOutput(true);
			conn.setDoInput(true);


			if (conn instanceof HttpsURLConnection) {
				if (bc.getHostnameVerifier() != null)
					((HttpsURLConnection) conn).setHostnameVerifier(bc.getHostnameVerifier());
				if (bc.getSslSocketFactory() != null)
					((HttpsURLConnection) conn).setSSLSocketFactory(bc.getSslSocketFactory());
			}

			//			            conn.connect();
			((HttpURLConnection) conn).setRequestProperty("Authorization", "Basic " + authStr);
			byte[] r = bc.prepareRequest(method, o);
			conn.getOutputStream().write(r);
			conn.getOutputStream().close();
			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				throw new BitcoinRPCException("Response code was: "+responseCode);
			}


			;
			String Output = new String(loadStream(conn.getInputStream(), true), bc.QUERY_CHARSET);


			return Output;
		} catch (IOException ex) {
			throw new BitcoinRPCException(method, Arrays.deepToString(null), ex);
		}
	}

	private static byte[] loadStream(InputStream in, boolean close) throws IOException {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		for (;;) {
			int nr = in.read(buffer);

			if (nr == -1)
				break;
			if (nr == 0)
				throw new IOException("Read timed out");

			o.write(buffer, 0, nr);
		}
		return o.toByteArray();
	}



	public String get_error(String error_code) {
		String Output = "<br><div><pre>" +error_code +"</pre></div>";
		return Output;
	}


	private String get_main() {
		String path = pathStaticPages+ "index.html";
		String output = "";
		try {
			output = readFile(path, Charset.defaultCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}


	private String get_page(String name) {
		String path = pathStaticPages+ "page_"+name+".html";
		String output = "";
		try {
			output = readFile(path, Charset.defaultCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			output = get_main();
		}
		return output;
	}

	private String create_header(WebFunc wf){
		String path1 = pathStaticPages+ "header_part1.html";
		String path2 = pathStaticPages+ "header_part2.html";
		String output = "";
		try {
			output = readFile(path1, Charset.defaultCharset());
			output += bitcoinVersion + " ("+serverName+")";
			output += readFile(path2, Charset.defaultCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}

	private String create_footer_simple(WebFunc wf){


		String output = "";
		//output += callMethod("help");
		output += "</div></body></html>";

		return output;

	}

	private String create_footer_close_script(WebFunc wf){
		String path1 = pathStaticPages+ "footer_close_script.html";
		String output = "";
		try {
			output = readFile(path1, Charset.defaultCharset());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}




}