package net.arptell.dhcpcord.util;

import net.arptell.dhcpcord.entities.*;
import net.dv8tion.jda.core.entities.Guild;

public class ListeningTCPConnection extends TCPConnection {
	
	Service service = null;
	
	public ListeningTCPConnection(String ip, int port, Guild guild, Service service) {
		super(ip, port, guild);
		this.service = service;
	}
	
	public Service getService() {
		return service;
	}
}
