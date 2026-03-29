package styles.commands.team;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import styles.commands.team.point.KOTTTeamPointCommand;

public class KOTTTeamCommand extends AbstractCommandCollection {

    public KOTTTeamCommand() {
        super("team", "Manage KOTH teams!");

        this.addSubCommand(new KOTTTeamListCommand());
        this.addSubCommand(new KOTTTeamPointCommand());
    }

}
