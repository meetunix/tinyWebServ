package de.tinyWebServ;

/**
 * Main class for tinyWebServ.
 *
 */
public class Server 
{
	private static int port = 8888;
	
    public static void main( String[] args )
    {
    	if (args.length >= 1) {
    		int portCandid = Integer.parseInt(args[0]);
    		if ( portCandid > 1023 && portCandid < 65536 ) {
    			port = portCandid;
    		}
    		else {
    			System.out.println("You want to start the Server on a well-know port"
    					+ " make sure you have sufficient rights.");
    			port = portCandid;
    		}
    	}
    	
    	Thread tinyWebServ = new Thread( new TinyWebServ(port));
    	tinyWebServ.run();
    	
    	//waiting that all Threads are done (TODO: using Features for that)
    }
}
