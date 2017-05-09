package siege.common;

import java.io.*;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;
import siege.common.kit.*;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = "siegemode", version = "1.0", acceptableRemoteVersions = "*")
public class SiegeMode
{
	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		KitDatabase.reloadAll();
		event.registerServerCommand(new CommandKit());
		event.registerServerCommand(new CommandKitCreate());
	}
	
	public static File getSiegeRootDirectory()
	{
		return new File(DimensionManager.getCurrentSaveRootDirectory(), "siegemode");
	}
	
	public static NBTTagCompound loadNBTFromFile(File file) throws FileNotFoundException, IOException
	{
		if (file.exists())
		{
			return CompressedStreamTools.readCompressed(new FileInputStream(file));
		}
		else
		{
			return new NBTTagCompound();
		}
	}
	
	public static void saveNBTToFile(File file, NBTTagCompound nbt) throws FileNotFoundException, IOException
	{
		CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(file));
	}
}
