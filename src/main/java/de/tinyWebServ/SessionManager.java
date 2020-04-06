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
