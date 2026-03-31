package styles.player;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.KOTT;
import styles.config.KOTTConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static Map<String, Float> getPlayerMoneyMap() { return KOTTConfig.getKottConfig().getPlayerMoneyMap; }

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

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new KOTTMoney();
    }
}
