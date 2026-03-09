package styles.commands.team;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.KOTHMatch;
import styles.team.KOTHTeam;

import javax.annotation.Nonnull;

import java.util.concurrent.CompletableFuture;

import static styles.utils.Utils.print;

public class KOTHTeamListCommand extends AbstractAsyncPlayerCommand {

    private final OptionalArg<Integer> showuuid;

    public KOTHTeamListCommand() {
        super("list", "Lists all Teams availables!");

        this.showuuid = withOptionalArg("showuuid", "Show the team name + uuid. (1. Name + UUID, 2. UUID Only)", ArgTypes.INTEGER);
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if(KOTHMatch.getKOTHMatchStatus()){

            int show = 0;
            if(this.showuuid.get(commandContext) != null){
                show = this.showuuid.get(commandContext);
            }

            print(commandContext, "Teams:");
            int i = 0;
            for(KOTHTeam team : KOTHMatch.getTeams().values()){
                String message = i + ": ";

                if(show == 1){
                    message += team.getDisplayName() + " : " + team.teamID;
                }else if(show == 2){
                    message += team.teamID;
                }else{
                    message += team.getDisplayName();
                }

                if(team.containsPlayerRef(playerRef)){
                    print(commandContext, message + " (your team)");
                }else{
                    print(commandContext, message);
                }

                i++;
            }
        }else{
            print(commandContext, "[KOTH] There isn't any match happening in the moment!");
        }

        return  CompletableFuture.completedFuture(null);
    }

}
