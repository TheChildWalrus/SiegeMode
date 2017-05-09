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
        return "/siege_kit <player> <kit>";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		if (args.length >= 2)
		{
			String playerName = args[0];
			EntityPlayerMP entityplayer = getPlayer(sender, playerName);
			if (entityplayer != null)
			{
				String kitName = args[1];
				Kit kit = KitDatabase.getKit(kitName);
				if (kit != null)
				{
					kit.applyTo(entityplayer);
					func_152373_a(sender, this, "Applied kit %s to %s", kitName, playerName);
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
        	return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        }
        if (args.length == 2)
        {
        	return getListOfStringsMatchingLastWord(args, KitDatabase.getAllKitNames().toArray(new String[0]));
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int i)
    {
        return i == 0;
    }
}
