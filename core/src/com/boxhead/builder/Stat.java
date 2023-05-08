package com.boxhead.builder;

import com.boxhead.builder.game_objects.Buildings;
import com.boxhead.builder.game_objects.ServiceBuilding;
import com.boxhead.builder.game_objects.Villager;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.boxhead.builder.game_objects.Villager.Order.Type.*;

public enum Stat {
    HUNGER(50, 80, 60, villager -> 0.001f,
            villager -> {
                ServiceBuilding pub = villager.seekNearestService(Buildings.Type.PUB);
                if (pub != null) {
                    pub.reserve();
                    villager.giveOrder(CLOCK_OUT);
                    villager.giveOrder(pub.getEntrancePosition());
                    villager.giveOrder(pub);
                }
            }),

    TIREDNESS(70, 90, 0, villager -> villager.isClockedIn() ? 0.002f : 0.001f,
            villager -> {
                if (villager.getHome() != null) {
                    villager.giveOrder(CLOCK_OUT);
                    villager.giveOrder(villager.getHome().getEntrancePosition());
                    villager.giveOrder(ENTER, villager.getHome());
                }
            }),

    HEALTH(70, 20, 100, villager -> -0.001f,
            villager -> {

            });

    public final int urgent, critical;
    public final Function<Villager, Float> getRate;
    public final float initVal;
    public final Consumer<Villager> fulfillNeed;
    /**
     * is increasing or decreasing over time
     */
    public final boolean isIncreasing;

    Stat(int urgent, int critical, float initVal,  Function<Villager, Float> getRate, Consumer<Villager> fulfillNeed) {
        this.urgent = urgent;
        this.critical = critical;
        this.initVal = initVal;
        this.getRate = getRate;
        this.fulfillNeed = fulfillNeed;
        isIncreasing = (urgent < critical);
    }

    public float getRate(Villager villager) {
        return getRate.apply(villager);
    }

    public void fulfillNeed(Villager villager) {
        fulfillNeed.accept(villager);
    }
}
