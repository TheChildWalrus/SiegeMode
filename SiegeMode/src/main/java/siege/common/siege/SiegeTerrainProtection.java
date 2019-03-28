package siege.common.siege;

import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class SiegeTerrainProtection
{
	private static final int MESSAGE_INTERVAL_SECONDS = 2;
	private static final Map<UUID, Long> lastPlayerMsgTimes = new HashMap();
	
	public static boolean isProtected(EntityPlayer entityplayer, World world, int i, int j, int k)
	{
		if (!entityplayer.capabilities.isCreativeMode)
		{
			double x = i + 0.5D;
			double y = j + 0.5D;
			double z = k + 0.5D;
			
			List<Siege> activeSieges = SiegeDatabase.getActiveSiegesAtPosition(x, y, z);
			for (Siege siege : activeSieges)
			{
				if (siege.getTerrainProtect())
				{
					return true;
				}
			}
			
			List<Siege> inactiveSieges = SiegeDatabase.getInactiveSiegesAtPosition(x, y, z);
			for (Siege siege : inactiveSieges)
			{
				if (siege.getTerrainProtectInactive())
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public static void warnPlayer(EntityPlayer entityplayer, String message)
	{
		UUID playerID = entityplayer.getUniqueID();
		long currentTimeMs = System.currentTimeMillis();
		boolean send = true;
		
		if (lastPlayerMsgTimes.containsKey(playerID))
		{
			long lastMsgTimeMs = lastPlayerMsgTimes.get(playerID);
			if (currentTimeMs - lastMsgTimeMs < MESSAGE_INTERVAL_SECONDS * 1000)
			{
				send = false;
			}
		}
		
		if (send)
		{
			Siege.warnPlayer(entityplayer, message);
			lastPlayerMsgTimes.put(playerID, currentTimeMs);
		}
	}
}
