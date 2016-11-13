package de.nerdclubtfg.signalbot.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;

import de.nerdclubtfg.signalbot.PrefixedPlugin;
import de.nerdclubtfg.signalbot.components.Signal;
import de.thoffbauer.signal4j.store.Group;
import de.thoffbauer.signal4j.store.User;

public class Fefe extends PrefixedPlugin {

	private Pattern startRegex = Pattern.compile("<li><a href=\"\\?ts=[a-z0-9]{8}\">\\[l\\]</a>");
	
	public Fefe() {
		super("!fefe");
	}
	
	@Override
	public void onMessage(User sender, Group group, SignalServiceDataMessage message) throws IOException {
		String id = stripPrefix(message.getBody().get());
		if(!id.matches("[a-z0-9]{8}")) {
			Signal.getInstance().sendMessage(sender, group, "Not a valid fefe id!");
			return;
		}
		String html = getHtml(sender, group, "https://blog.fefe.de/?ts=" + id);
		if(html == null) {
			return;
		}
		
		Matcher matcher = startRegex.matcher(html);
		matcher.find();
		int articleStart = matcher.end();
		String article = html.substring(
				articleStart,
				html.indexOf("</ul>", articleStart)
		);
		// replace <p> with newline and <i> and <b> with * (Markdown-like)
		article = article.replace("<p>", "\n\n").replace("<p u>", "\n\n");
		article = article.replace("<b>", "*").replace("</b>", "*");
		article = article.replace("<i>", "*").replace("</i>", "*");
        // format links
		article = article.replace("<a href=\"", "(").replace("\">", ")[").replace("</a>", "]");
        // format quotes
		article = article.replace("<blockquote>", "\n\n> ").replace("</blockquote>", "\n\n");
		
		Signal.getInstance().sendMessage(sender, group, article);
	}
	
	private String getHtml(User sender, Group group, String url) throws IOException {
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			InputStream trustStoreIn = Fefe.class.getResourceAsStream("isrgrootx1.keystore");
			keyStore.load(trustStoreIn, "letsencrypt".toCharArray());
			trustStoreIn.close();
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(keyStore);
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);
			SSLSocketFactory factory = context.getSocketFactory();
			
			HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
			connection.setSSLSocketFactory(factory);
			InputStream in = connection.getInputStream();
			Scanner scanner = new Scanner(in);
			String html = scanner.useDelimiter("\\A").next();
			scanner.close();
			
			return html;
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | KeyManagementException e) {
			e.printStackTrace();
			Signal.getInstance().sendMessage(sender, group, "Internal SSL error!");
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			Signal.getInstance().sendMessage(sender, group, "Connection error!");
			return null;
		}
	}
	
}
