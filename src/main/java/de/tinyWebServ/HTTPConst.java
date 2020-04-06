/*
 * Copyright (C) 2020 Martin Steinbach
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 */


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
	
	final static String DOCUMENT_DEFAULT_PATH = "www/";
	
	final static String COOKIE_UNKNOWN = "unknown";
	

}
