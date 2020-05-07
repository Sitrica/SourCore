package com.sitrica.core.command;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sitrica.core.SourPlugin;
import com.sitrica.core.command.AbstractCommand.ReturnType;
import com.sitrica.core.messaging.MessageBuilder;
import com.sitrica.core.sounds.SoundPlayer;
import com.sitrica.core.utils.Utils;

public class CommandHandler implements CommandExecutor {

	private final List<AbstractCommand> commands = new ArrayList<>();
	private final Class<? extends AbstractCommand> main;
	private final SourPlugin instance;

	public CommandHandler(SourPlugin instance, Class<? extends AbstractCommand> main, String... commandPackages) {
		Validate.notNull(main);
		this.main = main;
		this.instance = instance;
		Utils.getClassesOf(instance, AbstractCommand.class, commandPackages).forEach(clazz -> {
			try {
				commands.add(clazz.getConstructor(SourPlugin.class).newInstance(instance));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		});
		for (AbstractCommand command : commands) {
			String aliases = command.getAliases();
			if (aliases != null)
				instance.getCommand(command.getAliases()).setExecutor(new AliasesProcessor(instance, command));
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
		for (AbstractCommand abstractCommand : commands) {
			// It's the main command
			if (arguments.length <= 0 && abstractCommand.getClass().equals(main)) {
				String[] array = arguments;
				if (arguments.length > 0)
					array = Arrays.copyOfRange(arguments, 1, arguments.length);
				processRequirements(abstractCommand, arguments.length > 0 ? arguments[0] : label, sender, array);
				return true;
			} else if (arguments.length > 0 && abstractCommand.containsCommand(arguments[0])) {
				String[] array = arguments;
				if (arguments.length > 0)
					array = Arrays.copyOfRange(arguments, 1, arguments.length);
				processRequirements(abstractCommand, arguments.length > 0 ? arguments[0] : label, sender, array);
				return true;
			}
		}
		new MessageBuilder(instance, "messages.command-doesnt-exist").send(sender);
		return true;
	}

	protected void processRequirements(AbstractCommand command, String label, CommandSender sender, String[] arguments) {
		if (!(sender instanceof Player) && !command.isConsoleAllowed()) {
			 new MessageBuilder(instance, "messages.must-be-player")
			 		.replace("%command%", command.getSyntax(sender))
			 		.setPlaceholderObject(sender)
			 		.send(sender);
			return;
		}
		if (command.getPermissionNodes() == null || Arrays.stream(command.getPermissionNodes()).parallel().anyMatch(permission -> sender.hasPermission(permission))) {
			if (command instanceof AdminCommand) {
				if (sender instanceof Player && !sender.hasPermission(instance.getName().toLowerCase() + ".admin")) {
					new MessageBuilder(instance, "messages.no-permission").send(sender);
					return;
				}
			}
			ReturnType returnType = command.runCommand(label, sender, arguments);
			if (returnType == ReturnType.SYNTAX_ERROR) {
				 new MessageBuilder(instance, "messages.invalid-command", "messages.invalid-command-correction")
				 		.replace("%command%", command.getSyntax(sender))
				 		.setPlaceholderObject(sender)
				 		.send(sender);
			}
			if (returnType != ReturnType.SUCCESS && sender instanceof Player)
				new SoundPlayer(instance, "error").playTo((Player) sender);
			return;
		}
		new MessageBuilder(instance, "messages.no-permission").send(sender);
	}

	public List<AbstractCommand> getCommands() {
		return Collections.unmodifiableList(commands);
	}

}
