package net.arptell.dhcpcord.handlers;

import net.arptell.dhcpcord.exceptions.*;
import net.arptell.dhcpcord.util.TCPConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

import org.json.JSONObject;

import net.dv8tion.jda.core.entities.*;

public class DHCPConnectionHandler {
		
		private Socket conn = null;
		private PrintWriter output = null;
		private BufferedReader input = null;
		
		private String ip = null;
		private int port = -1;
		
		public DHCPConnectionHandler(String ip, int port) throws DHCPConnectException {
			try {
				System.out.println("Connecting to " + ip + ":" + port + "...");
				this.ip = ip;
				this.port = port;
				conn = new Socket(ip, port);
				output = new PrintWriter(conn.getOutputStream(), true);
				input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				input.readLine();
			}
			catch(Exception e) {
				throw new DHCPConnectException(e.getMessage());
			}
		}
		public String makeRequest(String request) throws Exception{
			if(conn.isClosed()) {
				throw new IllegalStateException("Already disconnected from the server!");
			}
			try {
				System.out.println(request);
				output.println(request);
				output.flush();
				return checkForException(input.readLine()).replace("%20", " ");
			}
			catch(IOException e) {
				close();
			}
			return null;
		}
		public DHCPConnectionHandler reconnect() throws Exception{
			try {
				close();
			}
			catch(Exception e) {}
			return new DHCPConnectionHandler(ip, port);
		}
		public String getIp(Member member) throws Exception{
			if(member.getUser().isBot()) {return null;}
			return makeRequest("GET IP " + member.getGuild().getId() + " " + member.getUser().getId());
		}
		public String assignIp(Member member) throws Exception{
			if(member.getUser().isBot()) {return null;}
			return makeRequest("ASSIGN IP " + member.getGuild().getId() + " " + member.getUser().getId());
		}
		public boolean setIp(Member member, String ip) throws Exception{
			if(member.getUser().isBot()) {return false;}
			return Boolean.parseBoolean(makeRequest("SET IP " + member.getGuild().getId() + " " + member.getUser().getId() + " " + ip));
		}
		public String getUser(Guild guild, String ip) throws Exception{
			return makeRequest("GET USER " + guild.getId() + " " + ip);
		}
		public void releaseIp(Member member) throws Exception{
			releaseIp(member.getGuild(), member.getUser());
		}
		public void releaseIp(Guild guild, User user) throws Exception{
			if(user.isBot()) {return;}
			makeRequest("RELEASE IP " + guild.getId() + " " + user.getId());
		}
		public void flush(Guild guild) throws Exception{
			makeRequest("FLUSH IP " + guild.getId() + " filler");
		}
		public String getService(String ip, int port, Guild guild) throws Exception{
			return makeRequest("GET SERVICE " + guild.getId() + " " + ip + " " + port);
		}
		public int getServicePort(String user, String name, Guild guild) throws Exception{
			if(user.contains(".")){
				user = getUser(guild, user);
			}
			return Integer.parseInt(makeRequest("GET PORT " + guild.getId() + " " + getUser(guild, user) + " " + name));
		}
		public void createService(TCPConnection conn) throws Exception{
			createService(conn.getIp(), conn.getPort(), conn.getGuild(), conn.getService().getJSON());
		}
		public void createService(String user, int port, Guild guild, JSONObject json) throws Exception{
			if(user.contains(".")){
				user = getUser(guild, user);
			}
			makeRequest("SET SERVICE " + guild.getId() + " " + user + " " + (json.has("name") ? json.getString("name") : "Untitled-" + json.hashCode()) + " " + port + " " + json.toString().replace("\n", "").replace(" ", "%20"));
		}
		public void deleteService(String user, int port, Guild guild) throws Exception{
			if(user.contains(".")) {
				user = getUser(guild, user);
			}
			makeRequest("RELEASE SERVICE " + guild.getId() + " " + user + " " + port);
		}
		private String checkForException(String s) throws Exception{
			System.out.println(s);
			if(s.startsWith("ERROR")) {
				String[] errSplit = s.split(" ", 3);
				String errType = errSplit[1];
				String errMsg = errSplit[2];
				throw new DHCPException(errMsg, errType);
			}
			return s;
		}
		public void close() throws IOException{
			if(conn.isClosed()) {
				throw new IllegalStateException("Already disconnected from the server!");
			}
			input.close();
			output.close();
			conn.close();
		}
}
