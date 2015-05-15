package net.samongi.SamChannels.Commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.samongi.SamChannels.SamChannelsInterface;
import net.samongi.SamChannels.Channels.Channel;
import net.samongi.SamChannels.Parser.ChannelConfiguration;
import net.samongi.SamongiLib.CommandHandling.ArgumentType;
import net.samongi.SamongiLib.CommandHandling.BaseCommand;
import net.samongi.SamongiLib.CommandHandling.ErrorType;
import net.samongi.SamongiLib.CommandHandling.SenderType;

public class CommandChat extends BaseCommand
{
  private SamChannelsInterface sc_interface;
  public CommandChat(String command_path, SamChannelsInterface sc_interface)
  {
    super(command_path);

    this.sc_interface = sc_interface;
    
    ArgumentType[] arg_allowed_1 = {ArgumentType.STRING}; // For simply switching channels
    ArgumentType[] arg_allowed_2 = {ArgumentType.STRING, ArgumentType.STRING}; // For sending a message to that channel.
    this.allowed_arguments.add(arg_allowed_1);
    this.allowed_arguments.add(arg_allowed_2);
    
    this.allowed_senders.add(SenderType.PLAYER);
  }

  @Override
  public boolean run(CommandSender sender, String[] args)
  {
    Player player = (Player) sender;
    String channel_name = args[0];
    Channel channel = sc_interface.getChannel(args[0]);
    
    // Create new channel path if it doesn't exist.
    if(channel == null)
    {
      if(!player.hasPermission("channels.can-create")) 
      {
        player.sendMessage(ChatColor.RED + "You do not have permission to create a new channel!");
        return true;
      }
    }
    // If we only have 1 argument (namely the channel)
    if(args.length == 1)
    {
      // Now we will try to switch to the channel
      boolean success = sc_interface.chatToChannel(player, args[0]);
      if(!success)
      {
        ChannelConfiguration channel_config = channel.getConfiguration();
        if(!player.hasPermission(channel_config.getPermissionChat())) player.sendMessage(ChatColor.RED + "You do not have permission to chat to this channel!");
        if(!player.hasPermission(channel_config.getPermissionListen())) player.sendMessage(ChatColor.RED + "You do not have permission to listen to this channel!");
      }
    }
    // If we have 2 arguments or more, then that is a message we will send.
    if(args.length >= 2)
    {
      String message = "";
      for(int i = 1; i < args.length; i++)
      {
        message += " " + args[i];
      }
      message = message.trim();
      boolean success = sc_interface.messageChannel(channel_name, player, message);
      if(!success)
      {
        ChannelConfiguration channel_config = channel.getConfiguration();
        if(!player.hasPermission(channel_config.getPermissionChat())) player.sendMessage(ChatColor.RED + "You do not have permission to chat to this channel!");
        if(!player.hasPermission(channel_config.getPermissionListen())) player.sendMessage(ChatColor.RED + "You do not have permission to listen to this channel!");
      }
    }
    
    return true;
  }

  @Override
  public void handleError(CommandSender sender, ErrorType type)
  {
    // TODO Auto-generated method stub
    
  }

}
