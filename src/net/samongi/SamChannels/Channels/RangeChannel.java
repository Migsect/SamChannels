package net.samongi.SamChannels.Channels;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.samongi.SamChannels.SamChannels;
import net.samongi.SamChannels.Parser.ChannelConfiguration;
import net.samongi.SamChannels.Titles.TitleManager;
import net.samongi.SamongiLib.Player.Group;
import net.samongi.SamongiLib.Player.Group.Action;
import net.samongi.SamongiLib.Player.Group.Comparer;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**A channel that controls if a message is sent to a subscriber based off proximity.
 * 
 * @author Alex
 *
 */
public class RangeChannel implements Channel, Subscribable
{
  private final Group group;
  private final Group subscribed_group;
  private final Map<String, Boolean> subscriptions = new HashMap<>();
  private final ChannelConfiguration config;
	
	// corruption will replace characters of the messanger's name and their message with corrupted letters
	//   based on a probability between the ranges.
	double corrupt_max_range; 
	double corrupt_min_range; 
	// silence will replace characters of the messanger's name and their message with spaces based on
	//   a probability based on the ranges.
	double silence_max_range;
	double silence_min_range;
	
	public RangeChannel(Group group, ChannelConfiguration config,
	    int corrupt_min_range, int corrupt_max_range, 
	    int silence_min_range, int silence_max_range)
	{
	  this.group = group;
    Comparer comp = (Player player) -> this.isSubscribed(player);
    this.subscribed_group = group.getSubSetGroup(comp);
    
    this.config = config;
		
		this.corrupt_max_range = corrupt_max_range;
		this.corrupt_min_range = corrupt_min_range;
		if(this.corrupt_min_range > this.corrupt_max_range) this.corrupt_min_range = this.corrupt_max_range;
		this.silence_max_range = silence_max_range;
    this.silence_min_range = silence_min_range;
    if(this.silence_min_range > this.silence_max_range) this.silence_min_range = this.silence_max_range;
	}
	
	public RangeChannel(Group group, ChannelConfiguration config, int corrupt_max_range, int silence_max_range)
	{
	  this.group = group;
    Comparer comp = (Player player) -> this.isSubscribed(player);
    this.subscribed_group = group.getSubSetGroup(comp);
    
    this.config = config;
    
    this.corrupt_max_range = corrupt_max_range;
    this.corrupt_min_range = corrupt_max_range;
    this.silence_max_range = silence_max_range;
    this.silence_min_range = silence_max_range;
	}
	
	public RangeChannel(Group group, ChannelConfiguration config)
	{
	  this.group = group;
    Comparer comp = (Player player) -> this.isSubscribed(player);
    this.subscribed_group = group.getSubSetGroup(comp);
    
    this.config = config;
    
    this.corrupt_max_range = this.config.getDoubleFromConfig("corruption.max");
    this.corrupt_min_range = this.config.getDoubleFromConfig("corruption.min");
    this.silence_max_range = this.config.getDoubleFromConfig("silencing.max");
    this.silence_min_range = this.config.getDoubleFromConfig("silencing.min");
    if(SamChannels.debug) SamChannels.logger.info("    RangeChannel: min-max Corruption: [" + this.corrupt_min_range + " - " + this.corrupt_max_range+ "]");
    if(SamChannels.debug) SamChannels.logger.info("    RangeChannel: min-max Silence: [" + this.silence_min_range + " - " + this.silence_max_range+ "]");
	}
	
	@Override
  public void sendMessage(String message)
  {
	  Action send_message = (Player player) -> player.sendMessage(message);
	  group.performAction(send_message);
  }

	@Override
  public void sendMessage(Player player, String message)
  {
	  // Do not do anything.  Reports to server if this is used
	  if(SamChannels.debug) SamChannels.logger.info("  WARNING: sendMessage Method used that shouldn't be from a RangeChannel");
  }
	
  @Override
  public void sendMessage(Player player, String message, String formatting_string, TitleManager title_manager)
  {
    List<Player> players = group.getPlayers();
    Location s_loc = player.getLocation().clone();
    // Maps to store probability data.

    double adj_corrupt_max = Math.pow(corrupt_max_range,2);
    double adj_corrupt_min = Math.pow(corrupt_min_range,2);
    double adj_silence_max = Math.pow(silence_max_range,2);
    double adj_silence_min = Math.pow(silence_min_range,2);
    
    for(Player p : players)
    {
      Location p_loc = p.getLocation().clone();
      double adj_dist = 
          Math.pow(p_loc.getX() - s_loc.getX(),2) +
          Math.pow(p_loc.getY() - s_loc.getY(),2) +
          Math.pow(p_loc.getZ() - s_loc.getZ(),2);
      
      // Calculate Silence Prob and put it into map
      double silence_prob = 0;
      if(adj_dist > adj_silence_max) silence_prob = 1;
      else if(adj_dist < adj_silence_min) silence_prob = 0;
      else silence_prob = (adj_dist - adj_silence_min) / (adj_silence_max - adj_silence_min);
      
      if(silence_prob >= 1) continue; // If silence is always, no sense in sending the message.
      
      // Calculate Corrupt Prob and put it into map
      double corrupt_prob = 0;
      if(adj_dist > adj_corrupt_max) corrupt_prob = 1;
      else if(adj_dist < adj_corrupt_min) corrupt_prob = 0;
      else corrupt_prob = (adj_dist - adj_corrupt_min) / (adj_corrupt_max - adj_corrupt_min);
      
      if(SamChannels.debug) SamChannels.logger.info("  Silence Prob : " + silence_prob);
      if(SamChannels.debug) SamChannels.logger.info("  Corrupt Prob : " + corrupt_prob);
      
      String used_name = player.getName();
      String used_name_display = player.getDisplayName();
      String used_message = message;
      String used_prefix = title_manager.getPlayerPrefix(player, this);
      String used_suffix = title_manager.getPlayerSuffix(player, this);
      if(corrupt_prob > 0)
      {
        used_name = corruptString(player.getName(), corrupt_prob);
        used_name_display = corruptString(player.getDisplayName(), corrupt_prob);
        used_message = corruptString(message, corrupt_prob);
        used_prefix = corruptString(used_prefix, corrupt_prob);
        used_suffix = corruptString(used_suffix, corrupt_prob);
      }
      if(silence_prob > 0)
      {
        used_name = silenceString(used_name, silence_prob);
        used_name_display = silenceString(used_name_display, silence_prob);
        used_message = silenceString(used_message, silence_prob);
        used_prefix = silenceString(used_prefix, silence_prob);
        used_suffix = silenceString(used_suffix, silence_prob);
      }
      
      String return_message = formatting_string;
      // return_message = return_message.replace("#{channel}", channel.toLowerCase().substring(0,1).toUpperCase() +channel.toLowerCase().substring(1));
      return_message = return_message.replace("#{channel}", this.config.getChannelName()); // No formatting
      return_message = return_message.replace("#{player}", used_name);
      return_message = return_message.replace("#{player_display}", used_name_display);
      return_message = return_message.replace("#{time}", new SimpleDateFormat("h.mm.ss").format(new Date()));
      return_message = return_message.replace("#{world_time}", "" + player.getWorld().getTime()); // Needs to be fixed to be more like real time. #lazy
      return_message = return_message.replace("#{world}", player.getWorld().getName());
      return_message = return_message.replace("#{server}", player.getServer().getName());
      return_message = return_message.replace("#{message}", used_message);
      return_message = return_message.replace("#{prefix}", used_prefix);
      return_message = return_message.replace("#{suffix}", used_suffix);
      
      p.sendMessage(return_message);
    }
  }
  
  /**Returns a corrupted string based on the probability.
   * 
   * @param string The string to corrupt.
   * @param prob The probability that a character of the string will be corrupted.
   * @return The corrupted string
   */
  private String corruptString(String string, double prob)
  {
    Random rand = new Random();
    String ret = "";
    char[] c_array = string.toCharArray();
    for(int i = 0; i < c_array.length; i++)
    {
      // if(SamChannels.debug) SamChannels.logger.info("  Compare Character : '" + (int)'§' + "' : '" + (int)c_array[i] + "'" );
      // if(SamChannels.debug) SamChannels.logger.info("                    : '" + '§' + "' : '" + c_array[i] + "'" );
      if(c_array[i] == '§'){
        ret = ret + c_array[i];
        ret = ret + c_array[i+1];
        i++;
        continue;
      }
      if(rand.nextDouble() < prob) ret = ret + getRandomCharacter();
      else ret = ret + c_array[i];
    }
    return ret;
  }
  
  /**Gets a random character
   * 
   * @return A random character (not just alphanumeric)
   */
  private char getRandomCharacter()
  {
    Random rand = new Random();
    // all characters are placed between 33 and 126, we'll take a random one
    int c = ' ' + rand.nextInt(94);
    return (char) c;
  }
  
  /**Returns a randomly silenced string based on the probability.
   * 
   * @param string The string to randomly silence.
   * @param prob The probability that a character of the string will be silenced.
   * @return The silenced string
   */
  private String silenceString(String string, double prob)
  {
    Random rand = new Random();
    String ret = "";
    char[] c_array = string.toCharArray();
    for(int i = 0; i < c_array.length; i++)
    {
      // if(SamChannels.debug) SamChannels.logger.info("  Compare Character : '" + (int)'§' + "' : '" + (int)c_array[i] + "'" );
      // if(SamChannels.debug) SamChannels.logger.info("                    : '" + '§' + "' : '" + c_array[i] + "'" );
      if(c_array[i] == '§'){
        ret = ret + c_array[i];
        ret = ret + c_array[i+1];
        i++; // Skip the next char
        continue;
      }
      if(rand.nextDouble() < prob) ret = ret + " ";
      else ret = ret + c_array[i];
    }
    return ret;
  }
  
	@Override
  public List<Player> getRecipients(){return group.getPlayers();}

	@Override
  public Map<String, Channel> getSubChannels()
  {
	  // TODO Auto-generated method stub
	  return null;
  }
	
  @Override
  public boolean handlesFormatting(){return true;}

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
