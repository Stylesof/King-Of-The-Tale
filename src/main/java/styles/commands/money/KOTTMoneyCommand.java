package styles.commands.money;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.player.component.KOTTMoney;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static styles.util.MessageHandler.printChat;
import static styles.util.MessageHandler.printLog;

public class KOTTMoneyCommand extends AbstractAsyncPlayerCommand {
    public KOTTMoneyCommand() {
        super("money", "Manage player(s) money!");

        this.addSubCommand(new KOTTMoneyListCommand());
        this.addSubCommand(new KOTTMoneyClearCommand());

        this.addSubCommand(new KOTTMoneyGetCommand());
        this.addSubCommand(new KOTTMoneyDepositCommand());
    }

    @Nonnull
    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if (!commandContext.isPlayer()) {
            printChat(commandContext, "Command to player(s) only!");
            return CompletableFuture.completedFuture(null);
        }

        KOTTMoney money = Objects.requireNonNull(playerRef.getReference()).getStore().getComponent(ref, KOTTMoney.getComponentType());
        float moneyQnt = 0.0f;
        if (money != null) {
            moneyQnt = money.moneyQuantity;
        }
        printChat(playerRef, "Your actual balance: $" + moneyQnt + "!");

        return CompletableFuture.completedFuture(null);
    }


    public static class KOTTMoneyListCommand extends AbstractAsyncPlayerCommand {
        private final DefaultArg<String> filter;
        public KOTTMoneyListCommand() {
            super("list", "List all player(s) by money using filters! Use \"/kott money filters\" to see all available filters");

            this.filter = this.withDefaultArg("filter", "Filter used to show values.", ArgTypes.STRING, "MONEY_HIGHER", "Filter by money size High to Low");
        }

        @Nonnull
        @Override
        protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            String _filter = this.filter.get(commandContext);
            assert  _filter != null;

            Map<String, Float> playerMoneyList = KOTTMoney.getPlayerMoneyMap(_filter);
            int i = 1;
            for (String name : playerMoneyList.keySet()) {
                printChat(commandContext, i + ". " + name + ": $" + playerMoneyList.get(name));
            }

            return CompletableFuture.completedFuture(null);
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
            if (!commandContext.isPlayer()) {
                printChat(commandContext, "To use this command as a non-player, you need to specify the target!");
                return CompletableFuture.completedFuture(null);
            }

            String playerName = this.playerName.get(commandContext);

            PlayerRef target = playerRef;
            if (playerName != null) {
                for (PlayerRef _playerRef : Universe.get().getPlayers()) {
                    if (_playerRef != null && _playerRef != playerRef && _playerRef.getUsername().equals(playerName)) {
                        target = _playerRef;
                    }
                }
            }
            if (target.getReference() == null) {
                printLog("Invalid target reference!");
                return CompletableFuture.completedFuture(null);
            }

            playerName = target.getUsername();

            KOTTMoney money = store.getComponent(target.getReference(), KOTTMoney.getComponentType());
            if (money != null) {
                money.moneyQuantity = 0;
            }

            KOTTMoney.KOTTMoneySaveFile.getKottConfig().removeFromPlayerMoneyList(playerName);
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
                    if (_playerRef != null && _playerRef.getReference() != null && _playerRef.isValid()) {
                        KOTTMoney.KOTTMoneySaveFile.getKottConfig().removeFromPlayerMoneyList(_playerRef.getUsername());
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

    public static class KOTTMoneyGetCommand extends AbstractAsyncPlayerCommand {
        private final RequiredArg<Integer> quantity;
        public KOTTMoneyGetCommand() {
            super("get", "Get money from account to hand!");

            this.quantity = withRequiredArg("quantity", "Quantity of money to get.", ArgTypes.INTEGER);
        }

        @Nonnull
        @Override
        protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            if (!commandContext.isPlayer()) {
                printChat( commandContext, "Command only to player(s)!");
                return CompletableFuture.completedFuture(null);
            }

            if (playerRef.getReference() == null) {
                printChat(playerRef, "Invalid player reference!");
                return  CompletableFuture.completedFuture(null);
            }

            Integer _quantity = quantity.get(commandContext);
            if (_quantity == null || _quantity <= 0) {
                printChat(playerRef, "Invalid quantity!");
                return CompletableFuture.completedFuture(null);
            }

            KOTTMoney money = store.getComponent(playerRef.getReference(), KOTTMoney.getComponentType());
            if (money == null || money.moneyQuantity < _quantity) {
                printChat(playerRef, "Insufficient money in account!");
                return CompletableFuture.completedFuture(null);
            }

            Player player = store.getComponent(playerRef.getReference(), Player.getComponentType());
            ItemStack moneyItem = new ItemStack("Money", _quantity);
            ItemStackTransaction transaction = player.giveItem(moneyItem, playerRef.getReference(), store);
            ItemStack remainder = transaction.getRemainder();
            if (remainder != null && !remainder.isEmpty()) {
                printChat(playerRef, "Your inventory is full!");
            } else {
                KOTTMoney.removeMoneyFromPlayer(playerRef, _quantity);
                printChat(playerRef, "Get $" + _quantity + ".00 of money!");
            }

            return CompletableFuture.completedFuture(null);
        }
    }

    public static class KOTTMoneyDepositCommand extends AbstractAsyncPlayerCommand {
        private final RequiredArg<Integer> quantity;
        public KOTTMoneyDepositCommand() {
            super("deposit", "Send money item to the bank!");

            this.quantity = this.withRequiredArg("quantity", "Quantity of money to deposit in bank!", ArgTypes.INTEGER);

            this.addSubCommand(new KOTTMoneyDepositAllCommand());
        }

        @Nonnull
        @Override
        protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            if (!commandContext.isPlayer()) {
                printChat(commandContext, "To use this command, you must be an player!");
                return CompletableFuture.completedFuture(null);
            }

            if (playerRef.getReference() == null) {
                printChat(commandContext, "Invalid player reference!");
                return CompletableFuture.completedFuture(null);
            }

            Integer _quantity = this.quantity.get(commandContext);
            if (_quantity == null || _quantity <= 0) {
                printChat(playerRef, "Invalid money quantity!");
                return CompletableFuture.completedFuture(null);
            }

            Player player = store.getComponent(playerRef.getReference(), Player.getComponentType());
            if (player != null && player.getInventory() != null && player.getInventory().getStorage() != null) {
                ItemStack moneyItem = new ItemStack("Money", _quantity);
                ItemContainer ic = player.getInventory().getContainerForItemPickup(moneyItem.getItem(), store.getComponent(playerRef.getReference(), PlayerSettings.getComponentType()));
                if (ic.canRemoveItemStack(moneyItem)) {
                    ic.removeItemStack(moneyItem);
                    KOTTMoney.addMoneyToPlayer(playerRef, _quantity);
                    printChat(playerRef, "$" + _quantity + ".00 send to bank!");
                } else {
                    printChat(playerRef, "Insufficient money to send!");
                }
            }
            return CompletableFuture.completedFuture(null);
        }

        public static class KOTTMoneyDepositAllCommand extends AbstractAsyncPlayerCommand {
            public KOTTMoneyDepositAllCommand() {
                super("all", "Deposit all physically money to the bank!");
            }

            @Nonnull
            @Override
            protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
                if (!commandContext.isPlayer()) {
                    printChat(commandContext, "Command to player(s) only!");
                    return CompletableFuture.completedFuture(null);
                }

                if (playerRef.getReference() == null) {
                    printChat(commandContext, "Invalid player reference!");
                }

                Player player = store.getComponent(playerRef.getReference(), Player.getComponentType());
                if (player != null && player.getInventory() != null && player.getInventory().getStorage() != null) {
                    int quantity = 0;
                    ItemStack moneyItem = new ItemStack("Money");
                    ItemContainer ic = player.getInventory().getContainerForItemPickup(moneyItem.getItem(), store.getComponent(playerRef.getReference(), PlayerSettings.getComponentType()));
                    for (short i = 0; i < ic.getCapacity(); i++) {
                        ItemStack itemStack = ic.getItemStack(i);
                        if (itemStack != null && itemStack.getItem() != Item.UNKNOWN && itemStack.getItem() == moneyItem.getItem()) {
                            quantity = Math.max(quantity, ic.getItemStack(i).getQuantity());
                        }
                    }
                    moneyItem = new ItemStack("Money", quantity);
                    ic.removeItemStack(moneyItem);
                    KOTTMoney.addMoneyToPlayer(playerRef, quantity);
                    printChat(playerRef, "Deposited $" + quantity + ".00 to bank!");
                }

                return CompletableFuture.completedFuture(null);
            }
        }
    }
}
