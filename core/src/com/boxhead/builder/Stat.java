package com.boxhead.builder;

import com.boxhead.builder.game_objects.ServiceBuilding;
import com.boxhead.builder.game_objects.Villager;

import java.util.function.Consumer;

import static com.boxhead.builder.game_objects.Villager.Order.Type.*;

public enum Stat {
    HUNGER(50, 80, 60, 0.001f,
            villager -> standardSeekService(villager, Service.BARTENDING)),

    TIREDNESS(70, 90, 0, 0.001f,
            villager -> {
                if (villager.getHome() != null) {
                    if (villager.isClockedIn()) {
                        villager.getWorkplace().endShift(villager);
                    }
                    villager.giveOrder(GO_TO, villager.getHome());
                }
            }),

    HEALTH(70, 20, 100, -0.001f,
            villager -> standardSeekService(villager, Service.HEALTHCARE));

    public final int mild, critical;
    public final float rate;
    public final float initVal;
    private final Consumer<Villager> fulfillNeed;
    /**
     * is increasing or decreasing over time
     */
    public final boolean isIncreasing;

    Stat(int mild, int critical, float initVal, float rate, Consumer<Villager> fulfillNeed) {
        this.mild = mild;
        this.critical = critical;
        this.initVal = initVal;
        this.rate = rate;
        this.fulfillNeed = fulfillNeed;
        isIncreasing = rate > 0;
    }

    public void fulfillNeed(Villager villager) {
        fulfillNeed.accept(villager);
    }

    private static void standardSeekService(Villager villager, Service service) {
        ServiceBuilding serviceBuilding = villager.seekNearestService(service);
        if (serviceBuilding == null) return;

        if (villager.isClockedIn()) {
            villager.getWorkplace().endShift(villager);
        }
        serviceBuilding.reserve();
        villager.giveOrder(serviceBuilding);
    }
}
