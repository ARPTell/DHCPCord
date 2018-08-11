package net.arptell.dhcpcord.entities;

//import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.EmbedBuilder;

public class Site implements Service{
	
	private EmbedBuilder site = null;
	
	public Site(EmbedBuilder site) {
		this.site = site;
	}
	public EmbedBuilder getEmbed() {
		return this.site;
	}
	public Object getContent() {
		return getEmbed();
	}
}
