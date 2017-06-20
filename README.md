# Siege Mode

Siege Mode is a server-side mod, or plugin, designed to facilitate the running of 'mock sieges' on servers. These are essentially minigame battles, for fun.

Previously, admins have had to spend much time setting up such sieges: arranging command blocks, respawn points, and being online to monitor the siege! But with this mod, the setup of sieges is greatly simplified, allowing them to be run with little to no effort beyond the original setup.

The mod does not add any actual 'content' in itself, hence why it is server-side. It adds a framework of various systems and commands, and the aim is essentially to automate the tasks that admins would have to perform otherwise.

Importantly, Siege Mode is ENTIRELY SEPARATE from the LOTR mod, and will remain so.
There is no integration with any LOTR features or systems, with the exception of a workaround which disables Fellowship PVP settings during sieges, and that was added only because it is absolutely essential.

Players do NOT need to install the mod to play it on a server! The mod only needs to be installed on the server, and indeed will do nothing if installed on a client.

The mod is still in development. There are other features planned, including new game modes and victory conditions.

# Features

The primary features of the Siege Mode mod are as follows:

## Sieges

Sieges are ongoing player battles, centred around specific locations, with teams set against one another.
(The name 'siege', while not strictly accurate, is used because that's what people like to call these events.)

A siege has a central location, and a defined radius, which together define a circular 'arena'. While the siege is active, the participants are prevented from leaving the arena, and anyone else is prevented from entering.
It is possible to set protection upon the terrain within the bounds of the arena: both while the siege is active, and/or while it is inactive. This setting prevents anyone from placing or breaking blocks, and opening containers, but does not prevent interacting with other blocks such as doors and gates.

During a siege, players do not use their own items. Instead, kits are used. (see below)

## Teams

A siege must have teams for players to join.  It is possible to create more than two teams.

Each team has a pre-defined spawn location. Players will appear here when they first join, and when respawning during the siege.

Each team has a pre-defined list of kits. Optionally, these kits can be limited to only a certain number of players at once. For a kit limited to N players, this means no more than N players may have the kit selected as their next kit to use after death.

It is possible to set a 'max team difference' for balance, so that new players cannot join a team if it has too many players.

## Kits

Kits are inventory presets used in the sieges, essentially 'classes' for players to choose between.
Server admins can create kits using the /siege_kit command. (see below)

The important parts of a kit are: the held item; equipped armour; and any other items in the inventory. When using the command to create or recreate a kit, it will copy these attributes from the operator.
Any active potion effects are also copied.

Importantly, kits are independent from sieges. One kit can be reused across multiple sieges, and if edited, the changes will apply when used in all sieges.
However, it is advised to design independent kits for different sieges: so that one admin may change their kits without fear of upsetting the balance of other sieges.

## Joining and Leaving Sieges

Players may join a siege at any time, using the /siege_play command. (see below)
Before joining a siege you *must* have emptied all items from your inventory (including armour) and if not, a chat message will appear informing you to put your items away.

Upon joining a siege, players have the opportunity to choose from the available teams. They may also choose from that team's available kits, or opt for random kit selection by choosing 'random' or leaving the parameter blank. (The TAB key is very useful to suggest and autocomplete these command parameters.)

Players can change to another available kit at any point (unless it's limited) or to random kit selection, but they will keep their current kit until after they next die.

Players can leave the siege at any time using the /siege_play command, and upon doing so their inventory will be cleared.

## Victory

The winner of the siege is the team who have collectively killed the most opponents when the timer runs out.

Once the siege ends, a message will be displayed to all players. This message summarises how each team performed. It also shows the MVP ('most valuable player'; whoever scored the most kills with deaths subtracted) and the player with the longest killstreak.

Players' inventories are cleared when the siege ends.

In the future, among other features, I hope to add new victory conditions, as well as optional rewards for the winning team.

## Scoreboard

The mod exploits the vanilla scoreboard system to provide a display of various statistics during a siege. These include kill/death count, killstreak, team name, time remaining...
Since the mod is server-side, it cannot alter any aspect of the scoreboard's appearance on-screen. The scoreboard is drawn by the client; the server only sends the data.

## Commands

Here follows a complete list of all commands included in the mod and their usage.

The commands are:
```/siege_play``` - for players, to join the siege
```/siege_setup``` - for admins, to create, edit, and run sieges
```/siege_kit``` - for admins, to manage the kits used in sieges

NOTE: Use TAB key to autocomplete command parameters.
Players can use TAB to cycle through the available team / kit names when joining a siege.
The character ~ can be used in place of coordinate parameters to refer to the operator's current coordinate, e.g. when setting respawn positions.

...

### ```/siege_play```... (for players)

```
/siege_play join [siege-name] [team-name] [kit-name]
/siege_play team [team-name] [kit-name]
/siege_play kit [kit-name]
/siege_play leave
```
...

### ```/siege_setup```... (for admins)

```
/siege_setup new [siege-name]

/siege_setup edit [siege-name] rename [name]
/siege_setup edit [siege-name] setcoords [x] [z] [radius]

/siege_setup edit [siege-name] teams new [team-name]
/siege_setup edit [siege-name] teams edit [team-name] ...
/siege_setup edit [siege-name] teams edit [team-name] rename [name]
/siege_setup edit [siege-name] teams edit [team-name] kit-add [name]
/siege_setup edit [siege-name] teams edit [team-name] kit-remove [name]
/siege_setup edit [siege-name] teams edit [team-name] kit-limit [limit]
/siege_setup edit [siege-name] teams edit [team-name] kit-unlimit
/siege_setup edit [siege-name] teams edit [team-name] setspawn [x] [y] [z]
/siege_setup edit [siege-name] teams remove [team-name]

/siege_setup edit [siege-name] max-team-diff [value]
/siege_setup edit [siege-name] respawn-immunity [seconds]
/siege_setup edit [siege-name] friendly-fire [on|off]
/siege_setup edit [siege-name] mob-spawning [on|off]
/siege_setup edit [siege-name] terrain-protect [on|off]
/siege_setup edit [siege-name] terrain-protect-inactive [on|off]

/siege_setup start [siege-name] [seconds]
/siege_setup active [siege-name] extend [seconds]
/siege_setup active [siege-name] end

/siege_setup delete [siege-name]
```
...

### ```/siege_kit```... (for admins)

```
/siege_kit new [kit-name] [player]
/siege_kit apply [kit-name] [player]
/siege_kit edit [kit-name] rename [name]
/siege_kit edit [kit-name] recreate [player]
/siege_kit delete [kit-name]
```
...

Have fun!
