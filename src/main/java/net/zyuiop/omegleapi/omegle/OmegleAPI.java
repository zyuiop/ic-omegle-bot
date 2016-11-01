package net.zyuiop.omegleapi.omegle;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.zyuiop.omegleapi.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;

/**
 * @author zyuiop
 */
public class OmegleAPI {
	private static final Map<String, OmegleSession> SESSIONS = new HashMap<>();
	private static final Set<String> CONTINUOUS_CHANNELS = new HashSet<>();
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
		synchronized (SESSIONS) {
			List<OmegleSession> sessions = new ArrayList<>(SESSIONS.values());
			sessions.forEach(OmegleSession::checkEvents);
		}
	}

	public static OmegleSession getSession(IChannel channel) {
		return SESSIONS.get(channel.getID());
	}

	public static OmegleSession openSession(IChannel channel, boolean continuous) throws Exception {
		synchronized (SESSIONS) {
			String id = channel.getID();
			if (SESSIONS.containsKey(id)) {
				DiscordBot.sendMessage(channel, "[Omegle] Fermez d'abord la session courante avec !omegle stop");
				return null;
			}

			String data = HttpUtil.post(new URL(OPEN_URL + "?lang=fr"), "");

			OmegleSession session = new OmegleSession(data.substring(1, data.length() - 1), channel);
			SESSIONS.put(id, session);
			if (continuous)
				CONTINUOUS_CHANNELS.add(id);
			return session;
		}
	}

	public static boolean toggleContinuous(IChannel channel) {
		if (CONTINUOUS_CHANNELS.remove(channel.getID()))
			return false;
		CONTINUOUS_CHANNELS.add(channel.getID());
		return true;
	}

	public static boolean isContinuous(IChannel channel) {
		return CONTINUOUS_CHANNELS.contains(channel.getID());
	}

	public static void removeSession(OmegleSession omegleSession) {
		synchronized (SESSIONS) {
			SESSIONS.remove(omegleSession.channel.getID());
			CONTINUOUS_CHANNELS.remove(omegleSession.channel.getID());
		}
	}
}
