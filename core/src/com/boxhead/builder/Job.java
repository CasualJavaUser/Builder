package com.boxhead.builder;

import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.game_objects.ProductionBuilding;

import java.io.Serializable;

public class Job implements Serializable {
    protected Recipe recipe = new Recipe();

    public void assign(Villager assignee, ProductionBuilding workplace) {}

    public void continuousTask(Villager assignee, ProductionBuilding workplace) {}

    public void onExit(Villager assignee, ProductionBuilding workplace) {}

    public Recipe getRecipe(ProductionBuilding productionBuilding) {
        return recipe;
    }

    public Object getPoI() {
        return null;
    }

    public enum ShiftTime {
        EIGHT_FOUR(8),
        FOUR_MIDNIGHT(16),
        MIDNIGHT_EIGHT(0),

        ELEVEN_SEVEN(11),
        SEVEN_THREE(19),
        THREE_ELEVEN(3);

        public final int start;
        public final int end;

        ShiftTime(int startingHour) {  //assumes 8-hour workday
            start = startingHour * World.HOUR;
            end = (start + 8 * World.HOUR) % World.FULL_DAY;
        }

        public boolean overlaps(ShiftTime otherShift) {
            int thisEnd = start < end ? end : end + World.FULL_DAY;
            int otherEnd = otherShift.start < otherShift.end ? otherShift.end : otherShift.end + World.FULL_DAY;

            return start < otherEnd && otherShift.start < thisEnd;
        }

        public boolean overlaps(int time) {
            int apparentEndTime = start < end ? end : end + World.FULL_DAY;
            return time >= start && time <= apparentEndTime;
        }
    }
}
