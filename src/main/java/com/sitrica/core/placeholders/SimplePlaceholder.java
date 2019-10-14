package com.sitrica.core.placeholders;

public abstract class SimplePlaceholder extends Placeholder<String> {
	
	public SimplePlaceholder(String... syntax) {
		super(syntax);
	}

	public SimplePlaceholder(int priority, String... syntax) {
		super(priority, syntax);
	}

	@Override
	public final String replace(String object) {
		return get();
	}
	
	public abstract String get();
	
}
