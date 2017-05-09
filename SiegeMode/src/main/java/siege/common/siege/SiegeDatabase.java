package siege.common.siege;

import java.io.File;
import java.util.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.apache.commons.lang3.StringUtils;

import siege.common.SiegeMode;
import cpw.mods.fml.common.FMLLog;

public class SiegeDatabase
{
	private static Map<String, Siege> siegeMap = new HashMap();
	
	public static Siege getSiege(String name)
	{
		return siegeMap.get(name);
	}
	
	public static boolean siegeExists(String name)
	{
		return siegeMap.get(name) != null;
	}
	
	public static List<String> getAllSiegeNames()
	{
		return new ArrayList(siegeMap.keySet());
	}
	
	public static boolean validSiegeName(String name)
	{
		return StringUtils.isAlphanumeric(name.replaceAll("_", ""));
	}
	
	public static void putAndSaveSiege(Siege siege)
	{
		siegeMap.put(siege.getSiegeName(), siege);
		saveSiegeToFile(siege);
	}
	
	public static void updateAllSieges(World world)
	{
		for (Siege siege : siegeMap.values())
		{
			if (siege.isSiegeWorld(world))
			{
				siege.updateSiege(world);
			}
		}
	}
	
	public static Siege getSiegeForPlayer(EntityPlayer entityplayer)
	{
		for (Siege siege : siegeMap.values())
		{
			if (siege.hasPlayer(entityplayer))
			{
				return siege;
			}
		}
		return null;
	}
	
	public static void updatePlayerInSiege(EntityPlayer entityplayer)
	{
		World world = entityplayer.worldObj;
		Siege siege = getSiegeForPlayer(entityplayer);
		if (siege != null && siege.isSiegeWorld(world))
		{
			siege.updatePlayer(entityplayer);
		}
	}
	
	private static File getOrCreateSiegeDirectory()
	{
		File dir = new File(SiegeMode.getSiegeRootDirectory(), "sieges");
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		return dir;
	}
	
	private static File getSiegeFile(Siege siege)
	{
		return getSiegeFile(siege.getSiegeName());
	}
	
	private static File getSiegeFile(String siegeName)
	{
		File siegeDir = getOrCreateSiegeDirectory();
		return new File(siegeDir, siegeName + ".dat");
	}
	
	public static boolean anyNeedSave()
	{
		for (Siege siege : siegeMap.values())
		{
			if (siege.needsSave())
			{
				return true;
			}
		}
		return false;
	}
	
	public static void save()
	{
		try
		{
			int i = 0;
			for (Siege siege : siegeMap.values())
			{
				if (siege.needsSave())
				{
					saveSiegeToFile(siege);
					i++;
				}
			}
			FMLLog.info("SiegeMode: Saved %d sieges", i);
		}
		catch (Exception e)
		{
			FMLLog.severe("Error saving siege data");
			e.printStackTrace();
		}
	}
	
	public static void reloadAll()
	{
		siegeMap.clear();
		try
		{
			File siegeDir = getOrCreateSiegeDirectory();
			File[] siegeFiles = siegeDir.listFiles();
			int i = 0;
			for (File dat : siegeFiles)
			{
				Siege siege = loadSiegeFromFile(dat);
				siegeMap.put(siege.getSiegeName(), siege);
				i++;
			}
			FMLLog.info("SiegeMode: Loaded %d sieges", i);
		}
		catch (Exception e)
		{
			FMLLog.severe("Error loading siege data");
			e.printStackTrace();
		}
	}
	
	private static Siege loadSiegeFromFile(File siegeFile)
	{
		try
		{
			NBTTagCompound nbt = SiegeMode.loadNBTFromFile(siegeFile);
			Siege siege = new Siege();
			siege.readFromNBT(nbt);
			return siege;
		}
		catch (Exception e)
		{
			FMLLog.severe("SiegeMode: Error loading siege %s", siegeFile.getName());
			e.printStackTrace();
		}
		return null;
	}

	public static void saveSiegeToFile(Siege siege)
	{
		try
		{
			NBTTagCompound nbt = new NBTTagCompound();
			siege.writeToNBT(nbt);
			SiegeMode.saveNBTToFile(getSiegeFile(siege), nbt);
		}
		catch (Exception e)
		{
			FMLLog.severe("SiegeMode: Error saving siege %s", siege.getSiegeName());
			e.printStackTrace();
		}
	}
}
