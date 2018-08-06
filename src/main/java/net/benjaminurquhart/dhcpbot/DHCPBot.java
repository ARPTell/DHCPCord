package net.benjaminurquhart.dhcpbot;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
public class DHCPBot extends ListenerAdapter{
	
	private static String token = "";
	private static JDA jda = null;
	private static HashMap<Guild, HashMap<User, String>> ips = new HashMap<>();
	private static final String[] IP_RANGES = {"192.168.%d.%d", "10.0.%d.%d"};
	private static final String[] IP_RANGES_NOFORMAT = {"192.168.x.x", "10.0.x.x"};
	private static final String PREFIX = "dhcp.";
	
	public static void main(String[] args) throws Exception{
		Scanner sc = new Scanner(new File("token.txt"));
		try {
			token = sc.nextLine();
			sc.close();
		}
		catch(Exception e) {
			System.err.println("Could not read token: " + e);
			System.exit(666);
		}
		jda = new JDABuilder(AccountType.BOT).setToken(token).addEventListener(new DHCPBot()).buildBlocking();
		jda.getPresence().setGame(Game.watching("ARP poisoning attacks happen"));
		List<Guild> guilds = jda.getGuilds();
		List<Member> members = null;
		for(Guild guild : guilds) {
			members = guild.getMembers();
			ips.put(guild, new HashMap<User, String>());
			for(Member member : members) {
				if(member.getUser().isBot()) {
					continue;
				}
				try {
					File file = new File("dhcp/" + guild.getId() + "/" + member.getUser().getId());
					sc = new Scanner(file);
					ips.get(guild).put(member.getUser(), sc.nextLine());
					sc.close();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	private String getIPOFuser(User user, HashMap<User, String> ipMap) {
		return ipMap.get(user);
	}
	private User getUserByIP(String ip, HashMap<User, String> ipMap) {
		User user = null;
		for(Map.Entry<User, String> ipPair : ipMap.entrySet()) {
			if(ipPair.getValue().equals(ip)) {
				user = ipPair.getKey();
				break;
			}
		}
		return user;
	}
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		HashMap<User, String> ipMap = ips.get(guild);
		String ipRange = IP_RANGES[(int)(guild.getIdLong() % 2L)];
		String ip = "";
		String id = guild.getId();
		FileWriter fw = null;
		int x = 0;
		String userId = member.getUser().getId();
		File config = new File("dhcp/" + id + "/" + userId);
		try {
			while(!(getUserByIP(String.format(ipRange, (x / 255), (x % 254) + 1), ipMap) == null)) {
				x++;
			}
			config.createNewFile();
			fw = new FileWriter(config);
			ip = String.format(ipRange, (x / 255), (x % 254) + 1);
			fw.write(ip);
			fw.close();
			ips.get(guild).put(member.getUser(), ip);
		}
		catch(IOException e) {
		}
	}
	
	@Override
	public void onGuildJoin(GuildJoinEvent event){
		Guild guild = event.getGuild();
		String id = guild.getId();
		ips.put(guild, new HashMap<>());
		File config = new File("dhcp/" + id);
		if(!config.exists()) {
			config.mkdirs();
		}
		List<Member> users = guild.getMembers();
		int bots = 0;
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
		String userId = null;
		String ipRange = IP_RANGES[(int)(guild.getIdLong() % 2L)];
		String ip = "";
		FileWriter fw = null;
		int x = 0;
		for(Member member : users) {
			userId = member.getUser().getId();
			config = new File("dhcp/" + id + "/" + userId);
			try {
				config.createNewFile();
				fw = new FileWriter(config);
				ip = String.format(ipRange, (x / 255), (x % 254) + 1);
				fw.write(ip);
				fw.close();
				ips.get(guild).put(member.getUser(), ip);
				x++;
			}
			catch(IOException e) {
				channel.sendMessage("Warning: failed to assign IP to user " + member.getUser().getName() + "#" + member.getUser().getDiscriminator() + ": " + e).queue();
				continue;
			}
		}
		channel.sendMessage("IP assignment complete! Users may now view their IP with `dhcp.status`").queue();
	}
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().isBot()) {
			return;
		}
		String msg = event.getMessage().getContentRaw().trim();
		String cmd = (msg.contains(" ") ? msg.split(" ")[0].toLowerCase() : msg.toLowerCase());
		Guild guild = event.getGuild();
		HashMap<User, String> ipMap = ips.get(guild);
		MessageChannel channel = event.getChannel();
		User user = event.getAuthor();
		String ipRange = IP_RANGES_NOFORMAT[(int)(guild.getIdLong() % 2L)];
		if(!cmd.startsWith(PREFIX)) {
			return;
		}
		cmd = cmd.replaceFirst(PREFIX, "").trim();
		if(cmd.equals("help")) {
			channel.sendMessage(
							"```Prefix: " + PREFIX + " | This guild is on the " + ipRange + " IP range "+ "\n\n" + 
							"dhcp.help           | summons this help menu\n" +
							"dhcp.status         | gets your IP address\n" +
							"dhcp.arp <query>    | submit ARP requests\n" +
							"dhcp.table          | shows a list of IPs and their corresponding owners. Only works in small guilds\n" +
							"dhcp.release [user] | assigns the given user a different IP (if one is available). Manage Server permission required for others.\n" +
							"" +
							"```").queue();
			return;
		}
		if(cmd.equals("table")) {
			HashMap<User, String> guildIps = ips.get(guild);
			String table = "";
			for(Map.Entry<User, String> entry : guildIps.entrySet()) {
				table += entry.getKey().getName() + "#" + entry.getKey().getDiscriminator() + " - " + entry.getValue() + "\n";
			}
			channel.sendMessage("```" + table + "```").queue();
		}
		if(cmd.equals("status")) {
			String ip = ipMap.get(user);
			channel.sendMessage(
					"```This guild is on the " + ipRange + " IP range\n\n" +
					"Your IP address is " + ip + "```").queue();
			return;
		}
		if(cmd.equals("arp")) {
			String query = msg.trim().toLowerCase().replaceFirst("dhcp.arp","").trim();
			if(query.equals("")) {
				channel.sendMessage("Usage: dhcp.arp <query>").queue();
				return;
			}
			try {
				String[] querySplit = query.split(" ");
				if(!(querySplit.length >= 4)){
					throw new IllegalArgumentException("Not enough arguments");
				}
				if(querySplit[0].equals("who") && querySplit.length == 5) {
					if(querySplit[1].equals("has")) {
						String tmpIp = querySplit[2].replace("?", "");
						String toTell = "";
						String toTellUser = "";
						String output = "Error: IP not registered";
						if(tmpIp.length() < 8 || !tmpIp.substring(0, 2).equals(ipRange.substring(0, 2))) {
							throw new IllegalArgumentException("IP must be on the same IP range as the guild");
						}
						try {
							if(querySplit[3].equals("tell")) {
								try {
									toTell = querySplit[4];
									toTellUser = getUserByIP(toTell, ipMap).getAsMention();
								}
								catch(NullPointerException e) {
									throw new IllegalArgumentException("Unregistered IP: " + toTell);
								}
								catch(IllegalArgumentException e) {
									throw new IllegalArgumentException("an IP must come after `tell` keyword");
								}
							}
						}
						catch(ArrayIndexOutOfBoundsException e) {}
						User ipUser = getUserByIP(tmpIp, ipMap);
						try {
							output = (ipUser.getName() + "#" + ipUser.getDiscriminator() + " has " + tmpIp);
						}
						catch(NullPointerException e) {}
						channel.sendMessage(toTellUser + " " + output).queue();
					}
				}
				else if(querySplit[0].matches("^(10|192)") && querySplit.length == 4){
					if(querySplit[1].equals("is") && querySplit[2].equals("at")){
						if(querySplit[3].matches("^(10|192)")){
							String curIp = querySplit[0];
							String newIp = querySplit[3];
							if(getUserByIP(newIp, ips) || getUserByIP(curIp, ips) != user){
								channel.sendMessage("Arp poisoning attacks are not yet implemented").queue();
								return;
							}
							if(getIPOFuser(user, ips).equals(newIp)){
								channel.sendMessage(user + " is at " + newIp).queue();
								return;
							}
							ips.put(user,newIp);
							channel.sendMessage(user + " is at " + newIp).queue();
							return;
						} else{
							throw new IllegalArgumentException("an IP must come after `at` keyword");
						}
					}	
				}
			}
			catch(Exception e) {
				channel.sendMessage("Error while parsing ARP request: " + e + "\nYou probably typed something wrong, try again").queue();
			}
			return;
		}
		if(cmd.equals("release")) {
			channel.sendMessage("Command not implemented (yet) - this is not a command not found message").queue();
			return;
		}
	}
}