package styles.team;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import styles.world.zone.KOTTTeamZone;
import styles.world.util.WorldBuilder;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

public class KOTTTeam {

    public final UUID teamID;
    private final List<PlayerRef> playerList = new ArrayList<>();
    private final List<NPCEntity> teamBots = new ArrayList<>();
    private final String displayName;

    private final Color teamColor;

    public int teamPoints = 0;

    private final KOTTTeamZone baseZone;
    public static final int distanceBaseFromZone = 100;

    public KOTTTeam(UUID id, String displayName, @Nonnull Vector3i basePosition, @Nonnull World world, @Nonnull UserMapMarker zoneMarker) {
        this.teamID = id;
        this.displayName = displayName;
        this.baseZone = new KOTTTeamZone(distanceBaseFromZone, basePosition, world, this, zoneMarker);
        this.teamColor = this.baseZone.getZoneMarker().getColorTint();

        genBots(1);
    }

    // Add player to the Team
    public void addPlayerRef(PlayerRef playerRef) {
        playerList.add(playerRef);
    }

    public boolean containsPlayer(PlayerRef playerRef) {
        return playerList.contains(playerRef);
    }

    public static boolean createTeam(Map<UUID, KOTTTeam> teamListRef, UUID id, String displayName, Vector3i basePosition, @Nonnull World world, @Nonnull UserMapMarker zoneMarker) {

        if(teamListRef.containsKey(id)){
            printLog("[KOTH] There is an Team with that ID already!");
            return false;
        }else{
            teamListRef.put(id, new KOTTTeam(id, displayName, basePosition, world, zoneMarker));
            return true;
        }
    }

    public CompletableFuture<Void> destroyTeamBase() {
        Vector3i basePos = getBaseZone().getPosition();
        basePos.y -= 2;
       return WorldBuilder.clearAreaSquare(basePos, 8, baseZone.getWorld()).thenCompose(unused1 -> {
            basePos.y += 4;
            return WorldBuilder.clearAreaSquare(basePos, 7, baseZone.getWorld());
        });
    }

    public void removeFromTeam(PlayerRef playerRef) {
        //baseZone.removeFromZone(playerRef);
        playerList.remove(playerRef);
    }

    public KOTTTeamZone getBaseZone() { return this.baseZone; }

    public String getDisplayName() { return displayName; }

    public List<PlayerRef> getPlayerList() { return playerList; }

    public int getPlayerCount() { return playerList.size(); }

    @Nonnull
    public Color getTeamColor() { return this.teamColor; }

    public void genBots(int count) {
    }
}
