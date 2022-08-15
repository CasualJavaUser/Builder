package com.boxhead.builder;

import com.boxhead.builder.game_objects.NPC;
import com.boxhead.builder.utils.BoxCollider;

public interface FieldWork extends WorldObject {
    Object getCharacteristic();

    boolean assignWorker(NPC npc);

    void dissociateWorker(NPC npc);

    boolean isFree();

    void work();

    void setWork(NPC npc, boolean b);

    BoxCollider getCollider();
}