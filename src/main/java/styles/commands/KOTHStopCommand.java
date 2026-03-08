package styles.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import styles.utils.Utils;
import styles.KOTH;

import javax.annotation.Nonnull;

import static styles.utils.Utils.print;

public class KOTHStopCommand extends CommandBase {

    public KOTHStopCommand() {
        super("stop", "Stop an active KOTH game session!");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext commandContext) {
        if(!KOTH.getKOTHMatchStatus()){
            print(commandContext, "There isn't an KOTH Match happening at the moment!");
            return;
        }
    }
}
