package siege.common.siege;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;

public class Siege
{
	private boolean needsSave = false;
	
	private String siegeName;
	
	private int dimension;
	private int xPos;
	private int zPos;
	private int radius;
	private int ticksRemaining;
	private static final double EDGE_PUT_RANGE = 5D;
	
	private List<SiegeTeam> siegeTeams = new ArrayList();
	private int maxTeamDifference;
	
	private Map<UUID, SiegePlayerData> playerDataMap = new HashMap();
	
	public String getSiegeName()
	{
		return siegeName;
	}
	
	public boolean isSiegeWorld(World world)
	{
		return world.provider.dimensionId == dimension;
	}
	
	public boolean isActive()
	{
		return ticksRemaining > 0;
	}
	
	public boolean hasPlayer(EntityPlayer entityplayer)
	{
		return getPlayerTeam(entityplayer) != null;
	}
	
	public SiegeTeam getPlayerTeam(EntityPlayer entityplayer)
	{
		for (SiegeTeam team : siegeTeams)
		{
			if (team.containsPlayer(entityplayer))
			{
				return team;
			}
		}
		return null;
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
	
	public void updateSiege(World world)
	{
		ticksRemaining--;
	}
	
	private void messagePlayer(EntityPlayer entityplayer, String text)
	{
		IChatComponent message = new ChatComponentText(text);
		message.getChatStyle().setColor(EnumChatFormatting.RED);
		entityplayer.addChatMessage(message);
	}
	
	public void updatePlayer(EntityPlayer entityplayer)
	{
		World world = entityplayer.worldObj;
		
		if (!entityplayer.capabilities.isCreativeMode)
		{
			double dx = entityplayer.posX - xPos;
			double dz = entityplayer.posZ - zPos;
			double dSq = dx * dx + dz * dz;
			boolean inSiegeRange = dSq <= (double)radius * (double)radius;
			float angle = (float)Math.atan2(dz, dx);
				
			if (hasPlayer(entityplayer))
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
	}
	
	public void onPlayerDeath(EntityPlayer entityplayer)
	{
		if (hasPlayer(entityplayer))
		{
			SiegeTeam team = getPlayerTeam(entityplayer);
			UUID playerID = entityplayer.getUniqueID();
			SiegePlayerData playerData = getPlayerData(playerID);
			
			entityplayer.inventory.clearInventory(null, -1);
			
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
			SiegeTeam team = getPlayerTeam(entityplayer);
			UUID playerID = entityplayer.getUniqueID();
			SiegePlayerData playerData = getPlayerData(playerID);
			
			BackupSpawnPoint bsp = playerData.getBackupSpawnPoint();
			if (bsp != null)
			{
				entityplayer.setSpawnChunk(bsp.spawnCoords, bsp.spawnForced, bsp.dimension);
			}
			playerData.setBackupSpawnPoint(null);
			
			String kitName = playerData.getChosenKit();
			if (kitName == null)
			{
				kitName = team.getRandomKitName(entityplayer.getRNG());
				messagePlayer(entityplayer, "Remember to choose a kit!");
			}
			
			Kit kit = KitDatabase.getKit(kitName);
			if (kit == null)
			{
				messagePlayer(entityplayer, "WARNING! No kit for name " + kitName + " exists! Tell an admin about this!");
			}
			else
			{
				kit.applyTo(entityplayer);
			}
		}
	}
	
	public void markDirty()
	{
		needsSave = true;
	}
	
	public boolean needsSave()
	{
		return needsSave;
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("Name", siegeName);
		
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
		siegeName = nbt.getString("Name");
		
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
				team.readFromNBT(nbt);
				siegeTeams.add(team);
			}
		}
		
		maxTeamDifference = nbt.getInteger("MaxTeamDiff");
		
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
