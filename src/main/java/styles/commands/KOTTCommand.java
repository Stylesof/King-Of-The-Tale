package styles.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import styles.commands.match.KOTTMatchCommand;
import styles.commands.team.KOTTTeamCommand;

public class KOTTCommand extends AbstractCommandCollection {


    public KOTTCommand() {
        super("kott", "King Of The Hytale Minigame");

        this.addSubCommand(new KOTTStartCommand());
        this.addSubCommand(new KOTTStopCommand());
        this.addSubCommand(new KOTTJoinCommand());

        this.addSubCommand(new KOTTTeamCommand());

        this.addSubCommand(new KOTTMatchCommand());
    }


}
