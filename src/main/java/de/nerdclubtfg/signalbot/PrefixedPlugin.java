package de.nerdclubtfg.signalbot;

import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;

import de.thoffbauer.signal4j.store.Group;
import de.thoffbauer.signal4j.store.User;

public abstract class PrefixedPlugin extends Plugin {
	
	private String prefix;
	
	public PrefixedPlugin(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public boolean accepts(User sender, Group group, SignalServiceDataMessage message) {
		return message.getBody().isPresent() && message.getBody().get().startsWith(prefix);
	}
	
	protected String stripPrefix(String body) {
		return body.substring(prefix.length()).trim();
	}

}
