package net.samongi.SamChannels.Parser;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.samongi.SamChannels.SamChannels;
import net.samongi.SamongiLib.Configuration.ConfigAccessor;
import net.samongi.SamongiLib.Utilities.StringUtilities;

public class ChannelConfiguration
{
  private final ConfigAccessor config;
  private final String key;
  
  private String channel_name = "_DEFAULT_";
  private String channel_type = "SERVER";

  private String privacy = "public";
  private boolean default_listen = true;
      
  private String permission_listen = "";
  private String permission_chat = "";
  private String permission_chat_color = "";
  private String permission_chat_bold = "";
  private String permission_chat_underline = "";
  private String permission_chat_italic = "";
  private String permission_chat_strikethrough = "";
  private String permission_chat_magic = "";
  
  private boolean allow_chat_codes = true;
  
  private String format = "[#{channel}] #{prefix}#{player_display}#{suffix} : #{message}";
  
  private List<String> tracks = new ArrayList<>();
  
  public ChannelConfiguration(ConfigAccessor config, String tag)
  {
    this.config = config;
    this.key = tag;
    
    this.channel_name = config.getConfig().getString("channels." + tag + ".name", "NO_NAME_DEFINED");
    if(this.channel_name.equals("NO_NAME_DEFINED")) SamChannels.logger.warning("Channel labeled in 'channels.yml' as: '" + this.key + "' did not have a name defined! Setting name to 'NO_NAME_DEFINED'");
    this.channel_type = config.getConfig().getString("channels." + tag + ".type", "SERVER").toUpperCase();
    if(SamChannels.debug && !config.getConfig().isString("channels." + tag + ".type")) SamChannels.logger.warning("Channel labeled in 'channels.yml' as: '" + this.key + "' did not have a type defined! Setting type to 'SERVER'");
      
    this.permission_listen = config.getConfig().getString("channels." + tag + ".permissions.listen", "");
    this.permission_chat = config.getConfig().getString("channels." + tag + ".permissions.speak", "");
    this.permission_chat_color = config.getConfig().getString("channels." + tag + ".permissions.chat-color", "");
    this.permission_chat_bold = config.getConfig().getString("channels." + tag + ".permissions.chat-bold", "");
    this.permission_chat_underline = config.getConfig().getString("channels." + tag + ".permissions.chat-underline", "");
    this.permission_chat_italic = config.getConfig().getString("channels." + tag + ".permissions.chat-italic", "");
    this.permission_chat_strikethrough = config.getConfig().getString("channels." + tag + ".permissions.chat-strikethrough", "");
    this.permission_chat_magic = config.getConfig().getString("channels." + tag + ".permissions.chat-magic", "");
    
    this.privacy = config.getConfig().getString("channels." + tag + ".privacy", "public");
    this.default_listen = config.getConfig().getBoolean("channels." + tag + ".default", true);
    this.allow_chat_codes = config.getConfig().getBoolean("channels." + tag + ".allow-chat-codes", true);
    this.format = config.getConfig().getString("channels." + tag + ".format", "[#{channel}] #{prefix}#{player_display}#{suffix} : #{message}");
    
    this.tracks = config.getConfig().getStringList("channels." + tag + ".tracks");
  }
  
  public String getChannelName(){return ChatColor.stripColor(this.getChannelDisplayName());}
  public String getChannelDisplayName(){return StringUtilities.formatString(this.channel_name);}
  public String getChannelRawName(){return this.channel_name;}
  public String getChannelType(){return this.channel_type;}
  
  public String getPrivacy(){return this.privacy;}
  public boolean getDefaultListen(){return this.default_listen;}
  
  public String getPermissionListen(){return this.permission_chat;}
  public String getPermissionChat(){return this.permission_listen;}
  public String getPermissionChatColor(){return this.permission_chat_color;}
  public String getPermissionChatBold(){return this.permission_chat_bold;}
  public String getPermissionChatUnderline(){return this.permission_chat_underline;}
  public String getPermissionChatItalic(){return this.permission_chat_italic;}
  public String getPermissionStrikethrough(){return this.permission_chat_strikethrough;}
  public String getPermissionChatMagic(){return this.permission_chat_magic;}
  
  public boolean getAllowChatCodes(){return this.allow_chat_codes;}
  public String getFormat(){return StringUtilities.formatString(this.format);}
  
  public List<String> getTracks(){return this.tracks;}
  
  public String getStringFromConfig(String path){return this.getStringFromConfig(path, null);}
  public String getStringFromConfig(String path, String def){return config.getConfig().getString("channels." + this.key + "." + path, def);}
  public boolean isStringFromConfig(String path){return config.getConfig().isString(path);}
  
  public int getIntFromConfig(String path){return this.getIntFromConfig(path, 0);}
  public int getIntFromConfig(String path, int def){return config.getConfig().getInt("channels." + this.key + "." + path, def);}
  public boolean isIntFromConfig(String path){return config.getConfig().isInt(path);}
  
  public double getDoubleFromConfig(String path){return this.getDoubleFromConfig(path, 0.0);}
  public double getDoubleFromConfig(String path, double def){return config.getConfig().getDouble("channels." + this.key + "." + path, def);}
  public boolean isDoubleFromConfig(String path){return config.getConfig().isDouble(path);}
  
  public boolean getBooleanFromConfig(String path){return this.getBooleanFromConfig(path, false);}
  public boolean getBooleanFromConfig(String path, boolean def){return config.getConfig().getBoolean("channels." + this.key + "." + path, def);}
  public boolean isBooleanFromConfig(String path){return config.getConfig().isBoolean(path);}
  
  public ConfigAccessor getConfig(){return this.config;}
  public String getKey(){return this.key;}
  
}
