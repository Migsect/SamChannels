package net.samongi.SamChannels.Commands;

import org.bukkit.command.CommandSender;

import net.samongi.SamChannels.SamChannelsInterface;
import net.samongi.SamongiLib.CommandHandling.BaseCommand;
import net.samongi.SamongiLib.CommandHandling.ErrorType;

public class CommandListen extends BaseCommand
{

  public CommandListen(String command_path, SamChannelsInterface sc_interface)
  {
    super(command_path);
    // TODO Auto-generated constructor stub
  }

  @Override
  public boolean run(CommandSender sender, String[] args)
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void handleError(CommandSender sender, ErrorType type)
  {
    // TODO Auto-generated method stub
    
  }

}
