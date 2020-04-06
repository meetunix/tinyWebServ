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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class TinyWorker implements Runnable {

	private Socket socket;
	private BufferedReader inReader;
	private BufferedOutputStream outStream;
	private String serverName;
	private String directory;

	// variables needed for connection handling
	private final long TIMEOUT = 10000; // 10 seconds
	private long startTime;
	private long endTime;
	private boolean isClosed = false;
	
	/* variables needed for logging
	* host ident authuser date request status bytes user-agent
	* https://en.wikipedia.org/wiki/Common_Log_Format
	*/
	private BlockingQueue<String> accLogQueue;
	private BlockingQueue<String> errLogQueue;
	private String host = "-";
	private String ident = "-";
	private String authuser = "-";
	private String requestHeader;
	// example dateString: [10/Oct/2000:13:55:36 -0700]
	private String dateString = "-";
	private SimpleDateFormat date = new SimpleDateFormat(
			"[dd/LLL/YYYY:kk:mm:ss Z]",new Locale("en")
			);
	private String userAgent = "-";
	private String clientID = HTTPConst.COOKIE_UNKNOWN;
	private SessionManager sessionManager;
	
	public long threadID;

	public TinyWorker(Socket client,
			BlockingQueue<String> accLogQueue,
			BlockingQueue<String> errLogQueue,
			SessionManager sManager,
			long threadID,
			String directory) {
		
		
		this.socket = client;
		this.accLogQueue = accLogQueue;
		this.errLogQueue = errLogQueue;
		this.sessionManager = sManager;
		this.threadID = threadID;
		this.directory = directory;
		
		try {
			this.serverName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			errLogQueue.add("can not get Hostname" +e );
		}
	}
	
	private void cleanup() {
		
		try {
			System.out.println("Worker: " + threadID + " cleanup ...");

			inReader.close();
			outStream.close();
			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setupConnection() throws IOException{
		
			System.out.println("The Client " + socket.getInetAddress()+ ":" + socket.getPort()
			+ " is connected");
			inReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outStream = new BufferedOutputStream(socket.getOutputStream());

			//following log-variable is stable as long as the worker exists
			host = new String(socket.getInetAddress() + ":[" + socket.getPort() + "]");
	}
	
	private String getContentType(Path currPath) {
		//default mime type is binary data
		String mimeType = "application/octet-stream";
		
		if (currPath.getFileName() != null) {
			String filename = currPath.getFileName().toString();
			
			if (filename.toLowerCase().matches(".*html$|.*htm$")) {mimeType = "text/html";}
			if (filename.toLowerCase().matches(".*jpeg$|.*jpg$")) {mimeType = "image/jpeg";}
			if (filename.toLowerCase().matches(".*png$")) {mimeType = "image/png";}
			if (filename.toLowerCase().matches(".*gif$")) {mimeType = "image/gif";}
			if (filename.toLowerCase().matches(".*bmp$")) {mimeType = "image/bmp";}
			if (filename.toLowerCase().matches(".*txt$")) {mimeType = "text/plain";}
			if (filename.toLowerCase().matches(".*pdf$")) {mimeType = "application/pdf";}
			if (filename.toLowerCase().matches(".*css$")) {mimeType = "text/css";}
			if (filename.toLowerCase().matches(".*js$")) {mimeType = "text/javascript";}

			return mimeType;

		}else {
			return null;
		}
	}
	
	private void sendResponse(String statusRequest, String statusResponse, Path currPath)
			throws IOException {

		byte[] bytesStatusLine = statusResponse.getBytes("ASCII");
		byte[] bytesNewLine = HTTPConst.HTTP_NEWLINE.getBytes("ASCII");
		byte[] bytesServerOption = ( new String (	HTTPConst.HEADER_SERVER
													+ serverName
													+ HTTPConst.HTTP_NEWLINE)).getBytes("ASCII"); 
		// these header fields are dynamic:
		byte[] bytesContent = null;
		//default content-type = text/html
		byte[] bytesContentType = (new String(	HTTPConst.HEADER_CONTENT_TYPE
												+ "text/html"
												+ HTTPConst.HTTP_NEWLINE)).getBytes("ASCII");
		byte[] bytesContentLength = null; 
		
		//content handling
		if (statusResponse.equals(HTTPConst.STATUSLINE_RESPONSE_501)) {
			bytesContent = "<b>501 Not implemented<b>".getBytes("UTF8");
		}else if (statusResponse.equals(HTTPConst.STATUSLINE_RESPONSE_404)) {
			bytesContent = "<b>404 Not found<b>".getBytes("UTF8");
		}else {
			bytesContent = Files.readAllBytes(currPath);
			bytesContentType = (new String(	HTTPConst.HEADER_CONTENT_TYPE
											+ getContentType(currPath)
											+ HTTPConst.HTTP_NEWLINE)).getBytes("ASCII");
		}

		bytesContentLength = (new String (	HTTPConst.HEADER_CONTENT_LENGTH
											+ bytesContent.length
											+ HTTPConst.HTTP_NEWLINE)).getBytes("ASCII");

		// create response and send it
		outStream.write(bytesStatusLine);
		outStream.write(bytesServerOption);
		outStream.write(bytesContentType);
		outStream.write(bytesContentLength); //HEAD OR not HEAD, that is the question
		// if (! statusRequest.equals(HTTPConst.REQUEST_METHOD_HEAD)) 
		// {outStream.write(bytesContentLength);}
		// 
		if (! sessionManager.isCookieAlreadySetAndSet(clientID)) {
			byte[] bytesCookie = (new String (	HTTPConst.HEADER_SET_COOKIE
												+ "clientid="
												+ clientID
												+ HTTPConst.HTTP_NEWLINE)).getBytes();
			outStream.write(bytesCookie);
		}
		outStream.write(bytesNewLine); //closes header
		// a response to HEAD does not include the requested file
		if (! statusRequest.equals(HTTPConst.REQUEST_METHOD_HEAD)) {outStream.write(bytesContent);}
		outStream.flush();

		//write to acces-log:
		dateString = date.format(new Date());
		logToAccess(	dateString,
						requestHeader,
						(statusResponse.split(" "))[1],
						Integer.toString(bytesContent.length));
	}
	
	private boolean getConnectionStatus(Map<String,String> headerBodyFields) throws IOException {

		boolean status = false;
		
		if (headerBodyFields.isEmpty() || headerBodyFields.get("connection") == null){
			return status;
		}

		if (headerBodyFields.get("connection").matches(".*[cC][lL][oO][sS][eE].*")) {
			status = true;
			}

		return status;
	}
	
	// log structure: host ident authuser date request status bytes user-agent
	private void logToAccess(
			String dateString,
			String requestHeader,
			String returnCode,
			String returnLength) {

		String logString = new String(host + " " +  ident + " " + authuser + " " + dateString + " "
				+ requestHeader + " " + returnCode + " " + returnLength + " " + userAgent);

		try {
			accLogQueue.put(logString);
		} catch (InterruptedException e) {
			errLogQueue.add("unable to log to access.log due to InterruptedException: " + e);
		}
	}

	private String getClientID(Map<String, String> headerBodyFields) {
		
		if (headerBodyFields.isEmpty() || headerBodyFields.get("cookie") == null){
			return HTTPConst.COOKIE_UNKNOWN;
		}
		else if(! headerBodyFields.get("cookie").toLowerCase().matches(".*clientid=.*")) {
			return HTTPConst.COOKIE_UNKNOWN;
		}
		else {
			return (headerBodyFields.get("cookie").trim().split("="))[1];
		}
	}

	@Override
	public void run() {
		
		try {
			setupConnection();
			
			while(! Thread.currentThread().isInterrupted()) {
				
				if (inReader.ready()) {
					// timeout from now on
					startTime = System.currentTimeMillis();
				
					//read the first line of the header
					requestHeader = inReader.readLine();
					if (requestHeader == null) {
						cleanup();
						return;
						}

					//parse the first line to get the http-request-method and validate
					String requestHeaderFields[] = requestHeader.split(" ");
					if (requestHeaderFields.length < 3) {
						cleanup();
						return;
					}

					String requestMethod = requestHeaderFields[0];
					String requestRessourcePath = requestHeaderFields[1];
					
					//retrieving the other fields from the request header
					Map<String,String> headerBodyFields = new HashMap<String, String>();					
					while (inReader.ready()) {
						String[] fields = inReader.readLine().split(": ");
						if (fields.length == 2) {
							//header-field-names are case insensitive;
							headerBodyFields.put(fields[0].toLowerCase(), fields[1]);
						}
					}
					userAgent = headerBodyFields.get("user-agent");
					//retrieving connection state from request
					isClosed = getConnectionStatus(headerBodyFields);
					
					/*
					 * setting ressource path to root directory and adding default ressource:
					 * index.html
					 */
					
					// if no docroot is given using the default one
					if (directory == null) {
						directory =  HTTPConst.DOCUMENT_DEFAULT_PATH;
					}
				
					if(requestRessourcePath.equals("/")) {
						requestRessourcePath = directory + "index.html";
					}else {
						requestRessourcePath = directory + requestRessourcePath;
					}

					//cookie handling: creating cookieJob
					clientID = getClientID(headerBodyFields);
					//System.out.println("WORKER: clientID: " + clientID);
					if(clientID.equals(HTTPConst.COOKIE_UNKNOWN) ) {
						// client not known -> generate new ID and write to sessionManager
						clientID = UUID.randomUUID().toString().replaceAll("-", "");
						sessionManager.addClientID(clientID);
					}
					
					//handling request-types
					if (requestMethod.equals(HTTPConst.REQUEST_METHOD_GET)) {	//GET
						Path currPath = Paths.get(requestRessourcePath);
						if(Files.exists(currPath)) {
							sendResponse(	HTTPConst.REQUEST_METHOD_GET,
											HTTPConst.STATUSLINE_RESPONSE_200,
											currPath);
						}else {
							sendResponse(	HTTPConst.REQUEST_METHOD_GET,
											HTTPConst.STATUSLINE_RESPONSE_404,
											null);
						}
					}
					else if (requestMethod.equals(HTTPConst.REQUEST_METHOD_HEAD)) { //HEAD
						Path currPath = Paths.get(requestRessourcePath);
						if(Files.exists(currPath)) {
							sendResponse(	HTTPConst.REQUEST_METHOD_HEAD,
											HTTPConst.STATUSLINE_RESPONSE_200,
											currPath);
						}else {
							sendResponse(	HTTPConst.REQUEST_METHOD_HEAD,
											HTTPConst.STATUSLINE_RESPONSE_404,
											null);
						}
					}
					else {
						sendResponse("",HTTPConst.STATUSLINE_RESPONSE_501, null);
					}
				}
				Thread.sleep(10);
				//accLogQueue.put("Log: Thread ");
				
				//check timeout and Connection status and interrupt thread
				endTime = System.currentTimeMillis();
				if (startTime + TIMEOUT < endTime) {
					System.out.println("Worker: " + threadID + " Connection closed due to timeout");
					Thread.currentThread().interrupt();
				}else if (isClosed) {
					System.out.println("Worker: " + threadID + " Connection closed by Client");
					Thread.currentThread().interrupt();
				}
			}

			} catch (InterruptedException e) {
				//interruptet while sleeping...
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				//TODO
				e.printStackTrace();
			}
		cleanup();
	}

}
