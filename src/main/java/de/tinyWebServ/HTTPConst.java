package de.tinyWebServ;

public abstract class HTTPConst {
	
	final static String REQUEST_METHOD_GET = "GET";
	final static String REQUEST_METHOD_HEAD = "HEAD";
	
	final static String STATUSLINE_RESPONSE_200 = "HTTP/1.1 200 OK\r\n";
	final static String STATUSLINE_RESPONSE_404 = "HTTP/1.1 404 Not Found\r\n";
	final static String STATUSLINE_RESPONSE_501 = "HTTP/1.1 501 Not implemented\r\n";
	
	final static String HEADER_SERVER = "Server: ";
	final static String HEADER_HOST = "Host: ";
	final static String HEADER_CONNECTION = "Connection: ";
	final static String HEADER_CONTENT_LENGTH = "Content-Length: ";
	final static String HEADER_CONTENT_TYPE = "Content-Type: ";
	final static String HEADER_SET_COOKIE = "set-cookie: ";
	final static String HTTP_NEWLINE = "\r\n";
	
	final static String DOCUMENT_PATH = "documents/";
	
	final static String COOKIE_UNKNOWN = "unknown";
	

}
