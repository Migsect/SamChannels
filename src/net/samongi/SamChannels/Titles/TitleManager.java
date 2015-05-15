package net.samongi.SamChannels.Titles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import net.samongi.SamChannels.SamChannels;
import net.samongi.SamChannels.Channels.Channel;
import net.samongi.SamongiLib.Configuration.ConfigAccessor;
import net.samongi.SamongiLib.Utilities.StringUtilities;

public class TitleManager
{
  Map<String, Track> tracks = new HashMap<>();
  
  public TitleManager(){}
  
  /**Registers all the titles in the named config file.
   * 
   * @param titles_config The configAccessor object that is based off the config file.
   */
  public void registerTitles(ConfigAccessor titles_config)
  {
    List<String> keys = new ArrayList<>();
    keys.addAll(titles_config.getConfig().getConfigurationSection("titles").getKeys(false));
    for(String key : keys)
    {
      // It should register itself with its track.
      new Title(titles_config, key, this);
    }
  }
  
  /**Registers the terack with the TitleManager
   * Overwrites any track already name the same.
   * 
   * @param track The track object to register.
   */
  public void registerTrack(Track track)
  {
    if(SamChannels.debug) SamChannels.logger.info("  Registered Track: '" + track.getName() + "'");
    tracks.put(track.getName().toLowerCase(), track);
  }
  
  /**Will check if the track exists
   * 
   * @param string The name of the track
   * @return True if it exists
   */
  public boolean hasTrack(String string){return this.tracks.containsKey(string.toLowerCase());}
  /**Checks if a track is registered
   * 
   * @param track A track object
   * @return True if it is registered
   */
  public boolean hasTrack(Track track){return this.tracks.containsKey(track.getName().toLowerCase());}
  /**Gets the track by name
   * 
   * @param string The name of the track to get
   * @return The track
   */
  public Track getTrack(String string){return this.tracks.get(string.toLowerCase());}
  
  /**Gets the player's defined prefix based off permissions and tracks as well as the channel being used.
   * 
   * @param player The player to base the prefix off of (permissions)
   * @param channel The channel to base the prefix off of (tracks)
   * @return
   */
  public String getPlayerPrefix(Player player, Channel channel)
  {
    // Creating a list of the tracks used by the channel.
    List<String> track_names = channel.getConfiguration().getTracks();
    List<Track> tracks = new ArrayList<>();
    for(String name : track_names)
    {
      // Check to see if the track actually exists
      if(!this.hasTrack(name))
      {
        if(SamChannels.debug) SamChannels.logger.warning("The non-existant track: '" + name + "' was specified in '" + channel.getConfiguration().getKey() + "' within the 'channels.yml' file.");
        continue;
      }
      // Add the track to the list.
      tracks.add(this.getTrack(name));
      if(SamChannels.debug) SamChannels.logger.info("  Getting Prefix : Track Added '" + this.getTrack(name).getName() + "'");
    }
    
    // Get the list of titles from each Track:
    List<Title> titles = new ArrayList<>();
    for(Track track : tracks) titles.addAll(track.getHighestPriorityTitles(player));
    
    // Get the list of titles in their order.
    Map<Integer, List<Title>> order_map = new HashMap<>();
    for(Title title : titles)
    {
      int order = title.getOrder();
      if(!order_map.containsKey(order)) order_map.put(order, new ArrayList<Title>());
      order_map.get(order).add(title);
      if(SamChannels.debug) SamChannels.logger.info("  Getting Prefix : Added Title '" + title.getPrefix() + "," + title.getSuffix() + "' to map with order '" + order + "'");
    }
    // Get the list of orders and sort them
    List<Integer> order_list = new ArrayList<>();
    order_list.addAll(order_map.keySet());
    Collections.sort(order_list); // Sorts the list
    
    String prefix = "";
    for(Integer i : order_list)
    {
      List<Title> order_titles = order_map.get(i);
      for(Title t : order_titles) prefix = prefix + t.getPrefix() + " ";
    }
    if(SamChannels.debug) SamChannels.logger.info("  Getting Prefix : Prefix made '" + prefix + "'");
    if(prefix.equals(" ")) prefix = "";
    return StringUtilities.formatString(prefix);
  }
  /**Gets the player's define suffix based off permissions and tracks as well as the channel being used.
   * 
   * @param player The player to base the suffix off of (permissions)
   * @param channel The channel to base the suffix off of (tracks allowed)
   * @return
   */
  public String getPlayerSuffix(Player player, Channel channel)
  {
 // Creating a list of the tracks used by the channel.
    List<String> track_names = channel.getConfiguration().getTracks();
    List<Track> tracks = new ArrayList<>();
    for(String name : track_names)
    {
      // Check to see if the track actually exists
      if(!this.hasTrack(name))
      {
        if(SamChannels.debug) SamChannels.logger.warning("The non-existant track: '" + name + "' was specified in '" + channel.getConfiguration().getKey() + "' within the 'channels.yml' file.");
        continue;
      }
      // Add the track to the list.
      tracks.add(this.getTrack(name));
      if(SamChannels.debug) SamChannels.logger.info("  Getting Suffix : Track Added '" + this.getTrack(name).getName() + "'");
    }
    
    // Get the list of titles from each Track:
    List<Title> titles = new ArrayList<>();
    for(Track track : tracks) titles.addAll(track.getHighestPriorityTitles(player));
    
    // Get the list of titles in their order.
    Map<Integer, List<Title>> order_map = new HashMap<>();
    for(Title title : titles)
    {
      int order = title.getOrder();
      if(!order_map.containsKey(order)) order_map.put(order, new ArrayList<Title>());
      order_map.get(order).add(title);
      if(SamChannels.debug) SamChannels.logger.info("  Getting Suffix : Added Title '" + title.getPrefix() + "," + title.getSuffix() + "' to map with order '" + order + "'");
    }
    // Get the list of orders and sort them
    List<Integer> order_list = new ArrayList<>();
    order_list.addAll(order_map.keySet());
    Collections.sort(order_list); // Sorts the list
    
    String suffix = "";
    for(Integer i : order_list)
    {
      List<Title> order_titles = order_map.get(i);
      for(Title t : order_titles) suffix = suffix + " " + t.getSuffix();
    }
    if(SamChannels.debug) SamChannels.logger.info("  Getting Suffix : Suffix made '" + suffix + "'");
    if(suffix.equals(" ")) suffix = "";
    return StringUtilities.formatString(suffix);
  }
}
