package de.tinyWebServ;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
	
	Map<String,Boolean> sessions = new HashMap<>();
	
	public synchronized void addClientID(String clientID) {
		sessions.put(clientID, false);
	}
	
	public synchronized boolean isCookieAlreadySetAndSet (String clientID) {
		if (sessions.get(clientID) == null) {
			return true;
		}else if (sessions.get(clientID)) {
			return true;
		}else {
			sessions.replace(clientID, true);
			return false;
		}
	}
}
