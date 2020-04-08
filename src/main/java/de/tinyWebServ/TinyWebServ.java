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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TinyWebServ implements Runnable {
	
	private Integer 				port;
	private Integer 				workers;
	private ServerSocket			serverSocket;
	private Thread					runningThread;
	private Thread					accLogger;
	private Thread					errLogger;
	private ExecutorService 		threadPool;
	private Map<String,Object>		config;

	protected BlockingQueue<String> accLogQueue = new ArrayBlockingQueue<>(1000);
	protected BlockingQueue<String> errLogQueue = new ArrayBlockingQueue<>(1000);

	// constructor
	public TinyWebServ(Map<String,Object> config) {
		this.port = 		(Integer)config.get("port");
		this.workers = 		(Integer)config.get("workers");
		this.config = config;
		
		// set maximum number of threads
		threadPool = Executors.newFixedThreadPool(workers);
	}
	
	private void cleanup()  {
		
		threadPool.shutdown();
		System.out.println("Server stopped ... cleanup ....");
		
		List<Runnable> runningWorkers = new ArrayList<Runnable>();
		
		try {

			runningWorkers = threadPool.shutdownNow();
			
			for(Runnable r: runningWorkers) {
				((Thread) r).interrupt();
			}
			
			serverSocket.close();
			accLogger.interrupt();
			errLogger.interrupt();

		} catch (IOException e) {
			System.err.println("ERROR: Unable to close server socket.");
			e.printStackTrace();
		}
		//TODO: check if all Threads are properly closed before I close myself

		System.out.println("Server has been stopped.");
	}
	
	private void errLogger(String logMessage) {
		try {
			errLogQueue.add(logMessage);
		}catch (IllegalStateException e) {
			System.err.println("ERROR: error queue seems to be full, no logging possible");
		}
	}
	
	@Override
	public void run() {
		//TODO: log path
		accLogger = new Thread( new TinyLogger(accLogQueue, "access.log"));
		errLogger = new Thread( new TinyLogger(errLogQueue, "error.log"));
		accLogger.start();
		errLogger.start();

		synchronized (this) {
			this.runningThread = Thread.currentThread();
		}
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			errLogger("ERROR: Unable to open port " + port
					+ " the process may need more privileges");
			System.exit(1);
		}

		SessionManager sessionManager = new SessionManager();

		long i = 0;
		while(! runningThread.isInterrupted() ) {
			Socket client = null;
			
			try {

				client = serverSocket.accept();
				i += 1;

			} catch (IOException e) {
				errLogger("ERROR: unable to open client port");
				e.printStackTrace();
			}
			
			threadPool.execute( new TinyWorker(
					client,
					accLogQueue,
					errLogQueue,
					sessionManager,
					i,
					config) );
		}
		cleanup();
	}
}
