package siege.common.kit;

import java.util.List;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandKitCreate extends CommandBase
{
	@Override
    public String getCommandName()
    {
        return "siege_kitCreate";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/siege_kitCreate <kit-name> [player]";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		if (args.length >= 1)
		{
			String kitName = args[0];
			if (!KitDatabase.validKitName(kitName))
			{
				throw new CommandException("Invalid kit name %s", kitName);
			}
			if (KitDatabase.kitExists(kitName))
			{
				throw new CommandException("A kit named %s already exists!", kitName);
			}
			else
			{
				EntityPlayerMP entityplayer;
				
				if (args.length >= 2)
				{
					entityplayer = getPlayer(sender, args[1]);
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
					Kit kit = Kit.createFrom(entityplayer, kitName);
					KitDatabase.addAndSaveKit(kit);
					func_152373_a(sender, this, "Created a new kit %s from the inventory of %s", kitName, entityplayer.getCommandSenderName());
					return;
				}
			}
		}
		
		throw new WrongUsageException(getCommandUsage(sender));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 2)
        {
        	return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int i)
    {
        return false;
    }
}
