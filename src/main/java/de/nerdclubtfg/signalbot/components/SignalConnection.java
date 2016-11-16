package de.nerdclubtfg.signalbot.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;

import de.thoffbauer.signal4j.SignalService;
import de.thoffbauer.signal4j.exceptions.NoGroupFoundException;
import de.thoffbauer.signal4j.listener.ConversationListener;
import de.thoffbauer.signal4j.listener.SecurityExceptionListener;
import de.thoffbauer.signal4j.store.Group;
import de.thoffbauer.signal4j.store.User;

public class SignalConnection extends Signal implements SecurityExceptionListener {

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
		signalService.addSecurityExceptionListener(this);
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

	@Override
	public void onSecurityException(User user, Exception e) {
		System.err.println("Security Exception: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
		if(e instanceof NoGroupFoundException) {
			System.err.println(
					"This error most probably occurs if this number was added to a group in a different registration. "
					+ "This can be fixed by leaving and re-entering the group.\n"
					+ "Therefore please enter the members of the group you just sent a message in manually (space seperated). "
					+ "If you do not know the members, please enter an empty line. The message will be considered a private message then:");
			@SuppressWarnings("resource") Scanner scanner = new Scanner(System.in);
			String[] members = scanner.nextLine().split(" ");
			if(members.length == 0) {
				System.err.println("Aborting");
				return;
			}
			Group group = new Group(((NoGroupFoundException) e).getId());
			group.setMembers(new ArrayList<>(Arrays.asList(members)));
			try {
				signalService.leaveGroup(group);
			} catch (IOException e1) {
				System.err.println("Could not leave group! " + e1.getMessage() + " (" + e1.getClass().getSimpleName() + ")");
			}
		}
	}
	
	
}
