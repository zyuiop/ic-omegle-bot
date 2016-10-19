package net.zyuiop.omegleapi.commands;

import net.zyuiop.omegleapi.CommandRegistry;
import sx.blah.discord.handle.obj.IMessage;

/**
 * @author zyuiop
 */
public abstract class DiscordCommand {
	private final String name;
	private String description;

	public DiscordCommand(String name) {
		this.name = name;

		CommandRegistry.registerCommand(this);
	}

	public DiscordCommand(String name, String description) {
		this(name);
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public abstract void run(IMessage message) throws Exception;

	public String getName() {
		return name;
	}
}
