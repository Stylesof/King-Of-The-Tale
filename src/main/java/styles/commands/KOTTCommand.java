package styles.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import styles.commands.team.KOTTTeamCommand;

public class KOTTCommand extends AbstractCommandCollection {


    public KOTTCommand() {
        super("koth", "King Of The Hytale Minigame");

        this.addSubCommand(new KOTTStartCommand());
        this.addSubCommand(new KOTTStopCommand());

        this.addSubCommand(new KOTTTeamCommand());
    }


}
