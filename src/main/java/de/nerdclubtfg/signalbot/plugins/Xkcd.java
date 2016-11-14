package de.nerdclubtfg.signalbot.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.nerdclubtfg.signalbot.PrefixedPlugin;
import de.nerdclubtfg.signalbot.components.Signal;
import de.thoffbauer.signal4j.store.Group;
import de.thoffbauer.signal4j.store.User;

public class Xkcd extends PrefixedPlugin {
	
	private static final String URL = "https://xkcd.com/";
	
	private Random random = new Random();

	public Xkcd() {
		super("!xkcd");
	}

	@Override
	public void onMessage(User sender, Group group, SignalServiceDataMessage message) throws IOException {
		try {
			String body = stripPrefix(message.getBody().get());
			if(body.matches("\\d+")) {
				sendXkcd(sender, group, body);
			} else if(body.isEmpty()) {
				// Get newest id
				Map<String, Object> info = getData("info.0.json");
				int id = Integer.valueOf(info.get("num").toString());
				// choose a random id
				id = random.nextInt(id);
				sendXkcd(sender, group, String.valueOf(id));
			} else {
				Signal.getInstance().sendMessage(sender, group, "Usage: !xkcd [id]");
			}
		} catch(IOException e) {
			e.printStackTrace();
			Signal.getInstance().sendMessage(sender, group, "Error: " + e.getMessage());
		}
	}
	
	private void sendXkcd(User sender, Group group, String id) throws IOException {
		Map<String, Object> info = getData(id + "/info.0.json");
		File image = toFile(info.get("img").toString());
		
		SignalServiceDataMessage.Builder message = SignalServiceDataMessage.newBuilder()
				.withBody(info.get("title").toString())
				.withAttachment(SignalServiceAttachment.newStreamBuilder()
						.withContentType("image/png")
						.withLength(image.length())
						.withStream(new FileInputStream(image))
						.build());
		Signal.getInstance().sendMessage(sender, group, message);
		image.delete();
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getData(String url) throws MalformedURLException, IOException {
		InputStream in = open(url);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(in, Map.class);
		in.close();
		return map;
	}
	
	private InputStream open(String url) throws IOException {
		HttpsURLConnection connection = (HttpsURLConnection) new URL(URL + url).openConnection();
		InputStream in = connection.getInputStream();
		return in;
	}
	
	private File toFile(String url) throws IOException {
		HttpsURLConnection connection = (HttpsURLConnection) new URL(url.replace("http://", "https://")).openConnection();
		InputStream in = connection.getInputStream();
		File temp = File.createTempFile("xkcd", ".png");
		Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
		in.close();
		return temp;
	}
	
}
