package net.dhcpcord.backend.handlers;

import net.dhcpcord.backend.DHCPBackend;
import net.dhcpcord.backend.errors.Errors;

public class ReleaseHandler implements Handler{

	@Override
	public String handle(String[] args) throws Exception{
		String entity = args[1];
		String guild = args[2];
		String user = args[3];
		switch(entity) {
		case "IP": DHCPBackend.release(guild, user); return "SUCCESS";
		default: throw new UnsupportedOperationException(Errors.ERR_IMPLEMENT + " Not Implemented");
		}
	}
}
