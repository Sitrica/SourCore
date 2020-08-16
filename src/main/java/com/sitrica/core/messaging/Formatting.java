package com.sitrica.core.messaging;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public static String colorHex(String input) {
		if (input == null) return "";

		Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
		Matcher matcher = hexPattern.matcher(input);
		StringBuffer buffer = new StringBuffer(input.length() + 32);

		while(matcher.find()) {
			String group = matcher.group(1);
			matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR
					+ "x" + ChatColor.COLOR_CHAR
					+ group.charAt(0)
					+ ChatColor.COLOR_CHAR
					+ group.charAt(1)
					+ ChatColor.COLOR_CHAR
					+ group.charAt(2)
					+ ChatColor.COLOR_CHAR
					+ group.charAt(3)
					+ ChatColor.COLOR_CHAR
					+ group.charAt(4)
					+ ChatColor.COLOR_CHAR
					+ group.charAt(5));
		}

		return matcher.appendTail(buffer).toString();
	}

	public static String color(String input) {
		if (input == null) return "";
		return colorHex(ChatColor.translateAlternateColorCodes('&', input));
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
