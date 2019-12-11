package com.sitrica.core.command;

import com.sitrica.core.SourPlugin;

public abstract class AdminCommand extends AbstractCommand {

	protected AdminCommand(SourPlugin instance, boolean console, String... commands) {
		super(instance, console, commands);
	}

}
