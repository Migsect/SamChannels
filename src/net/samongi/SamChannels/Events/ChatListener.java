package net.samongi.SamChannels.Events;

import net.samongi.SamChannels.Channels.ChannelManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener
{
	private ChannelManager manager;
	
	public ChatListener(ChannelManager manager)
	{
		this.manager = manager;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		if(event.isCancelled()) return;
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		String message = event.getMessage();
		manager.routeMessage(player, message);
	}
}
