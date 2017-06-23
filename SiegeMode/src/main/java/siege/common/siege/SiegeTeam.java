package siege.common.siege;

import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.common.util.Constants;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;

public class SiegeTeam
{
	private Siege theSiege;
	private String teamName;
	private List<UUID> teamPlayers = new ArrayList();
	private List<UUID> teamKits = new ArrayList();
	private Map<UUID, Integer> teamKitLimits = new HashMap();
	
	private int respawnX;
	private int respawnY;
	private int respawnZ;
	
	private int teamKills;
	private int teamDeaths;

	public SiegeTeam(Siege siege)
	{
		theSiege = siege;
	}
	
	public SiegeTeam(Siege siege, String s)
	{
		this(siege);
		teamName = s;
	}
	
	public void remove()
	{
		theSiege = null;
	}
	
	public String getTeamName()
	{
		return teamName;
	}
	
	public void rename(String s)
	{
		teamName = s;
		theSiege.markDirty();
	}
	
	public boolean containsPlayer(EntityPlayer entityplayer)
	{
		return containsPlayer(entityplayer.getUniqueID());
	}
	
	public boolean containsPlayer(UUID playerID)
	{
		return teamPlayers.contains(playerID);
	}
	
	public List<UUID> getPlayerList()
	{
		return teamPlayers;
	}
	
	public int onlinePlayerCount()
	{
		int i = 0;
		List playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (Object player : playerList)
		{
			EntityPlayer entityplayer = (EntityPlayer)player;
			if (containsPlayer(entityplayer))
			{
				i++;
			}
		}
		return i;
	}
	
	public boolean canPlayerJoin(EntityPlayer entityplayer)
	{
		int count = onlinePlayerCount();
		int lowestCount = theSiege.getSmallestTeamSize();
		if (count - lowestCount >= theSiege.getMaxTeamDifference())
		{
			return false;
		}
		
		return true;
	}
	
	public void joinPlayer(EntityPlayer entityplayer)
	{
		if (!containsPlayer(entityplayer))
		{
			UUID playerID = entityplayer.getUniqueID();
			teamPlayers.add(playerID);
			theSiege.markDirty();
		}
	}
	
	public void leavePlayer(EntityPlayer entityplayer)
	{
		if (containsPlayer(entityplayer))
		{
			UUID playerID = entityplayer.getUniqueID();
			teamPlayers.remove(playerID);
			theSiege.markDirty();
		}
	}
	
	public void clearPlayers()
	{
		teamPlayers.clear();
		theSiege.markDirty();
	}
	
	public Kit getRandomKit(Random random)
	{
		List<Kit> availableKits = new ArrayList();
		for (UUID kitID : teamKits)
		{
			Kit kit = KitDatabase.getKit(kitID);
			if (kit != null && isKitAvailable(kit))
			{
				availableKits.add(kit);
			}
		}
		
		if (availableKits.isEmpty())
		{
			return null;
		}
		
		Kit kit = availableKits.get(random.nextInt(availableKits.size()));
		return kit;
	}
	
	public boolean containsKit(Kit kit)
	{
		return teamKits.contains(kit.getKitID());
	}
	
	public void addKit(Kit kit)
	{
		teamKits.add(kit.getKitID());
		theSiege.markDirty();
	}
	
	public void removeKit(Kit kit)
	{
		teamKits.remove(kit.getKitID());
		theSiege.markDirty();
	}
	
	public boolean isKitLimited(Kit kit)
	{
		return getKitLimit(kit) >= 0;
	}
	
	public int getKitLimit(Kit kit)
	{
		UUID kitID = kit.getKitID();
		if (teamKitLimits.containsKey(kitID))
		{
			return teamKitLimits.get(kitID);
		}
		return -1;
	}
	
	public void limitKit(Kit kit, int limit)
	{
		teamKitLimits.put(kit.getKitID(), limit);
		theSiege.markDirty();
	}
	
	public void unlimitKit(Kit kit)
	{
		teamKitLimits.remove(kit.getKitID());
		theSiege.markDirty();
	}
	
	public boolean isKitAvailable(Kit kit)
	{
		if (isKitLimited(kit))
		{
			int limit = getKitLimit(kit);
			int using = countPlayersUsingKit(kit);
			if (using >= limit)
			{
				return false;
			}
		}
		return true;
	}
	
	private int countPlayersUsingKit(Kit kit)
	{
		UUID kitID = kit.getKitID();
		int i = 0;
		for (UUID player : teamPlayers)
		{
			SiegePlayerData playerData = theSiege.getPlayerData(player);
			if (playerData != null && kitID.equals(playerData.getChosenKit()))
			{
				i++;
			}
		}
		return i;
	}
	
	public List<String> listKitNames()
	{
		List<String> names = new ArrayList();
		for (UUID kitID : teamKits)
		{
			Kit kit = KitDatabase.getKit(kitID);
			if (kit != null)
			{
				names.add(kit.getKitName());
			}
		}
		return names;
	}
	
	public List<String> listUnincludedKitNames()
	{
		List<String> names = KitDatabase.getAllKitNames();
		names.removeAll(listKitNames());
		return names;
	}
	
	public ChunkCoordinates getRespawnPoint()
	{
		return new ChunkCoordinates(respawnX, respawnY, respawnZ);
	}
	
	public void setRespawnPoint(int i, int j, int k)
	{
		respawnX = i;
		respawnY = j;
		respawnZ = k;
		theSiege.markDirty();
	}
	
	public int getTeamKills()
	{
		return teamKills;
	}
	
	public void addTeamKill()
	{
		teamKills++;
		theSiege.markDirty();
	}
	
	public int getTeamDeaths()
	{
		return teamDeaths;
	}
	
	public void addTeamDeath()
	{
		teamDeaths++;
		theSiege.markDirty();
	}
	
	public String getSiegeOngoingScore()
	{
		return teamName + ": Kills: " + teamKills;
	}
	
	public String getSiegeEndMessage()
	{
		UUID mvpID = null;
		int mvpKills = 0;
		int mvpDeaths = 0;
		int mvpScore = Integer.MIN_VALUE;
		for (UUID player : teamPlayers)
		{
			SiegePlayerData playerData = theSiege.getPlayerData(player);
			int kills = playerData.getKills();
			int deaths = playerData.getDeaths();
			int score = kills - deaths;
			if (score > mvpScore || (score == mvpScore && deaths < mvpDeaths))
			{
				mvpID = player;
				mvpKills = kills;
				mvpDeaths = deaths;
				mvpScore = score;
			}
		}
		
		String message = teamName + ": Kills: " + teamKills + ", Deaths: " + teamDeaths;
		if (mvpID != null)
		{
			String mvp = UsernameCache.getLastKnownUsername(mvpID);
			message += (", MVP: " + mvp + " with " + mvpKills + " kills / " + mvpDeaths + " deaths");
		}
		return message;
	}
	
	public void onSiegeEnd()
	{
		teamPlayers.clear();
		teamKills = 0;
		teamDeaths = 0;
		theSiege.markDirty();
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("Name", teamName);
		
		NBTTagList playerTags = new NBTTagList();
		for (UUID player : teamPlayers)
		{
			playerTags.appendTag(new NBTTagString(player.toString()));
		}
		nbt.setTag("Players", playerTags);
		
		NBTTagList kitTags = new NBTTagList();
		for (UUID kitID : teamKits)
		{
			Kit kit = KitDatabase.getKit(kitID);
			if (kit != null)
			{
				NBTTagCompound kitData = new NBTTagCompound();
				String kitName = kit.getKitName();
				kitData.setString("Name", kitName);
				if (teamKitLimits.containsKey(kitID))
				{
					int limit = teamKitLimits.get(kitID);
					kitData.setInteger("Limit", limit);
				}
				kitTags.appendTag(kitData);
			}
		}
		nbt.setTag("TeamKits", kitTags);
		
		nbt.setInteger("RespawnX", respawnX);
		nbt.setInteger("RespawnY", respawnY);
		nbt.setInteger("RespawnZ", respawnZ);
		
		nbt.setInteger("Kills", teamKills);
		nbt.setInteger("Deaths", teamDeaths);
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		teamName = nbt.getString("Name");
		
		teamPlayers.clear();
		if (nbt.hasKey("Players"))
		{
			NBTTagList playerTags = nbt.getTagList("Players", Constants.NBT.TAG_STRING);
			for (int i = 0; i < playerTags.tagCount(); i++)
			{
				UUID player = UUID.fromString(playerTags.getStringTagAt(i));
				if (player != null)
				{
					teamPlayers.add(player);
				}
			}
		}
		
		teamKits.clear();
		teamKitLimits.clear();
		if (nbt.hasKey("TeamKits"))
		{
			NBTTagList kitTags = nbt.getTagList("TeamKits", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < kitTags.tagCount(); i++)
			{
				NBTTagCompound kitData = kitTags.getCompoundTagAt(i);
				String kitName = kitData.getString("Name");
				Kit kit = KitDatabase.getKit(kitName);
				if (kit != null)
				{
					teamKits.add(kit.getKitID());
					if (kitData.hasKey("Limit"))
					{
						int limit = kitData.getInteger("Limit");
						teamKitLimits.put(kit.getKitID(), limit);
					}
				}
			}
		}
		else if (nbt.hasKey("Kits"))
		{
			NBTTagList kitTags = nbt.getTagList("Kits", Constants.NBT.TAG_STRING);
			for (int i = 0; i < kitTags.tagCount(); i++)
			{
				String kitName = kitTags.getStringTagAt(i);
				Kit kit = KitDatabase.getKit(kitName);
				if (kit != null)
				{
					teamKits.add(kit.getKitID());
				}
			}
		}
		
		respawnX = nbt.getInteger("RespawnX");
		respawnY = nbt.getInteger("RespawnY");
		respawnZ = nbt.getInteger("RespawnZ");
		
		teamKills = nbt.getInteger("Kills");
		teamDeaths = nbt.getInteger("Deaths");
	}
}
