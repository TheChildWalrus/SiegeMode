package siege.common.kit;

import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class Kit
{
	private String kitName;
	
	private ItemStack[] armorItems = new ItemStack[4];
	private ItemStack heldItem;
	private List<ItemStack> otherItems = new ArrayList();
	
	public String getKitName()
	{
		return kitName;
	}
	
	public void applyTo(EntityPlayer entityplayer)
	{
		entityplayer.inventory.clearInventory(null, -1);
		
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
	
	public static Kit createFrom(EntityPlayer entityplayer, String name)
	{
		Kit kit = new Kit();
		kit.kitName = name;
		
		for (int i = 0; i < 4; i++)
		{
			ItemStack armor = entityplayer.getEquipmentInSlot(4 - i);
			kit.armorItems[i] = ItemStack.copyItemStack(armor);
		}
		
		for (int i = 0; i < entityplayer.inventory.mainInventory.length; i++)
		{
			ItemStack itemstack = entityplayer.inventory.mainInventory[i];
			if (i == entityplayer.inventory.currentItem)
			{
				kit.heldItem = ItemStack.copyItemStack(itemstack);
			}
			else
			{
				if (itemstack != null)
				{
					kit.otherItems.add(ItemStack.copyItemStack(itemstack));
				}
			}
		}
		
		return kit;
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("Name", kitName);
		
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
		kitName = nbt.getString("Name");
		
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
