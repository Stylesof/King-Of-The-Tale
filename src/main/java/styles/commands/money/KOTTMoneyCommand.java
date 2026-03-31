package styles.commands.money;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.player.KOTTMoney;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static styles.util.PrintMacros.print;

public class KOTTMoneyCommand extends AbstractAsyncPlayerCommand {
    public KOTTMoneyCommand() {
        super("money", "Manage player(s) money!");

        this.addSubCommand(new KOTTMoneyListCommand());
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        KOTTMoney money = playerRef.getReference().getStore().getComponent(ref, KOTTMoney.getComponentType());
        float moneyQnt = 0.0f;
        if (money != null) {
            moneyQnt = money.moneyQuantity;
        }
        print(playerRef, "Your actual balance: $" + moneyQnt + "!");

        return CompletableFuture.completedFuture(null);
    }


    public static class KOTTMoneyListCommand extends AbstractAsyncPlayerCommand {
        public KOTTMoneyListCommand() {
            super("list", "List all player(s) by money using filters!");
        }

        @Nonnull
        @Override
        protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {

            CompletableFuture<Void> fun = CompletableFuture.completedFuture(null);



            return fun;
        }
    }
}
