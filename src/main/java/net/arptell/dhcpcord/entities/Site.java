package net.arptell.dhcpcord.entities;

//import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.EmbedBuilder;

public class Site implements Service{
	
	private EmbedBuilder site = null;
	private String name = "Untitled";
	
	public Site(EmbedBuilder site) {
		this.site = site;
	}
	public Site(EmbedBuilder site, String name) {
		this.site = site;
		this.name = name;
	}
	public EmbedBuilder getEmbed() {
		return this.site;
	}
	public Object getContent() {
		return getEmbed();
	}
	public String getName() {
		return this.name;
	}
}
