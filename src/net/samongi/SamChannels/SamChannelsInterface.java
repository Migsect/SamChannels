package net.samongi.SamChannels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.samongi.SamChannels.Channels.Channel;
import net.samongi.SamChannels.Channels.ChannelManager;
import net.samongi.SamChannels.Channels.Subscribable;

/**Used internally and by thrid-party plugins to interact with the plugin.
 * 
 * @author Alex
 *
 */
public class SamChannelsInterface
{
  @SuppressWarnings("unused")
  private SamChannels plugin;
  private ChannelManager manager;
  
  public SamChannelsInterface(SamChannels plugin, ChannelManager manager)
  {
    this.plugin = plugin;
    this.manager = manager;
  }
  
  /**Subscribes the player to the channel.
   * Can fail if the player does not:
   *  - Have permission to listen to the channel.  
   *  - Has permission to create new custom channels.
   *  
   * Will not tell the player that they are subscribed to the channel.
   *  
   * @param player The player to set the channel to.
   * @param channel The name of the channel to have the player subscribe to.
   * @return Returns false if the channel couldn't be connected to by the player.
   */
  public boolean subscribeToChannel(Player player, String channel){return this.subscribeToChannel(player, channel, false);}
  
  /**Subscribes the player to the channel.
   * Can fail if the player does not:
   *  - Have permission to listen to the channel.  
   *  - Has permission to create new custom channels.
   *  
   * @param player The player to set the channel to.
   * @param channel The name of the channel to have the player subscribe to.
   * @param do_mesage Will tell the player that they switched channels.
   * @return Returns false if the channel couldn't be connected to by the player.
   */
  public boolean subscribeToChannel(Player player, String channel, boolean do_message)
  {
    // Check if the player has permission to listen to this channel
    Channel chan = this.getChannel(channel);
    String listen_perm = chan.getConfiguration().getPermissionListen();
    if(listen_perm.length() != 0 && !player.hasPermission(listen_perm)) return false;
    
    manager.setPlayerListenChannel(player, channel, true);
    
    if(do_message) player.sendMessage(ChatColor.YELLOW + "You are now listening to the " + chan.getConfiguration().getChannelDisplayName() + ChatColor.YELLOW + " Channel");
      
    return true;
  }
  
  /**Unsubscribes the player from the channel
   * 
   * @param player The player to unsubscribe
   * @param channel The channel name
   * @return False if it failed (it shouldn't)
   */
  public boolean unsubscribeToChannel(Player player, String channel){return this.unsubscribeToChannel(player, channel, false);}
  
  /**Unsubscribes the player from the channel
   * 
   * This will not tell the player that they have be unsubscribed.
   * 
   * @param player The player to unsubscribe
   * @param channel The channel name
   * @param do_mesage Will tell the player that they switched channels.
   * @return False if it failed (it shouldn't)
   */
  public boolean unsubscribeToChannel(Player player, String channel, boolean do_message)
  {
    manager.setPlayerListenChannel(player, channel, false);
    
    if(do_message) player.sendMessage(ChatColor.YELLOW + "You are no longer listening to the " + this.getChannel(channel).getConfiguration().getChannelDisplayName() + ChatColor.YELLOW + " Channel");
    
    return true;
  }
  
  /**Sets the player to chat to the channel with the listed name if they have permission.
   * Will also subscribe the player to the channel if the channel is subscribable and they are not already subscribed. 
   * If the player is a temporary on their current channel, this will unlisten them from that channel.
   * Can fail if the player does not:
   *  - Have permission to listen AND chat on the channel.
   *  - Has permission to create new custom channels.
   * This will not send a message to the player detailing that they switched channels.
   * 
   * @param player The player to switch the chat to.
   * @param channel The channel the player is switching to.
   * @return Returns false if the channel could not be chatted to by the player.
   */
  public boolean chatToChannel(Player player, String channel_name){return this.chatToChannel(player, channel_name, false);}
  /**Sets the player to chat to the channel with the listed name if they have permission.
   * Will also subscribe the player to the channel if the channel is subscribable and they are not already subscribed. 
   * If the player is a temporary on their current channel, this will unlisten them from that channel.
   * Can fail if the player does not:
   *  - Have permission to listen AND chat on the channel.
   *  - Has permission to create new custom channels.
   * 
   * @param player The player to switch the chat to.
   * @param channel The channel the player is switching to.
   * @param do_mesage Will tell the player that they switched channels.
   * @return Returns false if the channel could not be chatted to by the player.
   */
  public boolean chatToChannel(Player player, String channel_name, boolean do_message)
  {
    // When you chat to a channel (switch to it), you also have to listen to it.
    boolean status = this.subscribeToChannel(player, channel_name);
    if(status == false) return status;
    
    Channel channel = manager.getChannel(channel_name);
    String chat_permission = channel.getConfiguration().getPermissionChat();
    if(chat_permission.length() != 0 && !player.hasPermission(chat_permission)) return false;
    manager.changePlayerChannelRoute(player, channel_name);
    
    if(do_message) player.sendMessage(ChatColor.YELLOW + "You are now chatting to the " + channel.getConfiguration().getChannelDisplayName() + ChatColor.YELLOW + " Channel");
    
    return true;
  }
  
  /**Gets all the channnels names currently registered.
   * 
   * @return A list of all the registered channels.
   */
  public List<String> getChannels(){return manager.getChannels();}
  
  /**Gets all the channels the player is currently subscribed to
   * Note that if the channel is not subscribable, then it is automatically included.
   * 
   * @param player The player to get the channels subscribed to
   * @return A list of all the channels the player is subscribed to.
   */
  public List<String> getChannels(Player player)
  {
    List<String> channels = this.getChannels();
    List<String> return_channels = new ArrayList<>();
    for(String c : channels)
    {
      Channel channel = this.getChannel(c);
      // If the channel is not subscribable, it will be autoadded
      if(!(channel instanceof Subscribable))
      {
        return_channels.add(c);
        continue;
      }
      Subscribable s = (Subscribable) channel;
      if(s.isSubscribed(player)) return_channels.add(c);
    }
    return return_channels;
  }
  
  /**Gets the current channel this player is chatting to.
   * 
   * @param player The player to get the channel of
   * @return The Channel the player is currently chatting to.
   */
  public String getChatChannel(Player player){return this.manager.getPlayerRoute(player);}
  
  /**Gets the channel with the specified name
   * 
   * @param channel The name of the channel.
   * @return The channel object. Null if it doesn't exist.
   */
  public Channel getChannel(String channel){return manager.getChannel(channel);}
  
  /**Sends a message to the channel to be broadcast to all players.
   * 
   * @param channel_name The name of the channel to broadcast to
   * @param message The message to send.
   */
  public void messageChannel(String channel_name, String message){manager.routeMessage(channel_name, message);}
  
  /**Sends a message to the channel as if the player was simply chatting with it.
   * Returns false if it fails to do so (player can't create channels, or send messages to)
   * 
   * @param channel_name The name of the channel to send the message to
   * @param player The player that is sending the message
   * @param message The message
   * @return
   */
  public boolean messageChannel(String channel_name, Player player, String message)
  {
    // Subscribing the player to the channel
    boolean status = this.subscribeToChannel(player, channel_name);
    if(status == false) return status;
    
    // Getting the permission
    Channel channel = manager.getChannel(channel_name);
    String chat_permission = channel.getConfiguration().getPermissionChat();
    if(chat_permission.length() != 0 && !player.hasPermission(chat_permission)) return false;
    
    // Sending the message
    manager.routeMessage(channel_name, player, message);
    return true;
  }
  
  /**Registers the channel to be used.
   * 
   * @param channel The channel to register
   * @return If the channel successfully registered.
   */
  public boolean registerNewChannel(Channel channel)
  {
    return manager.registerChannel(channel);
  }
}
