package net.zyuiop.omegleapi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.zyuiop.omegleapi.commands.DiscordCommand;
import net.zyuiop.omegleapi.omegle.OmegleAPI;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * @author zyuiop
 */
public class CommandRegistry {
	private static Map<String, DiscordCommand> commandMap = new HashMap<>();

	public static void registerCommand(DiscordCommand command) {
		commandMap.put(command.getName().toLowerCase(), command);
	}

	public static DiscordCommand getCommand(String tag) {
		return commandMap.get(tag.toLowerCase());
	}

	public static Collection<DiscordCommand> getAll() {
		return commandMap.values();
	}

	public static void handle(IMessage message) throws Exception {
		if (message.getContent().startsWith("!") || message.getContent().startsWith("/")) {
			String[] data = message.getContent().split(" ");
			if (data[0].length() == 1) {
				return;
			}
			DiscordCommand command = CommandRegistry.getCommand(data[0].substring(1));
			if (command != null) {
				try {
					command.run(message);
				} catch (Exception e) {
					DiscordBot.sendMessage(message.getChannel(), "Erreur pendant l'exécution de la commande : " + e.getClass().getName());
					e.printStackTrace();
				}
			}
		} else if (OmegleAPI.isContinuous(message.getChannel())) {
			OmegleAPI.getSession(message.getChannel()).send(message.getContent());
			DiscordBot.deleteMessage(message);
		}
	}
}
