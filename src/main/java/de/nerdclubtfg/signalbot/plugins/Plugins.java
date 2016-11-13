package de.nerdclubtfg.signalbot.plugins;

import java.io.IOException;

import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;

import de.nerdclubtfg.signalbot.Plugin;
import de.nerdclubtfg.signalbot.PrefixedPlugin;
import de.nerdclubtfg.signalbot.components.Config;
import de.nerdclubtfg.signalbot.components.Signal;
import de.thoffbauer.signal4j.store.Group;
import de.thoffbauer.signal4j.store.User;

public class Plugins extends PrefixedPlugin {

	public Plugins() {
		super("!plugins");
	}

	@Override
	public void onMessage(User sender, Group group, SignalServiceDataMessage message) throws IOException {
		String body = stripPrefix(message.getBody().get());
		if(body.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Plugins:\n");
			for(Plugin plugin : Plugin.PLUGINS) {
				sb.append(plugin.getName()).append(" ");
				if(plugin.isEnabled()) {
					sb.append("\u2714"); // check mark
				} else {
					sb.append("\u274C"); // cross
				}
				sb.append("\n");
			}
			Signal.getInstance().sendMessage(sender, group, sb.toString());
		} else {
			if(!Config.getInstance().isSudo(sender)) {
				Signal.getInstance().sendMessage(sender, group, "This feature requires sudo!");
				return;
			}
			boolean enable = false;
			String name = body.substring(body.indexOf(' ') + 1);
			if(body.startsWith("enable")) {
				enable = true;
			} else if(body.startsWith("disable")) {
				enable = false;
			} else {
				Signal.getInstance().sendMessage(sender, group, "Usage: !plugins [enable/disable] [plugin]");
				return;
			}
			Plugin plugin = Plugin.getPlugin(name);
			if(plugin == null) {
				Signal.getInstance().sendMessage(sender, group, "Plugin not known!");
				return;
			}
			plugin.setEnabled(enable);
			if(enable) {
				Signal.getInstance().sendMessage(sender, group, "Plugin enabled.");
			} else {
				Signal.getInstance().sendMessage(sender, group, "Plugin disabled.");
			}
		}
	}

}
