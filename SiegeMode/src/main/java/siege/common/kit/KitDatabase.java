package siege.common.kit;

import java.io.File;
import java.util.*;

import net.minecraft.nbt.NBTTagCompound;

import org.apache.commons.lang3.StringUtils;

import siege.common.SiegeMode;
import cpw.mods.fml.common.FMLLog;

public class KitDatabase
{
	private static Map<String, Kit> kitMap = new HashMap();
	
	public static Kit getKit(String name)
	{
		return kitMap.get(name);
	}
	
	public static boolean kitExists(String name)
	{
		return kitMap.get(name) != null;
	}
	
	public static List<String> getAllKitNames()
	{
		return new ArrayList(kitMap.keySet());
	}
	
	public static boolean validKitName(String name)
	{
		return StringUtils.isAlphanumeric(name.replaceAll("_", ""));
	}
	
	public static void addAndSaveKit(Kit kit)
	{
		kitMap.put(kit.getKitName(), kit);
		saveKitToFile(kit);
	}
	
	private static File getOrCreateKitDirectory()
	{
		File dir = new File(SiegeMode.getSiegeRootDirectory(), "kits");
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		return dir;
	}
	
	private static File getKitFile(Kit kit)
	{
		return getKitFile(kit.getKitName());
	}
	
	private static File getKitFile(String kitName)
	{
		File kitDir = getOrCreateKitDirectory();
		return new File(kitDir, kitName + ".dat");
	}
	
	public static void reloadAll()
	{
		kitMap.clear();
		try
		{
			File kitDir = getOrCreateKitDirectory();
			File[] kitFiles = kitDir.listFiles();
			int i = 0;
			for (File dat : kitFiles)
			{
				Kit kit = loadKitFromFile(dat);
				kitMap.put(kit.getKitName(), kit);
				i++;
			}
			FMLLog.info("SiegeMode: Loaded %d kits", i);
		}
		catch (Exception e)
		{
			FMLLog.severe("Error loading kit data");
			e.printStackTrace();
		}
	}
	
	private static Kit loadKitFromFile(File kitFile)
	{
		try
		{
			NBTTagCompound nbt = SiegeMode.loadNBTFromFile(kitFile);
			Kit kit = new Kit();
			kit.readFromNBT(nbt);
			return kit;
		}
		catch (Exception e)
		{
			FMLLog.severe("SiegeMode: Error loading kit %s", kitFile.getName());
			e.printStackTrace();
		}
		return null;
	}

	public static void saveKitToFile(Kit kit)
	{
		try
		{
			NBTTagCompound nbt = new NBTTagCompound();
			kit.writeToNBT(nbt);
			SiegeMode.saveNBTToFile(getKitFile(kit), nbt);
		}
		catch (Exception e)
		{
			FMLLog.severe("SiegeMode: Error saving kit %s", kit.getKitName());
			e.printStackTrace();
		}
	}
}
