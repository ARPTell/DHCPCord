package net.dhcpcord.backend;

import java.net.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import net.dhcpcord.backend.errors.*;
import net.dhcpcord.backend.handlers.*;
import net.dhcpcord.backend.util.EntityChecker;

public class DHCPBackend {
	
	private static final String[] IP_RANGES = {"192.168.%d.%d", "10.0.%d.%d"};
	private static HashMap<String, ArrayList<String>> freedIps = new HashMap<>();
	private static PrintWriter output;
	private static Handler assignHandler = new AssignHandler();
	private static Handler flushHandler = new FlushHandler();
	private static Handler getHandler = new GetHandler();
	private static Handler setHandler = new SetHandler();
	private static Handler releaseHandler = new ReleaseHandler();
	private static Handler serviceHandler = new ServiceHandler();
	
	
	public static void main(String[] args) throws Exception{
		boolean crashed = false;
		ServerSocket server = new ServerSocket(47606);
		System.out.println("Started DHCP server on " + server.getInetAddress().getHostAddress() + ":" + server.getLocalPort());
		while(true) {
			try {
				Socket conn = server.accept();
				System.out.println("Received connection from " + conn.getRemoteSocketAddress());
				output = new PrintWriter(conn.getOutputStream());
				BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				output.println();
				output.flush();
				crashed = false;
				Handler handler = null;
				String cmd = null;
				String[] cmdParsed = null;
				System.out.println("Waiting for requests...");
				while((cmd = input.readLine()) != null) {
					System.out.println("Received request: " + cmd);
					try {
						cmdParsed = cmd.split(" ");
						handler = getHandler(cmdParsed[0]);
						if(handler == null) {
							throw new UnsupportedOperationException("Bad intent: " + cmdParsed[0]);
						}
						if(cmdParsed.length > 1 && !EntityChecker.isValid(cmdParsed[1])) {
							throw new UnsupportedOperationException("Bad entity: " + cmdParsed[1]);
						}
						output.println(handler.handle(cmdParsed));
					}
					catch(UnsupportedOperationException e) {
						output.println(Errors.ERR_SYNTAX + " " + e.getMessage());
					}
					catch(ArrayIndexOutOfBoundsException e){
						String arg;
						switch(e.getMessage()) {
						case "0": arg = "intent"; break;
						case "1": arg = "entity"; break;
						case "2": arg = "guild"; break;
						case "3": arg = "user"; break;
						case "4": arg = "ip"; break;
						default: arg = "unknown";
						}
						output.println(Errors.ERR_ARGS + " Missing argument: " + arg);
					}
					catch(Exception e) {
						e.printStackTrace();
						output.println(Errors.ERR_UNKNOWN + " " + e.getMessage());
					}
					output.flush();
				}
				System.out.println("Connection closed");
				
			}
			catch(Exception e) {
				System.out.println("An error occured:");
				e.printStackTrace();
				if(crashed) {
					server.close();
					System.exit(1);
				}
				crashed = true;
			}
		}
	}
	public static void createFolder(String guild) {
		new File("dhcp/" + guild).mkdirs();
	}
	/*
	private static boolean initCache() {
		System.out.println("Loading cache...");
		try {
			File file = new File("dhcp/");
			if(!file.exists()) {
				System.out.println("Creating DHCP Directory...");
				file.mkdirs();
			}
			File[] dirs = file.listFiles();
			File[] tmp;
			for(File f : dirs) {
				if(f.isDirectory()) {
					System.out.println("Scanning " + f.getPath());
					tmp = f.listFiles();
					cache.put(f.getName(), new HashMap<>());
					for(File fl : tmp) {
						System.out.println("Found file " + fl.getName());
						System.out.println("IP: " + getIp(f.getName(), fl.getName()));
					}
				}
			}
		}
		catch(Exception e) {
			return false;
		}
		System.out.println("Done!");
		return true;
	}*/
	@SuppressWarnings("resource")
	public static String getUser(String guild, String ip) throws Exception{
		System.out.println("Getting user associated with IP " + ip + "... (Guild: " + guild + ")");
		File file = new File("dhcp/" + guild + "/" + ip);
		if(file.exists()) {
			return new Scanner(file).nextLine();
		}
		System.out.println("IP " + ip + " not registered");
		throw new Exception("IP not assigned to a user");
	}
	public static String getIp(String guild, String user) throws Exception {
		return getIp(guild, user, true);
	}
	public static String getIp(String guild, String user, boolean assign) throws Exception{
		String ip = "";
		File file = new File("dhcp/" + guild);
		if(!file.exists()) {
			file.mkdirs();
		}
		file = new File("dhcp/" + guild + "/" + user);
		if(!file.exists() && assign) {
			return assignIp(guild, user);
		}
		try {
			Scanner sc = new Scanner(file);
			ip = sc.nextLine();
			sc.close();
		}
		catch(FileNotFoundException e) {
			ip = null;
		}
		return ip;
	}
	public static void setIp(String guild, String user, String ip, boolean write) throws Exception{
		try {
			createFolder(guild);
		}
		catch(Exception e) {}
		if(!write) {return;}
		File file = new File("dhcp/" + guild + "/" + user);
		FileWriter fw = new FileWriter(file);
		fw.write(ip);
		fw.close();
		file = new File("dhcp/" + guild + "/" + ip);
		fw = new FileWriter(file);
		fw.write(user);
		fw.close();
		try {
			freedIps.get(guild).remove(ip);
		}
		catch(Exception e) {}
		System.out.println("Assigned IP " + ip + " to user " + user);
	}
	public static String assignIPBulk(String guild, String userStr) throws Exception{
		try {
			createFolder(guild);
		}
		catch(Exception e) {}
		String[] users = userStr.split(",");
		String ip = null;
		String range = IP_RANGES[(int)(Long.parseLong(guild) % 2L)];
		if(freedIps.get(guild) == null) {
			freedIps.put(guild, new ArrayList<>());
		}
		int complete = 0;
		int x = 0;
		while(x < 255*255 && complete < users.length) {
			if(complete == users.length) { //Just in case
				return "Done";
			}
			if(!freedIps.get(guild).isEmpty()) {
				ip = freedIps.get(guild).remove(0);
				setIp(guild, users[complete], ip, true);
				complete++;
				continue;
			}
			ip = String.format(range, (x / 255), (x % 254) + 1);
			x++;
			try {
				getUser(guild, ip);
			}
			catch(Exception e) {
				setIp(guild, users[complete], ip, true);
				complete++;
			}
			if(complete == users.length) {
				return "Done";
			}
		}
		return "Done";
	}
	public static String assignIp(String guild, String user) throws Exception{
		try {
			createFolder(guild);
		}
		catch(Exception e) {}
		String ip = null;
		String range = IP_RANGES[(int)(Long.parseLong(guild) % 2L)];
		File file = null;
		if(freedIps.get(guild) == null) {
			freedIps.put(guild, new ArrayList<>());
		}
		if(freedIps.get(guild).isEmpty()) {
			int x = 0;
			do {
				ip = String.format(range, (x / 255), (x % 254) + 1);
				System.out.println("Trying IP " + ip + "...");
				file = new File("dhcp/" + guild + "/" + ip);
				if(!file.exists()) {
					break;
				}
				else {
					ip = null;
				}
				x++;
			}
			while(x < 255*255);
			if(ip == null) {
				throw new Exception(Errors.ERR_IP_ASSIGN + " IP range full for this guild!");
			}
		}
		else {
			ip = freedIps.get(guild).remove(0);
		}
		setIp(guild, user, ip, true);
		System.out.println(ip + " was free!");
		return ip;
	}
	public static void release(String guild, String user) throws Exception{
		try {
			freedIps.get(guild).add(getIp(guild, user));
		}
		catch(Exception e) {}
		File file = new File("dhcp/" + guild + "/" + getIp(guild, user));
		file.delete();
		file = new File("dhcp/" + guild + "/" + user);
		file.delete();
	}
	public static void flush(String guild) throws Exception{
		File folder = new File("dhcp/" + guild);
		if(!folder.exists()) {
			return;
		}
		File[] files = folder.listFiles();
		for(File file : files) {
			file.delete();
		}
		folder.delete();
		try {
			freedIps.remove(guild);
		}
		catch(Exception e) {}
		System.out.println("Done!");
	}
	public static PrintWriter getPrintWriter() {
		return output;
	}
	public static DHCPBackend getSelf() {
		return new DHCPBackend();
	}
	public static Handler getHandler(String intent) {
		switch(intent) {
		case "GET": return getHandler;
		case "SET": return setHandler;
		case "SERVICE": return serviceHandler;
		case "RELEASE": return releaseHandler;
		case "FLUSH": return flushHandler;
		case "ASSIGN": return assignHandler;
		case "ASSIGNBULK": return assignHandler;
		default: return null;
		}
	}
}
