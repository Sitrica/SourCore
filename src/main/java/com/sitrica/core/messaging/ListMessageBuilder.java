package com.sitrica.core.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;
import com.sitrica.core.SourPlugin;
import com.sitrica.core.placeholders.Placeholder;
import com.sitrica.core.placeholders.Placeholders;
import com.sitrica.core.placeholders.SimplePlaceholder;

public class ListMessageBuilder {

	private Map<Placeholder<?>, Object> placeholders = new HashMap<>();
	private final List<CommandSender> senders = new ArrayList<>();
	private ConfigurationSection configuration;
	private Object defaultPlaceholderObject;
	private final SourPlugin instance;
	private boolean prefix;
	private String node;

	/**
	 * Creates a ListMessageBuilder with the defined node.
	 * 
	 * @param node The configuration nodes from the messages.yml
	 */
	public ListMessageBuilder(SourPlugin instance, String node) {
		this(instance, true, node);
	}

	/**
	 * Creates a ListMessageBuilder with the defined ConfigurationSection.
	 * 
	 * @param node The configuration nodes from the ConfigurationSection
	 * @param section The ConfigurationSection to read from.
	 */
	public ListMessageBuilder(SourPlugin instance, boolean prefix, String node, ConfigurationSection section) {
		this(instance, prefix, node);
		this.configuration = section;
	}

	/**
	 * Creates a ListMessageBuilder with the defined nodes, and if it should contain the prefix.
	 * 
	 * @param prefix The boolean to enable or disable prefixing this message.
	 * @param nodes The configuration nodes from the messages.yml
	 */
	public ListMessageBuilder(SourPlugin instance, boolean prefix, String node) {
		this.instance = instance;
		this.prefix = prefix;
		this.node = node;
	}

	/**
	 * Set the senders to send this message to.
	 *
	 * @param senders The CommandSenders to send the message to.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder toSenders(CommandSender... senders) {
		this.senders.addAll(Sets.newHashSet(senders));
		return this;
	}

	/**
	 * Set the players to send this message to.
	 *
	 * @param senders The Players... to send the message to.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder toPlayers(Player... players) {
		this.senders.addAll(Sets.newHashSet(players));
		return this;
	}

	/**
	 * Set the players to send this message to.
	 *
	 * @param senders The Collection<Player> to send the message to.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder toPlayers(Collection<? extends Player> players) {
		this.senders.addAll(players);
		return this;
	}

	/**
	 * Add a placeholder to the MessageBuilder.
	 * 
	 * @param placeholderObject The object to be determined in the placeholder.
	 * @param placeholder The actual instance of the Placeholder.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder withPlaceholder(Object placeholderObject, Placeholder<?> placeholder) {
		placeholders.put(placeholder, placeholderObject);
		return this;
	}

	/**
	 * Set the configuration to read from, by default is the messages.yml
	 * 
	 * @param configuration The FileConfiguration to read from.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder fromConfiguration(ConfigurationSection section) {
		this.configuration = section;
		return this;
	}

	/**
	 * Created a single replacement and ignores the placeholder object.
	 * 
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param replacement The replacement e.g: the command.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder replace(String syntax, Object replacement) {
		placeholders.put(new SimplePlaceholder(syntax) {
			@Override
			public String get() {
				return replacement.toString();
			}
		}, replacement.toString());
		return this;
	}

	/**
	 * Created a single replacement and ignores the placeholder object.
	 * 
	 * @param priority The priority of the placeholder replacement.
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param replacement The replacement e.g: the command.
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder replace(int priority, String syntax, Object replacement) {
		placeholders.put(new SimplePlaceholder(priority, syntax) {
			@Override
			public String get() {
				return replacement.toString();
			}
		}, replacement.toString());
		return this;
	}

	/**
	 * Set the placeholder object, good if you want to allow multiple placeholders.
	 * 
	 * @param object The object to set
	 * @return The ListMessageBuilder for chaining.
	 */
	public ListMessageBuilder setPlaceholderObject(Object object) {
		this.defaultPlaceholderObject = object;
		return this;
	}

	/**
	 * Sends the final product of the builder.
	 */
	public void send(CommandSender... senders) {
		toSenders(senders).send();
	}

	/**
	 * Completes and returns the final product of the builder.
	 */
	public List<String> get() {
		List<String> list = new ArrayList<>();
		if (configuration == null)
			configuration = instance.getConfiguration("messages").orElse(instance.getConfig());
		boolean usedPrefix = false;
		for (String string : configuration.getStringList(node)) {
			if (prefix && !usedPrefix) {
				string = instance.getPrefix() + " " + Formatting.color(string);
				usedPrefix = true;
			} else {
				string = Formatting.color(string);
			}
			list.add(applyPlaceholders(string));
		}
		return list;
	}

	private String applyPlaceholders(String input) {
		// Registered Placeholders
		for (Entry<Placeholder<?>, Object> entry : placeholders.entrySet()) {
			Placeholder<?> placeholder = entry.getKey();
			for (String syntax : placeholder.getSyntaxes()) {
				if (!input.toLowerCase().contains(syntax.toLowerCase()))
					continue;
				if (placeholder instanceof SimplePlaceholder) {
					SimplePlaceholder simple = (SimplePlaceholder) placeholder;
					input = input.replaceAll(Pattern.quote(syntax), simple.get());
				} else {
					String replacement = placeholder.replace_i(entry.getValue());
					if (replacement != null)
						input = input.replaceAll(Pattern.quote(syntax), replacement);
				}
			}
		}
		// Default Placeholders
		for (Placeholder<?> placeholder : Placeholders.getPlaceholders()) {
			for (String syntax : placeholder.getSyntaxes()) {
				if (!input.toLowerCase().contains(syntax.toLowerCase()))
					continue;
				if (placeholder instanceof SimplePlaceholder) {
					SimplePlaceholder simple = (SimplePlaceholder) placeholder;
					input = input.replaceAll(Pattern.quote(syntax), simple.get());
				} else if (defaultPlaceholderObject != null && placeholder.getType().isAssignableFrom(defaultPlaceholderObject.getClass())) {
					String replacement = placeholder.replace_i(defaultPlaceholderObject);
					if (replacement != null)
						input = input.replaceAll(Pattern.quote(syntax), replacement);
				}
			}
		}
		// This allows users to insert new lines into their lores.
		int i = instance.getConfig().getInt("general.new-lines", 4); //The max about of new lines users are allowed.
		while (input.contains("%newline%") || input.contains("%nl%")) {
			input = input.replaceAll(Pattern.quote("%newline%"), "\n");
			input = input.replaceAll(Pattern.quote("%nl%"), "\n");
			i--;
			if (i <= 0)
				break;
		}
		return input;
	}

	/**
	 * Sends the final product of the builder if the senders are set.
	 */
	public void send() {
		List<String> list = get();
		if (senders.isEmpty())
			return;
		for (CommandSender sender : senders)
			list.forEach(message -> sender.sendMessage(message));
	}

}
