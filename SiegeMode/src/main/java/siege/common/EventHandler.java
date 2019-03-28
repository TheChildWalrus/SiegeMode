package siege.common;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;
import siege.common.siege.*;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
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
				if (KitDatabase.anyNeedSave())
				{
					KitDatabase.save();
				}
				
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
			if (Siege.hasSiegeGivenKit(entityplayer) && SiegeDatabase.getActiveSiegeForPlayer(entityplayer) == null)
			{
				if (!entityplayer.capabilities.isCreativeMode)
				{
					Kit.clearPlayerInvAndKit(entityplayer);
				}
				Siege.setHasSiegeGivenKit(entityplayer, false);
				Siege.dispel(entityplayer);
			}
		}
	}
	
	@SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event)
	{
		EntityPlayer entityplayer = event.player;
		World world = entityplayer.worldObj;
		
		if (!world.isRemote)
		{
			Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
			if (activeSiege != null)
			{
				activeSiege.onPlayerLogin((EntityPlayerMP)entityplayer);
			}
		}
	}
	
	@SubscribeEvent
    public void onPlayerLoggedOut(PlayerLoggedOutEvent event)
	{
		EntityPlayer entityplayer = event.player;
		World world = entityplayer.worldObj;
		
		if (!world.isRemote)
		{
			Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
			if (activeSiege != null)
			{
				activeSiege.onPlayerLogout((EntityPlayerMP)entityplayer);
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingAttacked(LivingAttackEvent event)
	{
		EntityLivingBase entity = event.entityLiving;
		DamageSource source = event.source;
		
		if (entity instanceof EntityPlayer)
		{
			EntityPlayer entityplayer = (EntityPlayer)entity;
			if (!entityplayer.worldObj.isRemote && !entityplayer.capabilities.isCreativeMode)
			{
				Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
				if (activeSiege != null)
				{
					SiegeTeam team = activeSiege.getPlayerTeam(entityplayer);
					
					Entity attacker = source.getEntity();
					if (attacker instanceof EntityPlayer)
					{
						EntityPlayer attackingPlayer = (EntityPlayer)attacker;
						
						if (attackingPlayer != entityplayer && team.containsPlayer(attackingPlayer) && !activeSiege.getFriendlyFire())
						{
							event.setCanceled(true);
							return;
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		EntityLivingBase entity = event.entityLiving;
		DamageSource source = event.source;
		
		if (entity instanceof EntityPlayer)
		{
			EntityPlayer entityplayer = (EntityPlayer)entity;
			if (!entityplayer.worldObj.isRemote)
			{
				Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
				if (activeSiege != null)
				{
					activeSiege.onPlayerDeath(entityplayer, source);
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
	public void onItemToss(ItemTossEvent event)
	{
		EntityPlayer entityplayer = event.player;
		if (!entityplayer.worldObj.isRemote)
		{
			Siege activeSiege = SiegeDatabase.getActiveSiegeForPlayer(entityplayer);
			if (activeSiege != null && !entityplayer.capabilities.isCreativeMode)
			{
				activeSiege.warnPlayer(entityplayer, "You cannot drop items during a siege");
				event.setCanceled(true);
				return;
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingSpawnCheck(LivingSpawnEvent.CheckSpawn event)
	{
		double posX = event.x;
		double posY = event.y;
		double posZ = event.z;
		
		List<Siege> siegesHere = SiegeDatabase.getActiveSiegesAtPosition(posX, posY, posZ);
		for (Siege siege : siegesHere)
		{
			if (!siege.getMobSpawning())
			{
				event.setResult(Result.DENY);
				return;
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockInteract(PlayerInteractEvent event)
	{
		EntityPlayer entityplayer = event.entityPlayer;
		World world = entityplayer.worldObj;
		ItemStack itemstack = entityplayer.inventory.getCurrentItem();
		int i = event.x;
		int j = event.y;
		int k = event.z;
		int side = event.face;

		Action action = event.action;

		Item item = itemstack == null ? null : itemstack.getItem();
		boolean isPlacingItem = item != null && (item instanceof ItemBlock || item instanceof ItemReed || item instanceof ItemBed || item instanceof ItemDoor);
		if ((action == Action.RIGHT_CLICK_BLOCK && isPlacingItem) || action == Action.LEFT_CLICK_BLOCK)
		{
			Block block = world.getBlock(i, j, k);
			int meta = world.getBlockMetadata(i, j, k);
			
			if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, i, j, k))
			{
				SiegeTerrainProtection.warnPlayer(entityplayer, "You cannot break or place blocks during the siege");
				event.setCanceled(true);
				
				if (block instanceof BlockDoor)
				{
					world.markBlockForUpdate(i, j - 1, k);
					world.markBlockForUpdate(i, j, k);
					world.markBlockForUpdate(i, j + 1, k);
				}
				
				return;
			}
		}
		
		if (action == Action.RIGHT_CLICK_BLOCK)
		{
			TileEntity te = world.getTileEntity(i, j, k);
			if (te instanceof IInventory)
			{
				if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, i, j, k))
				{
					SiegeTerrainProtection.warnPlayer(entityplayer, "You cannot interact with containers during the siege");
					event.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event)
	{
		EntityPlayer entityplayer = event.getPlayer();
		Block block = event.block;
		World world = event.world;
		int i = event.x;
		int j = event.y;
		int k = event.z;
		
		if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, i, j, k))
		{
			SiegeTerrainProtection.warnPlayer(entityplayer, "You cannot break or place blocks during the siege");
			event.setCanceled(true);
			return;
		}
	}
	
	@SubscribeEvent
	public void onFillBucket(FillBucketEvent event)
	{
		EntityPlayer entityplayer = event.entityPlayer;
		World world = event.world;
		MovingObjectPosition target = event.target;
		
		if (target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
		{
			int i = target.blockX;
			int j = target.blockY;
			int k = target.blockZ;
			
			if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, i, j, k))
			{
				SiegeTerrainProtection.warnPlayer(entityplayer, "You cannot break or place blocks during the siege");
				event.setCanceled(true);
				return;
			}
		}
	}
	
	@SubscribeEvent
    public void onEntityAttackedByPlayer(AttackEntityEvent event)
	{
		Entity entity = event.target;
		World world = entity.worldObj;
		EntityPlayer entityplayer = event.entityPlayer;
		
		if (!(entity instanceof EntityLivingBase))
		{
			int i = MathHelper.floor_double(entity.posX);
			int j = MathHelper.floor_double(entity.posY);
			int k = MathHelper.floor_double(entity.posZ);
			
			if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, i, j, k))
			{
				SiegeTerrainProtection.warnPlayer(entityplayer, "You cannot destroy non-living entities during the siege");
				event.setCanceled(true);
				return;
			}
		}
	}
}
