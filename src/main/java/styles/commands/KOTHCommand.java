package styles.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.commands.team.KOTHTeamCommand;
import styles.team.KOTHTeam;

import javax.annotation.Nonnull;

import static styles.utils.Utils.print;

public class KOTHCommand extends AbstractCommandCollection {


    public KOTHCommand() {
        super("koth", "King Of The Hytale Minigame");

        this.addSubCommand(new KOTHStartCommand());
        this.addSubCommand(new KOTHStopCommand());

        this.addSubCommand(new KOTHTeamCommand());
    }


}
