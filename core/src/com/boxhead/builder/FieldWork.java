package com.boxhead.builder;

public interface FieldWork {
    Object getCharacteristic();

    boolean assignWorker(NPC npc);

    void dissociateWorker(NPC npc);

    boolean isFree();

    void work();

    void setWork(NPC npc, boolean b);
}