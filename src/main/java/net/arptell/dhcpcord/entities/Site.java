package net.arptell.dhcpcord.entities;

import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.entities.EntityBuilder;

public class Site implements Service{
	
	private EmbedBuilder site = null;
	private String name = "Untitled";
	private JSONObject json = null;
	
	public Site(JSONObject data, EntityBuilder builder, String name) {
		this.site = new EmbedBuilder(builder.createMessageEmbed(DataObject.fromJson(data.toString())));
		this.json = data;
		this.name = name == null ? (json.has("title") ? json.getString("title") : "Untitled " + data.hashCode()) : "" + data.hashCode();
	}
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
	public JSONObject getJSON() {
		return json;
	}
	public String getName() {
		return this.name;
	}
}
