package net.arptell.dhcpcord.util;

import java.util.Random;
import net.arptell.dhcpcord.DHCPCord;

public class OutgoingTCPConnection extends TCPConnection {
	
	//To be used at a later point in time. Incomplete.
	
	public OutgoingTCPConnection(String ip, int port) {
		super(ip, Math.abs(new Random().nextInt(65536 - 49152) + 49152), null);
	}
}
