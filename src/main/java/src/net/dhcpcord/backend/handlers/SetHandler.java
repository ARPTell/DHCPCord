package net.dhcpcord.backend.handlers;

import net.dhcpcord.backend.DHCPBackend;
import net.dhcpcord.backend.errors.Errors;

public class SetHandler implements Handler{

	@Override
	public String handle(String[] args) throws Exception{
		String entity = args[1];
		String guild = args[2];
		String user = args[3];
		switch(entity) {
		case "IP": DHCPBackend.setIp(guild, user, args[4], true); return "SUCCESS";
		case "MASK": throw new UnsupportedOperationException(Errors.ERR_IMPLEMENT + " Unimplemented");
		case "SERVICE": throw new UnsupportedOperationException(Errors.ERR_IMPLEMENT + " Unimplemented");
		default: throw new UnsupportedOperationException(Errors.ERR_IMPLEMENT + " 'SET' operator not defined for entity " + entity);
		}
	}

	
}
