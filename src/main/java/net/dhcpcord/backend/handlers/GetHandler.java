package net.dhcpcord.backend.handlers;

import net.dhcpcord.backend.DHCPBackend;
import net.dhcpcord.backend.errors.Errors;

public class GetHandler implements Handler{
	
	@Override
	public String handle(String[] args) throws Exception{
		String entity = args[1];
		String guild = args[2];
		String user = args[3];
		switch(entity) {
		case "IP": return DHCPBackend.getIp(guild, user);
		case "USER": return DHCPBackend.getUser(guild, user);
		case "MASK": throw new UnsupportedOperationException(Errors.ERR_IMPLEMENT + " Unimplemented");
		case "SERVICE": throw new UnsupportedOperationException(Errors.ERR_IMPLEMENT + " Unimplemented");
		default: throw new UnsupportedOperationException(Errors.ERR_IMPLEMENT + " 'GET' operator not defined for entity " + entity);
		}
	}
}
