package net.dhcpcord.backend.handlers;

import net.dhcpcord.backend.DHCPBackend;

public interface Handler {

	String handle(String[] args) throws Exception;
	default DHCPBackend getServer() {
		return DHCPBackend.getSelf();
	}
}
