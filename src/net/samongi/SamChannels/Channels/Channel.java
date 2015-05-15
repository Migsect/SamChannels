package net.samongi.SamChannels.Channels;

import java.util.List;
import java.util.Map;

import net.samongi.SamChannels.Parser.ChannelConfiguration;
import net.samongi.SamChannels.Titles.TitleManager;

import org.bukkit.entity.Player;

public interface Channel
{
	/**Sends a message to this channel
	 * 
	 * @param message The message being sent.
	 */
	public void sendMessage(String message);
	
	/**Sends a message to the channel with the additional knowledge of the player sending it.
	 * The message should not be edited based on the player, this is only used for routing.
	 * 
	 * @param player The player that initiated the message.
	 * @param message The message being sent.
	 */
	public void sendMessage(Player player, String message);
	
	/**Sends a message to the channel with the additional knowledge of the player sending it
	 * as well as the formatting that is expected for custom formatting.
	 * 
	 * @param player The player that initiated the message.
	 * @param message The message being sent.
	 * @param formatting A formatting string
	 */
	public void sendMessage(Player player, String message, String formatting, TitleManager title_manager);
	
	/**Gets a list of players that the message could be sent to.
	 * 
	 * @return A list of players the message would be sent to (if they are online)
	 */
	public List<Player> getRecipients();
	
	/**Gets the list of sub-channels that this channel contains (if any)
	 * 
	 * @return A map of strings to channels.  Should return null if the channel has no sub-channels.
	 */
	public Map<String, Channel> getSubChannels();
	
	/**Returns whether or not the channel wishes to handle its own formatting or have the ChannelManager handle it.
	 * 
	 * @return True if it wants to handle its formatting.
	 */
	public boolean handlesFormatting();
	
	/**Gets the configuration of the channel which includes more information on how the channel operates.
	 * As well as supplying information on its type, name, and formatting.
	 * 
	 * @return A ChannelConfiguration Object
	 */
	public ChannelConfiguration getConfiguration();
	
}
