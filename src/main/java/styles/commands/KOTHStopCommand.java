package styles.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import styles.KOTHMatch;

import javax.annotation.Nonnull;

import static styles.util.Utils.print;

public class KOTHStopCommand extends CommandBase {

    public KOTHStopCommand() {
        super("stop", "Stop an active KOTH game session!");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext commandContext) {
        if(!KOTHMatch.getKOTHMatchStatus()){
            print(commandContext, "[KOTH] There isn't any match happening in the moment!");
        }else{
            KOTHMatch.stop();
            print(commandContext, "[KOTH] Stopped the active KOTH match!");
        }
    }
}
