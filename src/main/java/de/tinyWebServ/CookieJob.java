package de.tinyWebServ;

import java.util.LinkedList;
import java.util.List;

public class CookieJob {
	
	private String clientID;
	private List<String> ressourceRecords = new LinkedList<>();

	public CookieJob(String clientID) {
		this.clientID = clientID;
	}

	public String getClientID() {
		return clientID;
	}

	public List<String> getRessourceRecords() {
		return ressourceRecords;
	}

	public void setRessourceRecords(List<String> ressourceRecords) {
		this.ressourceRecords = ressourceRecords;
	}
	
	public void appendRessourceRecord(String ressourceRecord) {
		ressourceRecords.add(clientID + " " + ressourceRecord);
	}
}
