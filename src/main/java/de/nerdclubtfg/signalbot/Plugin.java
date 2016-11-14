package de.nerdclubtfg.signalbot;

import java.io.IOException;
import java.util.Arrays;

import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;

import de.nerdclubtfg.signalbot.plugins.*;
import de.thoffbauer.signal4j.store.Group;
import de.thoffbauer.signal4j.store.User;

public abstract class Plugin {

	public static final Plugin[] PLUGINS = new Plugin[] {
		new Echo(),
		new Fefe(),
		new Plugins(),
		new Tex(),
		new Xkcd()
	};
	
	public static Plugin getPlugin(String name) {
		return Arrays.stream(PLUGINS).filter(v -> v.getName().equals(name)).findFirst().orElse(null);
	}
	
	private boolean isEnabled;
	
	public abstract boolean accepts(User sender, Group group, SignalServiceDataMessage message);
	public abstract void onMessage(User sender, Group group, SignalServiceDataMessage message) throws IOException;
	
	public String getName() {
		return this.getClass().getSimpleName().toLowerCase();
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
}
