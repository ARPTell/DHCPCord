package net.arptell.dhcpcord.handlers;

import net.arptell.dhcpcord.exceptions.*;

import java.util.HashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

import net.dv8tion.jda.core.entities.*;

public class DHCPConnectionHandler {
		
		private HashMap<Member, String> ipCache = new HashMap<>();
		private Socket conn = null;
		private PrintWriter output = null;
		private BufferedReader input = null;
		
		protected DHCPConnectionHandler(String ip, int port) throws DHCPConnectException {
			try {
				conn = new Socket(ip, port);
				output = new PrintWriter(conn.getOutputStream(), true);
				input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			}
			catch(Exception e) {
				throw new DHCPConnectException(e.getMessage());
			}
		}
		private String makeRequest(String request) {
			if(conn.isClosed()) {
				throw new IllegalStateException("Already disconnected from the server!");
			}
			try {
				output.print(request);
				return input.readLine();
			}
			catch(IOException e) {}
			return null;
		}
		public String getIp(Member member) {
			String ip = ipCache.get(member);
			if(ip == null) {
				ip = makeRequest("GET IP " + member.getGuild().getId() + " " + member.getUser().getId());
				ipCache.put(member, ip);
			}
			return ip;
		}
		public String assignIp(Member member) {
			String ip = makeRequest("ASSIGN IP " + member.getGuild().getId() + " " + member.getUser().getId());
			ipCache.put(member, ip);
			return ip;
		}
		public boolean setIp(Member member, String ip) {
			boolean success = Boolean.parseBoolean(makeRequest("SET IP " + member.getGuild().getId() + " " + member.getUser().getId() + " " + ip));
			if(success) {
				ipCache.put(member, ip);
			}
			return success;
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
