package com.sitrica.core.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sitrica.core.SourPlugin;
import com.sitrica.core.messaging.Formatting;
import com.sitrica.core.objects.StringList;
import com.sitrica.core.placeholders.Placeholder;
import com.sitrica.core.placeholders.Placeholders;
import com.sitrica.core.placeholders.SimplePlaceholder;
import com.sitrica.core.utils.DeprecationUtils;
import com.sitrica.core.utils.Utils;

public class ItemStackBuilder {

	private Map<Placeholder<?>, Object> placeholders = new HashMap<>();
	private final List<String> additionalLores = new ArrayList<>();
	private Object defaultPlaceholderObject;
	private ConfigurationSection section;
	private final SourPlugin instance;
	private boolean glowing;
	private String node;

	public ItemStackBuilder(SourPlugin instance, String node) {
		this.instance = instance;
		this.node = node;
	}

	/**
	 * Creates a ItemStackBuilder with the defined nodes..
	 * 
	 * @param instance The SourPlugin using this ItemStackBuilder.
	 * @param section The ConfigurationSection where the correct values for the ItemStackBuilder exists.
	 */
	public ItemStackBuilder(SourPlugin instance, ConfigurationSection section) {
		this.instance = instance;
		this.section = section;
	}

	/**
	 * Add a placeholder to the ItemStackBuilder.
	 * 
	 * @param placeholderObject The object to be determined in the placeholder.
	 * @param placeholder The actual instance of the Placeholder.
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder withPlaceholder(Object placeholderObject, Placeholder<?> placeholder) {
		placeholders.put(placeholder, placeholderObject);
		return this;
	}

	/**
	 * Created a single replacement and ignores the placeholder object.
	 * 
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param replacement The replacement e.g: the command.
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder replace(String syntax, Object replacement) {
		placeholders.put(new SimplePlaceholder(syntax) {
			@Override
			public String get() {
				return replacement.toString();
			}
		}, replacement.toString());
		return this;
	}

	/**
	 * Created a list replacement and ignores the placeholder object.
	 * @param <T>
	 * 
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param replacement The replacement e.g: the command.
	 * @return The ItemStackBuilder for chaining.
	 */
	public <T> ItemStackBuilder replace(String syntax, Collection<T> collection, Function<T, String> mapper) {
		replace(syntax, new StringList(collection, mapper).toString());
		return this;
	}

	/**
	 * Created a single replacement and ignores the placeholder object with priority.
	 * 
	 * @param priority The priority of the placeholder.
	 * @param syntax The syntax to check within the messages e.g: %command%
	 * @param replacement The replacement e.g: the command.
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder replace(int priority, String syntax, Object replacement) {
		placeholders.put(new SimplePlaceholder(priority, syntax) {
			@Override
			public String get() {
				return replacement.toString();
			}
		}, replacement.toString());
		return this;
	}

	/**
	 * Set the configuration to read from, by default is the config.yml
	 * 
	 * @param configuration The FileConfiguration to read from.
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder fromConfiguration(ConfigurationSection section) {
		this.section = section;
		if (node != null)
			this.section = section.getConfigurationSection(node);
		return this;
	}

	/**
	 * Set the placeholder object, good if you want to allow multiple placeholders.
	 * 
	 * @param object The object to set
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder setPlaceholderObject(Object object) {
		this.defaultPlaceholderObject = object;
		return this;
	}

	public ItemStackBuilder withAdditionalLores(Collection<String> lores) {
		this.additionalLores.addAll(lores);
		return this;
	}

	public ItemStackBuilder withAdditionalLoresIf(boolean boo, Collection<String> lores) {
		if (!boo)
			return this;
		this.additionalLores.addAll(lores);
		return this;
	}

	public ItemStackBuilder glowingIf(boolean glowing) {
		glowingIf(() -> glowing);
		return this;
	}

	/**
	 * Set the placeholder object, good if you want to allow multiple placeholders.
	 * 
	 * @param object The object to set
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder glowingIf(Supplier<Boolean> glowing) {
		if (section == null)
			section = instance.getConfiguration("inventories")
					.orElse(instance.getConfig())
					.getConfigurationSection(node);
		if (!section.getBoolean("glowing", true))
			return this;
		this.glowing = glowing.get();
		return this;
	}

	/**
	 * Set the section to read from.
	 * 
	 * @param section The ConfigurationSection to read from.
	 * @return The ItemStackBuilder for chaining.
	 */
	public ItemStackBuilder setConfigurationSection(ConfigurationSection section) {
		this.section = section;
		return this;
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
					input = input.replaceAll(Pattern.quote(syntax), placeholder.replace_i(entry.getValue()));
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
					input = input.replaceAll(Pattern.quote(syntax), placeholder.replace_i(defaultPlaceholderObject));
				}
			}
		}
		return input;
	}

	/**
	 * Grab the final ItemStack built from the builder.
	 */
	public ItemStack build() {
		if (section == null) {
			if (node == null) {
				instance.consoleMessage("A configuration node is formatted incorrectly.");
				return null;
			}
			section = instance.getConfiguration("inventories")
					.orElse(instance.getConfig())
					.getConfigurationSection(node);
		}
		String title = section.getString("title", "");
		title = applyPlaceholders(title);
		String matName = section.getString("material", "STONE");
		Material material = Utils.materialAttempt(applyPlaceholders(matName), "STONE");
		ItemStack itemstack = new ItemStack(material);
		ItemMeta meta = itemstack.getItemMeta();
		meta.setDisplayName(Formatting.color(title));
		List<String> lores = section.getStringList("lore");
		if (lores == null || lores.isEmpty())
			lores = section.getStringList("description");
		lores.addAll(additionalLores);
		if (lores != null && !lores.isEmpty()) {
			meta.setLore(lores.parallelStream()
					.map(lore -> applyPlaceholders(lore))
					.map(lore -> Formatting.color(lore))
					.collect(Collectors.toList()));
		}
		if (section.getBoolean("glowing", false) || glowing) {
			meta.addEnchant(Enchantment.DURABILITY, 1, false);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		if (section.isConfigurationSection("enchantments")) {
			ConfigurationSection enchantmentSection = section.getConfigurationSection("enchantments");
			for (String name : enchantmentSection.getKeys(false)) {
				@SuppressWarnings("deprecation")
				Enchantment enchantment = Enchantment.getByName(name.toUpperCase(Locale.US));
				if (enchantment == null)
					continue;
				int level = enchantmentSection.getInt(name);
				meta.addEnchant(enchantment, level, true);
			}
		}
		if (section.isConfigurationSection("attributes")) {
			ConfigurationSection attributeSection = section.getConfigurationSection("attributes");
			for (String name : attributeSection.getKeys(false)) {
				Attribute attribute;
				try {
					attribute = Attribute.valueOf(name.toUpperCase(Locale.US));
				} catch (Exception e) {
					continue;
				}
				if (attribute == null)
					continue;
				double level = attributeSection.getDouble(name);
				AttributeModifier modifier = new AttributeModifier(instance.getName(), level, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
				meta.addAttributeModifier(attribute, modifier);
			}
		}
		if (section.isList("itemflags")) {
			for (String string : section.getStringList("itemflags")) {
				ItemFlag flag;
				try {
					flag = ItemFlag.valueOf(string.toUpperCase(Locale.US));
				} catch (Exception e) {
					continue;
				}
				if (flag == null)
					continue;
				meta.addItemFlags(flag);
			}
		}
		// Sets the itemMeta
		itemstack.setItemMeta(DeprecationUtils.setupItemMeta(meta, applyPlaceholders(section.getString("material-meta", ""))));
		return itemstack;
	}

}
