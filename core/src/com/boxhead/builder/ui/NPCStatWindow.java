package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.NPC;

public class NPCStatWindow extends StatWindow<NPC> {
    private String job = null, name = null;
    private int[] stats = null;

    public NPCStatWindow() {
        super();
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (pinned) {
            updatePosition();
        }
        super.draw(batch);
        batch.draw(pinnedObject.getTexture(),
                position.x + 10, position.y + 115,
                pinnedObject.getTexture().getRegionWidth() * 4,
                pinnedObject.getTexture().getRegionHeight() * 4);

        updateStats();
        String s = name + "\njob: " + job;
        String stat;
        for (int i = 0; i < NPC.Stats.values().length; i++) {
            stat = NPC.Stats.values()[i].toString().toLowerCase() + ": " + pinnedObject.getStats()[i];
            s += "\n" + stat;
        }
        UI.FONT.draw(batch, s, position.x + 10, position.y + 105);
    }

    @Override
    protected Vector3 getObjectScreenPosition() {
        return BuilderGame.getGameScreen().getCamera().project( new Vector3(pinnedObject.getSpritePosition().x * World.TILE_SIZE,
                                                                            pinnedObject.getSpritePosition().y * World.TILE_SIZE, 0));
    }

    @Override
    protected void updateStats() {
        name = pinnedObject.getName() + " " + pinnedObject.getSurname();
        if(name.length() > 15) name = pinnedObject.getName() + "\n" + pinnedObject.getSurname();
        job = pinnedObject.getJob().toString().toLowerCase();
        stats = pinnedObject.getStats();
    }
}
