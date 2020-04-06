/**
 * tinyWebServ - a small and fast multithreaded webserver for publishing static homepages
 *
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(	description = "small and fast multithreaded webserver",
			mixinStandardHelpOptions = true, name = "tinyWebServ", version = "tinyWebServ 0.1.0")
public class Server implements Callable<Integer> 
{
	private Map<String,Object> config = new HashMap<>();

	@Override
	public Integer call() throws Exception {

		config.put("port", port);
		config.put("workers", workers);
		config.put("directory", directory);
		
    	Thread tinyWebServ = new Thread( new TinyWebServ(config));
    	tinyWebServ.run();

		return 0;
	}
	
	private Integer checkPort(Integer portCandid) {

		if ( portCandid > 1023 && portCandid < 65536 ) {
			port = portCandid;
		}
		else {
			System.out.println("You want to start the server on a well-know port"
					+ " make sure you have sufficient rights.");
			port = portCandid;
		}

		return port;
	}
	
	// picocli option definitions
	@Option(names = {"-d", "--directory"}, description = "The directory to serve.")
	private String directory = null;

	@Option(names = {"-p", "--port"}, description = "TCP port")
	private Integer port = 8080;
	
	@Option(names = {"-w", "--workers"}, description = "Number of maximum threads (Default: 100).")
	private Integer workers = 100;
	
    public static void main( String[] args )
    {
    	
    	int exitCode = new CommandLine(new Server()).execute(args);
    	System.exit(exitCode);
    	
    	//waiting that all Threads are done (TODO: using Features for that)
    }

}
