package com.sitrica.core.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.sitrica.core.SourPlugin;

public class AliasesProcessor implements CommandExecutor {

	private final AbstractCommand command;
	private final SourPlugin instance;

	public AliasesProcessor(SourPlugin instance, AbstractCommand command) {
		this.instance = instance;
		this.command = command;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		instance.getCommandHandler().processRequirements(this.command, label, sender, args);
		return true;
	}

}
