package net.arptell.dhcpcord.deprecated;

import net.arptell.dhcpcord.entities.*;
import net.arptell.dhcpcord.util.TCPConnection;
import net.dv8tion.jda.core.entities.Guild;
@Deprecated
public class ListeningTCPConnection extends TCPConnection {
	
	Service service = null;
	
	public ListeningTCPConnection(String ip, int port, Guild guild, Service service) {
		super(ip, port, guild, service);
		this.service = service;
	}
	
	public Service getService() {
		return service;
	}
	public void setService(Service service) {
		this.service = service;
	}
}
