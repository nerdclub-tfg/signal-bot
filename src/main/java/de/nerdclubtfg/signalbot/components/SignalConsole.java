package de.nerdclubtfg.signalbot.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;

import de.thoffbauer.signal4j.listener.ConversationListener;
import de.thoffbauer.signal4j.store.User;

public class SignalConsole extends Signal {
	
	private ArrayList<ConversationListener> listeners = new ArrayList<>();
	private Scanner scanner = new Scanner(System.in);

	@Override
	public void sendMessage(String address, SignalServiceDataMessage message) throws IOException {
		ArrayList<String> addresses = new ArrayList<>();
		addresses.add(address);
		sendMessage(addresses, message);
	}

	@Override
	public void sendMessage(List<String> addresses, SignalServiceDataMessage message) throws IOException {
		String out = String.format(
				"Send message to %s:\n"
				+ "%s",
				String.join(", ", addresses),
				message.getBody().or("no body"));
		System.out.println(out);
	}

	@Override
	public void addConversationListener(ConversationListener listener) {
		listeners.add(listener);
	}

	@Override
	public void pull(int timeoutMillis) throws IOException {
		System.out.print("Enter sender: ");
		String sender = scanner.nextLine();
		System.out.println("Body:");
		String body = "";
		String line;
		while(!(line = scanner.nextLine()).isEmpty()) {
			body += line + "\n";
		}
		
		SignalServiceDataMessage message = SignalServiceDataMessage.newBuilder()
				.withTimestamp(System.currentTimeMillis())
				.withBody(body)
				.build();
		for(ConversationListener listener : listeners) {
			listener.onMessage(new User(sender), message, null);
		}
	}

}
