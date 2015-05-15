package net.samongi.SamChannels.Titles;

import net.samongi.SamChannels.SamChannels;
import net.samongi.SamongiLib.Configuration.ConfigAccessor;

public class Title
{
  private String permission = "";
  
  private String prefix = "";
  private String suffix = "";
  
  private int order = 0;
  
  private Track track;
  private int track_priority = -1;
  
  public Title(ConfigAccessor config, String key, TitleManager manager)
  {

    if(SamChannels.debug) SamChannels.logger.info("  Creating Title: '" + key + "'");
    this.permission = config.getConfig().getString("titles." + key + ".permission", "");
    
    this.prefix = config.getConfig().getString("titles." + key + ".prefix", "");
    this.suffix = config.getConfig().getString("titles." + key + ".suffix", "");
    
    this.order = config.getConfig().getInt("titles." + key + ".order", 0);
    
    String track_name = config.getConfig().getString("titles." + key + ".track", "");
    // if(SamChannels.debug) SamChannels.logger.info("    Track Name: '" + track_name + "'");
    this.track_priority = config.getConfig().getInt("titles." + key + ".track-priority", -1);
    
    if(manager.hasTrack(track_name))
    {
      this.track = manager.getTrack(track_name);
      this.track.addTitle(this);
    }
    else
    {
      this.track = new Track(track_name);
      manager.registerTrack(this.track);
    }
  }
  
  public String getPermission(){return this.permission;}
  
  public String getPrefix(){return this.prefix;}
  public String getSuffix(){return this.suffix;}
  
  public int getOrder(){return this.order;}
  
  public Track getTrack(){return this.track;}
  public int getTrackPriority(){return this.track_priority;}
}
