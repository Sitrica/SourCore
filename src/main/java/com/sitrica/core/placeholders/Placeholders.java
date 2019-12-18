package com.sitrica.core.placeholders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

public class Placeholders {

	private static final List<Placeholder<?>> placeholders = new ArrayList<>();

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

	public static List<Placeholder<?>> getPlaceholders() {
		List<Placeholder<?>> alternative = Lists.newArrayList(placeholders);
		Collections.sort(alternative, Comparator.comparing(Placeholder::getPriority));
		return alternative;
	}

}
