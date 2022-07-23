package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.utils.Vector2i;

public class NPCStatWindow extends Window{
    private NPC npc = null;
    private String job = null, name = null;
    private int[] stats = null;
    private boolean pinned;

    public NPCStatWindow() {
        super(Textures.getUI("stat_window"));
    }

    public void show(NPC npc) {
        this.npc = npc;
        pinned = true;
        updateStats();
        updatePosition();
        setVisible(true);
    }

    @Override
    public void onHold() {
        pinned = false;
        super.onHold();
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (pinned) {
            updatePosition();
        }
        super.draw(batch);
        batch.draw(npc.getTexture(), position.x + 10, position.y + 115, npc.getTexture().getRegionWidth() * 4, npc.getTexture().getRegionHeight() * 4);

        updateStats();
        String s = name + "\njob: " + job;
        String stat;
        for (int i = 0; i < NPC.Stats.values().length; i++) {
            stat = NPC.Stats.values()[i].toString().toLowerCase() + ": " + npc.getStats()[i];
            s += "\n" + stat;
        }
        UI.FONT.draw(batch, s, position.x + 10, position.y + 105);
    }

    private void updatePosition() {
        Vector3 npcPosition = BuilderGame.getGameScreen().getCamera().project(new Vector3(npc.getSpritePosition().x * World.TILE_SIZE, npc.getSpritePosition().y * World.TILE_SIZE, 0));
        position.set((int)(npcPosition.x + 20/BuilderGame.getGameScreen().getCamera().zoom), (int)(npcPosition.y + 10/BuilderGame.getGameScreen().getCamera().zoom));
    }

    private void updateStats() {
        name = npc.getName() + " " + npc.getSurname();
        if(name.length() > 15) name = npc.getName() + "\n" + npc.getSurname();
        job = npc.getJob().toString().toLowerCase();
        stats = npc.getStats();
    }
}
