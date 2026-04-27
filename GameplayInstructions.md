# Gameplay Instructions
In the actual state, to use any command, you need to have Administrator Permissions (OP), it's can be done by using the <code>/op self</code>  command.
## 1. World Settings
To enable PvP, you will need to enable PvP in World Settings. If you have an dedicated server, you can use <code>/world config pvp true</code> to enable PvP without needing to quit the World.

## 2. Game / Mod Settings
In game, use the <code>/kott gui</code> command to open the UI Interface to configure and start the KOTT (King Of The Tale) match.

### GUI Configuration
#### CHOOSE THE TEAM COUNT
&nbsp; &nbsp; This configuration sets the amount of Teams in the match. (Default: 1), if set an value above 1, it will automatically balance the teams, defining all players from the world to an team. **It's under development, expect a Team Selection menu system in future.**
#### CHOOSE THE AREA SIZE
&nbsp; &nbsp; This configuration sets the Point Zone (Zone which allow teams to mark points) area size in meters(Default: 100). Increasing this value, will increase the total match area too, and make the Team Bases far for each other.
#### CHOOSE THE WORLD
&nbsp; &nbsp; This entry, allows you to choose which world to start the match by it's name. **Yes, you can start 2 differents matches in 2 differents worlds**. For default it uses the actual World Name (Name from which World the commaand was executed).
#### SAFE (WIP and Disabled)
#### LOOP 
&nbsp; &nbsp; This checkbox allows you to choose if you want to restart the match after ends (Default: false). If you mark this box, when the match ends, by <code>/kott end</code> or when any team reaches 100 points, the match will be re-created in another place in the same World.
#### START

## 3. In-Match
#### Team Base
&nbsp; &nbsp; Inside your base you can buy items in the Weapon Shop NPC, for buy items you need money, you can get money by marking points to your team or killing enemies (More options in future), and using <code>/kott money get (value)</code> you can physically get the money to trade.
#### Point Zone
&nbsp; &nbsp; When inside the Point Zone, the team with more players inside that Zone will mark an point to that team, the first team to reach 100 points is the Winner.
#### Outro
##### Team
&nbsp; &nbsp; In the actual mod state, to identify who is from your team, when a match starts, is giving an Armband with the respective team color to each player, you must use it to avoid teamkill.
##### Death
&nbsp; &nbsp; When you die, your inventory is cleared and is given to you an pistol + 20 crude arrows (ammo), so you are never unarmed.
##### Fighter NPC
&nbsp; &nbsp; Every 30 seconds, the game tries to spawn an armed NPC, you need take caution agains't him, because he have an insane aim :-D.
