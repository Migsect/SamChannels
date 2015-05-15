package net.samongi.SamChannels.Channels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.samongi.SamChannels.SamChannels;
import net.samongi.SamChannels.SamChannelsInterface;
import net.samongi.SamChannels.Parser.ChannelConfiguration;
import net.samongi.SamChannels.Titles.TitleManager;
import net.samongi.SamongiLib.Configuration.ConfigAccessor;
import net.samongi.SamongiLib.Player.Group;
import net.samongi.SamongiLib.Player.ServerGroup;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**Used to store channels and their information.  
 *   Works as a data-object that wraps a hashmap of channel names.
 *   Handles async access to the channels, allowing multiple reads at a time while only allowing one write.
 *   Writes occur when adding new channels or adding a player to a static channel.
 *   Reads occur when getting the player list a channel represents.
 * @author Migsect
 *
 */
public class ChannelManager
{
  private final SamChannels plugin;
  
  private String default_route;
  
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock read_lock = lock.readLock();
	private final Lock write_lock = lock.writeLock();
	
	private final Map<String, Channel> channels = new HashMap<>(); //  should all be referenced by lowercase
	@SuppressWarnings("unused")
  private final Map<String, Channel> temp_channels = new HashMap<>();
	private final Map<String, String> player_routes = new HashMap<>(); // Player Name -> Channel Name
	private final List<String> default_listens = new ArrayList<>(); // list of all the channels that are default listens
	
	private final TitleManager title_manager;
	
	/* formats can differ depending on channels or fall to the default fo rmatting.
	 *  Variables are used within the strings in tags:
	 *   #{channel}					: The channel being used
	 *   #{player}					:	The player's true name
	 *   #{player_display}	: The player's display_name
	 *   #{time}   					: The server time
	 *   #{world_time}			: The time of the world the player is in
	 *   #{world}						: The world the player is in.
	 *   #{server}					: The server the player is in.
	 *   #{suffix}		:	The player's group suffix (no support yet)
	 *   #{preffix}   : The player's group preffix (no support yet)
	 */
	private String default_format = "[#{channel}] #{prefix}#{player_display}#{suffix} : #{message}"; // Can be changed by configuration.
	
	public ChannelManager(SamChannels plugin, TitleManager title_manager)
	{
    this.plugin = plugin;
    this.title_manager = title_manager;
	}
	
	/**Registers a channel with the Channel Manager.
	 * Write Locks.
	 *  
	 * This will return false and NOT register the channel if the channel already exists.
	 * 
	 * @param channel The channel to register.
	 * @return False if the channel already exists. (Overwrite protecting)
	 */
	public boolean registerChannel(Channel channel)
	{
		this.write_lock.lock();
		boolean ret = true;
		try
		{
			// We check to see if the channels contains the key.  If it does, we need to return false.
			//   We do not want to return right not because we need to unlock.
		  if(SamChannels.debug) SamChannels.logger.info("  ChannelManager: Attempting to register channel '" + channel.getConfiguration().getChannelName() + "'");
			if(channels.containsKey(channel.getConfiguration().getChannelName().toLowerCase())) 
			{
				ret = false;
			}
			else
			{
				this.channels.put(channel.getConfiguration().getChannelName().toLowerCase(), channel);
				if(channel.getConfiguration().getDefaultListen())
				{
				  default_listens.add(channel.getConfiguration().getChannelName().toLowerCase());
				}
			}
		} 
		finally 
		{
			// Unlock writing.
			this.write_lock.unlock();
		}
		return ret;
	}
	
	/**Registers channels based on a configuration file. (see channels.yml)
	 * 
	 * @param config The config to parse and register channels with.
	 */
	public void registerConfigChannels(ConfigAccessor config)
  {
    // grabbing all the keys of each individual channel.  These keys have no configuration value.
    List<String> channel_keys = new ArrayList<>();
    channel_keys.addAll(config.getConfig().getConfigurationSection("channels").getKeys(false));
    for(String key : channel_keys)
    {
      ChannelConfiguration c_config = new ChannelConfiguration(config, key);
      Channel channel = null;
      Group server_group = new ServerGroup(plugin.getServer());
      switch (c_config.getChannelType().toUpperCase())
      {
        case "SERVER":
          channel = new GroupChannel(server_group, c_config);
          break;
        case "RANGE":
          channel = new RangeChannel(server_group, c_config);
          break;
      }
      if(channel == null) continue;
      this.registerChannel(channel);
    }
  }
	
	/**Deregisters the channel with the specified name.  Returns false if the channel does not exist.
	 * 
	 * @param channel The channel to try to remove.
	 * @return False if the channel does not exist. (Can be ignored, extra functionality)
	 */
	public boolean deregisterChannel(String channel)
	{
		this.write_lock.lock();
		boolean ret = true;
		try
		{
		  if(SamChannels.debug) SamChannels.logger.info("  ChannelManager: Attempting to deregister channel - " + channel.toLowerCase());
			if(!channels.containsKey(channel.toLowerCase())) 
			{
				ret = false;
			}
			else
			{
				this.channels.remove(channel.toLowerCase());
			}
		} 
		finally 
		{
			// Unlock writing.
			this.write_lock.unlock();
		}
		return ret;
	}
	
	/**Checks to see if a channel exists.
	 * 
	 * @param channel The channel name to check for.
	 * @return True if the channel is registered.
	 */
	public boolean containsChannel(String channel)
	{
		this.read_lock.lock();
		boolean ret = false;
		try
		{
			ret = channels.containsKey(channel.toLowerCase());
		}
		finally
		{
			// Unlock reading.
			this.read_lock.unlock();
		}
		return ret;
	}

	/**Routes the message to the player's currently set channel
	 * 
	 * @param player The player who sent the message
	 * @param message The (unmodified) message being sent
	 */
	public void routeMessage(Player player, String message)
	{
	  this.read_lock.lock();
    try
    {
      routeMessage(player_routes.get(player.getName()), player, message);
    }
    finally
    {
      this.read_lock.unlock();
    }
	}
	
	/**Routes the message to the listed channel.
	 * 
	 * @param chan The channel to send the message to
	 * @param player The player sending the message.
	 * @param message The message
	 */
	public void routeMessage(String chan, Player player, String message)
	{
	  String message_to_send = message;
    this.read_lock.lock();
    try
    {
      Channel channel = channels.get(chan.toLowerCase());
      if(SamChannels.debug) SamChannels.logger.info("  ChannelManager: Routing \""+ message_to_send +"\" for " + player.getName() + " to to channel '" + channel.getConfiguration().getChannelName() + "'");
      
      // Getting the formatting string
      String formatting_string = default_format;
      if(channel.getConfiguration().getFormat().length() != 0) formatting_string = channel.getConfiguration().getFormat();
      if(SamChannels.debug) SamChannels.logger.info("  ChannelManager: Using formating '" + formatting_string + "'"); 
      
      // This will format the message being sent by the player based on their permissions.
      if(channel.getConfiguration().getAllowChatCodes()) message_to_send = paintColors(channel, player, message_to_send);
      
      // The two routes, if the channel wants to it's own formatting let it do it.
      if(channel.handlesFormatting()) channel.sendMessage(player, message_to_send, formatting_string, title_manager);
      else channel.sendMessage(player, formatMessage(channel, player, message_to_send, formatting_string));
    }
    finally
    {
      this.read_lock.unlock();
    }
	}
	
	/**Sends an direct message to the channel with no formatting.
	 * 
	 * @param channel The channel
	 * @param message The message
	 */
	public void routeMessage(String chan, String message)
	{
	  this.read_lock.lock();
    try
    {
  	  Channel channel = channels.get(chan.toLowerCase());
  	  if(SamChannels.debug) SamChannels.logger.info("  ChannelManager: Routing \""+ message +"\" for to to channel \"" + channel.getConfiguration().getChannelName() + "\"");
  	  channel.sendMessage(message);
    }
    finally
    {
      this.read_lock.unlock();
    }
	}
	
	/**Formats the message 
	 * Variables are used within the strings in tags:
	 *   #{channel}					: The channel being used
	 *   #{player}					:	The player's true name
	 *   #{player_display}	: The player's display_name
	 *   #{time}   					: The server time
	 *   #{world_time}			: The time of the world the player is in
	 *   #{world}						: The world the player is in.
	 *   #{server}					: The server the player is in.
	 *   #{suffix}		      : Uses titles to determine the proper suffix
	 *   #{preffix}         : Uses titles to determine the proper prefix
	 * @param channel The name of the channel being used
	 * @param player The player
	 * @param message The raw message
	 * @return
	 */
	private String formatMessage(Channel channel, Player player, String message, String formatting_string)
	{
		String return_message = formatting_string;
		
		// return_message = return_message.replace("#{channel}", channel.toLowerCase().substring(0,1).toUpperCase() +channel.toLowerCase().substring(1));
		return_message = return_message.replace("#{channel}", channel.getConfiguration().getChannelDisplayName()); // No formatting
		return_message = return_message.replace("#{player}", player.getName());
		return_message = return_message.replace("#{player_display}", player.getDisplayName());
		return_message = return_message.replace("#{time}", new SimpleDateFormat("h.mm.ss").format(new Date()));
		return_message = return_message.replace("#{world_time}", "" + player.getWorld().getTime()); // Needs to be fixed to be more like real time. #lazy
		return_message = return_message.replace("#{world}", player.getWorld().getName());
		return_message = return_message.replace("#{server}", player.getServer().getName());
		return_message = return_message.replace("#{message}", message);
		return_message = return_message.replace("#{prefix}", title_manager.getPlayerPrefix(player, channel));
		return_message = return_message.replace("#{suffix}", title_manager.getPlayerSuffix(player, channel));
		
		return return_message;
	}
	 
	
	/**Used to paint colors for the message actually sent by the player.
	 * Replaces '&' based color codes if the player has the permission to have formatting.
	 * 
	 * @param channel The channel the player is sending the message to.
	 * @param player The player sending the message
	 * @param message The message
	 * @return A string formatting depending on if the player had the right permissions
	 */
	private static String paintColors(Channel channel, Player player, String message)
	{
	  String return_message = message;
	  
	  String color_perm = channel.getConfiguration().getPermissionChatColor();
	  String bold_perm = channel.getConfiguration().getPermissionChatBold();
	  String underline_perm = channel.getConfiguration().getPermissionChatUnderline();
	  String italic_perm = channel.getConfiguration().getPermissionChatItalic();
	  String stikethrough_perm = channel.getConfiguration().getPermissionStrikethrough();
	  String magic_perm = channel.getConfiguration().getPermissionChatMagic();
	  
	  // If there 'ins't' a permission (empty string) or the player has the permission.
	  if(color_perm.length() == 0 || player.hasPermission(color_perm))
	  {
	    return_message = return_message.replace("&0", "" + ChatColor.BLACK);
	    return_message = return_message.replace("&1", "" + ChatColor.DARK_BLUE);
	    return_message = return_message.replace("&2", "" + ChatColor.DARK_GREEN);
	    return_message = return_message.replace("&3", "" + ChatColor.DARK_AQUA);
	    return_message = return_message.replace("&4", "" + ChatColor.DARK_RED);
	    return_message = return_message.replace("&5", "" + ChatColor.DARK_PURPLE);
	    return_message = return_message.replace("&6", "" + ChatColor.GOLD);
	    return_message = return_message.replace("&7", "" + ChatColor.GRAY);
	    return_message = return_message.replace("&8", "" + ChatColor.DARK_GRAY);
	    return_message = return_message.replace("&9", "" + ChatColor.BLUE);
	    return_message = return_message.replace("&a", "" + ChatColor.GREEN);
	    return_message = return_message.replace("&b", "" + ChatColor.AQUA);
	    return_message = return_message.replace("&c", "" + ChatColor.RED);
	    return_message = return_message.replace("&d", "" + ChatColor.LIGHT_PURPLE);
	    return_message = return_message.replace("&e", "" + ChatColor.YELLOW);
	    return_message = return_message.replace("&f", "" + ChatColor.WHITE);
	    
	    return_message = return_message.replace("&p", "" + ChatColor.RESET);
	  }
	  if(bold_perm.length() == 0 || player.hasPermission(bold_perm))
	  {
	    return_message = return_message.replace("&l", "" + ChatColor.BOLD);
	    
	    return_message = return_message.replace("&p", "" + ChatColor.RESET);
	  }
	  if(underline_perm.length() == 0 || player.hasPermission(underline_perm))
    {
	    return_message = return_message.replace("&n", "" + ChatColor.UNDERLINE);
	    
	    return_message = return_message.replace("&p", "" + ChatColor.RESET);
    }
	  if(italic_perm.length() == 0 || player.hasPermission(italic_perm))
    {
	    return_message = return_message.replace("&o", "" + ChatColor.ITALIC);
	    
	    return_message = return_message.replace("&p", "" + ChatColor.RESET);
    }
	  if(stikethrough_perm.length() == 0 || player.hasPermission(stikethrough_perm))
	  {
	    return_message = return_message.replace("&m", "" + ChatColor.STRIKETHROUGH);
	    
	    return_message = return_message.replace("&p", "" + ChatColor.RESET);
	  }
	  if(magic_perm.length() == 0 || player.hasPermission(magic_perm))
    {
	    return_message = return_message.replace("&k", "" + ChatColor.MAGIC);
	    
	    return_message = return_message.replace("&p", "" + ChatColor.RESET);
    }
	  
	  return return_message;
	}
	
	/**Sets the player's current channel they are speaking to
	 * 
	 * @param player The player
	 * @param to_channel The channel being switched to.
	 */
	public void changePlayerChannelRoute(Player player, String to_channel)
	{
	  this.write_lock.lock();
    try
    {
      // We check to see if the channels contains the key.  If it does, we need to return false.
      //   We do not want to return right not because we need to unlock.
      if(SamChannels.debug) SamChannels.logger.info("  ChannelManager: Setting " + player.getName() + "'s channel to " + to_channel);
      player_routes.put(player.getName(), to_channel);
    } 
    finally 
    {
      // Unlock writing.
      this.write_lock.unlock();
    }
	}
	
	/**Adds the channel such that the player listens to it.
   * 
   * @param player The player
   * @param channel The channel being added as listening.
   */
	public boolean setPlayerListenChannel(Player player, String channel, boolean state)
	{
	  boolean ret_state = false;
	  this.write_lock.lock();
    try
    {
      if(channels.containsKey(channel.toLowerCase()))
      {
        // Get the channel.  We cannot use other methods because of locks.
        Channel chan = channels.get(channel.toLowerCase());
        if(chan instanceof Subscribable)
        {
          Subscribable sub = (Subscribable) chan;
          if(state) ret_state = sub.subscribe(player);
          else ret_state = sub.unsubscribe(player);
        }
      }
    } 
    finally 
    {
      // Unlock writing.
      this.write_lock.unlock();
    }
    return ret_state;
	}
	
	/**Sets the default channel that a player will be set to when they first login.
	 * 
	 * @param channel The channel to set as he default route.
	 */
	public void setDefaultRoute(String channel)
	{
	  this.write_lock.lock();
    try
    {
      if(SamChannels.debug) SamChannels.logger.info("  ChannelManager: Setting default channel route: " + channel.toLowerCase());
      this.default_route = channel.toLowerCase();
    } 
    finally 
    {
      // Unlock writing.
      this.write_lock.unlock();
    }
	}
	
	/**Returns the default channel a player will be set to chat to when they logon.
	 * 
	 * @return The default channel
	 */
	public String getDefaultRoute()
	{
	  String return_route;
	  this.read_lock.lock();
    try
    {
      return_route = this.default_route;
    } 
    finally 
    {
      // Unlock writing.
      this.read_lock.unlock();
    }
    return return_route;
	}
	
	/**Returns a list of the channels a player will be subscribed to on login if the channels are subscribable.
	 * 
	 * @return The list of channels.
	 */
	public List<String> getDefaultListens()
	{
	  List<String> return_listens = null;
	  this.read_lock.lock();
    try
    {
      return_listens = this.default_listens;
    } 
    finally 
    {
      // Unlock writing.
      this.read_lock.unlock();
    }
    return return_listens;
	}
	
	/**Returns the channel specified.
	 * 
	 * @param channel The name of the channel
	 * @return The channel
	 */
	public Channel getChannel(String channel)
	{
	  Channel return_channel = null;
	  this.read_lock.lock();
    try
    {
      return_channel = channels.get(channel.toLowerCase());
    } 
    finally 
    {
      // Unlock writing.
      this.read_lock.unlock();
    }
    return return_channel;
	}
	
	/**Gets all the channels by name
	 * 
	 * @return A list of all the channels
	 */
	public List<String> getChannels()
	{
	  List<String> return_channels = new ArrayList<>();
    this.read_lock.lock();
    try
    {
      return_channels.addAll(channels.keySet());
    } 
    finally 
    {
      // Unlock writing.
      this.read_lock.unlock();
    }
    return return_channels;
	}
	
	/**Gets the channel the player is currently chatting to.
	 * 
	 * @param player The player to find the channel of
	 * @return Name of the channel the player is chatting to
	 */
	public String getPlayerRoute(Player player)
	{
	  String route = "";
	  this.read_lock.lock();
    try
    {
      // The route doesn't exist, then we need to correct it so it does.
      if(!this.player_routes.containsKey(player.getName())) route = "";
      else route = this.player_routes.get(player.getName());
    } 
    finally 
    {
      // Unlock writing.
      this.read_lock.unlock();
    }
    // Well we need a route and correct this embarrishing happening.
    if(route.equals(""))
    {
      this.changePlayerChannelRoute(player, this.default_route);
      return this.default_route;
    }
    else return route;
	}
	
	/**Will set the player's base channels (listen and chat)
	 * Will all handle all saved information about the player.
	 * @param player The player to load
	 */
	public void loadPlayer(Player player)
	{
	  // FOR NOW this just sets the player to the defaults
	  SamChannelsInterface sc_interface = new SamChannelsInterface(plugin, this);
	  // Setting the default chat channels.
	  sc_interface.chatToChannel(player, this.getDefaultRoute(), true);
    for(String s : this.default_listens)
    {
      sc_interface.subscribeToChannel(player, s, true);
    }
	}
	/**Will save all player's information (such as channel and any subscriptions)
	 * 
	 * @param player The player to save
	 */
	public void savePlayer(Player player)
	{
	  
	}
}
