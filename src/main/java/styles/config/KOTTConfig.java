package styles.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import styles.KOTT;

import java.util.HashMap;
import java.util.Map;

public class KOTTConfig {

    public Map<String, Float> playerMoneyList = new HashMap<>();

    public static final BuilderCodec<KOTTConfig> CODEC = BuilderCodec.builder(KOTTConfig.class, KOTTConfig::new)
            .append(new KeyedCodec<>("PlayerMoneyList", new MapCodec<>(Codec.FLOAT, HashMap<String, Float>::new) ),
                    (data, value) -> data.playerMoneyList = value,
                    (data) -> data.playerMoneyList)
            .add()
            .build();

    public static KOTTConfig getKottConfig() { return KOTT.instance.kottConfig.get(); }

    public void addToPlayerMoneyList(String username, float money) {
        KOTTConfig config = getKottConfig();
        config.playerMoneyList.put(username, money);
        KOTT.instance.kottConfig.save();
    }
}
