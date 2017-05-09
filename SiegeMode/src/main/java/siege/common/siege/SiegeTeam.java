package siege.common.siege;

import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.Constants;

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
	
	public boolean containsPlayer(EntityPlayer entityplayer)
	{
		return teamPlayers.contains(entityplayer.getUniqueID());
	}
	
	public ChunkCoordinates getRespawnPoint()
	{
		return new ChunkCoordinates(respawnX, respawnY, respawnZ);
	}
	
	public String getRandomKitName(Random random)
	{
		if (teamKits.isEmpty())
		{
			return null;
		}
		return teamKits.get(random.nextInt(teamKits.size()));
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
