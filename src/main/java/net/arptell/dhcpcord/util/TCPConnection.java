package net.arptell.dhcpcord.util;

import net.arptell.dhcpcord.entities.Service;
import net.dv8tion.jda.api.entities.Guild;

public class TCPConnection {
	
	private String ip = null;
	private int port = -1;
	private Guild guild = null;
	private boolean isListening = true;
	private Service service;
	
	public TCPConnection(String ip, int port, Guild guild, Service service) {
		if(port < 0 || port > 65535) {
			throw new IllegalArgumentException("Port cannot be smaller than 0 or larger than 65535!");
		}
		this.ip = ip;
		this.port = port;
		this.guild = guild;
		this.service = service;
	}
	public int getPort() {return this.port;}
	public String getIp() {return this.ip;}
	public Guild getGuild() {return this.guild;}
	
	public boolean equals(TCPConnection conn) {
		return (this.ip.equals(conn.getIp()) && this.port == conn.getPort() && conn.getGuild().getIdLong() == this.guild.getIdLong()/* && (this.isListening == conn.isListening())*/);
	}
	public boolean equals(String ip, int port, Guild guild) {
		return (this.getIp().equals(ip) && this.getPort() == port && this.getGuild().getIdLong() == guild.getIdLong());
	}
	public boolean isListening() {return this.isListening;}
	public Service getService() {return this.service;}
	public String toString() {
		return "Guild: " + guild + ", IP: " + ip + ", Port: " + port;
	}
}
