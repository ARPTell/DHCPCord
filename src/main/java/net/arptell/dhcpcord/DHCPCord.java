package net.arptell.dhcpcord;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.arptell.dhcpcord.exceptions.*;
import net.arptell.dhcpcord.handlers.DHCPConnectionHandler;
import net.arptell.dhcpcord.util.*;
import net.arptell.dhcpcord.entities.*;

public class DHCPCord extends ListenerAdapter{
	
	private static String token = "";
	private static HashMap<Guild, String> muteRoles = new HashMap<>();
	//private static final String[] IP_RANGES = {"192.168.%d.%d", "10.0.%d.%d"};
	private static final String[] IP_RANGES_NOFORMAT = {"192.168.x.x", "10.0.x.x"};
	private static ArrayList<TCPConnection> connections = new ArrayList<>();
	private static final String OWNERS = "153353572711530496 273216249021071360 190544080164487168";
	private static DHCPConnectionHandler conn = null;
	private static boolean loading = true;
	//private static final String NUMS = "0123456789.";
	private static final String PREFIX = "dhcp.";

	public DHCPCord() throws Exception {
		// Constructor!
		Scanner sc = new Scanner(new File("token.txt"));
		try {
			token = sc.nextLine();
			sc.close();
		}
		catch(Exception e) {
			System.err.println("Could not read token: " + e);
			System.exit(666);
		}
		conn = new DHCPConnectionHandler("localhost", 47606);
	}

	public void run() throws Exception {
		//Scanner sc;
		JDA jda = new JDABuilder(AccountType.BOT).setToken(token).addEventListener(this).buildBlocking();
		jda.getPresence().setGame(Game.watching("ARP poisoning attacks happen"));
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Test site - 127.0.0.1:80");
		eb.addField("Hello there!", "This is a test site. Please ignore.", true);
		eb.setColor(Color.GREEN);
		System.out.println("Loading 'database'...");
		List<Guild> guilds = jda.getGuilds();
		for(Guild guild : guilds) {
			connections.add(new TCPConnection("127.0.0.1", 80, guild, new Site(eb)));
		}
		System.out.println("Done!");
		loading = false;
	}
	
	public static void main(String[] args) throws Exception {
		new DHCPCord().run();
	}
	public DHCPConnectionHandler getConnHandler() {
		return conn;
	}
	public String getReason(String[] arr) {
		String out = "";
		ArrayList<String> tmp = new ArrayList<>();
		for(int i = arr.length - 1; i >= 0; i--) {
			try {
				Long.parseLong(arr[i]);
			}
			catch(NumberFormatException e) {
				tmp.add(arr[i]);
			}
		}
		if(tmp.isEmpty()) {
			return "No reason given";
		}
		Collections.reverse(tmp);
		for(String s : tmp) {
			out += s + " ";
		}
		return out;
	}
	public boolean connectionExists(TCPConnection conn) {
		for(TCPConnection tcp : connections) {
			if(tcp.equals(conn)) {
				return true;
			}
		}
		return false;
	}
	public void openPort(TCPConnection conn) throws PortBindException{
		if(connectionExists(conn)) {
			throw new PortBindException("Failed to bind to " + conn.getIp() + ":" + conn.getPort() + ", IP-port combination is already in use.");
		}
		connections.add(conn);
	}
	public void closePort(TCPConnection conn) throws PortCloseException{
		if(connectionExists(conn)) {
			connections.remove(conn);
			return;
		}
		throw new PortCloseException("Failed to unbind from " + conn.getIp() + ":" + conn.getPort() + ", port is already closed");
	}
	public TCPConnection getConnection(String ip, int port, Guild guild) {
		for(TCPConnection conn : connections) {
			if(conn.equals(new TCPConnection(ip, port, guild, null))) {
				System.out.println(conn);
				return conn;
			}
		}
		return null;
	}
	public String fixHex(String hex) {
		if(hex.length() == 1) {
			return "0" + hex;
		}
		return hex;
	}
	public String generateMAC(User user) {
		return generateMAC(user.getId());
	}
	public String generateMAC(String id) {
		try {
			if(Long.parseLong(id) < 0) {
				id = Long.toString(Math.abs(Long.parseLong(id)));
			}
		}
		catch(NumberFormatException e) {
			throw new IllegalArgumentException("Invalid ID");
		}
		while(id.length() < 18) {
			id = "0" + id;
		}
		String mac = "";
		String[] splitId = new String[6];
		int adder = id.length() - 18;
		for(int i = 0; i < 6; i++) {
			splitId[i] = id.substring(i*3 + adder, (i*3)+3 + adder);
		}
		if(adder > 0) {
			adder = 0;
			String tmp = id.substring(0, adder);
			for(char c : tmp.toCharArray()) {
				adder += Integer.parseInt(Character.toString(c));
			}
		}
		splitId[0] = fixHex(Integer.toHexString(Integer.parseInt((splitId[0] + adder)) % 256));
		splitId[1] = fixHex(Integer.toHexString(Integer.parseInt((splitId[1] + adder)) % 256));
		splitId[2] = fixHex(Integer.toHexString(Integer.parseInt((splitId[2] + adder)) % 256));
		splitId[3] = fixHex(Integer.toHexString(Integer.parseInt((splitId[3] + adder)) % 256));
		splitId[4] = fixHex(Integer.toHexString(Integer.parseInt((splitId[4] + adder)) % 256));
		splitId[5] = fixHex(Integer.toHexString(Integer.parseInt((splitId[5] + adder)) % 256));
		mac = splitId[0] + ":" + splitId[1] + ":" + splitId[2] + ":" + splitId[3] + ":" + splitId[4] + ":" + splitId[5];
		return mac.toUpperCase();
	}
	public String getIPOfUser(Member member) throws Exception{
		return conn.getIp(member);
	}
	public User getUserByIP(String ip, Guild guild) throws Exception{
		return guild.getJDA().getUserById(conn.getUser(guild, ip));
	}
	public void setUserIp(String ip, Member member) throws Exception{
		conn.setIp(member, ip);
	}
	public void removeUserIP(Member member) throws Exception{
		conn.releaseIp(member);
	}
	public String stripIOSQuotes(String str) {
		return str.replace("\u201C", "\"").replace("\u201D", "\"");
	}
	public Role getMutedRole(Guild guild) {
		Role muteRole = null;
		try {
			muteRole = guild.getRoleById(muteRoles.get(guild));
		}
		catch(Exception e) {
			try {
				muteRole = guild.getRolesByName("Muted", true).get(0);
			}
			catch(NullPointerException exc) {}
		}
		return muteRole;
	}
	private boolean eval(String toEval, MessageReceivedEvent event) {
		if(toEval.equalsIgnoreCase("dhcp.eval")) {
			event.getChannel().sendMessage("```Usage: dhcp.eval <code>```").queue();
			return true;
		}
		toEval = stripIOSQuotes(toEval).replace(PREFIX + "eval ", "");
		ScriptEngine se = new ScriptEngineManager().getEngineByName("Nashorn");
        se.put("bot", this);
        se.put("event", event);
        //se.put("System", System.class);
        se.put("jda", event.getJDA());
        se.put("eb", new EmbedBuilder());
        se.put("guild", event.getGuild());
        se.put("channel", event.getChannel());
        //se.put("JDAInfo", new JDAInfo());
        String out = null;
        try
        {
        	try {
        		out = se.eval(toEval).toString();
        	}
        	catch(NullPointerException e) {}
            event.getChannel().sendMessage("```" + out + "```").queue();
            return true;
        }
        catch(IllegalArgumentException exception) {
	        File file = new File("outputdhcp.txt");
	        FileWriter fw = null;
	        try {
	        	if(file.exists()) {
	        		file.delete();
	        	}
	        	file.createNewFile();
	        	fw = new FileWriter(file);
	        	fw.write(out);
	        	fw.close();
	        }
	        catch(Exception e) {}
	        event.getChannel().sendMessage("Output too long!").addFile(new File("outputdhcp.txt")).queue();
        }
        catch(Exception e)
        {
        	event.getChannel().sendMessage("```" + e.toString() + "```").queue();
            return false;
        }
        return true;
	}
	public void renewIP(String ip, Guild guild) throws Exception{
		renewIP(getUserByIP(ip, guild), guild);
	}
	public void renewIP(User user, Guild guild) throws Exception{
		renewIp(guild.getMember(user));
	}
	public void renewIp(Member member) throws Exception{
		conn.releaseIp(member);
		conn.assignIp(member);
	}
	//TODO: hex things
	public String stringToHex(String string) {
		  StringBuilder buf = new StringBuilder(200);
		  for (char ch: string.toCharArray()) {
		    if (buf.length() > 0)
		      buf.append(' ');
		    buf.append(String.format("%04x", (int) ch));
		  }
		  return buf.toString();
		}
	public int[] getGuildStats(Guild guild) {
		int[] out = new int[3];
		///Why map when you can do it yourself :D
		out[0] = guild.getMembers().size();
		for(Member m : guild.getMembers()) {
			if(m.getUser().isBot()) {
				out[1]++;
			}
			else {
				out[2]++;
			}
		}
		return out;
	}
	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		try {
			conn.flush(event.getGuild());
		}
		catch(Exception e) {}
	}
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		try {
			conn.releaseIp(event.getGuild(), event.getUser());
		}
		catch(Exception e) {}
	}
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		while(loading) {
			try {
				Thread.sleep(1000);
			}
			catch(InterruptedException e){
				
			}
			continue;
		}
		Member member = event.getMember();
		if(member.getUser().isBot()) {
			return;
		}
		String ip = "";
		try {
			ip = conn.assignIp(member);
			System.out.println("Gave IP " + ip + " to user " + member.getUser().getName() + "#" + member.getUser().getDiscriminator());
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
	
	@Override
	public void onGuildJoin(GuildJoinEvent event){
		while(loading) {
			try {
				Thread.sleep(1000);
			}
			catch(InterruptedException e){
				
			}
			continue;
		}
		Guild guild = event.getGuild();
		JDA jda = event.getJDA();
		List<Member> users = guild.getMembers();
		int bots = 0;
		System.out.println("Joined guild " + guild);
		for(Member member : users) {
			if(member.getUser().isBot()) {
				bots++;
			}
		}
		MessageChannel channel = null;
		for(MessageChannel ch : guild.getTextChannels()) {
			if(guild.getSelfMember().hasPermission((Channel)ch, Permission.MESSAGE_WRITE)) {
				channel = ch;
				break;
			}
		}
		if(channel == null) {
			channel = jda.getGuildById("293722203373961218").getTextChannelById("476035974715932673");
		}
		channel.sendMessage("Thank you for being a geek. Please wait while I assign IP address to all the users in the guild (this may take a while)\n" +
							"Users to process: " + (users.size() - bots) + " (bots are not assigned IPs and are not reflected in the count. Skipping " + bots + " bots)").queue();
		for(Member member : users) {
			if(member.getUser().isBot()) {
				continue;
			}
			try {
				conn.assignIp(member);
			}
			catch(Exception e) {
				channel.sendMessage("Warning: failed to assign IP to user " + member.getUser().getName() + "#" + member.getUser().getDiscriminator() + ": " + e).queue();
				continue;
			}
		}
		channel.sendMessage("IP assignment complete! Users may now view their IP with `dhcp.status`").queue();
	}
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().isBot() || event.getAuthor().getDiscriminator().equals("259601353419128833")) {
			return;
		}
		String msg = event.getMessage().getContentRaw().trim();
		String cmd = (msg.contains(" ") ? msg.split(" ")[0].toLowerCase() : msg.toLowerCase());
		Guild guild = event.getGuild();
		MessageChannel channel = event.getChannel();
		User user = event.getAuthor();
		if(channel.getType().equals(ChannelType.PRIVATE)) {
			channel.sendMessage("no u").queue();
			return;
		}
		String ipRange = IP_RANGES_NOFORMAT[(int)(guild.getIdLong() % 2L)];
		if(!cmd.startsWith(PREFIX)) {
			return;
		}
		cmd = cmd.replaceFirst(PREFIX, "").trim();
		System.out.println("Received command: " + msg);
		if(cmd.equals("help")) {
			channel.sendMessage(
							"```Prefix: " + PREFIX + " | This guild is on the " + ipRange + " IP range "+ " \nAll IPs are dynamic and reset on bot restart (static IPs coming soon)\n\n" + 
							"dhcp.help                           | summons this help menu\n" +
							"dhcp.status                         | gets your IP address\n" +
							"dhcp.arp <query>                    | submit ARP requests\n" +
							"dhcp.table                          | shows a list of IPs and their corresponding owners. Only works in small guilds\n" +
							"dhcp.release <user> [users...]      | kicks the given users off the network. Requires Kick Member perms.\n" +
							"dhcp.macban <user>, [users...]      | bans the given users from the network. Requires Ban Member perms\n" +
							"dhcp.sourcequench <user> [users...] | mutes the given users. Requires Manage Roles perms\n" +
							"dhcp.quench <user> [users...]       | alias of dhcp.sourcequench\n" +
							"dhcp.unsourcequench <user> [users..]| unmutes the given users. Requires Manage Roles perms\n" +
							"dhcp.unquench <user> [users...]     | alias of dhcp.unsourcequench\n" +
							"dhcp.getmac <user>                  | gets the MAC address of the given user\n" +
 							"```").queue();
			return;
		}
		if(cmd.equals("table")) {
			if(loading) {
				channel.sendMessage("Bot is still loading! Check back later").queue();
				return;
			}
			String table = "";
			for(Member member : guild.getMembers()) {
				if(member.getUser().isBot()) {
					continue;
				}
				try {
					table += member.getUser().getName() + "#" + member.getUser().getDiscriminator() + " - " + getIPOfUser(member) + "\n";
				}
				catch(Exception e) {}
				if(table.length() > 2000) {
					channel.sendMessage("Small guilds only ;)").queue();
					return;
				}
			}
			channel.sendMessage("```" + table + "```").queue();
		}
		if(cmd.equals("status")) {
			if(loading) {
				channel.sendMessage("Bot is still loading! Check back later").queue();
				return;
			}
			String ip = null;
			try {
				ip = getIPOfUser(guild.getMember(user));
			}
			catch(Exception e) {
				ip = e.toString();
			}
			channel.sendMessage(
					"```This guild is on the " + ipRange + " IP range\n\n" +
					"Your IP address is " + ip + "\n" + 
					"Your MAC address is " + generateMAC(user) + "\n" +
					(user.getId().length() > 18 ? "Warning, your MAC is not guarenteed to be unique since your user ID is larger than 18 digits" : "") +"```").queue();
			return;
		}
		if(cmd.equals("arp")) {
			if(loading) {
				channel.sendMessage("Bot is still loading! Check back later").queue();
				return;
			}
			String query = msg.trim().toLowerCase().replaceFirst(PREFIX + cmd,"").trim();
			if(query.equals("")) {
				channel.sendMessage("Usage: dhcp.arp <query>").queue();
				return;
			}
			try {
				String[] querySplit = query.split(" ");
				String output = "Error: IP not registered";
				if(!(querySplit.length >= 4)){
					throw new ARPSyntaxException("Not enough arguments");
				}
				//System.out.println(querySplit[0]);
				
				if(querySplit[0].equals("who") && querySplit.length == 5) {
					if(querySplit[1].equals("has")) {
						String tmpIp = querySplit[2].replace("?", "");
						String toTell = "";
						String toTellUser = "";
						if(tmpIp.length() < 8 || !tmpIp.substring(0, 2).equals(ipRange.substring(0, 2))) {
							throw new IllegalArgumentException("IP must be on the same IP range as the guild");
						}
						try {
							if(querySplit[3].equals("tell")) {
								try {
									toTell = querySplit[4];
									toTellUser = getUserByIP(toTell, guild).getAsMention();
								}
								catch(NullPointerException e) {
									throw new IllegalArgumentException("Unregistered IP: `" + toTell.replace("`", "") + "`");
								}
								catch(IllegalArgumentException e) {
									throw new ARPSyntaxException("an IP must come after `tell` keyword");
								}
							}
						}
						catch(ArrayIndexOutOfBoundsException e) {}
						User ipUser = getUserByIP(tmpIp, guild);
						try {
							output = (ipUser.getName() + "#" + ipUser.getDiscriminator() + " has " + tmpIp);
						}
						catch(NullPointerException e) {}
						channel.sendMessage(toTellUser + " " + output).queue();
					}
					else {
						throw new ARPSyntaxException("`has` must come after `who` keyword (`who is` coming soon)");
					}
				}/*
				if(querySplit[1].equals("is") && querySplit[2].equals("at")) {
						if(!getIPOfUser(user, ipMap).equals(querySplit[0])) {
							throw new ScriptException("ARP attacks are not supported (yet)");
						}
						String newIp = querySplit[3];
						if(newIp.length() < 8 || !newIp.substring(0, 2).equals(ipRange.substring(0, 2))) {
							throw new IllegalArgumentException("IP must be on the same IP range as the guild");
						}
						if(getUserByIP(newIp, ipMap) == null) {
							ips.get(guild).put(user, newIp);
							setUserIp(newIp, user, guild);
							output = querySplit[0] + " is now at " + newIp;
						}
						else {
							throw new ScriptException("ARP attacks are not supported (yet)");
						}
					channel.sendMessage(output).queue();
				}*/
				else if(querySplit[0].matches("^(?:10\\.0|192\\.168)\\.\\d{0,3}\\.\\d{0,3}") && querySplit.length == 4){
					if(querySplit[1].equals("is") && querySplit[2].equals("at")){
						if(querySplit[3].matches("^(?:10\\.0|192\\.168)\\.\\d{0,3}\\.\\d{0,3}")){
							String curIp = querySplit[0];
							String newIp = querySplit[3];
							if(newIp.length() < 8 || !newIp.substring(0, 2).equals(ipRange.substring(0, 2))) {
								throw new IllegalArgumentException("IP must be on the same IP range as the guild");
							}
							if(!(getUserByIP(newIp, guild) == null) || !getUserByIP(curIp, guild).equals(user)){
								throw new UnsupportedOperationException("ARP attacks are not supported (yet)");
							}
							if(!getIPOfUser(guild.getMember(user)).equals(newIp)){
								setUserIp(newIp, guild.getMember(user));
							}
							channel.sendMessage(user.getAsMention() + " " + curIp + " is at " + newIp).queue();
							return;
						} else{
							throw new ARPSyntaxException("IP must come after `at` keyword");
						}
					} else {
						throw new ARPSyntaxException("`is at <IP>` must come after IP");
					}
				}
				else {
					throw new ARPSyntaxException("Invalid syntax, request must start with `who` or an IP address");
				}
			}
			catch(Exception e) {
				channel.sendMessage("Error while parsing ARP request: " + e + "\nYou probably typed something wrong, try again").queue();
			}
			return;
		}
		if(cmd.equals("release")) {
			if(loading) {
				channel.sendMessage("Bot is still loading! Check back later").queue();
				return;
			}
			//channel.sendMessage("Command not implemented (yet) - this is not a command not found message").queue();
			if(!msg.contains(" ")) {
				channel.sendMessage("Usage: dhcp.release <user> [users...]").queue();
				return;
			}
			if(!(guild.getMember(user).hasPermission(Permission.KICK_MEMBERS) && guild.getSelfMember().hasPermission(Permission.KICK_MEMBERS))) {
				channel.sendMessage("Missing permissions: Kick members").queue();
				return;
			}
			String output = "";
			String ids = msg.trim().toLowerCase().replaceFirst(PREFIX+cmd,"").trim();
			String[] usersToKick = ids.split(" ");
			String reason = getReason(usersToKick);
			User kickedUser = null;
			for(String id : usersToKick) {
				try {
					try {
						if(id.contains(".")) {
							kickedUser = getUserByIP(id, guild);
						}
						else {
							kickedUser = guild.getMemberById(id.replace("<@", "").replace(">","")).getUser();
						}
						kickedUser.openPrivateChannel().queue((ch) -> ch.sendMessage("You were kicked from " + guild.getName() + ": " + reason).queue());
					}
					catch(Exception e) {}
					guild.getController().kick(guild.getMember(kickedUser), reason).queue();
					output += "Kicked " + kickedUser.getName() + "#" + kickedUser.getDiscriminator() + "\n";
				}
				catch(Exception e) {
					if(!(kickedUser == null)) {
						output += "Failed to kick **" + kickedUser.getName() + "#" + kickedUser.getDiscriminator() + "**: " + e.getMessage() + "\n";
					}
					else {
						output += "Failed to kick **" + id + "**: User not in guild";
					}
				}
			}
			channel.sendMessage(output).queue();
			return;
		}
		if(cmd.equals("macban")) {
			if(loading) {
				channel.sendMessage("Bot is still loading! Check back later").queue();
				return;
			}
			if(!msg.contains(" ")) {
				channel.sendMessage("Usage: dhcp.macban <user> [users...]").queue();
				return;
			}
			if(!(guild.getMember(user).hasPermission(Permission.BAN_MEMBERS) && guild.getSelfMember().hasPermission(Permission.BAN_MEMBERS))) {
				channel.sendMessage("Missing permissions: Ban members").queue();
				return;
			}
			String output = "";
			String ids = msg.trim().toLowerCase().replaceFirst("dhcp.macban","").trim();
			String[] usersToBan = ids.split(" ");
			User bannedUser = null;
			String reason = getReason(usersToBan);
			boolean conflict = false;
			//boolean brk = false;
			for(String id : usersToBan) {
				//brk = false;
				try {
					try {/*
						for(char c : id.toCharArray()) {
							if(!NUMS.contains(Character.toString(c))) {
								if(reason.equals("No reason given")) {
									reason = "";
								}
								brk = true;
								reason += id + " ";
								break;
							}
						}
						if(brk) {
							continue;
						}*/
						try {
							Long.parseLong(id);
						}
						catch(Exception e) {
							continue;
						}
						if(id.contains(".")) {
							bannedUser = getUserByIP(id, guild);
						}
						else if(id.contains(":")) {
							ArrayList<Member> membersWithMAC = new ArrayList<>();
							for(Member member : guild.getMembers()) {
								if(generateMAC(member.getUser()).equals(id.toUpperCase())) {
									membersWithMAC.add(member);
								}
							}
							int size = membersWithMAC.size();
							if(size == 0) {
								output += "No members with MAC address **" + id + "**\n";
								continue;
							}
							if(size == 1) {
								bannedUser = membersWithMAC.get(0).getUser();
							}
							if(size > 1) {
								String out = "The following members have the same MAC for some reason:\n";
								for(Member member : membersWithMAC) {
									out += member.getUser().getName() + "#" + member.getUser().getDiscriminator() + " (" + member.getUser().getId() + ")\n";
								}
								out += "\nConflicting MAC: " + id;
								channel.sendMessage(out).queue();
								conflict = true;
								continue;
							}
						}
						else {
							bannedUser = event.getJDA().retrieveUserById(id).complete();
						}
						String banReasonToPMBCJDAIsWeird = reason;
						bannedUser.openPrivateChannel().queue((ch) -> ch.sendMessage("You were banned from " + guild.getName() + ": " + banReasonToPMBCJDAIsWeird).queue());
					}
					catch(Exception e) {}
					guild.getController().ban(bannedUser, 7, "[Banned by " + user.getName() + "#" + user.getDiscriminator() + "]: " + reason).queue();
					output += "Banned **" + bannedUser.getName() + "#" + bannedUser.getDiscriminator() + "**\n";
				}
				catch(Exception e) {
					if(bannedUser == null) {
						try {
							bannedUser = event.getJDA().retrieveUserById(id).complete();
						}
						catch(Exception exc) {
							
						}
					}
					output += "Failed to ban **" + (bannedUser == null ? "(" + id + ")" : bannedUser.getName() + "#" + bannedUser.getDiscriminator()) + "**: " + e.getMessage() + "\n";
				}
			}
			if(conflict) {
				output += "\nConflicting matches were not banned";
			}
			channel.sendMessage(output).queue();
			return;
		}
		if(cmd.equals("quench") || cmd.equals("sourcequench")) {
			if(loading) {
				channel.sendMessage("Bot is still loading! Check back later").queue();
				return;
			}
			if(!msg.contains(" ")) {
				channel.sendMessage("Usage: dhcp.quench <user> [users...]").queue();
				return;
			}
			if(!(guild.getMember(user).hasPermission(Permission.MANAGE_ROLES) && guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES))) {
				channel.sendMessage("Missing permissions: Manage roles").queue();
				return;
			}
			String output = "";
			String ids = msg.trim().toLowerCase().replaceFirst(PREFIX+cmd,"").trim();
			String[] usersToMute = ids.split(" ");
			User mutedUser = null;
			//String reason = "No reason given";
			for(String id : usersToMute) {
				try {
					try {
						if(id.contains(".")) {
							mutedUser = getUserByIP(id, guild);
						}
						else {
							mutedUser = guild.getMemberById(id).getUser();
						}
					}
					catch(Exception e) {}
					guild.getController().addSingleRoleToMember(guild.getMember(mutedUser), getMutedRole(guild)).queue();
					output += "Muted **" + mutedUser.getName() + "#" + mutedUser.getDiscriminator() + "**\n";
				}
				catch(Exception e) {
					output += "Failed to mute **" + mutedUser.getName() + "#" + mutedUser.getDiscriminator() + "**: " + e.getMessage() + "\n";
					//e.printStackTrace();
				}
			}
			channel.sendMessage(output).queue();
			return;
		}
		if(cmd.equals("unquench") || cmd.equals("unsourcequench")) {
			if(loading) {
				channel.sendMessage("Bot is still loading! Check back later").queue();
				return;
			}
			if(!msg.contains(" ")) {
				channel.sendMessage("Usage: dhcp.unquench <user> [users...]").queue();
				return;
			}
			if(!(guild.getMember(user).hasPermission(Permission.MANAGE_ROLES) && guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES))) {
				channel.sendMessage("Missing permissions: Manage roles").queue();
				return;
			}
			String output = "";
			String ids = msg.trim().toLowerCase().replaceFirst(PREFIX+cmd,"").trim();
			String[] usersToUnmute = ids.split(" ");
			User mutedUser = null;
			//String reason = "No reason given";
			for(String id : usersToUnmute) {
				try {
					try {
						if(id.contains(".")) {
							mutedUser = getUserByIP(id, guild);
						}
						else {
							mutedUser = guild.getMemberById(id).getUser();
						}
					}
					catch(Exception e) {}
					guild.getController().removeSingleRoleFromMember(guild.getMember(mutedUser), getMutedRole(guild)).queue();
					output += "Unmuted **" + mutedUser.getName() + "#" + mutedUser.getDiscriminator() + "**\n";
				}
				catch(Exception e) {
					output += "Failed to unmute **" + mutedUser.getName() + "#" + mutedUser.getDiscriminator() + "**: " + e.getMessage() + "\n";
					//e.printStackTrace();
				}
			}
			channel.sendMessage(output).queue();
			return;
		}
		if(cmd.equals("eval")) {
			String userId = user.getId();
			if(OWNERS.contains(userId)) {
				channel.sendMessage("Evaluating on behalf of " + user.getAsMention()).queue();
				eval(msg, event);
			}
			else {
				channel.sendMessage("no u").queue();
			}
		}
		if(cmd.equals("renew")) {
			if(loading) {
				channel.sendMessage("Bot is still loading! Check back later").queue();
				return;
			}
			try {
				if(!msg.contains(" ")) {
					renewIP(user, guild);
					channel.sendMessage("Your new IP is " + getIPOfUser(guild.getMember(user))).queue();
					return;
				}
				String ip = msg.split(" ")[1];
				if(guild.getMember(user).hasPermission(Permission.MANAGE_SERVER) || OWNERS.contains(user.getId())) {
					User userToRenew = null;
					if(OWNERS.contains(user.getId())) {
						channel.sendMessage("Owner overrides activated").queue();
					}
					if(ip.contains(".")) {
						userToRenew = getUserByIP(ip, guild);
					}
					else {
						userToRenew = guild.getMemberById(ip.replace("<@", "").replace(">", "")).getUser();
					}
					try {
						renewIP(userToRenew, guild);
					}
					catch(Exception e) {
						channel.sendMessage("Error: " + e.getMessage()).queue();
						return;
					}
					channel.sendMessage("Renewed IP of user " + userToRenew.getName() + "#" + userToRenew.getDiscriminator() + ".\n" +
					"Their new IP is " + getIPOfUser(guild.getMember(user))).queue();
				}
				else {
					channel.sendMessage("You are missing permissions: Manage Server").queue();
					return;
				}
			}
			catch(Exception e) {
				channel.sendMessage("An error occured while processing your request:\n" + e);
			}
		}
		if(cmd.equals("getmac")) {
			if(!msg.contains(" ")) {
				channel.sendMessage("Usage: dhcp.getmac [user]").queue();
				return;
			}
			String id = msg.split(" ")[1];
			id = id.replace("<@", "").replace(">","");
			try {
				User userGiven = null;
				try {
					if(id.contains(".")) {
						if(!ipRange.startsWith(id.substring(0,  2))) {
							throw new IllegalArgumentException("IP must be on the same IP range as the guild");
						}
						userGiven = getUserByIP(id, guild);
						id = userGiven.getId();
					}
					else {
						userGiven = event.getJDA().retrieveUserById(id).complete();
					}
				}
				catch(Exception e) {}
				String name = "Unknown user";
				String discrim = "";
				if(userGiven != null) {
					name = userGiven.getName();
					discrim = "#" + userGiven.getDiscriminator();
				}
				channel.sendMessage("MAC address for " + name + discrim + " (" + id + "): `" + generateMAC(id) + "`").queue();
			}
			catch(IllegalArgumentException e) {
				channel.sendMessage("Invalid User ID").queue();
			}
		}
		if(cmd.equals("conn")) {
			if(!msg.contains(" ")) {
				channel.sendMessage("Usage: dhcp.conn <ip> <port>").queue();
				return;
			}
			String ip = null;
			int port = -1;
			try {
				ip = msg.split(" ")[1];
				port = Integer.parseInt(msg.split(" ")[2]);
			}
			catch(Exception e) {
				channel.sendMessage("Usage: dhcp.conn <ip> <port>").queue();
				return;
			}
			try {
				TCPConnection conn = getConnection(ip, port, guild);
				if(conn == null) {
					throw new PortConnectException("Unable to connect to " + ip + ":" + port + ", connection refused.");
				}
				Service service = conn.getService();
				if(service == null) {
					throw new UnknownServiceException("No service is provided by that host");
				}
				if(service instanceof Site) {
					EmbedBuilder eb = ((Site)service).getEmbed();
					eb.setFooter("Requested by " + getIPOfUser(guild.getMember(user)) + " (" + generateMAC(user.getId()) + ")", user.getAvatarUrl());
					channel.sendMessage(eb.build()).queue();
				}
				else {
					throw new UnknownServiceException("Unknown service: " + service);
				}
			}
			catch(Exception e) {
				channel.sendMessage("There was an error processing your request: " + e).queue();
			}
		}
		if(cmd.equals("chars")) {
			if(!msg.contains(" ")) {
				channel.sendMessage("Usage: dhcp.chars <string>").queue();
				return;
			}
			String emote = msg.substring((PREFIX+cmd).length());
			channel.sendMessage("`\\u" + stringToHex(emote).toUpperCase().replace(" ", "\\u") + "`").queue();
			return;
		}
		if(cmd.equals("service")) {
			if(!msg.contains(" ")) {
				channel.sendMessage("Usage: dhcp.service <start|stop|create|delete|status> <name> [<port>]").queue();
				return;
			}
			String[] request = msg.split(" ");
			String intent = request[1];
			try {
				String serviceName = null;
				try {
					serviceName = request[2];
				}
				catch(ArrayIndexOutOfBoundsException e) {
					throw new IllegalArgumentException("No service provided!");
				}
				if(intent.equals("create")) {
					TCPConnection conn = null;
					try {
						String html = "";
						for(int i = 4; i < request.length; i++) {
							html += request[i] + " ";
						}
						System.out.println(html);
						conn = new TCPConnection(getIPOfUser(guild.getMember(user)), Integer.parseInt(request[3]), guild, new Site(HTMLParser.parseHTML(html)));
					}
					catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
						e.printStackTrace();
						throw new IllegalArgumentException("`Usage: dhcp.service create <name> <port>");
					}
					openPort(conn);
					channel.sendMessage("Created service " + serviceName + " listening on " + getIPOfUser(guild.getMember(user)) + ":" + request[3]).queue();
					return;
				}
			}
			catch(Exception e) {
				channel.sendMessage("Well, that happened: " + e).queue();
				return;
			}
		}
		if(cmd.equals("flush")) {
			if(OWNERS.contains(user.getId())) {
				channel.sendMessage("Attempting to flush IP registry for guild " + guild.getName() + " (" + guild.getId() + ")...").queue();
				try {
					conn.flush(guild);
					channel.sendMessage("Done! Cleared cache for " + getGuildStats(guild)[2] + " users.").queue();
				}
				catch(Exception e) {
					channel.sendMessage("Failed to flush this guild!\n" + e).queue();
				}
			}
			else {
				channel.sendMessage("\\*Toilet flushing noises\\*").queue();
			}
			return;
		}
		if(cmd.equals("fakerejoin")) {
			if(OWNERS.contains(user.getId())) {
				int[] stats = getGuildStats(guild);
				String users = "";
				channel.sendMessage("Alright, flushing tables and reassigning IPs to all users in this guild. Processing " + stats[2] + " users and ignoring " + stats[1] + " bots...").queue();
				try {
					conn.flush(guild);
					for(Member member : guild.getMembers()) {
						if(member.getUser().isBot()) {
							continue;
						}
						users += member.getUser().getId() + ",";
					}
					users = users.substring(0, users.length() - 1);
					conn.makeRequest("ASSIGNBULK IP " + guild.getId() + " " + users);
					channel.sendMessage("Finished!").queue();
				}
				catch(Exception e) {
					channel.sendMessage(e.toString()).queue();
				}
			}
			else {
				channel.sendMessage(":eyes: no u").queue();
			}
			return;
		}
		if(cmd.equals("reconnect")) {
			if(OWNERS.contains(user.getId())) {
				try {
					conn = conn.reconnect();
					channel.sendMessage("Session resumed successfully!").queue();
				}
				catch(Exception e) {
					channel.sendMessage("Failed to reconnect to server!\n" + e).queue();
				}
			}
		}
		if(cmd.equals("pingry")) {
			channel.sendMessage("<@190544080164487168>").queue();
			return;
		}
		if(cmd.equals("despacito") && guild.getId().equals("110373943822540800")) {
			try {
				channel.sendMessage("des").complete();
				channel.sendMessage("pa").completeAfter(750L, TimeUnit.MILLISECONDS);
				channel.sendMessage("cito").completeAfter(750L, TimeUnit.MILLISECONDS);
			}
			catch(Exception e) {}
			return;
		}
		if(cmd.equals("rawrequest")) {
			if(OWNERS.contains(user.getId())) {
				try {
					channel.sendMessage(conn.makeRequest(msg.split(" ", 2)[1])).queue();
				}
				catch(Exception e) {
					channel.sendMessage(e.toString()).queue();
				}
			}
			else {
				channel.sendMessage("no u").queue();
			}
			return;
		}
	}
}
