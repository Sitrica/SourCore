package com.sitrica.core.placeholders;

import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;

public class Placeholders {

	private static TreeSet<Placeholder<?>> placeholders = new TreeSet<>(Comparator.comparing(Placeholder::getPriority));

	public static void registerPlaceholder(Placeholder<?> placeholder) {
		placeholders.add(placeholder);
	}

	/**
	 * Grab a placeholder by it's syntax.
	 * Example: %command% to be replaced by a String command.
	 * 
	 * @param syntax The syntax to grab e.g: %player%
	 * @return The placeholder if the syntax was found.
	 */
	public static Optional<Placeholder<?>> getPlaceholder(String syntax) {
		for (Placeholder<?> placeholder : placeholders) {
			for (String s : placeholder.getSyntaxes()) {
				if (s.equals(syntax)) {
					return Optional.of(placeholder);
				}
			}
		}
		return Optional.empty();
	}

	public static TreeSet<Placeholder<?>> getPlaceholders() {
		return placeholders;
	}

}
