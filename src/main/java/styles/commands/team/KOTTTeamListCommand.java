package styles.commands.team;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.util.log.LogTypes;
import styles.world.KOTTMatch;
import styles.team.KOTTTeam;

import javax.annotation.Nonnull;

import java.util.concurrent.CompletableFuture;

import static styles.util.PrintMacros.print;
import static styles.util.PrintMacros.printL;
import static styles.util.log.PrintLog.printLog;

public class KOTTTeamListCommand extends AbstractAsyncPlayerCommand {

    private final OptionalArg<Integer> showuuid;
    private final OptionalArg<String> world_name;

    public KOTTTeamListCommand() {
        super("list", "Lists all Teams availables!");

        this.showuuid = withOptionalArg("showuuid", "Show the team name + uuid. (1. Name + UUID, 2. UUID Only)", ArgTypes.INTEGER);
        this.world_name = withOptionalArg("world", "Specify the world name of the match.", ArgTypes.STRING);
    }

    /*===========================================================
        FUNCTION TO LIST TO THE PLAYER ALL TEAMS AVAILABLE, IF
        HAS AN ACTIVE MATCH IN THE WORLD SPECIFIED
     ==========================================================*/
    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String _word_name = world_name.get(commandContext);
        World _world;

        // Verify if the world name is valid
        if (_word_name == null) {
            _word_name = world.getName();
        }

        // Verify if the world is valid
        _world = Universe.get().getWorld(_word_name);
        if (_world == null) {
            printLog(playerRef, LogTypes.KOTTInvalidWorld);
            return CompletableFuture.completedFuture(null);
        }

        // Verify if has an match active in the world
        KOTTMatch match = KOTTMatch.getMatchesList().get(_word_name);
        if (match == null || !match.getKOTTMatchStatus()){
            print(commandContext, "There isn't any match happening in the moment!");
            return CompletableFuture.completedFuture(null);
        }

        int show = 0;
        if (this.showuuid.get(commandContext) != null) {
            show = this.showuuid.get(commandContext);
        }

        print(commandContext, "Teams:");
        int i = 0;
        for(KOTTTeam team : match.getTeams().values()){
            String message = i + ": ";

            if(show == 1){
                message += team.getDisplayName() + " : " + team.teamID;
            }else if(show == 2){
                message += team.teamID;
            }else{
                message += team.getDisplayName();
            }

            if(team.containsPlayer(playerRef)){
                print(commandContext, message + " (your team)");
            }else{
                print(commandContext, message);
            }

            i++;
        }

        return  CompletableFuture.completedFuture(null);
    }

}
