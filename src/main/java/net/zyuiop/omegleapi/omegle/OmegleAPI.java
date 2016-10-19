package net.zyuiop.omegleapi.omegle;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import net.zyuiop.omegleapi.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;

/**
 * @author zyuiop
 */
public class OmegleAPI {
	/**
	 * The base omegle url
	 */
	public static String BASE_URL = "http://front9.omegle.com";

	/**
	 * The URL used to start a chat
	 */
	public static URL OPEN_URL;

	/**
	 * The URL used to disconnect from a chat
	 */
	public static URL DISCONNECT_URL;

	/**
	 * The URL used to parse events
	 */
	public static URL EVENT_URL;

	/**
	 * The URL used to send messages
	 */
	public static URL SEND_URL;

	/**
	 * The URL used to change typing status
	 */
	public static URL TYPING_URL;

	private static Map<String, OmegleSession> SESSIONS = new HashMap<>();

	static {
		try {
			OPEN_URL = new URL(BASE_URL + "/start");
			DISCONNECT_URL = new URL(BASE_URL + "/disconnect");
			EVENT_URL = new URL(BASE_URL + "/events");
			SEND_URL = new URL(BASE_URL + "/send");
			TYPING_URL = new URL(BASE_URL + "/typing");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public static void checkEvents() {
		SESSIONS.values().forEach(OmegleSession::checkEvents);
	}

	public static OmegleSession getSession(IChannel channel) {
		return SESSIONS.get(channel.getID());
	}

	public static OmegleSession openSession(IChannel channel) throws Exception {
		String id = channel.getID();
		if (SESSIONS.containsKey(id)) {
			DiscordBot.sendMessage(channel, "[Omegle] Fermez d'abord la session courante avec !omegle stop");
			return null;
		}

		String data = HttpUtil.post(OPEN_URL, "");

		OmegleSession session = new OmegleSession(data.substring(1, data.length() - 1), channel);
		SESSIONS.put(id, session);
		return session;
	}

	public static void removeSession(OmegleSession omegleSession) {
		SESSIONS.remove(omegleSession.channel.getID());
	}
}
