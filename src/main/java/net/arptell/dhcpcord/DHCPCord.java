package net.arptell.dhcpcord;

import net.dv8tion.jda.core.AccountType;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Scanner;

import javax.script.ScriptException;

public class DHCPCord extends ListenerAdapter{
	
	private static String token = "";
	private static JDA jda = null;
	private static HashMap<Guild, HashMap<User, String>> ips = new HashMap<>();
	private static HashMap<Guild, String> muteRoles = new HashMap<>();
	private static final String[] IP_RANGES = {"192.168.%d.%d", "10.0.%d.%d"};
	private static final String[] IP_RANGES_NOFORMAT = {"192.168.x.x", "10.0.x.x"};
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
	}

	public void run() throws Exception {
		Scanner sc;
		jda = new JDABuilder(AccountType.BOT).setToken(token).addEventListener(this).buildBlocking();
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
	
	public static void main(String[] args) throws Exception {
		new DHCPCord().run();
	}
	private String getIPOfUser(User user, HashMap<User, String> ipMap) {
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
	private void setUserIp(String ip, User user, Guild guild) throws IOException{
		File file = new File("dhcp/" + guild.getId() + "/" + user.getId());
		FileWriter fw = null;
		if(file.exists()) {
			file.delete();
		}
		file.createNewFile();
		fw = new FileWriter(file);
		fw.write(ip);
		fw.close();
	}
	private void removeUserIP(User user, Guild guild) {
		File file = new File("dhcp/" + guild.getId() + "/" + user.getId());
		if(file.exists()) {
			file.delete();
		}
		try {
			ips.get(guild).remove(user);
		}
		catch(Exception e){}
	}
	private Role getMutedRole(Guild guild) {
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
	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		File file = new File("dhcp/" + event.getGuild().getId());
		if(file.exists()) {
			file.delete();
		}
		ips.remove(event.getGuild());
	}
	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		removeUserIP(user, guild);
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
			System.out.println("Gave IP " + ip + " to user " + member.getUser().getName() + "#" + member.getUser().getDiscriminator());
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
							"dhcp.help                      | summons this help menu\n" +
							"dhcp.status                    | gets your IP address\n" +
							"dhcp.arp <query>               | submit ARP requests\n" +
							"dhcp.table                     | shows a list of IPs and their corresponding owners. Only works in small guilds\n" +
							"dhcp.release <user> [users...] | kicks the given users off the network. Requires Kick Member perms.\n" +
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
				String output = "Error: IP not registered";
				if(!(querySplit.length >= 4)){
					throw new IllegalArgumentException("Not enough arguments");
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
				}
				*/
				if(querySplit[0].matches("^(?:10\\.0|192\\.168)\\.\\d{0,3}\\.\\d{0,3}") && querySplit.length == 4){
					if(querySplit[1].equals("is") && querySplit[2].equals("at")){
						if(querySplit[3].matches("^(?:10\\.0|192\\.168)\\.\\d{0,3}\\.\\d{0,3}")){
							String curIp = querySplit[0];
							String newIp = querySplit[3];
							if(newIp.length() < 8 || !newIp.substring(0, 2).equals(ipRange.substring(0, 2))) {
								throw new IllegalArgumentException("IP must be on the same IP range as the guild");
							}
							if(!((getUserByIP(newIp, ipMap) == null) || getUserByIP(curIp, ipMap).equals(user))){
								throw new ScriptException("ARP attacks are not supported (yet)");
							}
							if(!getIPOfUser(user, ipMap).equals(newIp)){
								ips.get(guild).put(user, newIp);
								setUserIp(newIp, user, guild);
							}
							channel.sendMessage(user.getAsMention() + " " + curIp + " is at " + newIp).queue();
							return;
						} else{
							throw new IllegalArgumentException("IP must come after `at` keyword");
						}
					} else {
						throw new IllegalArgumentException("`is at <IP>` must come after IP");
					}
				}
			}
			catch(Exception e) {
				channel.sendMessage("Error while parsing ARP request: " + e + "\nYou probably typed something wrong, try again").queue();
			}
			return;
		}
		if(cmd.equals("release")) {
			//channel.sendMessage("Command not implemented (yet) - this is not a command not found message").queue();
			if(!msg.contains(" ")) {
				channel.sendMessage("Usage: dhcp.release <user> [users...]").queue();
			}
			if(!(guild.getMember(user).hasPermission(Permission.KICK_MEMBERS) && guild.getSelfMember().hasPermission(Permission.KICK_MEMBERS))) {
				channel.sendMessage("Missing permissions: Kick members").queue();
				return;
			}
			String output = "";
			String ids = msg.trim().toLowerCase().replaceFirst("dhcp.release","").trim();
			String[] usersToKick = ids.split(" ");
			User kickedUser = null;
			for(String id : usersToKick) {
				try {
					try {
						if(id.contains(".")) {
							kickedUser = getUserByIP(id, ipMap);
						}
						else {
							kickedUser = guild.getMemberById(id.replace("<@", "").replace(">","")).getUser();
						}
						kickedUser.openPrivateChannel().queue((ch) -> ch.sendMessage("You were kicked from " + guild.getName()).queue());
					}
					catch(Exception e) {}
					guild.getController().kick(guild.getMember(kickedUser)).queue();
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
			String reason = "No reason given";
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
						if(id.contains(".")) {
							bannedUser = getUserByIP(id, ipMap);
						}
						else {
							bannedUser = guild.getMemberById(id).getUser();
						}
						String banReasonToPMBCJDAIsWeird = reason;
						bannedUser.openPrivateChannel().queue((ch) -> ch.sendMessage("You were banned from " + guild.getName() + ": " + banReasonToPMBCJDAIsWeird).queue());
					}
					catch(Exception e) {}
					guild.getController().ban(guild.getMember(bannedUser), 7, "[Banned by " + user.getName() + "#" + user.getDiscriminator() + "]: " + reason).queue();
					output += "Banned **" + bannedUser.getName() + "#" + bannedUser.getDiscriminator() + "**\n";
				}
				catch(Exception e) {
					output += "Failed to ban **" + bannedUser.getName() + "#" + bannedUser.getDiscriminator() + "**: " + e.getMessage() + "\n";
				}
			}
			channel.sendMessage(output).queue();
			return;
		}
		if(cmd.equals("quench") || cmd.equals("sourcequench")) {
			if(!msg.contains(" ")) {
				channel.sendMessage("Usage: dhcp.quench <user> [users...]").queue();
				return;
			}
			if(!(guild.getMember(user).hasPermission(Permission.MANAGE_ROLES) && guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES))) {
				channel.sendMessage("Missing permissions: Manage roles").queue();
				return;
			}
			String output = "";
			String ids = msg.trim().toLowerCase().replaceFirst("dhcp.sourcequench","").replaceFirst("dhcp.quench", "").trim();
			String[] usersToMute = ids.split(" ");
			User mutedUser = null;
			String reason = "No reason given";
			for(String id : usersToMute) {
				try {
					try {
						if(id.contains(".")) {
							mutedUser = getUserByIP(id, ipMap);
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
	}
}
