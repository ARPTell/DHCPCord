package net.arptell.dhcpcord.exceptions;

public class DHCPException extends Exception{

	private String type = null;
	public DHCPException(String s, String type) {
		super(s);
		this.type = type;
	}
	public DHCPException(String s) {
		super(s);
		this.type = "???";
	}
	public String toString() {
		return super.toString() + " (Exception type: " + type + ")";
	}
}
