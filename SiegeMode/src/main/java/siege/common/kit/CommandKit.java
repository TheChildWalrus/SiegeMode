package siege.common.kit;

import java.util.List;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandKit extends CommandBase
{
	@Override
    public String getCommandName()
    {
        return "siege_kit";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/siege_kit <...> (use TAB key to autocomplete parameters)";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		if (args.length >= 1)
		{
			String option = args[0];
			
			if (option.equals("new"))
			{
				String kitName = args[1];
				if (KitDatabase.kitExists(kitName))
				{
					throw new CommandException("A kit named %s already exists!", kitName);
				}
				if (!KitDatabase.validKitName(kitName))
				{
					throw new CommandException("Invalid kit name %s", kitName);
				}
				
				EntityPlayerMP entityplayer;
				if (args.length >= 3)
				{
					entityplayer = getPlayer(sender, args[2]);
				}
				else
				{
					entityplayer = getCommandSenderAsPlayer(sender);
				}
				
				if (entityplayer == null)
				{
					throw new PlayerNotFoundException();
				}
				else
				{
					Kit kit = Kit.createNewKit(entityplayer, kitName);
					KitDatabase.addAndSaveKit(kit);
					func_152373_a(sender, this, "Created a new kit %s from the inventory of %s", kitName, entityplayer.getCommandSenderName());
					return;
				}
			}
			else if (option.equals("apply"))
			{
				String kitName = args[1];
				Kit kit = KitDatabase.getKit(kitName);
				if (kit != null)
				{
					EntityPlayerMP entityplayer;
					if (args.length >= 3)
					{
						entityplayer = getPlayer(sender, args[2]);
					}
					else
					{
						entityplayer = getCommandSenderAsPlayer(sender);
					}
					
					if (entityplayer == null)
					{
						throw new PlayerNotFoundException();
					}
					else
					{
						kit.applyTo(entityplayer);
						func_152373_a(sender, this, "Applied kit %s to %s", kitName, entityplayer.getCommandSenderName());
						return;
					}
				}
				else
				{
					throw new CommandException("No kit for name %s", kitName);
				}
			}
			else if (option.equals("edit"))
			{
				String kitName = args[1];
				Kit kit = KitDatabase.getKit(kitName);
				if (kit != null)
				{
					String editFunction = args[2];
					if (editFunction.equals("rename"))
					{
						String newName = args[3];
						if (!KitDatabase.validKitName(newName))
						{
							throw new CommandException("Invalid kit rename %s", newName);
						}
						if (KitDatabase.kitExists(newName))
						{
							throw new CommandException("A kit named %s already exists!", newName);
						}
						
						kit.rename(newName);
						func_152373_a(sender, this, "Renamed kit %s to %s", kitName, newName);
						return;
					}
					else if (editFunction.equals("recreate"))
					{
						EntityPlayerMP entityplayer;
						if (args.length >= 4)
						{
							entityplayer = getPlayer(sender, args[3]);
						}
						else
						{
							entityplayer = getCommandSenderAsPlayer(sender);
						}
						
						if (entityplayer == null)
						{
							throw new PlayerNotFoundException();
						}
						else
						{
							kit.createFrom(entityplayer);
							func_152373_a(sender, this, "Recreated kit %s from the inventory of %s", kitName, entityplayer.getCommandSenderName());
							return;
						}
					}
				}
				else
				{
					throw new CommandException("No kit for name %s", kitName);
				}
			}
			else if (option.equals("delete"))
			{
				String kitName = args[1];
				Kit kit = KitDatabase.getKit(kitName);
				if (kit != null)
				{
					KitDatabase.deleteKit(kit);
					func_152373_a(sender, this, "Deleted kit %s", kitName);
					return;
				}
				else
				{
					throw new CommandException("No kit for name %s", kitName);
				}
			}
		}
		
		throw new WrongUsageException(getCommandUsage(sender));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
        	return getListOfStringsMatchingLastWord(args, "new", "apply", "edit", "delete");
        }
        if (args.length >= 2)
        {
        	String option = args[0];
        	if (option.equals("new"))
        	{
        		if (args.length >= 3)
        		{
        			return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        		}
        	}
        	else if (option.equals("apply"))
        	{
        		if (args.length == 2)
        		{
        			return getListOfStringsMatchingLastWord(args, KitDatabase.getAllKitNames().toArray(new String[0]));
        		}
        		else if (args.length >= 3)
        		{
        			return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        		}
        	}
        	else if (option.equals("edit"))
        	{
        		if (args.length == 2)
        		{
        			return getListOfStringsMatchingLastWord(args, KitDatabase.getAllKitNames().toArray(new String[0]));
        		}
        		else if (args.length == 3)
        		{
        			return getListOfStringsMatchingLastWord(args, "rename", "recreate");
        		}
        		else if (args.length >= 4)
        		{
        			String editFunction = args[2];
        			if (editFunction.equals("recreate"))
        			{
        				return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        			}
        		}
        	}
        	else if (option.equals("delete"))
        	{
        		return getListOfStringsMatchingLastWord(args, KitDatabase.getAllKitNames().toArray(new String[0]));
        	}
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int i)
    {
        return i == 0;
    }
}
