package net.samongi.SamChannels.Channels;

import java.util.List;

import org.bukkit.entity.Player;

public interface Subscribable
{
  /**Checks to see if the player is subscribed to this object
   * 
   * @param player The player to check if they are subscribed.
   * @return True if they are subscribed.
   */
  public boolean isSubscribed(Player player);
  
  /**Subscribes the player to the channel (If they are already subscribed, nothing happens)
   * 
   * @param player The player to subscribe
   */
  public boolean subscribe(Player player);
  
  /**Unsubscribes the player to the channel (If they are already unsubscribed, nothing happens)
   * 
   * @param player The player to unsubscribe
   */
  public boolean unsubscribe(Player player);
  
  /**Gets a list of all the subscribed players to the channel.
   * 
   * @return The list of subscribed players.
   */
  public List<Player> getSubscribedPlayers();
  
  /**Gets a list of all the unsubscribed players to the channel.
   * This really only has relevance if the channel is a SERVER channel and can have off-state players.
   * 
   * @return The list of unsubsribed players.
   */
  public List<Player> getUnsubscribedPlayers();
}
