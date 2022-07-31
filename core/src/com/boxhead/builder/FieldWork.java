package com.boxhead.builder;

import com.boxhead.builder.game_objects.NPC;

public interface FieldWork {
    Object getCharacteristic();

    boolean assignWorker(NPC npc);

    void dissociateWorker(NPC npc);

    boolean isFree();

    void work();

    void setWork(NPC npc, boolean b);
}