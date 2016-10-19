package net.zyuiop.omegleapi.omegle;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import net.zyuiop.omegleapi.DiscordBot;
import org.json.JSONArray;
import sx.blah.discord.handle.obj.IChannel;

/**
 * @author zyuiop
 */
public class OmegleSession {
	protected final IChannel channel;
	private final String sessionId;
	private String encodedId;
	private boolean active = true;
	private AtomicInteger failCount = new AtomicInteger(0);

	public OmegleSession(String sessionId, IChannel channel) {
		this.sessionId = sessionId;
		this.channel = channel;
		try {
			this.encodedId = URLEncoder.encode(sessionId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void checkEvents() {
		if (!active)
			return;

		try {
			String resp = HttpUtil.post(OmegleAPI.EVENT_URL, "id=" + encodedId);

			if (resp.equals("null")) {
				if (failCount.addAndGet(1) >= 3) {
					disconnect();
				}
				return;
			} else {
				failCount.set(0);
			}

			parseEvents(new JSONArray(resp));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void send(final String text) throws Exception {
		if (!active) { return; }

		Map<String, Object> map = new HashMap<>();
		map.put("id", sessionId);
		map.put("msg", text);
		String resp = HttpUtil.post(OmegleAPI.SEND_URL, map);
		if (!resp.equals("win")) {
			throw new Exception("Unable to send message, response: "
					+ resp);
		} else {
			DiscordBot.sendMessage(channel, "[Omegle] <you> : " + text);
		}
	}

	public void parseEvents(JSONArray events) throws Exception {
		for (int i = 0; i < events.length(); i++) {
			JSONArray e = events.getJSONArray(i);
			Logger.getAnonymousLogger().info(e.toString());

			try {
				String event = (e.getString(0));
				// typing
				// stoppedTyping
				// serverMessage
				if (event.equalsIgnoreCase("gotMessage")) {
					String message = e.getString(1);
					DiscordBot.sendMessage(channel, "[Omegle] <stranger> : " + message);
				} else if (event.equalsIgnoreCase("strangerDisconnected")) {
					DiscordBot.sendMessage(channel, "[Omegle] La cible s'est déconnectée.");
					disconnect();
				} else if (event.equalsIgnoreCase("serverMessage")) {
					String message = e.getString(1);
					DiscordBot.sendMessage(channel, "[Omegle] <server> : " + message);
				} else if (event.equalsIgnoreCase("typing")) {
					channel.setTypingStatus(true);
				} else if (event.equalsIgnoreCase("stoppedTyping")) {
					channel.setTypingStatus(false);
				}
			} catch (IllegalArgumentException ex) {
				// Ignore unknown events
			}
		}
	}

	public void disconnect() throws Exception {
		if (!active) { return; }

		String resp = HttpUtil.post(OmegleAPI.DISCONNECT_URL, "id="
				+ encodedId);
		if (!resp.equals("win")) {
			throw new Exception("Unable to disconnect, response: "
					+ resp);
		}
		active = false;
		OmegleAPI.removeSession(this);
		DiscordBot.sendMessage(channel, "Session Omegle fermée !");
	}

	public String getSessionId() {
		return sessionId;
	}
}
