package com.sitrica.core.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;
import com.sitrica.core.SourPlugin;
import com.sitrica.core.objects.StringList;
import com.sitrica.core.placeholders.Placeholder;
import com.sitrica.core.placeholders.Placeholders;
import com.sitrica.core.placeholders.SimplePlaceholder;

public class MessageBuilder {

	private final Map<Placeholder<?>, Object> placeholders = new TreeMap<>(Comparator.comparing(Placeholder::getPriority));
	private final List<CommandSender> senders = new ArrayList<>();
	private final SourPlugin instance;
	private Object defaultPlaceholderObject;
	private ConfigurationSection section;
	private ClickEvent clickEvent;
	private HoverEvent hoverEvent;
	private TextComponent complete;
	private String[] nodes;
	private final boolean prefix;

	/**
	 * Creates a MessageBuilder with the defined nodes..
	 *
	 * @param nodes The configuration nodes from the messages.yml
	 */
	public MessageBuilder(SourPlugin instance, String... nodes) {
		this.instance = instance;
		this.prefix = true;
		this.nodes = nodes;
	}

	/**
	 * Creates a MessageBuilder from the defined ConfigurationSection.
	 *
	 * @param node The configuration nodes from the ConfigurationSection.
	 * @param section The ConfigurationSection to read from.
	 */
	public MessageBuilder(SourPlugin instance, boolean prefix, String node, ConfigurationSection section) {
		this(instance, prefix, node);
		this.section = section;
	}

	/**
	 * Creates a MessageBuilder with the defined nodes, and if it should contain the prefix.
	 *
	 * @param prefix The boolean to enable or disable prefixing this message.
	 * @param nodes The configuration nodes from the messages.yml
	 */
	public MessageBuilder(SourPlugin instance, boolean prefix, String... nodes) {
		this.instance = instance;
		this.prefix = prefix;
		this.nodes = nodes;
	}

	/**
	 * Set the senders to send this message to.
	 *
	 * @param senders The CommandSenders to send the message to.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder toSenders(Collection<CommandSender> senders) {
		this.senders.addAll(senders);
		return this;
	}

	/**
	 * Set the senders to send this message to.
	 *
	 * @param senders The CommandSenders to send the message to.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder toSenders(CommandSender... senders) {
		return toSenders(Sets.newHashSet(senders));
	}

	/**
	 * Set the players to send this message to.
	 *
	 * @param players The Players... to send the message to.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder toPlayers(Player... players) {
		this.senders.addAll(Sets.newHashSet(players));
		return this;
	}

	/**
	 * Set the players to send this message to.
	 *
	 * @param players The Collection<Player> to send the message to.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder toPlayers(Collection<? extends Player> players) {
		this.senders.addAll(players);
		return this;
	}

	/**
	 * Add a placeholder to the MessageBuilder.
	 *
	 * @param placeholderObject The object to be determined in the placeholder.
	 * @param placeholder The actual instance of the Placeholder.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder withPlaceholder(Object placeholderObject, Placeholder<?> placeholder) {
		placeholders.put(placeholder, placeholderObject);
		return this;
	}

	/**
	 * Set the configuration to read from, by default is the messages.yml
	 *
	 * @param section The FileConfiguration to read from.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder fromConfiguration(ConfigurationSection section) {
		this.section = section;
		return this;
	}

	/**
	 * Created a list replacement and ignores the placeholder object.
	 *
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param collection The collection.
	 * @param mapper The mapper.
	 * @return The MessageBuilder for chaining.
	 */
	public <T> MessageBuilder replace(String syntax, Collection<T> collection, Function<T, String> mapper) {
		replace(syntax, new StringList(collection, mapper).toString());
		return this;
	}

	/**
	 * Created a single replacement and ignores the placeholder object.
	 *
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param replacement The replacement e.g: the command.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder replace(String syntax, Object replacement) {
		placeholders.put(new SimplePlaceholder(syntax) {
			@Override
			public String get() {
				return replacement.toString();
			}
		}, replacement.toString());
		return this;
	}

	/**
	 * Created a single replacement and ignores the placeholder object with priority.
	 *
	 * @param priority The priority of the placeholder.
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param replacement The replacement e.g: the command.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder replace(int priority, String syntax, Object replacement) {
		placeholders.put(new SimplePlaceholder(priority, syntax) {
			@Override
			public String get() {
				return replacement.toString();
			}
		}, replacement.toString());
		return this;
	}

	/**
	 * Set the configuration nodes from messages.yml
	 *
	 * @param nodes The nodes to use.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder setNodes(String... nodes) {
		this.nodes = nodes;
		return this;
	}

	/**
	 * Set the placeholder object, good if you want to allow multiple placeholders.
	 *
	 * @param object The object to set
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder setPlaceholderObject(Object object) {
		this.defaultPlaceholderObject = object;
		return this;
	}

	/**
	 * Sends the message as an actionbar to the defined players.
	 *
	 * @param players the players to send to
	 */
	public void sendActionbar(Player... players) {
		toPlayers(players).sendActionbar();
	}

	/**
	 * Sends the message as a title to the defined players.
	 *
	 * @param players the players to send to
	 */
	public void sendTitle(Player... players) {
		toPlayers(players).sendTitle();
	}

	/**
	 * Sends the final product of the builder.
	 */
	public void send(Collection<Player> senders) {
		toPlayers(senders).send();
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
	public TextComponent get() {
		if (section == null)
			section = instance.getConfiguration("messages").orElse(instance.getConfig());
		if (prefix)
			complete = new TextComponent(Formatting.messagesPrefixed(instance, section, nodes).trim());
		else
			complete = new TextComponent(Formatting.messages(section, nodes).trim());
		complete = new TextComponent(applyPlaceholders(complete.getText()).trim());
		return complete;
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
	 * Sends the final product of the builder as a title if the players using toPlayers are set.
	 *
	 * WARNING: The title method needs to have the following as a configuration, this is special.
	 * title:
	 * 	  enabled: false
	 * 	  title: "&2Example"
	 * 	  subtitle: "&5&lColors work too."
	 * 	  fadeOut: 20
	 * 	  fadeIn: 20
	 * 	  stay: 200
	 */
	public void sendTitle() {
		if (section == null)
			section = instance.getConfiguration("messages").orElse(instance.getConfig());
		if (nodes.length != 1)
			return;
		if (!section.getBoolean(nodes[0] + ".enabled", false))
			return;
		String subtitle = section.getString(nodes[0] + ".subtitle", "");
		String title = section.getString(nodes[0] + ".title", "");
		int fadeOut = section.getInt(nodes[0] + ".fadeOut", 20);
		int fadeIn = section.getInt(nodes[0] + ".fadeIn", 20);
		int stay = section.getInt(nodes[0] + ".stay", 200);
		title = applyPlaceholders(title).replaceAll("\n", "");
		subtitle = applyPlaceholders(subtitle).replaceAll("\n", "");
		Player[] players = senders.parallelStream()
				.filter(sender -> sender instanceof Player)
				.toArray(Player[]::new);
		if (senders != null && senders.size() > 0)
			new Title.Builder()
					.subtitle(subtitle)
					.fadeOut(fadeOut)
					.fadeIn(fadeIn)
					.title(title)
					.stay(stay)
					.send(players);
	}

	/**
	 * Sends the final product of the builder as an action bar if the players using toPlayers are set.
	 */
	public void sendActionbar() {
		get();
		complete = new TextComponent(complete.getText().replaceAll("\n", ""));
		if (senders != null && senders.size() > 0) {
			for (CommandSender sender : senders) {
				if (sender instanceof Player)
					Actionbar.sendActionBar((Player)sender, complete.getText());
			}
		}
	}

	/**
	 * Adds a hover event to the message
	 * @param hoverEvent The hover event.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder setHoverEvent(HoverEvent hoverEvent) {
		this.hoverEvent = hoverEvent;
		return this;
	}

	/**
	 * Adds a click event to the message
	 * @param clickEvent The hover event.
	 * @return The MessageBuilder for chaining.
	 */
	public MessageBuilder setClickEvent(ClickEvent clickEvent) {
		this.clickEvent = clickEvent;
		return this;
	}

	/**
	 * Sends the final product of the builder if the senders are set.
	 */
	public void send() {
		get();
		if (clickEvent != null) complete.setClickEvent(clickEvent);
		if (hoverEvent != null) complete.setHoverEvent(hoverEvent);
		if (!senders.isEmpty()) {
			for (CommandSender sender : senders)
				sender.spigot().sendMessage(complete);
		}
	}

	@Override
	public String toString() {
		return get().getText();
	}

}
