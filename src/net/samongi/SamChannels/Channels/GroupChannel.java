package net.samongi.SamChannels.Channels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.samongi.SamChannels.Parser.ChannelConfiguration;
import net.samongi.SamChannels.Titles.TitleManager;
import net.samongi.SamongiLib.Player.Group;
import net.samongi.SamongiLib.Player.Group.Action;
import net.samongi.SamongiLib.Player.Group.Comparer;

import org.bukkit.entity.Player;


/**Channels contain a list of players to route messages sent to it to.
 * 
 * @author Alex
 *
 */
public class GroupChannel implements Channel, Subscribable
{
	private final Group group;
	private final Group subscribed_group;
	private final Map<String, Boolean> subscriptions = new HashMap<>();
	private final ChannelConfiguration config;
	
	public GroupChannel(Group group, ChannelConfiguration configuration)
	{
		this.group = group;
		Comparer comp = (Player player) -> this.isSubscribed(player);
		this.subscribed_group = group.getSubSetGroup(comp);
		
		this.config = configuration;
	}
	
	@Override
  public void sendMessage(String message)
  {
	  Action send_message = (Player player) -> player.sendMessage(message);
	  subscribed_group.performAction(send_message);
  }
	
	@Override
  public void sendMessage(Player player, String message){this.sendMessage(message);}
	
  @Override
  public void sendMessage(Player player, String message, String formatting, TitleManager title_manager){this.sendMessage(message);}
	
	@Override
  public List<Player> getRecipients(){return group.getPlayers();}
	
	@Override
  public Map<String, Channel> getSubChannels(){return null;}

  @Override
  public boolean handlesFormatting(){return false;}

  @Override
  public ChannelConfiguration getConfiguration(){return this.config;}

  @Override
  public boolean isSubscribed(Player player)
  {
    if(!subscriptions.containsKey(player.getName())) return false;
    return subscriptions.get(player.getName());
  }

  @Override
  public boolean subscribe(Player player)
  {
    subscriptions.put(player.getName(), true);
    return true;
  }

  @Override
  public boolean unsubscribe(Player player)
  {
    subscriptions.remove(player.getName());
    return true;
  }

  @Override
  public List<Player> getSubscribedPlayers(){return subscribed_group.getPlayers();}

  @Override
  public List<Player> getUnsubscribedPlayers(){return group.getSubSetGroup((Comparer)(Player player) -> !this.isSubscribed(player)).getPlayers();}
  
	
	
}
