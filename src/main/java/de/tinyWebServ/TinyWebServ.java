package de.tinyWebServ;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TinyWebServ implements Runnable {
	
	private int 					port;
	private ServerSocket			serverSocket;
	private Thread					runningThread;
	private Thread					accLogger;
	private Thread					errLogger;
	private ExecutorService 		threadPool = Executors.newFixedThreadPool(100);

	protected BlockingQueue<String> accLogQueue = new ArrayBlockingQueue<>(1000);
	protected BlockingQueue<String> errLogQueue = new ArrayBlockingQueue<>(1000);

	public TinyWebServ(int port) {
		this.port = port;
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
		/*
		runningWorkers.add(accLogger);
		runningWorkers.add(errLogger);

		boolean allThreadsClosed = true;
		for(Runnable r: runningWorkers) {
			((Thread)r).isAlive();
		}
		 */

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
		
		long i = 0;
		while(! runningThread.isInterrupted() ) {
			Socket client = null;
			
			try {

				Thread.sleep(10);
				client = serverSocket.accept();
				i += 1;

			} catch (IOException e) {
				errLogger("ERROR: unable to open client port");
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			SessionManager sManager = new SessionManager();
			threadPool.execute( new TinyWorker(client, accLogQueue, errLogQueue, sManager, i) );
			//if (i == 4) { runningThread.interrupt();}
		}
		cleanup();
	}
}
