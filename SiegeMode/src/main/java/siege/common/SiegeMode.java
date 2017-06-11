package siege.common;

import java.io.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import siege.common.kit.*;
import siege.common.siege.*;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = "siegemode", version = "1.0", acceptableRemoteVersions = "*")
public class SiegeMode
{
	@Mod.Instance
	public static SiegeMode instance;
	
	private EventHandler eventHandler;
	
	@Mod.EventHandler
	public void load(FMLInitializationEvent event)
	{
		eventHandler = new EventHandler();
		FMLCommonHandler.instance().bus().register(eventHandler);
		MinecraftForge.EVENT_BUS.register(eventHandler);
	}
	
	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		KitDatabase.reloadAll();
		SiegeDatabase.reloadAll();
		event.registerServerCommand(new CommandKit());
		event.registerServerCommand(new CommandSiegeSetup());
		event.registerServerCommand(new CommandSiegePlay());
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
