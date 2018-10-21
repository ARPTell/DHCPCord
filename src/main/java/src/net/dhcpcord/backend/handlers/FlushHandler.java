package net.dhcpcord.backend.handlers;

import net.dhcpcord.backend.DHCPBackend;
import net.dhcpcord.backend.errors.Errors;

public class FlushHandler implements Handler {

	@Override
	public String handle(String[] args) throws Exception{
		String entity = args[1];
		String guild = args[2];
		switch(entity) {
		case "IP": DHCPBackend.flush(guild); return "SUCCESS";
		case "SERVICE": throw new UnsupportedOperationException(Errors.ERR_IMPLEMENT + " Unimplemented");
		default: throw new UnsupportedOperationException(Errors.ERR_IMPLEMENT + " 'FLUSH' operator not defined for entity " + entity);
		}
	}

}
