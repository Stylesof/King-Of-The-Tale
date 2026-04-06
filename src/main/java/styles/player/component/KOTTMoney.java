package styles.player.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.KOTT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static styles.util.MessageHandler.printChat;

public class KOTTMoney implements Component<EntityStore> {
    public float moneyQuantity;

    public static final BuilderCodec<KOTTMoney> CODEC = BuilderCodec.builder(KOTTMoney.class, KOTTMoney::new)
            .append(new KeyedCodec<>("MoneyQuantity", BuilderCodec.FLOAT),
                    (data, value) -> data.moneyQuantity = value,
                    data -> data.moneyQuantity)
            .addValidator(Validators.nonNull())
            .addValidator(Validators.min(0.0f))
            .add()
            .build();

    public KOTTMoney() {
        this.moneyQuantity = 0;
    }

    public KOTTMoney(KOTTMoney clone) {
        this.moneyQuantity = clone.moneyQuantity;
    }

    public static ComponentType<EntityStore, KOTTMoney> getComponentType() {
        return KOTT.getInstance().kottMoneyComponent;
    }

    public enum FilterType{
        MONEY_HIGHER,   // Money HIGHER
        MONEY_LOWER,
        ADD_NEWEST,     // Newest Added to the list
        ADD_OLDEST
    }

    public static Map<String, Float> getPlayerMoneyMap() { return KOTTMoneySaveFile.getKottConfig().getPlayerMoneyMap; }

    public static Map<String, Float> getPlayerMoneyMap(@Nonnull String filter) {
        filter = filter.toUpperCase();
        if (filter.equals(FilterType.MONEY_HIGHER.toString())) {
            return getPlayerMoneyMap(FilterType.MONEY_HIGHER);
        } else if (filter.equals(FilterType.MONEY_LOWER.toString())) {
            return getPlayerMoneyMap(FilterType.MONEY_LOWER);
        } else if (filter.equals(FilterType.ADD_NEWEST.toString())) {
            return getPlayerMoneyMap(FilterType.ADD_NEWEST);
        } else {
            return getPlayerMoneyMap(FilterType.ADD_OLDEST);
        }
    }

    public static Map<String, Float> getPlayerMoneyMap(FilterType filter) {
        Map<String, Float> playerMoneyMap = getPlayerMoneyMap();
        if (getPlayerMoneyMap().isEmpty()) return new HashMap<>();

        Map<String, Float> sortedMap = new LinkedHashMap<>();

        switch (filter){
            case MONEY_LOWER -> sortedMap = playerMoneyMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (var, var2) -> var,
                            LinkedHashMap::new
                    ));

            case MONEY_HIGHER -> sortedMap = playerMoneyMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (var, var2) -> var,
                            LinkedHashMap::new
                    ));

            case ADD_OLDEST -> sortedMap = playerMoneyMap;
        }

        if (filter == FilterType.ADD_NEWEST) {
            for (int i = playerMoneyMap.size() - 1; i >= 0; i--) {
                sortedMap.put(playerMoneyMap.keySet().toArray(new String[0])[i], playerMoneyMap.values().toArray(new Float[0])[i]);
            }
        }

        return sortedMap;
    }

    public static void addMoneyToPlayer(@Nonnull PlayerRef playerRef, int quantity) {
        World world = Universe.get().getWorld(playerRef.getWorldUuid());
        KOTTMoney money = world.getEntityStore().getStore().getComponent(playerRef.getReference(), KOTTMoney.getComponentType());
        if (money == null) {
            money = new KOTTMoney();
            world.getEntityStore().getStore().addComponent(playerRef.getReference(), KOTTMoney.getComponentType(), money);
        }

        money.moneyQuantity += quantity;

        KOTTMoneySaveFile.getKottConfig().addToPlayerMoneyList(playerRef.getUsername(), money.moneyQuantity);
    }

    public static void removeMoneyFromPlayer(@Nonnull PlayerRef playerRef, int quantity) {
        addMoneyToPlayer(playerRef, -quantity);
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new KOTTMoney();
    }

    public static class KOTTMoneySaveFile {
        public Map<String, Float> getPlayerMoneyMap = new LinkedHashMap<>();

        public static final BuilderCodec<KOTTMoneySaveFile> CODEC = BuilderCodec.builder(KOTTMoneySaveFile.class, KOTTMoneySaveFile::new)
                .append(new KeyedCodec<>("PlayerMoneyList", new MapCodec<>(Codec.FLOAT, HashMap<String, Float>::new)),
                        (data, value) -> data.getPlayerMoneyMap = value,
                        (data) -> data.getPlayerMoneyMap)
                .add()
                .build();

        public void addToPlayerMoneyList(String username, float money) {
            KOTTMoneySaveFile config = getKottConfig();

            Map<String, Float> _playerMoneyMap = new LinkedHashMap<>(getPlayerMoneyMap);
            _playerMoneyMap.put(username, money);
            config.getPlayerMoneyMap = _playerMoneyMap;
            KOTT.getInstance().kottMoneySaveFile.save();
        }

        public void removeFromPlayerMoneyList(@Nonnull String username) {
            KOTTMoneySaveFile config = getKottConfig();
            for(String _username : config.getPlayerMoneyMap.keySet()) {
                if (_username.equals(username)) {
                    Map<String, Float> playerMoneyMap2 = new LinkedHashMap<>(getPlayerMoneyMap);
                    playerMoneyMap2.remove(username);
                    config.getPlayerMoneyMap = playerMoneyMap2;
                    saveKOTTConfig();
                    return;
                }
            }

        }

        public static KOTTMoneySaveFile getKottConfig() {
            return KOTT.getInstance().kottMoneySaveFile.get();
        }

        public static void saveKOTTConfig() {
            KOTT.getInstance().kottMoneySaveFile.save();
        }

        public void clearPlayerMoneyList() {
            KOTTMoneySaveFile config = getKottConfig();
            config.getPlayerMoneyMap.clear();
            saveKOTTConfig();
        }
    }
}
