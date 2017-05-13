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
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;
import siege.common.kit.KitDatabase;
import siege.common.siege.*;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
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
					SiegeMode.clearPlayerInv(entityplayer);
				}
				Siege.setHasSiegeGivenKit(entityplayer, false);
			}
		}
	}
	
	@SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedOutEvent event)
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
		
		if (!world.canMineBlock(entityplayer, i, j, k))
		{
			return;
		}
		if (!entityplayer.canPlayerEdit(i, j, k, side, itemstack))
		{
			return;
		}
		
		Item item = itemstack == null ? null : itemstack.getItem();
		boolean isPlacingItem = item != null && (item instanceof ItemBlock || item instanceof ItemReed || item instanceof ItemBed || item instanceof ItemDoor);
		if ((event.action == Action.RIGHT_CLICK_BLOCK && isPlacingItem) || event.action == Action.LEFT_CLICK_BLOCK)
		{
			Block block = world.getBlock(i, j, k);
			int meta = world.getBlockMetadata(i, j, k);
			
			boolean sendWarn = event.action != Action.LEFT_CLICK_BLOCK;
			if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, i, j, k, sendWarn))
			{
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
		
		if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, i, j, k, true))
		{
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
			
			if (!world.isRemote && SiegeTerrainProtection.isProtected(entityplayer, world, i, j, k, true))
			{
				event.setCanceled(true);
				return;
			}
		}
	}
}
