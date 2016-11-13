package de.nerdclubtfg.signalbot.components;

import java.io.IOException;
import java.util.List;

import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;
import org.whispersystems.signalservice.api.messages.SignalServiceGroup;
import org.whispersystems.signalservice.api.messages.SignalServiceGroup.Type;

import de.thoffbauer.signal4j.listener.ConversationListener;
import de.thoffbauer.signal4j.store.Group;
import de.thoffbauer.signal4j.store.User;

public abstract class Signal {
	
	private static Signal instance;
	
	public static Signal getInstance() {
		return Signal.instance;
	}
	
	public static void setInstance(Signal instance) {
		Signal.instance = instance;
	}
	
	public void sendMessage(User sender, Group group, String body) throws IOException {
		sendMessage(sender, group, SignalServiceDataMessage.newBuilder().withBody(body));
	}
	
	public void sendMessage(User sender, Group group, SignalServiceDataMessage.Builder messageBuilder) throws IOException {
		messageBuilder.withTimestamp(System.currentTimeMillis());
		if(group != null) {
			messageBuilder.asGroupMessage(SignalServiceGroup.newBuilder(Type.DELIVER).withId(group.getId().getId()).build());
			sendMessage(group.getMembers(), messageBuilder.build());
		} else {
			sendMessage(sender.getNumber(), messageBuilder.build());
		}
	}
	
	public abstract void sendMessage(String address, SignalServiceDataMessage message) throws IOException;
	public abstract void sendMessage(List<String> addresses, SignalServiceDataMessage message) throws IOException;
	public abstract void addConversationListener(ConversationListener listener);
	public abstract void pull(int timeoutMillis) throws IOException;

}
