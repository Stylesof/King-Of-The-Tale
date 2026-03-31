package styles.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import styles.KOTT;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class KOTTConfig {

    private Map<String, Float> playerMoneyList = new HashMap<>();

    public static final BuilderCodec<KOTTConfig> CODEC = BuilderCodec.builder(KOTTConfig.class, KOTTConfig::new)
            .append(new KeyedCodec<>("PlayerMoneyList", new MapCodec<>(Codec.FLOAT, HashMap<String, Float>::new)),
                    (data, value) -> data.playerMoneyList = value,
                    (data) -> data.playerMoneyList)
            .add()
            .build();

    public static KOTTConfig getKottConfig() {
        return KOTT.instance.kottConfig.get();
    }

    public static void saveKOTTConfig() {
        KOTT.instance.kottConfig.save();
    }

    public void addToPlayerMoneyList(String username, float money) {
        KOTTConfig config = getKottConfig();
        config.playerMoneyList.put(username, money);
        KOTT.instance.kottConfig.save();
    }

    public void removeFromPlayerMoneyList(@Nonnull String username) {
        KOTTConfig config = getKottConfig();
        for(String _username : config.playerMoneyList.keySet()) {
            if (_username.equals(username)) {
                config.playerMoneyList.remove(username);
                saveKOTTConfig();
                return;
            }
        }

    }

    public void clearPlayerMoneyList() {
        KOTTConfig config = getKottConfig();
        config.playerMoneyList.clear();
        saveKOTTConfig();
    }
}
