package net.samongi.SamChannels;

import java.util.logging.Logger;

import net.samongi.SamChannels.Channels.ChannelManager;
import net.samongi.SamChannels.Commands.CommandChannels;
import net.samongi.SamChannels.Commands.CommandChat;
import net.samongi.SamChannels.Commands.CommandListen;
import net.samongi.SamChannels.Events.ChatListener;
import net.samongi.SamChannels.Events.PlayerListener;
import net.samongi.SamChannels.Titles.TitleManager;
import net.samongi.SamongiLib.CommandHandling.CommandHandler;
import net.samongi.SamongiLib.Configuration.ConfigAccessor;
import net.samongi.SamongiLib.Player.Group;
import net.samongi.SamongiLib.Player.Group.Action;
import net.samongi.SamongiLib.Player.ServerGroup;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SamChannels extends JavaPlugin
{
	static final public Logger logger = Logger.getLogger("Minecraft"); // Logger is static to allow easy use across plugin;
	static public boolean debug;
	
	private ChannelManager channel_manager;
	
	//Enabling
	public void onEnable()
	{
	  // Server Log Message
	  PluginDescriptionFile pdf = this.getDescription();
		SamChannels.logger.info(pdf.getName() + " has been enabled.");
		
		// Get standard config:
		getConfig().options().copyDefaults(true);
		saveConfig();
		reloadConfig();
		
	  // Getting the debugger and sayin the config was read succesfully
    debug = this.getConfig().getBoolean("debug");
    if(SamChannels.debug) SamChannels.logger.info("Debug is ON");
		if(SamChannels.debug) SamChannels.logger.info("'config.yml' has been read successfully");
		
		// Get channel config:
		if(SamChannels.debug) SamChannels.logger.info("Reading 'channels.yml'");
		ConfigAccessor channel_config = new ConfigAccessor(this,"channels.yml");
		channel_config.getConfig().options().copyDefaults(true);
		channel_config.saveConfig();
		channel_config.reloadConfig();
		
		// Get titles config:
		if(SamChannels.debug) SamChannels.logger.info("Reading 'titles.yml'");
		ConfigAccessor title_config = new ConfigAccessor(this, "titles.yml");
		title_config.getConfig().options().copyDefaults(true);
		title_config.saveConfig();
		title_config.reloadConfig();
		
	  
	  
		// Title Manager creation
		if(SamChannels.debug) SamChannels.logger.info("Creating Title Manager...");
		TitleManager title_manager = new TitleManager();
		if(SamChannels.debug) SamChannels.logger.info("Registering Titles...");
		title_manager.registerTitles(title_config);
		
		
		// Channel Manager creation
		if(SamChannels.debug) SamChannels.logger.info("Creating Channel Manager...");
		this.channel_manager = new ChannelManager(this, title_manager);
		
		channel_manager.setDefaultRoute(channel_config.getConfig().getString("default_chat"));
		channel_manager.registerConfigChannels(channel_config);
		
		// Going through the channel configuration to set the channels up.
		
		// Register Events
		PluginManager pm = this.getServer().getPluginManager();
		if(SamChannels.debug) SamChannels.logger.info("Registering ChatListener...");
		pm.registerEvents(new ChatListener(channel_manager), this);
		if(SamChannels.debug) SamChannels.logger.info("Registering PlayerListener...");
    pm.registerEvents(new PlayerListener(channel_manager), this);
    
    // Resetting player channels as if they logged in:
    Group group = new ServerGroup(this.getServer());
    Action action = (Player player) -> channel_manager.loadPlayer(player);
    group.performAction(action);
    
    // Command Handling:
    SamChannelsInterface sc_interface = this.getInterface();
    CommandHandler command_handler = new CommandHandler(this);
    command_handler.registerCommand(new CommandChannels("channel", command_handler));
    command_handler.registerCommand(new CommandChat("channel chat", sc_interface));
    command_handler.registerCommand(new CommandListen("channel listen", sc_interface));
	}
	
  //Disabling
	public void onDisable()
	{
	  // Server Log Message
		PluginDescriptionFile pdf = this.getDescription();
		SamChannels.logger.info(pdf.getName() + " has been disabled");
	}
	
	
	
	public SamChannelsInterface getInterface()
	{
	  return new SamChannelsInterface(this, channel_manager);
	}
}
