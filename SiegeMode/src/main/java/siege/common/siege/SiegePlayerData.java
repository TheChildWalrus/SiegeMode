package siege.common.siege;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

public class SiegePlayerData
{
	private Siege theSiege;
	
	private BackupSpawnPoint backupSpawnPoint;
	private String chosenKit;
	private String nextTeam;

	public SiegePlayerData(Siege siege)
	{
		theSiege = siege;
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		if (backupSpawnPoint != null)
		{
			nbt.setInteger("BSP_Dim", backupSpawnPoint.dimension);
			ChunkCoordinates bspCoords = backupSpawnPoint.spawnCoords;
			nbt.setInteger("BSP_X", bspCoords.posX);
			nbt.setInteger("BSP_Y", bspCoords.posY);
			nbt.setInteger("BSP_Z", bspCoords.posZ);
			nbt.setBoolean("BSP_Forced", backupSpawnPoint.spawnForced);
		}
		
		if (chosenKit != null)
		{
			nbt.setString("Kit", chosenKit);
		}
		
		if (nextTeam != null)
		{
			nbt.setString("NextTeam", nextTeam);
		}
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
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
		
		chosenKit = nbt.getString("Kit");
		nextTeam = nbt.getString("NextTeam");
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
	
	public String getChosenKit()
	{
		return chosenKit;
	}

	public void setChosenKit(String kit)
	{
		chosenKit = kit;
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
}
