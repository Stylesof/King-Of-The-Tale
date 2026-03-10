package styles.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import styles.commands.team.KOTHTeamCommand;

import static styles.util.PrintMacros.print;

public class KOTHCommand extends AbstractCommandCollection {


    public KOTHCommand() {
        super("koth", "King Of The Hytale Minigame");

        this.addSubCommand(new KOTHStartCommand());
        this.addSubCommand(new KOTHStopCommand());

        this.addSubCommand(new KOTHTeamCommand());
    }


}
