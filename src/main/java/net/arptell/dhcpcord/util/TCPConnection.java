package net.arptell.dhcpcord.util;

import net.dv8tion.jda.core.entities.Guild;

public abstract class TCPConnection {
	
	private String ip = null;
	private int port = -1;
	private Guild guild = null;
	
	public TCPConnection(String ip, int port, Guild guild) {
		this.ip = ip;
		this.port = port;
		this.guild = guild;
	}
	
	public int getPort() {return this.port;}
	public String getIp() {return this.ip;}
	public Guild getGuild() {return this.guild;}
	
	public boolean equals(TCPConnection conn) {
		return (this.ip.equals(conn.getIp()) && this.port == conn.getPort() && conn.getGuild().getIdLong() == this.guild.getIdLong());
	}
	
}
