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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BlockingQueue;

public class TinyLogger implements Runnable {

	private BlockingQueue<String> queue;
	private String path;
	private Path nioPath;
	private BufferedWriter outWriter;
	
	public TinyLogger(BlockingQueue<String> queue,String path) {
		this.queue = queue;
		this.path = path;
	}
	
	private void cleanup() {
		System.out.println("cleanup logger ...");

		try {
			outWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initLogFile() {
		try {
			nioPath = Paths.get(path);

			if(! Files.exists(nioPath)) {Files.createFile(nioPath);}
			
			outWriter = Files.newBufferedWriter(
					nioPath,
					StandardCharsets.UTF_8,
					StandardOpenOption.APPEND
					);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		initLogFile();
		
		while(! Thread.currentThread().isInterrupted()) {
			try {

				outWriter.write(queue.take());
				outWriter.newLine();
				outWriter.flush();

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		cleanup();
	}
}
