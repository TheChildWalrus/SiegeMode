package siege.common.siege;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;

public class Siege
{
	private boolean needsSave = false;
	
	private UUID siegeID;
	private String siegeName;
	
	private boolean isLocationSet = false;
	private int dimension;
	private int xPos;
	private int zPos;
	private int radius;
	public static final int MAX_RADIUS = 2000;
	private int ticksRemaining = 0;
	private static final double EDGE_PUT_RANGE = 5D;
	
	private List<SiegeTeam> siegeTeams = new ArrayList();
	private int maxTeamDifference = 8;
	
	private Map<UUID, SiegePlayerData> playerDataMap = new HashMap();
	
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
		int smallestSize = Integer.MAX_VALUE;
		for (SiegeTeam team : siegeTeams)
		{
			int size = team.playerCount();
			if (size < smallestSize)
			{
				smallestSize = size;
			}
		}
		return smallestSize;
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
	
	public void endSiege()
	{
		ticksRemaining = 0;
		
		List playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (Object player : playerList)
		{
			EntityPlayer entityplayer = (EntityPlayer)player;
			if (hasPlayer(entityplayer))
			{
				leavePlayer(entityplayer);
				messagePlayer(entityplayer, "The siege has ended!");
			}
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
					EntityPlayer entityplayer = (EntityPlayer)player;
					boolean inSiege = hasPlayer(entityplayer);
					updatePlayer(entityplayer, inSiege);
				}
			}
		}
	}
	
	public void joinPlayer(EntityPlayer entityplayer, SiegeTeam team, String kitName)
	{
		team.joinPlayer(entityplayer);
		
		ChunkCoordinates teamSpawn = team.getRespawnPoint();
		entityplayer.setPositionAndUpdate(teamSpawn.posX + 0.5D, teamSpawn.posY, teamSpawn.posZ + 0.5D);
		
		if (kitName != null)
		{
			getPlayerData(entityplayer).setChosenKit(kitName);
		}
		
		applyPlayerKit(entityplayer);
	}
	
	public void leavePlayer(EntityPlayer entityplayer)
	{
		SiegeTeam team = getPlayerTeam(entityplayer);
		team.leavePlayer(entityplayer);
		restoreAndClearBackupSpawnPoint(entityplayer);
		entityplayer.inventory.clearInventory(null, -1);
	}
	
	private void messagePlayer(EntityPlayer entityplayer, String text)
	{
		IChatComponent message = new ChatComponentText(text);
		message.getChatStyle().setColor(EnumChatFormatting.RED);
		entityplayer.addChatMessage(message);
	}
	
	private void updatePlayer(EntityPlayer entityplayer, boolean inSiege)
	{
		World world = entityplayer.worldObj;
		
		if (!entityplayer.capabilities.isCreativeMode)
		{
			double dx = entityplayer.posX - xPos;
			double dz = entityplayer.posZ - zPos;
			double dSq = dx * dx + dz * dz;
			boolean inSiegeRange = dSq <= (double)radius * (double)radius;
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
			UUID playerID = entityplayer.getUniqueID();
			SiegePlayerData playerData = getPlayerData(playerID);
			
			String nextTeamName = playerData.getNextTeam();
			if (nextTeamName != null)
			{
				SiegeTeam nextTeam = getTeam(nextTeamName);
				if (nextTeam != null)
				{
					SiegeTeam team = getPlayerTeam(entityplayer);
					team.leavePlayer(entityplayer);
					nextTeam.joinPlayer(entityplayer);
				}
				
				playerData.setNextTeam(null);
			}
			
			entityplayer.inventory.clearInventory(null, -1);
			
			int dim = entityplayer.dimension;
			ChunkCoordinates coords = entityplayer.getBedLocation(dim);
			boolean forced = entityplayer.isSpawnForced(dim);
			
			BackupSpawnPoint bsp = new BackupSpawnPoint(dim, coords, forced);
			playerData.setBackupSpawnPoint(bsp);
			markDirty();
			
			SiegeTeam team = getPlayerTeam(entityplayer);
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
		
		String kitName = playerData.getChosenKit();
		if (kitName == null || !team.containsKit(kitName))
		{
			kitName = team.getRandomKitName(entityplayer.getRNG());
			messagePlayer(entityplayer, "No kit chosen! Using a random kit: " + kitName);
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
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("SiegeID", siegeID.toString());
		nbt.setString("Name", siegeName);
		
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
