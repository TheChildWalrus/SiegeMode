package siege.common.siege;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import siege.common.kit.Kit;

public class SiegePlayerData
{
	private Siege theSiege;
	
	private boolean warnedClearInv = false;
	
	private BackupSpawnPoint backupSpawnPoint;
	private UUID currentKit;
	private UUID chosenKit;
	private String nextTeam;
	
	private int kills;
	private int deaths;
	private int killstreak;

	public SiegePlayerData(Siege siege)
	{
		theSiege = siege;
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setBoolean("WarnedClearInv", warnedClearInv);
		
		if (backupSpawnPoint != null)
		{
			nbt.setInteger("BSP_Dim", backupSpawnPoint.dimension);
			ChunkCoordinates bspCoords = backupSpawnPoint.spawnCoords;
			nbt.setInteger("BSP_X", bspCoords.posX);
			nbt.setInteger("BSP_Y", bspCoords.posY);
			nbt.setInteger("BSP_Z", bspCoords.posZ);
			nbt.setBoolean("BSP_Forced", backupSpawnPoint.spawnForced);
		}
		
		if (currentKit != null)
		{
			nbt.setString("CurrentKit", currentKit.toString());
		}
		
		if (chosenKit != null)
		{
			nbt.setString("Kit", chosenKit.toString());
		}
		
		if (nextTeam != null)
		{
			nbt.setString("NextTeam", nextTeam);
		}
		
		nbt.setInteger("Kills", kills);
		nbt.setInteger("Deaths", deaths);
		nbt.setInteger("Killstreak", killstreak);
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		warnedClearInv = nbt.getBoolean("WarnedClearInv");
		
		backupSpawnPoint = null;
		if (nbt.hasKey("BSP_Dim"))
		{
			int bspDim = nbt.getInteger("BSP_Dim");
			int bspX = nbt.getInteger("BSP_X");
			int bspY = nbt.getInteger("BSP_Y");
			int bspZ = nbt.getInteger("BSP_Z");
			boolean bspForced = nbt.getBoolean("BSP_Forced");
			ChunkCoordinates bspCoords = new ChunkCoordinates(bspX, bspY, bspZ);
			backupSpawnPoint = new BackupSpawnPoint(bspDim, bspCoords, bspForced);
		}
		
		if (nbt.hasKey("CurrentKit"))
		{
			currentKit = UUID.fromString(nbt.getString("CurrentKit"));
		}
		
		if (nbt.hasKey("Kit"))
		{
			chosenKit = UUID.fromString(nbt.getString("Kit"));
		}
		
		nextTeam = nbt.getString("NextTeam");
		
		kills = nbt.getInteger("Kills");
		deaths = nbt.getInteger("Deaths");
		killstreak = nbt.getInteger("Killstreak");
	}
	
	public boolean getWarnedClearInv()
	{
		return warnedClearInv;
	}
	
	public void setWarnedClearInv(boolean flag)
	{
		warnedClearInv = flag;
		theSiege.markDirty();
	}
	
	public BackupSpawnPoint getBackupSpawnPoint()
	{
		return backupSpawnPoint;
	}
	
	public void setBackupSpawnPoint(BackupSpawnPoint bsp)
	{
		backupSpawnPoint = bsp;
		theSiege.markDirty();
	}
	
	public UUID getCurrentKit()
	{
		return currentKit;
	}

	public void setCurrentKit(Kit kit)
	{
		currentKit = kit == null ? null : kit.getKitID();
		theSiege.markDirty();
	}
	
	public UUID getChosenKit()
	{
		return chosenKit;
	}

	public void setChosenKit(Kit kit)
	{
		chosenKit = kit == null ? null : kit.getKitID();
		theSiege.markDirty();
	}
	
	public String getNextTeam()
	{
		return nextTeam;
	}

	public void setNextTeam(String team)
	{
		nextTeam = team;
		theSiege.markDirty();
	}
	
	public int getKills()
	{
		return kills;
	}
	
	public void onKill()
	{
		kills++;
		killstreak++;
		theSiege.markDirty();
	}
	
	public int getDeaths()
	{
		return deaths;
	}
	
	public void onDeath()
	{
		deaths++;
		killstreak = 0;
		theSiege.markDirty();
	}
	
	public int getKillstreak()
	{
		return killstreak;
	}
}
