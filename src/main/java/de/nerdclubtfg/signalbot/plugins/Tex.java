package de.nerdclubtfg.signalbot.plugins;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;

import de.nerdclubtfg.signalbot.PrefixedPlugin;
import de.nerdclubtfg.signalbot.components.Signal;
import de.thoffbauer.signal4j.store.Group;
import de.thoffbauer.signal4j.store.User;

public class Tex extends PrefixedPlugin {
	
	private static final String URL = "https://latex.codecogs.com/png.download?\\dpi{300}%20\\LARGE%20";
	private static final byte[] PNG_HEADER = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
	private static final int PADDING = 30;

	public Tex() {
		super("!tex");
	}
	
	@Override
	public void onMessage(User sender, Group group, SignalServiceDataMessage message) throws IOException {
		String body = stripPrefix(message.getBody().get());
		if(body.isEmpty()) {
			Signal.getInstance().sendMessage(sender, group, "Usage: !tex [tex source]");
			return;
		}
		String url = URL + URLEncoder.encode(body, "utf-8").replace("+", "%20");
		try {
			// Download response
			File file = toFile(url);
			
			// Test for PNG header and interpret as string otherwise
			FileInputStream in = new FileInputStream(file);
			byte[] buf = new byte[8];
			if(in.read(buf) != 8 || !Arrays.equals(buf, PNG_HEADER)) {
				String beginning = new String(buf, "utf-8");
				Scanner scanner = new Scanner(in);
				scanner.useDelimiter("\\A");
				String end = scanner.next();
				scanner.close();
				Signal.getInstance().sendMessage(sender, group, beginning + end);
				in.close();
				file.delete();
				return;
			}
			in.close();
			
			// Remove alpha channel
			BufferedImage imgWithAlpha = ImageIO.read(file);
			BufferedImage imgWithoutAlpha = new BufferedImage(imgWithAlpha.getWidth() + PADDING, imgWithAlpha.getHeight() + PADDING, 
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = imgWithoutAlpha.createGraphics();
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, imgWithoutAlpha.getWidth(), imgWithoutAlpha.getHeight());
			g2d.drawImage(imgWithAlpha, PADDING / 2, PADDING / 2, null);
			g2d.dispose();
			ImageIO.write(imgWithoutAlpha, "PNG", file);
			
			// Send PNG
			SignalServiceDataMessage.Builder reply = SignalServiceDataMessage.newBuilder()
					.withAttachment(SignalServiceAttachment.newStreamBuilder()
							.withContentType("image/png")
							.withLength(file.length())
							.withStream(new FileInputStream(file))
							.build());
			Signal.getInstance().sendMessage(sender, group, reply);
			file.delete();
		} catch(IOException e) {
			e.printStackTrace();
			Signal.getInstance().sendMessage(sender, group, "Error: " + e.getMessage());
		}
	}

	private File toFile(String url) throws IOException {
		HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
		InputStream in = connection.getInputStream();
		File temp = File.createTempFile("tex", ".png");
		Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
		in.close();
		return temp;
	}
}
