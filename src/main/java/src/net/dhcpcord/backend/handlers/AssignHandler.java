package net.dhcpcord.backend.handlers;

import net.dhcpcord.backend.DHCPBackend;
import net.dhcpcord.backend.errors.Errors;

public class AssignHandler implements Handler{

	@Override
	public String handle(String[] args) throws Exception{
		String entity = args[1];
		String guild = args[2];
		String arg = args[3];
		switch(entity) {
		case "IP": return args[0].equals("ASSIGNBULK") ? DHCPBackend.assignIPBulk(guild, arg) : DHCPBackend.assignIp(guild, arg);
		default: throw new UnsupportedOperationException(Errors.ERR_IMPLEMENT + " 'ASSIGN' operator not defined for entity " + entity);
		}
	}
}
