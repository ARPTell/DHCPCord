package net.arptell.dhcpcord.handlers;

import net.arptell.dhcpcord.DHCPCord;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public class BulkAssignHandler extends Thread {

	private Guild guild;
	private DHCPCord self;
	private TextChannel channel;
	
	public BulkAssignHandler(DHCPCord self, TextChannel channel) {
		this.self = self;
		this.guild = channel.getGuild();
		this.channel = channel;
	}
	@Override
	public void run() {
		DHCPConnectionHandler conn = self.getConnHandler();
		String users = "";
		for(Member member : guild.getMembers()) {
			if(member.getUser().isBot()) {
				continue;
			}
			users += member.getUser().getId() + ",";
		}
		users = users.substring(0, users.length() - 1);
		try {
			conn.makeRequest("ASSIGNBULK IP " + guild.getId() + " " + users);
			channel.sendMessage("IP assignment complete! Users may now view their IP with `dhcp.status`").queue();
		}
		catch(Exception e) {
			channel.sendMessage("Failed to assign IPs: " + e).queue();
		}
	}
}
