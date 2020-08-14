package com.sitrica.core.messaging;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.sitrica.core.SourPlugin;

public class Formatting {

	public static String messagesPrefixed(SourPlugin instance, ConfigurationSection section, String... nodes) {
		FileConfiguration messages = instance.getConfiguration("messages").orElse(instance.getConfig());
		String complete = messages.getString("messages.prefix", instance.getPrefix());
		return Formatting.color(complete + messages(section, Arrays.copyOfRange(nodes, 0, nodes.length)));
	}

	public static String messages(ConfigurationSection section, String... nodes) {
		String complete = "";
		List<String> list = Arrays.asList(nodes);
		Collections.reverse(list);
		int i = 0;
		for (String node : list) {
			if (i == 0)
				complete = section.getString(node, "Error " + section.getCurrentPath() + "." + node) + complete;
			else
				complete = section.getString(node, "Error " + section.getCurrentPath() + "." + node) + " " + complete;
			i++;
		}
		return Formatting.color(complete);
	}

	public static String color(String input) {
		if (input == null) return "";
		return ChatColor.translateAlternateColorCodes('&', input);
	}

	public static String colorAndStrip(String input) {
		if (input == null) return "";
		return stripColor(color(input));
	}

	public static String stripColor(String input) {
		if (input == null) return "";
		return ChatColor.stripColor(input);
	}

}
