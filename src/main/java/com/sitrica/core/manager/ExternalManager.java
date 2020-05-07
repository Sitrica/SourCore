package com.sitrica.core.manager;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import com.sitrica.core.SourPlugin;

public abstract class ExternalManager implements Listener {

	protected final SourPlugin instance;
	protected final String name;

	protected ExternalManager(SourPlugin instance, String name, boolean listener) {
		this.instance = instance;
		this.name = name;
		if (listener)
			Bukkit.getPluginManager().registerEvents(this, instance);
	}

	public String getName() {
		return name;
	}

	public abstract boolean isEnabled();

}
