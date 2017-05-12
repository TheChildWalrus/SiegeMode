package siege.common.kit;

import java.util.*;

import siege.common.SiegeMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class Kit
{
	private boolean needsSave = false;
	private boolean deleted = false;
	
	private UUID kitID;
	private String kitName;
	
	private ItemStack[] armorItems = new ItemStack[4];
	private ItemStack heldItem;
	private List<ItemStack> otherItems = new ArrayList();
	
	public Kit()
	{
		kitID = UUID.randomUUID();
	}
	
	public UUID getKitID()
	{
		return kitID;
	}
	
	public String getKitName()
	{
		return kitName;
	}
	
	public void rename(String s)
	{
		String oldName = kitName;
		kitName = s;
		markDirty();
		KitDatabase.renameKit(this, oldName);
	}
	
	public void applyTo(EntityPlayer entityplayer)
	{
		SiegeMode.clearPlayerInv(entityplayer);
		
		for (int i = 0; i < 4; i++)
		{
			ItemStack armor = ItemStack.copyItemStack(armorItems[i]);
			if (armor != null)
			{
				entityplayer.setCurrentItemOrArmor(4 - i, armor);
			}
		}
		
		entityplayer.setCurrentItemOrArmor(0, ItemStack.copyItemStack(heldItem));
		
		for (ItemStack itemstack : otherItems)
		{
			entityplayer.inventory.addItemStackToInventory(ItemStack.copyItemStack((itemstack)));
		}
	}
	
	public void createFrom(EntityPlayer entityplayer)
	{
		Arrays.fill(armorItems, null);
		for (int i = 0; i < 4; i++)
		{
			ItemStack armor = entityplayer.getEquipmentInSlot(4 - i);
			armorItems[i] = ItemStack.copyItemStack(armor);
		}
		
		heldItem = null;
		otherItems.clear();
		for (int i = 0; i < entityplayer.inventory.mainInventory.length; i++)
		{
			ItemStack itemstack = entityplayer.inventory.mainInventory[i];
			if (i == entityplayer.inventory.currentItem)
			{
				heldItem = ItemStack.copyItemStack(itemstack);
			}
			else
			{
				if (itemstack != null)
				{
					otherItems.add(ItemStack.copyItemStack(itemstack));
				}
			}
		}
		
		markDirty();
	}
	
	public static Kit createNewKit(EntityPlayer entityplayer, String name)
	{
		Kit kit = new Kit();
		kit.kitName = name;
		kit.createFrom(entityplayer);
		return kit;
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
	
	public boolean isDeleted()
	{
		return deleted;
	}
	
	public void deleteKit()
	{
		deleted = true;
		markDirty();
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("KitID", kitID.toString());
		nbt.setString("Name", kitName);
		nbt.setBoolean("Deleted", deleted);
		
		NBTTagList armorTags = new NBTTagList();
		for (int i = 0; i < armorItems.length; i++)
		{
			ItemStack armorItem = armorItems[i];
			if (armorItem != null)
			{
				NBTTagCompound itemData = new NBTTagCompound();
				itemData.setByte("ArmorSlot", (byte)i);
				armorItem.writeToNBT(itemData);
				armorTags.appendTag(itemData);
			}
		}
		nbt.setTag("ArmorItems", armorTags);
		
		if (heldItem != null)
		{
			NBTTagCompound heldData = new NBTTagCompound();
			heldItem.writeToNBT(heldData);
			nbt.setTag("HeldItem", heldData);
		}
		
		if (!otherItems.isEmpty())
		{
			NBTTagList otherTags = new NBTTagList();
			for (ItemStack itemstack : otherItems)
			{
				NBTTagCompound itemData = new NBTTagCompound();
				itemstack.writeToNBT(itemData);
				otherTags.appendTag(itemData);
			}
			nbt.setTag("OtherItems", otherTags);
		}
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		kitID = UUID.fromString(nbt.getString("KitID"));
		kitName = nbt.getString("Name");
		deleted = nbt.getBoolean("Deleted");
		
		Arrays.fill(armorItems, null);
		if (nbt.hasKey("ArmorItems"))
		{
			NBTTagList otherTags = nbt.getTagList("ArmorItems", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < otherTags.tagCount(); i++)
			{
				NBTTagCompound itemData = otherTags.getCompoundTagAt(i);
				int slot = itemData.getByte("ArmorSlot");
				if (slot >= 0 && slot < armorItems.length)
				{
					ItemStack itemstack = ItemStack.loadItemStackFromNBT(itemData);
					if (itemstack != null)
					{
						armorItems[slot] = itemstack;
					}
				}
			}
		}
		
		if (nbt.hasKey("HeldItem"))
		{
			NBTTagCompound heldData = nbt.getCompoundTag("HeldItem");
			heldItem = ItemStack.loadItemStackFromNBT(heldData);
		}
		
		otherItems.clear();
		if (nbt.hasKey("OtherItems"))
		{
			NBTTagList otherTags = nbt.getTagList("OtherItems", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < otherTags.tagCount(); i++)
			{
				NBTTagCompound itemData = otherTags.getCompoundTagAt(i);
				ItemStack itemstack = ItemStack.loadItemStackFromNBT(itemData);
				if (itemstack != null)
				{
					otherItems.add(itemstack);
				}
			}
		}
	}
}
