package styles.commands.team;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

import javax.annotation.Nonnull;

public class KOTHTeamCommand extends AbstractCommandCollection {

    public KOTHTeamCommand() {
        super("team", "Manage KOTH teams!");

        this.addSubCommand(new KOTHTeamListCommand());
    }

}
