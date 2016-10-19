package net.zyuiop.omegleapi.omegle;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
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
	private int failCount;

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
		try {
			String resp = HttpUtil.post(OmegleAPI.EVENT_URL, "id=" + encodedId);

			if (resp.equals("null")) {
				if (++failCount >= 3) {
					disconnect();
				}
				return;
			}

			parseEvents(new JSONArray(resp));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void send(final String text) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("id", sessionId);
		map.put("msg", text);
		String resp = HttpUtil.post(OmegleAPI.SEND_URL, map);
		if (!resp.equals("win")) {
			throw new Exception("Unable to send message, response: "
					+ resp);
		}
	}

	public void parseEvents(JSONArray events) throws Exception {
		for (int i = 0; i < events.length(); i++) {
			JSONArray e = events.getJSONArray(i);

			try {
				String event = (e.getString(0));
				if (event.equalsIgnoreCase("gotMessage")) {
					String message = e.getString(1);
					DiscordBot.sendMessage(channel, "[Omegle] <stranger> : " + message);
				} else if (event.equalsIgnoreCase("strangerDisconnected")) {
					DiscordBot.sendMessage(channel, "[Omegle] La cible s'est déconnectée.");
					disconnect();
				}
			} catch (IllegalArgumentException ex) {
				// Ignore unknown events
			}
		}
	}

	public void disconnect() throws Exception {
		String resp = HttpUtil.post(OmegleAPI.DISCONNECT_URL, "id="
				+ encodedId);
		if (!resp.equals("win")) {
			throw new Exception("Unable to disconnect, response: "
					+ resp);
		}
		OmegleAPI.removeSession(this);
		DiscordBot.sendMessage(channel, "Session Omegle fermée !");
	}

	public String getSessionId() {
		return sessionId;
	}
}
