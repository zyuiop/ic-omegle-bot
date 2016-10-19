package net.zyuiop.omegleapi;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.MessageUpdateEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * @author zyuiop
 */
public class DiscordEventHandler {
	@EventSubscriber
	public void onMessage(MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
		IMessage message = event.getMessage();
		onMessage(message);
	}

	@EventSubscriber
	public void onMessage(MessageUpdateEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
		if (event.getNewMessage().getContent().equalsIgnoreCase(event.getOldMessage().getContent())) {
			return;
		}

		IMessage message = event.getNewMessage();
		onMessage(message);
	}

	private void onMessage(IMessage message) throws RateLimitException, DiscordException, MissingPermissionsException {
		CommandRegistry.handle(message);
	}
}
