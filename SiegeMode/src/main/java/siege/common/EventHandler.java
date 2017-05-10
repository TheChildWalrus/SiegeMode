package siege.common;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
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

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		EntityPlayer entityplayer = event.entityPlayer;
		World world = event.world;
		Action action = event.action;
		int i = event.x;
		int j = event.y;
		int k = event.z;
		
		if (!world.isRemote)
		{
			Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
			if (activeSiege != null && !entityplayer.capabilities.isCreativeMode)
			{
				if (action == Action.RIGHT_CLICK_BLOCK)
				{
					Block block = world.getBlock(i, j, k);
					TileEntity te = world.getTileEntity(i, j, k);
					if (te instanceof IInventory)
					{
						activeSiege.messagePlayer(entityplayer, "You cannot interact with containers during a siege");
						event.setCanceled(true);
						return;
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onItemToss(ItemTossEvent event)
	{
		EntityPlayer entityplayer = event.player;
		if (!entityplayer.worldObj.isRemote)
		{
			Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
			if (activeSiege != null && !entityplayer.capabilities.isCreativeMode)
			{
				activeSiege.messagePlayer(entityplayer, "You cannot drop items during a siege");
				event.setCanceled(true);
				return;
			}
		}
	}
}
