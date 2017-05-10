package siege.common;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import siege.common.siege.Siege;
import siege.common.siege.SiegeDatabase;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import cpw.mods.fml.common.gameevent.*;
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
			SiegeDatabase.updateActiveSieges(world);
			
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
	public void onLivingDeath(LivingDeathEvent event)
	{
		EntityLivingBase entity = event.entityLiving;
		if (entity instanceof EntityPlayer)
		{
			EntityPlayer entityplayer = (EntityPlayer)entity;
			if (!entityplayer.worldObj.isRemote)
			{
				Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
				if (activeSiege != null)
				{
					activeSiege.onPlayerDeath(entityplayer);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		EntityPlayer entityplayer = event.player;
		if (!entityplayer.worldObj.isRemote)
		{
			Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
			if (activeSiege != null)
			{
				activeSiege.onPlayerRespawn(entityplayer);
			}
		}
	}
}
