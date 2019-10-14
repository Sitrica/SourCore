package com.sitrica.core.messaging;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.entity.Player;

import com.sitrica.core.utils.Utils;

public class Actionbar {

	// Caching
	private final static boolean classes, method;

	static {
		classes = Utils.classExists("net.md_5.bungee.api.ChatMessageType") && Utils.classExists("net.md_5.bungee.api.chat.TextComponent");
		if (!classes)
			method = false;
		else
			method = Utils.methodExists(Player.Spigot.class, "sendMessage", ChatMessageType.class, TextComponent.class);
	}

	public static void sendActionBar(Player player, String... messages) {
		if (classes && method) {
			for (String message : messages) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Formatting.color(message)));
			}
		}
	}

}
