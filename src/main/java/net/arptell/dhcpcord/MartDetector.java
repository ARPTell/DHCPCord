package net.arptell.dhcpcord;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MartDetector extends ListenerAdapter {

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		User user = event.getUser();
		Guild guild = event.getGuild();
		if(user.getId().equals("161866631004422144") && guild.getId().equals("110373943822540800")) {
			guild.getTextChannelById("110373943822540800").sendMessage(":tada: OH HEY LOOK MART'S BACK :tada:").queue();
		}
	}
}
