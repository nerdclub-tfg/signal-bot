package de.nerdclubtfg.signalbot.components;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;

import de.thoffbauer.signal4j.SignalService;
import de.thoffbauer.signal4j.listener.ConversationListener;
import de.thoffbauer.signal4j.store.DataStore;

public class SignalConnection extends Signal {

	private static final String USER_AGENT = "signal-bot";
	
	private SignalService signalService;
	private Timer preKeysTimer;
	
	public SignalConnection() throws IOException {
		signalService = new SignalService();
		if(!signalService.isRegistered()) {
			if(!signalService.isRegistered()) {
				Scanner scanner = new Scanner(System.in);
				System.out.println("Url (or 'production' or 'staging' for whispersystems' server):");
				String url = scanner.nextLine();
				url = url.replace("production", "https://textsecure-service.whispersystems.org");
				url = url.replace("staging", "https://textsecure-service-staging.whispersystems.org");
				System.out.println("Phone Number:");
				String phoneNumber = scanner.nextLine();
				System.out.println("Device type, one of 'primary' (new registration) or 'secondary' (linking):");
				String deviceType = scanner.nextLine();
				
				if(deviceType.equals("primary")) {
					signalService.startConnectAsPrimary(url, USER_AGENT, phoneNumber, false);
					System.out.println("Verification code: ");
					String code = scanner.nextLine();
					code = code.replace("-", "");
					signalService.finishConnectAsPrimary(code);
				} else if(deviceType.equals("secondary")) {
					try {
						String uuid = signalService.startConnectAsSecondary(url, USER_AGENT, phoneNumber);
						System.out.println("Scan this uuid as a QR code, e.g. using an online qr code generator "
								+ "(The url does not contain sensitive information):");
						System.out.println(uuid);
						signalService.finishConnectAsSecondary(USER_AGENT, false);
						signalService.requestSync();
					} catch (TimeoutException e) {
						scanner.close();
						throw new IOException(e);
					}
				} else {
					scanner.close();
					throw new IOException("Invalid option!");
				}
				scanner.close();
				System.out.println("Registered!");
			}
			preKeysTimer = new Timer(true);
			preKeysTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						signalService.checkPreKeys(80);
					} catch (IOException e) {
						System.err.println("Could not update prekeys! " + e.getMessage());
					}
				}
			}, 0, 30 * 1000);
		}
	}

	public void sendMessage(String address, SignalServiceDataMessage message) throws IOException {
		signalService.sendMessage(address, message);
	}

	public void sendMessage(List<String> addresses, SignalServiceDataMessage message) throws IOException {
		signalService.sendMessage(addresses, message);
	}

	public void addConversationListener(ConversationListener listener) {
		signalService.addConversationListener(listener);
	}

	public void pull(int timeoutMillis) throws IOException {
		signalService.pull(timeoutMillis);
	}
	
	
}
