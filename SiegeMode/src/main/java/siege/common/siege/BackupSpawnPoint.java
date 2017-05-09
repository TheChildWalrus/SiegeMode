package siege.common.siege;

import net.minecraft.util.ChunkCoordinates;

public class BackupSpawnPoint
{
	public int dimension;
	public ChunkCoordinates spawnCoords;
	public boolean spawnForced;
	
	public BackupSpawnPoint(int dim, ChunkCoordinates coords, boolean forced)
	{
		dimension = dim;
		spawnCoords = coords;
		spawnForced = forced;
	}
}
