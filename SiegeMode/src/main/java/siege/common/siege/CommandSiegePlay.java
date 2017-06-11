package siege.common.siege;

import java.util.List;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;

public class CommandSiegePlay extends CommandBase
{
	@Override
    public String getCommandName()
    {
        return "siege_play";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
	
	@Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }
	
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/siege_play <...> (use TAB key to autocomplete parameters)";
    }
	
	@Override
    public void processCommand(ICommandSender sender, String[] args)
    {
		EntityPlayerMP operator = getCommandSenderAsPlayer(sender);
		if (args.length >= 1)
		{
			String option = args[0];
			
			if (option.equals("join"))
			{
				if (SiegeDatabase.getActiveSiegeForPlayer(operator) != null)
				{
					throw new CommandException("You are already taking part in a siege!");
				}
				
				String siegeName = args[1];
				Siege siege = SiegeDatabase.getSiege(siegeName);
				if (siege != null && siege.isActive())
				{
					if (siege.isPlayerInDimension(operator))
					{
						if (args.length >= 3)
						{
							String teamName = args[2];
							SiegeTeam team = siege.getTeam(teamName);
							if (team != null)
							{
								if (team.canPlayerJoin(operator))
								{
									Kit kit = null;
									if (args.length >= 4)
									{
										String kitNameArg = args[3];
										Kit kitArg = KitDatabase.getKit(kitNameArg);
										if (kitArg != null && team.containsKit(kitArg))
										{
											if (team.isKitAvailable(kitArg))
											{
												kit = kitArg;
											}
											else
											{
												int limit = team.getKitLimit(kitArg);
												throw new CommandException("Kit %s is limited to %s players in team %s! Try another kit", kitArg.getKitName(), String.valueOf(limit), teamName);
											}
										}
									}
									
									if (siege.joinPlayer(operator, team, kit))
									{
										if (kit == null)
										{
											func_152373_a(sender, this, "Joined siege %s on team %s", siegeName, teamName);
										}
										else
										{
											func_152373_a(sender, this, "Joined siege %s on team %s as kit %s", siegeName, teamName, kit.getKitName());
										}
									}
									return;
								}
								else
								{
									throw new CommandException("Cannot join siege %s on team %s: too many players! Try another team", siegeName, teamName);
								}
							}
							else
							{
								throw new CommandException("Cannot join siege %s on team %s: no such team exists!", siegeName, teamName);
							}
						}
						else
						{
							throw new CommandException("Specify a team to join!");
						}
					}
					else
					{
						throw new CommandException("Cannot join siege %s: you are in the wrong dimension!");
					}
				}
				else
				{
					throw new CommandException("Cannot join siege %s: no such active siege exists!", siegeName);
				}
			}
			else if (option.equals("team"))
			{
				Siege siege = SiegeDatabase.getActiveSiegeForPlayer(operator);
				if (siege != null && siege.isActive())
				{
					String teamName = args[1];
					SiegeTeam team = siege.getTeam(teamName);
					if (team != null)
					{
						if (team.canPlayerJoin(operator))
						{
							Kit kit = null;
							if (args.length >= 3)
							{
								String kitNameArg = args[2];
								if (KitDatabase.isRandomKitID(kitNameArg))
								{
									kit = null;
								}
								else
								{
									Kit kitArg = KitDatabase.getKit(kitNameArg);
									if (kitArg != null && team.containsKit(kitArg))
									{
										if (team.isKitAvailable(kitArg) || kitArg.getKitID().equals(siege.getPlayerData(operator).getChosenKit()))
										{
											kit = kitArg;
										}
										else
										{
											int limit = team.getKitLimit(kitArg);
											throw new CommandException("Kit %s is limited to %s players in team %s! Try another kit", kitArg.getKitName(), String.valueOf(limit), teamName);
										}
									}
								}
							}
							
							siege.getPlayerData(operator).setNextTeam(teamName);
							siege.getPlayerData(operator).setChosenKit(kit);

							if (kit == null)
							{
								func_152373_a(sender, this, "Switching to team %s after death", teamName);
							}
							else
							{
								func_152373_a(sender, this, "Switching to team %s with kit %s after death", teamName, kit.getKitName());
							}
							return;
						}
						else
						{
							throw new CommandException("Cannot switch to team %s: too many players!", teamName);
						}
					}
					else
					{
						throw new CommandException("Cannot switch to team %s: no such team exists!", teamName);
					}
				}
				else
				{
					throw new CommandException("You are not currently taking part in a siege!");
				}
			}
			else if (option.equals("kit"))
			{
				Siege siege = SiegeDatabase.getActiveSiegeForPlayer(operator);
				if (siege != null && siege.isActive())
				{
					SiegeTeam team = siege.getPlayerTeam(operator);
					String teamName = team.getTeamName();
					String kitName = args[1];
					
					if (KitDatabase.isRandomKitID(kitName))
					{
						siege.getPlayerData(operator).setRandomChosenKit();
						func_152373_a(sender, this, "Switching to random kit selection after death", kitName);
						return;
					}
					else
					{
						Kit kit = KitDatabase.getKit(kitName);
						if (kit != null && team.containsKit(kit))
						{
							if (team.isKitAvailable(kit) || kit.getKitID().equals(siege.getPlayerData(operator).getChosenKit()))
							{
								siege.getPlayerData(operator).setChosenKit(kit);
								func_152373_a(sender, this, "Switching to kit %s after death", kitName);
								return;
							}
							else
							{
								int limit = team.getKitLimit(kit);
								throw new CommandException("Kit %s is limited to %s players in team %s! Try another kit", kitName, String.valueOf(limit), teamName);
							}
						}
						else
						{
							throw new CommandException("Cannot switch to kit %s: no such kit exists on team %s!", kitName, teamName);
						}
					}
				}
				else
				{
					throw new CommandException("You are not currently taking part in a siege!");
				}
			}
			else if (option.equals("leave"))
			{
				Siege siege = SiegeDatabase.getActiveSiegeForPlayer(operator);
				if (siege != null && siege.isActive())
				{
					siege.leavePlayer(operator, true);
					func_152373_a(sender, this, "Left siege %s", siege.getSiegeName());
					return;
				}
				else
				{
					throw new CommandException("You are not currently taking part in a siege!");
				}
			}
		}
		
		throw new WrongUsageException(getCommandUsage(sender));
    }
	
	@Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
		EntityPlayerMP operator = getCommandSenderAsPlayer(sender);
		Siege currentSiege = SiegeDatabase.getActiveSiegeForPlayer(operator);
        if (args.length == 1)
        {
        	if (currentSiege == null)
        	{
        		return getListOfStringsMatchingLastWord(args, "join");
        	}
        	else
        	{
        		return getListOfStringsMatchingLastWord(args, "team", "kit", "leave");
        	}
        }
        if (args.length >= 2)
        {
        	String option = args[0];
        	if (option.equals("join") && currentSiege == null)
        	{
        		if (args.length == 2)
        		{
        			return getListOfStringsMatchingLastWord(args, SiegeDatabase.listActiveSiegeNames().toArray(new String[0]));
        		}
        		else if (args.length >= 3)
        		{
        			String siegeName = args[1];
        			Siege siege = SiegeDatabase.getSiege(siegeName);
        			if (siege != null && siege.isActive())
        			{
                		if (args.length == 3)
                		{
                			return getListOfStringsMatchingLastWord(args, siege.listTeamNames().toArray(new String[0]));
                		}
                		else if (args.length >= 4)
                		{
                			String teamName = args[2];
                			SiegeTeam team = siege.getTeam(teamName);
                			if (team != null)
                			{
                				List<String> kitNames = team.listKitNames();
                				kitNames.add(KitDatabase.getRandomKitID());
                				return getListOfStringsMatchingLastWord(args, kitNames.toArray(new String[0]));
                			}
                		}
        			}
        		}
        	}
        	else if (option.equals("team") && currentSiege != null)
        	{
        		if (args.length == 2)
        		{
        			return getListOfStringsMatchingLastWord(args, currentSiege.listTeamNames().toArray(new String[0]));
        		}
        		else if (args.length >= 3)
        		{
        			String teamName = args[1];
        			SiegeTeam team = currentSiege.getTeam(teamName);
        			if (team != null)
        			{
        				List<String> kitNames = team.listKitNames();
        				kitNames.add(KitDatabase.getRandomKitID());
        				return getListOfStringsMatchingLastWord(args, kitNames.toArray(new String[0]));
        			}
        		}
        	}
        	else if (option.equals("kit") && currentSiege != null)
        	{
        		SiegeTeam team = currentSiege.getPlayerTeam(operator);
        		if (team != null)
        		{
    				List<String> kitNames = team.listKitNames();
    				kitNames.add(KitDatabase.getRandomKitID());
    				return getListOfStringsMatchingLastWord(args, kitNames.toArray(new String[0]));
        		}
        	}
        }
        return null;
    }
	
    @Override
    public boolean isUsernameIndex(String[] args, int i)
    {
        return false;
    }
}
