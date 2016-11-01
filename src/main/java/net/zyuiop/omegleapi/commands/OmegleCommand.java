package net.zyuiop.omegleapi.commands;

import java.util.Arrays;
import net.zyuiop.omegleapi.DiscordBot;
import net.zyuiop.omegleapi.omegle.OmegleAPI;
import net.zyuiop.omegleapi.omegle.OmegleSession;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.obj.IMessage;

/**
 * @author zyuiop
 */
public class OmegleCommand extends DiscordCommand {
	public OmegleCommand() {
		super("omegle", "!omegle help pour plus d'infos");
	}


	@Override
	public void run(IMessage message) throws Exception {
		String[] args = message.getContent().split(" ");
		if (args.length == 1 || args[1].equalsIgnoreCase("help")) {
			DiscordBot.sendMessage(message.getChannel(), "```!omegle start [channel] : démarre une session Omegle\n" +
					"!omegle send <message> : envoie un message\n" +
					"!omegle stop : termine une session omegle\n" +
					"!omegle channel : active/désactive le mode 'wholechan'```");
			return;
		}

		String action = args[1];
		if (action.equalsIgnoreCase("start")) {
			OmegleSession s;
			boolean channel = false;
			if (args.length > 2 && args[2].equalsIgnoreCase("channel"))
				channel = true;

			if ((s = OmegleAPI.openSession(message.getChannel(), channel)) != null) {
				String dispMessage = "**Session Omegle " + s.getSessionId() + " ouverte !**";
				if (!channel)
					dispMessage += "Utilisez `!omegle send` pour parler";
				DiscordBot.sendMessage(message.getChannel(), dispMessage);
			}
		} else if (action.equalsIgnoreCase("stop")) {
			OmegleSession session = OmegleAPI.getSession(message.getChannel());
			if (session == null) {
				DiscordBot.sendMessage(message.getChannel(), "**Aucune session Omegle ouverte pour le moment.**");
				return;
			}
			session.disconnect();
		} else if (action.equalsIgnoreCase("channel")) {
			OmegleSession session = OmegleAPI.getSession(message.getChannel());
			if (session == null) {
				DiscordBot.sendMessage(message.getChannel(), "**Aucune session Omegle ouverte pour le moment.**");
				return;
			}

			if (OmegleAPI.toggleContinuous(message.getChannel())) {
				DiscordBot.sendMessage(message.getChannel(), "**Les messages du salon sont désormais tous envoyés sur Omegle.**");
			} else {
				DiscordBot.sendMessage(message.getChannel(), "**Vous devez désormais utiliser !omegle send pour parler.**");
			}
		} else if (action.equalsIgnoreCase("send")) {
			OmegleSession session = OmegleAPI.getSession(message.getChannel());
			if (session == null) {
				DiscordBot.sendMessage(message.getChannel(), "**Aucune session Omegle ouverte pour le moment.**");
				return;
			}

			if (args.length < 3) {
				DiscordBot.sendMessage(message.getChannel(), "**Aucune message indiqué.**");
				return;
			}

			session.send(StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " "));
			DiscordBot.deleteMessage(message);
		}
	}
}
