package siege.common.siege;

import java.util.*;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.scoreboard.*;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import siege.common.kit.Kit;
import siege.common.kit.KitDatabase;

public class SiegePlayerData
{
	private Siege theSiege;
	
	private boolean warnedClearInv = false;
	
	private BackupSpawnPoint backupSpawnPoint;
	private UUID currentKit;
	private UUID chosenKit;
	private String nextTeam;
	
	private int kills;
	private int deaths;
	private int killstreak;
	
	private ScoreObjective lastSentSiegeObjective = null;
	private int siegeObjectiveCount = 0;

	public SiegePlayerData(Siege siege)
	{
		theSiege = siege;
	}
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setBoolean("WarnedClearInv", warnedClearInv);
		
		if (backupSpawnPoint != null)
		{
			nbt.setInteger("BSP_Dim", backupSpawnPoint.dimension);
			ChunkCoordinates bspCoords = backupSpawnPoint.spawnCoords;
			nbt.setInteger("BSP_X", bspCoords.posX);
			nbt.setInteger("BSP_Y", bspCoords.posY);
			nbt.setInteger("BSP_Z", bspCoords.posZ);
			nbt.setBoolean("BSP_Forced", backupSpawnPoint.spawnForced);
		}
		
		if (currentKit != null)
		{
			nbt.setString("CurrentKit", currentKit.toString());
		}
		
		if (chosenKit != null)
		{
			nbt.setString("Kit", chosenKit.toString());
		}
		
		if (nextTeam != null)
		{
			nbt.setString("NextTeam", nextTeam);
		}
		
		nbt.setInteger("Kills", kills);
		nbt.setInteger("Deaths", deaths);
		nbt.setInteger("Killstreak", killstreak);
	}
	
	public void readFromNBT(NBTTagCompound nbt)
	{
		warnedClearInv = nbt.getBoolean("WarnedClearInv");
		
		backupSpawnPoint = null;
		if (nbt.hasKey("BSP_Dim"))
		{
			int bspDim = nbt.getInteger("BSP_Dim");
			int bspX = nbt.getInteger("BSP_X");
			int bspY = nbt.getInteger("BSP_Y");
			int bspZ = nbt.getInteger("BSP_Z");
			boolean bspForced = nbt.getBoolean("BSP_Forced");
			ChunkCoordinates bspCoords = new ChunkCoordinates(bspX, bspY, bspZ);
			backupSpawnPoint = new BackupSpawnPoint(bspDim, bspCoords, bspForced);
		}
		
		if (nbt.hasKey("CurrentKit"))
		{
			currentKit = UUID.fromString(nbt.getString("CurrentKit"));
		}
		
		if (nbt.hasKey("Kit"))
		{
			chosenKit = UUID.fromString(nbt.getString("Kit"));
		}
		
		nextTeam = nbt.getString("NextTeam");
		
		kills = nbt.getInteger("Kills");
		deaths = nbt.getInteger("Deaths");
		killstreak = nbt.getInteger("Killstreak");
	}
	
	public boolean getWarnedClearInv()
	{
		return warnedClearInv;
	}
	
	public void setWarnedClearInv(boolean flag)
	{
		warnedClearInv = flag;
		theSiege.markDirty();
	}
	
	public BackupSpawnPoint getBackupSpawnPoint()
	{
		return backupSpawnPoint;
	}
	
	public void setBackupSpawnPoint(BackupSpawnPoint bsp)
	{
		backupSpawnPoint = bsp;
		theSiege.markDirty();
	}
	
	public UUID getCurrentKit()
	{
		return currentKit;
	}

	public void setCurrentKit(Kit kit)
	{
		currentKit = kit == null ? null : kit.getKitID();
		theSiege.markDirty();
	}
	
	public UUID getChosenKit()
	{
		return chosenKit;
	}

	public void setChosenKit(Kit kit)
	{
		chosenKit = kit == null ? null : kit.getKitID();
		theSiege.markDirty();
	}
	
	public String getNextTeam()
	{
		return nextTeam;
	}

	public void setNextTeam(String team)
	{
		nextTeam = team;
		theSiege.markDirty();
	}
	
	public int getKills()
	{
		return kills;
	}
	
	public void onKill()
	{
		kills++;
		killstreak++;
		theSiege.markDirty();
	}
	
	public int getDeaths()
	{
		return deaths;
	}
	
	public void onDeath()
	{
		deaths++;
		killstreak = 0;
		theSiege.markDirty();
	}
	
	public int getKillstreak()
	{
		return killstreak;
	}
	
	public void updateSiegeScoreboard(EntityPlayerMP entityplayer, boolean forceClear)
	{
		World world = entityplayer.worldObj;
		SiegeTeam team = theSiege.getPlayerTeam(entityplayer);

		Scoreboard scoreboard = world.getScoreboard();
		ScoreObjective siegeObjective = null;
		
		// TODO: change this to account for when the siege ends: remove scoreboards / start a timer etc.
		boolean inSiege = team != null;
		if (inSiege && !forceClear)
		{
			// create a new siege objective, with a new name, so we can send all the scores one by one, and only then display it
			String newObjName = "siege_" + siegeObjectiveCount;
			siegeObjectiveCount++;
			siegeObjective = new ScoreObjective(scoreboard, newObjName, null);
			String displayName = "SiegeMode: " + theSiege.getSiegeName();
			siegeObjective.setDisplayName(displayName);
			
			String kitName = "";
			Kit currentKit = KitDatabase.getKit(getCurrentKit());
			if (currentKit != null)
			{
				kitName = currentKit.getKitName();
			}
			
			String timeRemaining = theSiege.isActive() ? ("Time: " + Siege.ticksToTimeString(theSiege.getTicksRemaining())) : "Ended";
			
			// clever trick to control the ordering of the objectives: put actual scores in the 'playernames', and put the desired order in the 'scores'!
			
			List<Score> allSiegeStats = new ArrayList();
			allSiegeStats.add(new Score(scoreboard, siegeObjective, timeRemaining));
			allSiegeStats.add(null);
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Team: " + team.getTeamName()));
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Kit: " + kitName));
			allSiegeStats.add(null);
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Kills: " + getKills()));
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Deaths: " + getDeaths()));
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Killstreak: " + getKillstreak()));
			allSiegeStats.add(null);
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Team K: " + team.getTeamKills()));
			allSiegeStats.add(new Score(scoreboard, siegeObjective, "Team D: " + team.getTeamDeaths()));
			
			// recreate the siege objective (or create for first time if not sent before)
			Packet pktObjective = new S3BPacketScoreboardObjective(siegeObjective, 0);
			entityplayer.playerNetServerHandler.sendPacket(pktObjective);
			
			int index = allSiegeStats.size();
			int gaps = 0;
			for (Score score : allSiegeStats)
			{
				if (score == null)
				{
					// create a unique gap string, based on how many gaps we've already had
					String gapString = "";
					for (int l = 0; l <= gaps; l++)
					{
						gapString += "-";
					}
					score = new Score(scoreboard, siegeObjective, gapString);
					gaps++;
				}
				
				// avoid string too long in packet
				String scoreName = score.getPlayerName();
				int maxLength = 16;
				if (scoreName.length() > maxLength)
				{
					scoreName = scoreName.substring(0, Math.min(scoreName.length(), maxLength));
				}
				score = new Score(score.getScoreScoreboard(), score.func_96645_d(), scoreName);
				
				score.setScorePoints(index);
				Packet pktScore = new S3CPacketUpdateScore(score, 0);
				entityplayer.playerNetServerHandler.sendPacket(pktScore);
				index--;
			}
		}
		
		// remove last objective only AFTER sending new objective & all scores
		if (lastSentSiegeObjective != null)
		{
			Packet pkt = new S3BPacketScoreboardObjective(lastSentSiegeObjective, 1);
			entityplayer.playerNetServerHandler.sendPacket(pkt);
			lastSentSiegeObjective = null;
		}
		
		// if a new objective was sent, display it
		if (siegeObjective != null)
		{
			Packet pktDisplay = new S3DPacketDisplayScoreboard(1, siegeObjective);
			entityplayer.playerNetServerHandler.sendPacket(pktDisplay);
			lastSentSiegeObjective = siegeObjective;
		}
	}
}
