package com.sitrica.core;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.sitrica.core.messaging.Formatting;

public abstract class SourPlugin extends JavaPlugin {

	private final String[] managerPackages;
	private final String prefix;

	/**
	 * @param prefix Define the default prefix of the plugin.
	 */
	public SourPlugin(String prefix, String... managerPackages) {
		this.managerPackages = managerPackages;
		this.prefix = prefix;
	}

	/**
	 * Grabs the configuration defined by the String from the plugin.
	 * 
	 * @param name The file name without it's file extension.
	 * @return FileConfiguration if the plugin has registered such configuration.
	 */
	public abstract Optional<FileConfiguration> getConfiguration(String name);

	public void consoleMessage(String string) {
		Bukkit.getConsoleSender().sendMessage(Formatting.color(prefix + string));
	}

	public void debugMessage(String string) {
		if (getConfig().getBoolean("debug"))
			consoleMessage("&b" + string);
	}

	/**
	 * @return The package names where managers exist to be registered.
	 */
	public String[] getManagerPackages() {
		return managerPackages;
	}

	/** 
	 * @return The default string prefix of the plugin.
	 */
	public String getPrefix() {
		return prefix;
	}

}
