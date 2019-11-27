package com.sitrica.core.manager.external;

import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.sitrica.core.SourPlugin;
import com.sitrica.core.manager.ExternalManager;

public class CitizensManager extends ExternalManager {

	private Plugin citizens;

	public CitizensManager(SourPlugin instance) {
		super(instance, "citizens", false);
		citizens = CitizensAPI.getPlugin();
		if (citizens != null)
			instance.consoleMessage("Hooked into Citizens!");
	}

	public boolean isCitizen(Entity entity) {
		if (entity == null)
			return false;
		if (citizens == null)
			return false;
		if (entity.hasMetadata("NPC"))
			return true;
		if (CitizensAPI.getNPCRegistry().isNPC(entity))
			return true;
		return false;
	}

	@Override
	public boolean isEnabled() {
		return citizens != null;
	}

}
