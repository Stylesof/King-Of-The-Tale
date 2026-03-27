package styles.team;

import com.hypixel.hytale.builtin.path.path.PatrolPath;
import com.hypixel.hytale.builtin.path.path.TransientPath;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.MovementDirection;
import com.hypixel.hytale.protocol.RespondToHitUpdate;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.commands.NPCPathCommand;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionWander;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.entities.PathManager;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.builders.BuilderRoleAbstract;
import it.unimi.dsi.fastutil.Pair;
import styles.world.KOTTMatch;
import styles.world.KOTTTeamZone;
import styles.world.util.WorldBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;

public class KOTTTeam {

    public final UUID teamID;
    private final List<PlayerRef> playerList = new ArrayList<>();
    private final List<NPCEntity> teamBots = new ArrayList<>();
    private final String displayName;

    public int teamPoints = 0;

    private final KOTTTeamZone baseZone;
    public static final int distanceBaseFromZone = 100;

    public KOTTTeam(UUID id, String displayName, @Nonnull Vector3i basePosition, @Nonnull World world, @Nullable UserMapMarker zoneMarker) {
        this.teamID = id;
        this.displayName = displayName;
        this.baseZone = new KOTTTeamZone(distanceBaseFromZone, basePosition, world, this, zoneMarker);

        genBots(1);
    }

    // Add player to the Team
    public void addPlayerRef(PlayerRef playerRef) {
        playerList.add(playerRef);
    }

    public boolean containsPlayer(PlayerRef playerRef) {
        return playerList.contains(playerRef);
    }

    public static boolean createTeam(Map<UUID, KOTTTeam> teamListRef, UUID id, String displayName, Vector3i basePosition, @Nonnull World world, @Nullable UserMapMarker zoneMarker) {

        if(teamListRef.containsKey(id)){
            printL("[KOTH] There is an Team with that ID already!");
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

    public void genBots(int count) {
    }
}
