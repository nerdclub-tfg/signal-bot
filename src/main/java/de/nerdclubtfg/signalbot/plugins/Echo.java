package de.nerdclubtfg.signalbot.plugins;

import java.io.IOException;

import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;

import de.nerdclubtfg.signalbot.PrefixedPlugin;
import de.nerdclubtfg.signalbot.components.Signal;
import de.thoffbauer.signal4j.store.Group;
import de.thoffbauer.signal4j.store.User;

public class Echo extends PrefixedPlugin {

	public Echo() {
		super("!echo");
	}

	@Override
	public void onMessage(User user, Group group, SignalServiceDataMessage message) throws IOException {
		Signal.getInstance().sendMessage(user, group, stripPrefix(message.getBody().get()));
	}

}
