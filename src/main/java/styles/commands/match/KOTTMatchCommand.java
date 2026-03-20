package styles.commands.match;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class KOTTMatchCommand extends AbstractCommandCollection {
    public KOTTMatchCommand() {
        super("match", "Match commands");

        this.addSubCommand(new KOTTMatchListCommand());
    }
}
