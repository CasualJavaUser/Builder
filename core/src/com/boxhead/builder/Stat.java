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
                    villager.giveOrder(CLOCK_OUT);
                    villager.giveOrder(EXIT, villager.getWorkplace());
                    villager.giveOrder(GO_TO, villager.getHome());
                }
            }),

    HEALTH(70, 20, 100, -0.001f,
            villager -> standardSeekService(villager, Service.HEALTHCARE));

    public final int urgent, critical;
    public final float rate;
    public final float initVal;
    public final Consumer<Villager> fulfillNeed;
    /**
     * is increasing or decreasing over time
     */
    public final boolean isIncreasing;

    Stat(int urgent, int critical, float initVal,  float rate, Consumer<Villager> fulfillNeed) {
        this.urgent = urgent;
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

        serviceBuilding.reserve();
        villager.giveOrder(CLOCK_OUT);
        villager.giveOrder(EXIT, villager.getWorkplace());
        villager.giveOrder(GO_TO, serviceBuilding);
        villager.giveOrder(serviceBuilding);
    }
}
