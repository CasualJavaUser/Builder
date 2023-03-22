package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Villager;

public class NPCStatWindow extends StatWindow<Villager> {
    private String job = null, name = null;
    private int[] npcStatList = null;
    private final int IMAGE_SCALE = 4;

    public NPCStatWindow(UI.Layer layer) {
        super(layer);
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        batch.draw(pinnedObject.getTexture(),
                getGlobalPosition().x + leftPadding,
                getGlobalPosition().y + getContentHeight() - Villager.TEXTURE_SIZE * IMAGE_SCALE,
                Villager.TEXTURE_SIZE * IMAGE_SCALE,
                Villager.TEXTURE_SIZE * IMAGE_SCALE);
        UI.FONT.draw(batch, stats, getGlobalPosition().x + leftPadding,
                getGlobalPosition().y + getContentHeight() - verticalPadding * 2 - Villager.TEXTURE_SIZE * IMAGE_SCALE);
    }

    @Override
    protected Vector3 getObjectScreenPosition() {
        return GameScreen.camera.project( new Vector3(pinnedObject.getSpritePosition().x * World.TILE_SIZE,
                                                                            pinnedObject.getSpritePosition().y * World.TILE_SIZE, 0));
    }

    @Override
    protected void updateStats() {
        name = pinnedObject.getName() + " " + pinnedObject.getSurname();
        job = pinnedObject.getJob().toString();
        npcStatList = pinnedObject.getStats();

        stats = name + "\n" + job;
        String stat;
        for (int i = 0; i < Villager.Stats.values().length; i++) {
            stat = Villager.Stats.values()[i].toString().toLowerCase() + ": " + pinnedObject.getStats()[i];
            stats += "\n" + stat;
        }
    }

    @Override
    protected void updateWindowSize() {
        super.updateWindowSize();
        setContentHeight(getContentHeight() + verticalPadding + Villager.TEXTURE_SIZE * IMAGE_SCALE + 20);
    }
}
