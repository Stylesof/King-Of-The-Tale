package styles.player;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import styles.KOTT;

import javax.annotation.Nullable;

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
        return KOTT.instance.kottMoneyComponent;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new KOTTMoney();
    }
}
