package siege.common.siege;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.Constants;
import siege.common.SiegeMode;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;
import cpw.mods.fml.common.event.FMLInterModComms;

public class Siege
{
	private boolean needsSave = false;
	private boolean deleted = false;
	
	private UUID siegeID;
	private String siegeName;
	
	private boolean isLocationSet = false;
	private int dimension;
	private int xPos;
	private int zPos;
	private int radius;
	public static final int MAX_RADIUS = 2000;
	private int ticksRemaining = 0;
	private static final int SCORE_INTERVAL = 30 * 20;
	private static final double EDGE_PUT_RANGE = 2D;
	
	private List<SiegeTeam> siegeTeams = new ArrayList();
	private int maxTeamDifference = 3;
	private boolean friendlyFire = false;
	private boolean mobSpawning = false;
	private boolean terrainProtect = true;
	private boolean terrainProtectInactive = false;
	
	private Map<UUID, SiegePlayerData> playerDataMap = new HashMap();
	private static final int KILLSTREAK_ANNOUNCE = 3;
	
	public Siege(String s)
	{
		siegeID = UUID.randomUUID();
		siegeName = s;
	}
	
	public UUID getSiegeID()
	{
		return siegeID;
	}
	
	public String getSiegeName()
	{
		return siegeName;
	}
	
	public void rename(String s)
	{
		String oldName = siegeName;
		siegeName = s;
		markDirty();
		SiegeDatabase.renameSiege(this, oldName);
	}
	
	public void setCoords(int dim, int x, int z, int r)
	{
		dimension = dim;
		xPos = x;
		zPos = z;
		radius = r;
		isLocationSet = true;
		markDirty();
	}
	
	public boolean isLocationInSiege(double x, double y, double z)
	{
		double dx = x - (xPos + 0.5D);
		double dz = z - (zPos + 0.5D);
		double dSq = dx * dx + dz * dz;
		return dSq <= (double)radius * (double)radius;
	}
	
	public SiegeTeam getTeam(String teamName)
	{
		for (SiegeTeam team : siegeTeams)
		{
			if (team.getTeamName().equals(teamName))
			{
				return team;
			}
		}
		return null;
	}
	
	public void createNewTeam(String teamName)
	{
		SiegeTeam team = new SiegeTeam(this, teamName);
		siegeTeams.add(team);
		markDirty();
	}
	
	public boolean removeTeam(String teamName)
	{
		SiegeTeam team = getTeam(teamName);
		if (team != null)
		{
			siegeTeams.remove(team);
			team.remove();
			markDirty();
			return true;
		}
		return false;
	}
	
	public List<String> listTeamNames()
	{
		List<String> names = new ArrayList();
		for (SiegeTeam team : siegeTeams)
		{
			names.add(team.getTeamName());
		}
		return names;
	}
	
	public int getSmallestTeamSize()
	{
		boolean flag = false;
		int smallestSize = -1;
		for (SiegeTeam team : siegeTeams)
		{
			int size = team.playerCount();
			if (!flag || size < smallestSize)
			{
				smallestSize = size;
			}
			flag = true;
		}
		return smallestSize;
	}
	
	public boolean hasPlayer(EntityPlayer entityplayer)
	{
		return getPlayerTeam(entityplayer) != null;
	}
	
	public SiegeTeam getPlayerTeam(EntityPlayer entityplayer)
	{
		return getPlayerTeam(entityplayer.getUniqueID());
	}
	
	public SiegeTeam getPlayerTeam(UUID playerID)
	{
		for (SiegeTeam team : siegeTeams)
		{
			if (team.containsPlayer(playerID))
			{
				return team;
			}
		}
		return null;
	}
	
	public SiegePlayerData getPlayerData(EntityPlayer entityplayer)
	{
		return getPlayerData(entityplayer.getUniqueID());
	}
	
	public SiegePlayerData getPlayerData(UUID player)
	{
		SiegePlayerData data = playerDataMap.get(player);
		if (data == null)
		{
			data = new SiegePlayerData(this);
			playerDataMap.put(player, data);
		}
		return data;
	}
	
	public List<String> listAllPlayerNames()
	{
		List<String> names = new ArrayList();
		List playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (Object player : playerList)
		{
			EntityPlayer entityplayer = (EntityPlayer)player;
			if (hasPlayer(entityplayer))
			{
				names.add(entityplayer.getCommandSenderName());
			}
		}
		return names;
	}
	
	public int getMaxTeamDifference()
	{
		return maxTeamDifference;
	}
	
	public void setMaxTeamDifference(int d)
	{
		maxTeamDifference = d;
		markDirty();
	}
	
	public boolean getFriendlyFire()
	{
		return friendlyFire;
	}
	
	public void setFriendlyFire(boolean flag)
	{
		friendlyFire = flag;
		markDirty();
	}
	
	public boolean getMobSpawning()
	{
		return mobSpawning;
	}
	
	public void setMobSpawning(boolean flag)
	{
		mobSpawning = flag;
		markDirty();
	}
	
	public boolean getTerrainProtect()
	{
		return terrainProtect;
	}
	
	public void setTerrainProtect(boolean flag)
	{
		terrainProtect = flag;
		markDirty();
	}
	
	public boolean getTerrainProtectInactive()
	{
		return terrainProtectInactive;
	}
	
	public void setTerrainProtectInactive(boolean flag)
	{
		terrainProtectInactive = flag;
		markDirty();
	}
	
	public boolean isSiegeWorld(World world)
	{
		return world.provider.dimensionId == dimension;
	}
	
	public boolean canBeStarted()
	{
		return isLocationSet && !siegeTeams.isEmpty();
	}
	
	public void startSiege(int duration)
	{
		playerDataMap.clear();
		for (SiegeTeam team : siegeTeams)
		{
			team.clearPlayers();
		}
		ticksRemaining = duration;
		markDirty();
	}
	
	public void extendSiege(int duration)
	{
		if (isActive())
		{
			ticksRemaining += duration;
			markDirty();
		}
	}
	
	public int getTicksRemaining()
	{
		return ticksRemaining;
	}
	
	public static String ticksToTimeString(int ticks)
	{
		int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;
        
        String sSeconds = String.valueOf(seconds);
        if (sSeconds.length() < 2)
        {
        	sSeconds = "0" + sSeconds;
        }
        
        String sMinutes = String.valueOf(minutes);
        
        String timeDisplay = sMinutes + ":" + sSeconds;
        return timeDisplay;
	}
	
	public void endSiege()
	{
		ticksRemaining = 0;

		messageAllPlayers("The siege has ended!");
		
		List<SiegeTeam> winningTeams = new ArrayList();
		int winningScore = -1;
		for (SiegeTeam team : siegeTeams)
		{
			int score = team.getTeamKills();
			if (score > winningScore)
			{
				winningScore = score;
				winningTeams.clear();
				winningTeams.add(team);
			}
			else if (score == winningScore)
			{
				winningTeams.add(team);
			}
		}
		String winningTeamName = "";
		if (!winningTeams.isEmpty())
		{
			if (winningTeams.size() == 1)
			{
				SiegeTeam team = winningTeams.get(0);
				winningTeamName = team.getTeamName();
			}
			else
			{
				for (SiegeTeam team : winningTeams)
				{
					if (!winningTeamName.isEmpty())
					{
						winningTeamName += ", ";
					}
					winningTeamName += team.getTeamName();
				}
			}
		}
		
		if (winningTeams.size() == 1)
		{
			messageAllPlayers("Team " + winningTeamName + " won with " + winningScore + " kills!");
		}
		else
		{
			messageAllPlayers("Teams " + winningTeamName + " tied with " + winningScore + " kills each!");
		}
		
		messageAllPlayers("---");
		for (SiegeTeam team : siegeTeams)
		{
			String teamMsg = team.getSiegeEndMessage();
			messageAllPlayers(teamMsg);
		}
		messageAllPlayers("---");
		
		UUID mvpID = null;
		int mvpKills = 0;
		int mvpDeaths = 0;
		for (SiegeTeam team : siegeTeams)
		{
			for (UUID player : team.getPlayerList())
			{
				SiegePlayerData playerData = getPlayerData(player);
				int kills = playerData.getKills();
				int deaths = playerData.getDeaths();
				if (kills > mvpKills || (kills == mvpKills && deaths < mvpDeaths))
				{
					mvpID = player;
					mvpKills = kills;
					mvpDeaths = deaths;
				}
			}
		}
		if (mvpID != null)
		{
			String mvp = UsernameCache.getLastKnownUsername(mvpID);
			messageAllPlayers("MVP was " + mvp + " (" + getPlayerTeam(mvpID).getTeamName() + ") with " + mvpKills + " kills / " + mvpDeaths + " deaths");
			messageAllPlayers("---");
		}
		
		messageAllPlayers("Congratulations to " + winningTeamName + ", and well played by all!");
		
		List playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (Object player : playerList)
		{
			EntityPlayerMP entityplayer = (EntityPlayerMP)player;
			if (hasPlayer(entityplayer))
			{
				leavePlayer(entityplayer, false);
			}
		}
		playerDataMap.clear();
		
		for (SiegeTeam team : siegeTeams)
		{
			team.onSiegeEnd();
		}
		
		markDirty();
	}
	
	public boolean isActive()
	{
		return ticksRemaining > 0;
	}
	
	public void updateSiege(World world)
	{
		if (isActive())
		{
			ticksRemaining--;
			if (MinecraftServer.getServer().getTickCounter() % 100 == 0)
			{
				markDirty();
			}
			
			if (ticksRemaining <= 0)
			{
				endSiege();
			}
			else
			{
				List playerList = world.playerEntities;
				for (Object player : playerList)
				{
					EntityPlayerMP entityplayer = (EntityPlayerMP)player;
					boolean inSiege = hasPlayer(entityplayer);
					updatePlayer(entityplayer, inSiege);
				}
				
				if (ticksRemaining % SCORE_INTERVAL == 0)
				{
					List<SiegeTeam> teamsSorted = new ArrayList();
					teamsSorted.addAll(siegeTeams);
					Collections.sort(teamsSorted, new Comparator<SiegeTeam>()
					{
						@Override
						public int compare(SiegeTeam team1, SiegeTeam team2)
						{
							int score1 = team1.getTeamKills();
							int score2 = team2.getTeamKills();
							if (score1 > score2)
							{
								return -1;
							}
							else if (score1 < score2)
							{
								return 1;
							}
							else
							{
								return team1.getTeamName().compareTo(team2.getTeamName());
							}
						}
					});
					
					for (SiegeTeam team : teamsSorted)
					{
						messageAllPlayers(team.getSiegeOngoingScore());
					}
				}
			}
		}
	}
	
	public boolean joinPlayer(EntityPlayer entityplayer, SiegeTeam team, Kit kit)
	{
		SiegePlayerData playerData = getPlayerData(entityplayer);
		if (!playerData.getWarnedClearInv())
		{
			messagePlayer(entityplayer, "WARNING! Joining a siege will clear all items from your inventory!");
			messagePlayer(entityplayer, "You will not be warned again!");
			playerData.setWarnedClearInv(true);
			return false;
		}
		else
		{
			team.joinPlayer(entityplayer);
			
			ChunkCoordinates teamSpawn = team.getRespawnPoint();
			entityplayer.setPositionAndUpdate(teamSpawn.posX + 0.5D, teamSpawn.posY, teamSpawn.posZ + 0.5D);
			
			if (kit != null)
			{
				getPlayerData(entityplayer).setChosenKit(kit);
			}
			applyPlayerKit(entityplayer);
			
			return true;
		}
	}
	
	public void leavePlayer(EntityPlayerMP entityplayer, boolean forceClearScores)
	{
		// TODO: implement a timer or something; for now scores stay until they relog, better than immediately disappearing
		getPlayerData(entityplayer).updateSiegeScoreboard(entityplayer, forceClearScores);
		
		SiegeTeam team = getPlayerTeam(entityplayer);
		team.leavePlayer(entityplayer);
		
		restoreAndClearBackupSpawnPoint(entityplayer);
		SiegeMode.clearPlayerInv(entityplayer);
		
		UUID playerID = entityplayer.getUniqueID();
		playerDataMap.remove(playerID);
	}
	
	public void messagePlayer(EntityPlayer entityplayer, String text)
	{
		IChatComponent message = new ChatComponentText(text);
		message.getChatStyle().setColor(EnumChatFormatting.RED);
		entityplayer.addChatMessage(message);
	}
	
	private void messageAllPlayers(String text)
	{
		List playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (Object player : playerList)
		{
			EntityPlayer entityplayer = (EntityPlayer)player;
			if (hasPlayer(entityplayer))
			{
				messagePlayer(entityplayer, text);
			}
		}
	}
	
	private void updatePlayer(EntityPlayerMP entityplayer, boolean inSiege)
	{
		World world = entityplayer.worldObj;
		SiegePlayerData playerData = getPlayerData(entityplayer);
		SiegeTeam team = getPlayerTeam(entityplayer);
		
		if (!entityplayer.capabilities.isCreativeMode)
		{
			boolean inSiegeRange = isLocationInSiege(entityplayer.posX, entityplayer.posY, entityplayer.posZ);
			double dx = entityplayer.posX - (xPos + 0.5D);
			double dz = entityplayer.posZ - (zPos + 0.5D);
			float angle = (float)Math.atan2(dz, dx);
				
			if (inSiege)
			{
				if (!inSiegeRange)
				{
					double putRange = radius - EDGE_PUT_RANGE;
					int newX = xPos + MathHelper.floor_double(putRange * MathHelper.cos(angle));
					int newZ = zPos + MathHelper.floor_double(putRange * MathHelper.sin(angle));
					int newY = world.getTopSolidOrLiquidBlock(newX, newZ);
					entityplayer.setPositionAndUpdate(newX + 0.5D, newY + 0.5D, newZ + 0.5D);
					
					messagePlayer(entityplayer, "Stay inside the siege area!");
				}
				
				FMLInterModComms.sendRuntimeMessage(SiegeMode.instance, "lotr", "FS_DISABLE", entityplayer.getCommandSenderName());
			}
			else
			{
				if (inSiegeRange)
				{
					double putRange = radius + EDGE_PUT_RANGE;
					int newX = xPos + MathHelper.floor_double(putRange * MathHelper.cos(angle));
					int newZ = zPos + MathHelper.floor_double(putRange * MathHelper.sin(angle));
					int newY = world.getTopSolidOrLiquidBlock(newX, newZ);
					entityplayer.setPositionAndUpdate(newX + 0.5D, newY + 0.5D, newZ + 0.5D);
					
					messagePlayer(entityplayer, "A siege is occurring here - stay out of the area!");
				}
			}
		}
		
		playerData.updateSiegeScoreboard(entityplayer, false);
	}
	
	public void onPlayerDeath(EntityPlayer entityplayer, DamageSource source)
	{
		if (hasPlayer(entityplayer))
		{
			UUID playerID = entityplayer.getUniqueID();
			SiegePlayerData playerData = getPlayerData(playerID);
			SiegeTeam team = getPlayerTeam(entityplayer);
			
			if (!entityplayer.capabilities.isCreativeMode)
			{
				playerData.onDeath();
				team.addTeamDeath();
				
				EntityPlayer killingPlayer = null;
				Entity killer = source.getEntity();
				if (killer instanceof EntityPlayer)
				{
					killingPlayer = (EntityPlayer)killer;
				}
				else
				{
					EntityLivingBase lastAttacker = entityplayer.func_94060_bK();
					if (lastAttacker instanceof EntityPlayer)
					{
						killingPlayer = (EntityPlayer)lastAttacker;
					}
				}
				
				if (killingPlayer != null)
				{
					if (hasPlayer(killingPlayer) && !killingPlayer.capabilities.isCreativeMode)
					{
						SiegePlayerData killingPlayerData = getPlayerData(killingPlayer);
						killingPlayerData.onKill();
						SiegeTeam killingTeam = getPlayerTeam(killingPlayer);
						killingTeam.addTeamKill();
						
						int killstreak = killingPlayerData.getKillstreak();
						if (killstreak >= KILLSTREAK_ANNOUNCE)
						{
							messageAllPlayers(killingPlayer.getCommandSenderName() + " (" + killingTeam.getTeamName() + ") has a killstreak of " + killstreak + "!");
						}
					}
				}
			}
			
			String nextTeamName = playerData.getNextTeam();
			if (nextTeamName != null)
			{
				SiegeTeam nextTeam = getTeam(nextTeamName);
				if (nextTeam != null && nextTeam != team)
				{
					team.leavePlayer(entityplayer);
					nextTeam.joinPlayer(entityplayer);
					team = getPlayerTeam(entityplayer);
					
					messageAllPlayers(entityplayer.getCommandSenderName() + " is now playing on team " + team.getTeamName());
				}
				
				playerData.setNextTeam(null);
			}
			
			// to not drop siege kit
			SiegeMode.clearPlayerInv(entityplayer);
			
			int dim = entityplayer.dimension;
			ChunkCoordinates coords = entityplayer.getBedLocation(dim);
			boolean forced = entityplayer.isSpawnForced(dim);
			
			BackupSpawnPoint bsp = new BackupSpawnPoint(dim, coords, forced);
			playerData.setBackupSpawnPoint(bsp);
			markDirty();
			
			ChunkCoordinates teamSpawn = team.getRespawnPoint();
			entityplayer.setSpawnChunk(teamSpawn, true, dim);
		}
	}
	
	public void onPlayerRespawn(EntityPlayer entityplayer)
	{
		if (hasPlayer(entityplayer))
		{
			restoreAndClearBackupSpawnPoint(entityplayer);
			applyPlayerKit(entityplayer);
		}
	}
	
	private void restoreAndClearBackupSpawnPoint(EntityPlayer entityplayer)
	{
		UUID playerID = entityplayer.getUniqueID();
		SiegePlayerData playerData = getPlayerData(playerID);
		
		BackupSpawnPoint bsp = playerData.getBackupSpawnPoint();
		if (bsp != null)
		{
			entityplayer.setSpawnChunk(bsp.spawnCoords, bsp.spawnForced, bsp.dimension);
		}
		playerData.setBackupSpawnPoint(null);
	}
	
	public void applyPlayerKit(EntityPlayer entityplayer)
	{
		SiegeTeam team = getPlayerTeam(entityplayer);
		UUID playerID = entityplayer.getUniqueID();
		SiegePlayerData playerData = getPlayerData(playerID);
		
		Kit kit = KitDatabase.getKit(playerData.getChosenKit());
		if (kit == null || !team.containsKit(kit))
		{
			kit = team.getRandomKit(entityplayer.getRNG());
			messagePlayer(entityplayer, "No kit chosen! Using a random kit: " + kit.getKitName());
		}
		
		kit.applyTo(entityplayer);
		playerData.setCurrentKit(kit);
		setHasSiegeGivenKit(entityplayer, true);
	}
	
	public static boolean hasSiegeGivenKit(EntityPlayer entityplayer)
	{
		return entityplayer.getEntityData().getBoolean("HasSiegeKit");
	}
	
	public static void setHasSiegeGivenKit(EntityPlayer entityplayer, boolean flag)
	{
		entityplayer.getEntityData().setBoolean("HasSiegeKit", flag);
	}
	
	public void onPlayerLogout(EntityPlayerMP entityplayer)
	{
		SiegePlayerData playerData = getPlayerData(entityplayer);
		if (playerData != null)
		{
			playerData.onLogout(entityplayer);
		}
	}
	
	public void markDirty()
	{
		needsSave = true;
	}
	
	public void markSaved()
	{
		needsSave = false;
	}
	
	public boolean needsSave()
	{
		return needsSave;
	}
	
	public boolean isDeleted()
	{
		return deleted;
	}
	
	public void deleteSiege()
	{
		if (isActive())
		{
			endSiege();
		}
		
		deleted = true;
		markDirty();
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("SiegeID", siegeID.toString());
		nbt.setString("Name", siegeName);
		nbt.setBoolean("Deleted", deleted);
		
		nbt.setBoolean("LocationSet", isLocationSet);
		nbt.setInteger("Dim", dimension);
		nbt.setInteger("XPos", xPos);
		nbt.setInteger("ZPos", zPos);
		nbt.setInteger("Radius", radius);
		
		nbt.setInteger("TicksRemaining", ticksRemaining);
		
		NBTTagList teamTags = new NBTTagList();
		for (SiegeTeam team : siegeTeams)
		{
			NBTTagCompound teamData = new NBTTagCompound();
			team.writeToNBT(teamData);
			teamTags.appendTag(teamData);
		}
		nbt.setTag("Teams", teamTags);
		
		nbt.setInteger("MaxTeamDiff", maxTeamDifference);
		nbt.setBoolean("FriendlyFire", friendlyFire);
		nbt.setBoolean("MobSpawning", mobSpawning);
		nbt.setBoolean("TerrainProtect", terrainProtect);
		nbt.setBoolean("TerrainProtectInactive", terrainProtectInactive);
		
		NBTTagList playerTags = new NBTTagList();
		for (Entry<UUID, SiegePlayerData> e : playerDataMap.entrySet())
		{
			UUID playerID = e.getKey();
			SiegePlayerData player = e.getValue();
			
			NBTTagCompound playerData = new NBTTagCompound();
			playerData.setString("PlayerID", playerID.toString());
			player.writeToNBT(playerData);
			playerTags.appendTag(playerData);
		}
		nbt.setTag("PlayerData", playerTags);
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		siegeID = UUID.fromString(nbt.getString("SiegeID"));
		siegeName = nbt.getString("Name");
		deleted = nbt.getBoolean("Deleted");
		
		isLocationSet = nbt.getBoolean("LocationSet");
		dimension = nbt.getInteger("Dim");
		xPos = nbt.getInteger("XPos");
		zPos = nbt.getInteger("ZPos");
		radius = nbt.getInteger("Radius");
		
		ticksRemaining = nbt.getInteger("TicksRemaining");
		
		siegeTeams.clear();
		if (nbt.hasKey("Teams"))
		{
			NBTTagList teamTags = nbt.getTagList("Teams", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < teamTags.tagCount(); i++)
			{
				NBTTagCompound teamData = teamTags.getCompoundTagAt(i);
				SiegeTeam team = new SiegeTeam(this);
				team.readFromNBT(teamData);
				siegeTeams.add(team);
			}
		}
		
		maxTeamDifference = nbt.getInteger("MaxTeamDiff");
		friendlyFire = nbt.getBoolean("FriendlyFire");
		mobSpawning = nbt.getBoolean("MobSpawning");
		terrainProtect = nbt.getBoolean("TerrainProtect");
		terrainProtectInactive = nbt.getBoolean("TerrainProtectInactive");
		
		playerDataMap.clear();
		if (nbt.hasKey("PlayerData"))
		{
			NBTTagList playerTags = nbt.getTagList("PlayerData", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < playerTags.tagCount(); i++)
			{
				NBTTagCompound playerData = playerTags.getCompoundTagAt(i);
				UUID playerID = UUID.fromString(playerData.getString("PlayerID"));
				if (playerID != null)
				{
					SiegePlayerData player = new SiegePlayerData(this);
					player.readFromNBT(playerData);
					playerDataMap.put(playerID, player);
				}
			}
		}
	}
}
