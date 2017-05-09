package siege.common.siege;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class Siege
{
	private String siegeName;
	
	private int xPos;
	private int zPos;
	private int radius;
	
	private int ticksRemaining;
	
	public boolean isActive()
	{
		return ticksRemaining > 0;
	}
	
	public void updateSiege(World world)
	{
		ticksRemaining--;
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("Name", siegeName);
		
		nbt.setInteger("XPos", xPos);
		nbt.setInteger("ZPos", zPos);
		nbt.setInteger("Radius", radius);
		
		nbt.setInteger("TicksRemaining", ticksRemaining);
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		siegeName = nbt.getString("Name");
		
		xPos = nbt.getInteger("XPos");
		zPos = nbt.getInteger("ZPos");
		radius = nbt.getInteger("Radius");
		
		ticksRemaining = nbt.getInteger("TicksRemaining");
	}
}
