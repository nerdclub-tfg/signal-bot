package de.nerdclubtfg.signalbot;

import java.io.IOException;
import java.util.List;

import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;
import org.whispersystems.signalservice.api.messages.multidevice.ReadMessage;

import de.nerdclubtfg.signalbot.components.Config;
import de.nerdclubtfg.signalbot.components.Signal;
import de.nerdclubtfg.signalbot.components.SignalConnection;
import de.nerdclubtfg.signalbot.components.SignalConsole;
import de.thoffbauer.signal4j.listener.ConversationListener;
import de.thoffbauer.signal4j.store.Group;
import de.thoffbauer.signal4j.store.User;

public class SignalBot implements ConversationListener {
	
	public void start(boolean offline) throws IOException {
		if(offline) {
			Signal.setInstance(new SignalConsole());
		} else {
			Signal.setInstance(new SignalConnection());
		}
		Config.load();
		
		Config config = Config.getInstance();
		for(Plugin plugin : Plugin.PLUGINS) {
			plugin.setEnabled(config.isEnabled(plugin));
		}
		
		Signal signal = Signal.getInstance();
		signal.addConversationListener(this);
		while(true) {
			try {
				signal.pull(60 * 1000);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onMessage(User sender, SignalServiceDataMessage message, Group group) {
		for(Plugin plugin : Plugin.PLUGINS) {
			try {
				if(plugin.isEnabled() && plugin.accepts(sender, group, message)) {
					plugin.onMessage(sender, group, message);
				}
			} catch(Exception e) {
				try {
					Signal.getInstance().sendMessage(sender, group, 
							SignalServiceDataMessage.newBuilder().withBody("Internal Error!"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		boolean offline = args.length != 0 && args[0].equals("offline");
		new SignalBot().start(offline);
	}

	@Override
	public void onContactUpdate(User contact) {}

	@Override
	public void onGroupUpdate(User sender, Group group) {}

	@Override
	public void onReadUpdate(List<ReadMessage> readList) {}
	
}
