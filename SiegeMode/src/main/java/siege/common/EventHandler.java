package siege.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import siege.common.siege.SiegeDatabase;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class EventHandler
{
	@SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event)
	{
		World world = event.world;
		
		if (world.isRemote)
		{
			return;
		}
		
		if (event.phase == Phase.START)
		{
			if (world == DimensionManager.getWorld(0))
			{
				// loading?
			}
		}
			
		if (event.phase == Phase.END)
		{
			SiegeDatabase.updateAllSieges(world);
			
			if (world == DimensionManager.getWorld(0))
			{
				if (SiegeDatabase.anyNeedSave())
				{
					SiegeDatabase.save();
				}
			}
		}
	}
	
	@SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		EntityPlayer entityplayer = event.player;
		World world = entityplayer.worldObj;
		
		if (world.isRemote)
		{
			return;
		}
			
		if (event.phase == Phase.END)
		{
			SiegeDatabase.updatePlayerInSiege(entityplayer);
		}
	}
}
