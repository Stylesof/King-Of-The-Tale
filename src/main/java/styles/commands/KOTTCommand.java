package styles.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import styles.commands.match.KOTTMatchCommand;
import styles.commands.money.KOTTMoneyCommand;
import styles.commands.scoreboard.KOTTScoreboardCommand;
import styles.commands.team.KOTTTeamCommand;
import styles.commands.test.TestCommand;
import styles.commands.test.TestGUICommand;

public class KOTTCommand extends AbstractCommandCollection {

    public KOTTCommand() {
        super("kott", "King Of The Hytale Minigame");

        this.addSubCommand(new KOTTStartCommand());
        this.addSubCommand(new KOTTJoinCommand());
        this.addSubCommand(new KOTTStopCommand());
        this.addSubCommand(new KOTTLeaveCommand());
        this.addSubCommand(new KOTTEndCommand());

        this.addSubCommand(new KOTTTeamCommand());

        this.addSubCommand(new KOTTMatchCommand());

        this.addSubCommand(new KOTTGUICommand());

        this.addSubCommand(new KOTTClearMarkersCommand());

        this.addSubCommand(new KOTTMoneyCommand());

        this.addSubCommand(new KOTTScoreboardCommand());

        // === FOR TEST ===
        //this.addSubCommand(new TestCommand());
        //this.addSubCommand(new TestGUICommand());
    }
}
