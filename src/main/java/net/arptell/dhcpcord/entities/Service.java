package net.arptell.dhcpcord.entities;

import org.json.JSONObject;

public interface Service {
	
	public Object getContent();
	public String getName();
	public JSONObject getJSON();
}
