package styles.commands.money;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.config.KOTTConfig;
import styles.player.KOTTMoney;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

import static styles.util.PrintMacros.print;

public class KOTTMoneyCommand extends AbstractAsyncPlayerCommand {
    public KOTTMoneyCommand() {
        super("money", "Manage player(s) money!");

        this.addSubCommand(new KOTTMoneyClearCommand());
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

    public static class KOTTMoneyClearCommand extends AbstractAsyncPlayerCommand {

        private final OptionalArg<String> playerName;

        public KOTTMoneyClearCommand() {
            super("clear", "Clear player(s) money!");
            this.addSubCommand(new KOTTMoneyClearAllCommand());

            this.playerName = this.withOptionalArg("player", "Player to clear the money.", ArgTypes.STRING);
        }

        @Nonnull
        @Override
        protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            String playerName = this.playerName.get(commandContext);

            PlayerRef target = playerRef;
            if (playerName != null) {
                for (PlayerRef _playerRef : Universe.get().getPlayers()) {
                    if (_playerRef != null && _playerRef.getUsername().equals(playerName)) {
                        target = _playerRef;
                    }
                }
            }

            playerName = target.getUsername();

            KOTTMoney money = store.getComponent(target.getReference(), KOTTMoney.getComponentType());
            if (money != null) {
                money.moneyQuantity = 0;
            }

            KOTTConfig.getKottConfig().removeFromPlayerMoneyList(playerName);
            return CompletableFuture.completedFuture(null);
        }

        public static class KOTTMoneyClearAllCommand extends AbstractAsyncPlayerCommand{
            public KOTTMoneyClearAllCommand() {
                super("all", "Clear all players from the server money!");
            }

            @Nonnull
            @Override
            protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
                for (PlayerRef _playerRef : Universe.get().getPlayers()) {
                    if (_playerRef != null && _playerRef.isValid()) {
                        KOTTMoney money = store.getComponent(_playerRef.getReference(), KOTTMoney.getComponentType());
                        if (money != null) {
                            money.moneyQuantity = 0;
                        }
                    }
                }

                return CompletableFuture.completedFuture(null);
            }
        }
    }
}
