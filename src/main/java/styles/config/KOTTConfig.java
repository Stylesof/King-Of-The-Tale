package styles.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import styles.KOTT;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class KOTTConfig {

    public Map<String, Float> getPlayerMoneyMap = new LinkedHashMap<>();

    public static final BuilderCodec<KOTTConfig> CODEC = BuilderCodec.builder(KOTTConfig.class, KOTTConfig::new)
            .append(new KeyedCodec<>("PlayerMoneyList", new MapCodec<>(Codec.FLOAT, HashMap<String, Float>::new)),
                    (data, value) -> data.getPlayerMoneyMap = value,
                    (data) -> data.getPlayerMoneyMap)
            .add()
    .build();

    public void addToPlayerMoneyList(String username, float money) {
        KOTTConfig config = getKottConfig();

        Map<String, Float> playerMoneyMap2 = new LinkedHashMap<>(getPlayerMoneyMap);
        playerMoneyMap2.put(username, money);
        config.getPlayerMoneyMap = playerMoneyMap2;
        KOTT.getInstance().kottConfigRef.save();
    }

    public void removeFromPlayerMoneyList(@Nonnull String username) {
        KOTTConfig config = getKottConfig();
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

    public static KOTTConfig getKottConfig() {
        return KOTT.getInstance().kottConfigRef.get();
    }

    public static void saveKOTTConfig() {
        KOTT.getInstance().kottConfigRef.save();
    }

    public void clearPlayerMoneyList() {
        KOTTConfig config = getKottConfig();
        config.getPlayerMoneyMap.clear();
        saveKOTTConfig();
    }
}
