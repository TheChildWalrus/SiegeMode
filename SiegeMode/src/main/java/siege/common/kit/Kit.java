package siege.common.kit;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class Kit
{
	private String kitName;
	
	private ItemStack[] armorItems = new ItemStack[4];
	private ItemStack heldItem;
	private List<ItemStack> otherItems = new ArrayList();
	
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
}
