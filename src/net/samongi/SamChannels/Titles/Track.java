package net.samongi.SamChannels.Titles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

public class Track
{
  private String name;
  
  private List<Title> titles = new ArrayList<>();
  
  public Track(String name)
  {
    this.name = name;
  }
  
  /**Returns the name of the track.
   * 
   * @return The name
   */
  public String getName(){return this.name;}
  
  /**Adds a title to the track
   * 
   * @param title The title
   */
  public void addTitle(Title title)
  {
    titles.add(title);
  }
  
  /**Returns a list of titles that should be used with the player.
   * This is based off the player's priority.  If the priority is equal, they both are returned.
   * This is why a list is returned as opposed to a singular title.
   * 
   * @param player The player to check if they have a permission for the title.
   * @return A list of titles (assumed to return a single one)
   */
  public List<Title> getHighestPriorityTitles(Player player)
  {
    // Generates a list of title that the player has permission to use.
    List<Title> allowed_list = new ArrayList<>();
    for(Title title : this.titles) if(title.getPermission().length() == 0 || player.hasPermission(title.getPermission())) allowed_list.add(title);
    // Generates a priority to title mapping for the allowed_list.  This is a map of ints to a list of titles.
    Map<Integer, List<Title>> priority_mapping = new HashMap<>();
    priority_mapping.put(-1, new ArrayList<Title>()); // For -1 since it is the base
    for(Title title : allowed_list)
    {
      int priority = title.getTrackPriority();
      if(!priority_mapping.containsKey(priority)) priority_mapping.put(priority, new ArrayList<Title>());
      priority_mapping.get(priority).add(title);
    }
    // Finding the highest priority.
    List<Integer> priorities = new ArrayList<>();
    priorities.addAll(priority_mapping.keySet());
    int highest_priority = -1;
    for(int i : priorities) if(i > highest_priority) highest_priority = i;
    // Returning the highest priority list in the 
    return priority_mapping.get(highest_priority);
  }
}
