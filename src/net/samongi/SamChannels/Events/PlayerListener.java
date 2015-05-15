package net.samongi.SamChannels.Events;

import net.samongi.SamChannels.Channels.ChannelManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener
{
	ChannelManager manager;
	
	public PlayerListener(ChannelManager manager)
	{
	  this.manager = manager;
	}
	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event)
	{
		manager.loadPlayer(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event)
	{
	  manager.savePlayer(event.getPlayer());
	}
}
