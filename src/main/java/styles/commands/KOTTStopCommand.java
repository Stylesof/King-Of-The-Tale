package styles.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import styles.KOTTMatch;

import javax.annotation.Nonnull;

import static styles.util.PrintMacros.print;

public class KOTTStopCommand extends CommandBase {

    public KOTTStopCommand() {
        super("stop", "Stop an active KOTH game session!");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext commandContext) {
        if(!KOTTMatch.getKOTHMatchStatus()){
            print(commandContext, "[KOTH] There isn't any match happening in the moment!");
        }else{
            KOTTMatch.stop();
            print(commandContext, "[KOTH] Stopped the active KOTH match!");
        }
    }
}
