package siege.common.siege;

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
}
