package de.tinyWebServ;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BlockingQueue;

public class CookieManager implements Runnable {
	
	private BlockingQueue<CookieJob> cookieQueue;
	private String directory;
	private BufferedWriter outWriter;

	public CookieManager(BlockingQueue<CookieJob> cookieQueue, String directory) {
		this.cookieQueue = cookieQueue;
		this.directory = directory;
	}

	private boolean setupDir() {
		Path dir = Paths.get(directory);
		try {
			if(Files.isDirectory(dir) && Files.isWritable(dir)) {
				return true;
			}
			else {
				Files.createDirectory(dir);
				return true;
			}
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public void run() {
		
		boolean dirCreated = setupDir();
		
		while(! Thread.currentThread().isInterrupted()) {
			while(! cookieQueue.isEmpty() && dirCreated) {
				try {
					CookieJob currCookie = cookieQueue.take();
					Path currPath = Paths.get(directory + currCookie.getClientID());
					
					if(! Files.exists(currPath)) {
						Files.createFile(currPath);
						//debugInfo:
						//System.out.println("CookieManager: New Client detected: "
						//+ currCookie.getClientID());
					}
					else {
						//debugInfo:
						//System.out.println("CookieManager: Client already known: "
						//+ currCookie.getClientID());
					}

					outWriter = Files.newBufferedWriter(	currPath,
															StandardCharsets.UTF_8,
															StandardOpenOption.APPEND);
					
					for(String ressource: currCookie.getRessourceRecords()) {
						outWriter.write(ressource);
						outWriter.newLine();
					}
					outWriter.close();
					
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		System.out.println("shutdown Cookie-Manager ...");
	}
}
