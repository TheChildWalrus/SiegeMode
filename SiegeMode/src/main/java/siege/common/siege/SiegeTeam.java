package siege.common.siege;

import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.Constants;
import siege.common.kit.KitDatabase;

public class SiegeTeam
{
	private Siege theSiege;
	private String teamName;
	private List<UUID> teamPlayers = new ArrayList();
	private List<String> teamKits = new ArrayList();
	
	private int respawnX;
	private int respawnY;
	private int respawnZ;

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
		return teamPlayers.contains(entityplayer.getUniqueID());
	}
	
	public int playerCount()
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
		if (containsPlayer(entityplayer))
		{
			return false;
		}
		
		int count = playerCount();
		int lowestCount = theSiege.getSmallestTeamSize();
		if (count - lowestCount > theSiege.getMaxTeamDifference())
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
	
	public String getRandomKitName(Random random)
	{
		if (teamKits.isEmpty())
		{
			return null;
		}
		return teamKits.get(random.nextInt(teamKits.size()));
	}
	
	public boolean containsKit(String kitName)
	{
		return teamKits.contains(kitName);
	}
	
	public void addKit(String kitName)
	{
		teamKits.add(kitName);
		theSiege.markDirty();
	}
	
	public void removeKit(String kitName)
	{
		teamKits.remove(kitName);
		theSiege.markDirty();
	}
	
	public List<String> listKitNames()
	{
		return new ArrayList(teamKits);
	}
	
	public List<String> listUnincludedKitNames()
	{
		List<String> names = KitDatabase.getAllKitNames();
		names.removeAll(teamKits);
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
		for (String kit : teamKits)
		{
			kitTags.appendTag(new NBTTagString(kit));
		}
		nbt.setTag("Kits", kitTags);
		
		nbt.setInteger("RespawnX", respawnX);
		nbt.setInteger("RespawnY", respawnY);
		nbt.setInteger("RespawnZ", respawnZ);
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
		if (nbt.hasKey("Kits"))
		{
			NBTTagList kitTags = nbt.getTagList("Kits", Constants.NBT.TAG_STRING);
			for (int i = 0; i < kitTags.tagCount(); i++)
			{
				String kit = kitTags.getStringTagAt(i);
				teamKits.add(kit);
			}
		}
		
		respawnX = nbt.getInteger("RespawnX");
		respawnY = nbt.getInteger("RespawnY");
		respawnZ = nbt.getInteger("RespawnZ");
	}
}
