package net.dhcpcord.backend.util;

public class EntityChecker {

	public static boolean isValid(String entity) {
		for(Entitiy e : Entitiy.values()) {
			if(e.toString().equals(entity)) {
				return true;
			}
		}
		return false;
	}
}
