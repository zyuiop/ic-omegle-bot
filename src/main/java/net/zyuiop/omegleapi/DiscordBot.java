package net.zyuiop.omegleapi;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import net.zyuiop.omegleapi.commands.OmegleCommand;
import net.zyuiop.omegleapi.omegle.OmegleAPI;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * @author zyuiop
 */
public class DiscordBot {
	private static BlockingQueue<DiscordDelayTask> messages = new LinkedBlockingQueue<>();

	public static void main(String... args) throws DiscordException, MalformedURLException {
		Properties properties = new Properties(buildDefault());
		File props = new File("omeglebot.properties");

		if (props.exists() && props.isFile()) {
			try {
				FileReader reader = new FileReader(props);
				properties.load(reader);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Cannot read properties file, aborting startup.");
				return;
			}
		} else {
			try {
				FileWriter writer = new FileWriter(props);
				properties.store(writer, "Created by OmegleBot");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String token = properties.getProperty("token");
		if (token == null || token.isEmpty()) {
			if (args.length == 0) {
				System.out.println("No token provided.");
				return;
			}

			token = args[0];
		}

		System.out.println("Initializing commands...");
		new OmegleCommand();

		new Thread(() -> {
			while (true) {
				try {
					DiscordDelayTask message = messages.take();
					long time = message.send();

					while (time > 0) {
						Thread.sleep(time + 100);
						time = message.send();
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(1000);
					try {
						OmegleAPI.checkEvents();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

		System.out.println("Connecting to Discord !");
		IDiscordClient client = new ClientBuilder().withToken(token).login();
		client.getDispatcher().registerListener(new DiscordEventHandler());
	}

	private static Properties buildDefault() {
		Properties def = new Properties();
		def.setProperty("token", "");
		return def;
	}

	public static void sendMessage(IChannel channel, String message) {
		messages.add(new SendableMessage(channel, message));
	}

	private static interface DiscordDelayTask {
		long send();
	}

	public static void deleteMessage(IMessage message) {
		messages.add(new DeleteMessage(message));
	}

	private static class DeleteMessage implements DiscordDelayTask {
		private final IMessage delete;

		private DeleteMessage(IMessage delete) {
			this.delete = delete;
		}

		public long send() {
			try {
				delete.delete();
			} catch (MissingPermissionsException | DiscordException e) {
				e.printStackTrace();
			} catch (RateLimitException e) {
				return e.getRetryDelay();
			}
			return 0;
		}
	}

	private static class SendableMessage implements DiscordDelayTask {
		private final IChannel channel;
		private final String message;

		private SendableMessage(IChannel channel, String message) {
			this.channel = channel;
			this.message = message;
		}

		public long send() {
			try {
				channel.sendMessage(message);
			} catch (MissingPermissionsException | DiscordException e) {
				e.printStackTrace();
			} catch (RateLimitException e) {
				return e.getRetryDelay();
			}
			return 0;
		}
	}
}
