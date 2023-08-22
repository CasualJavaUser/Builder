package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Villager;

public class NPCStatWindow extends StatWindow<Villager> {
    private final int IMAGE_SCALE = 4;

    public NPCStatWindow(UI.Layer layer) {
        super(layer);
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        batch.draw(pinnedObject.getTexture(),
                getGlobalPosition().x + UI.PADDING,
                getGlobalPosition().y + getContentHeight() - Villager.TEXTURE_SIZE * IMAGE_SCALE,
                Villager.TEXTURE_SIZE * IMAGE_SCALE,
                Villager.TEXTURE_SIZE * IMAGE_SCALE);
        UI.FONT.draw(batch, stats, getGlobalPosition().x + UI.PADDING,
                getGlobalPosition().y + getContentHeight() - UI.PADDING * 2 - Villager.TEXTURE_SIZE * IMAGE_SCALE);
    }

    @Override
    protected Vector3 getObjectScreenPosition() {
        return GameScreen.camera.project( new Vector3(pinnedObject.getSpritePosition().x * World.TILE_SIZE,
                                                                            pinnedObject.getSpritePosition().y * World.TILE_SIZE, 0));
    }

    @Override
    protected void updateStats() {
        String name = pinnedObject.getName() + " " + pinnedObject.getSurname();
        String job = pinnedObject.getJob().toString();

        stats = "";

        if (Debug.isOpen()) {
            stats += "ID: " + pinnedObject.getId() + "\n";
        }

        stats += name + "\n" + (pinnedObject.getGender() ? "female" : "male") + "\n" + job;
        stats += "\nage: " + pinnedObject.ageInYears();
        String stat;
        for (int i = 0; i < Stat.values().length; i++) {
            stat = Stat.values()[i].toString().toLowerCase() + ": " + (int)pinnedObject.getStats()[i];
            stats += "\n" + stat;
        }

        stats += "\neducation: " + (int)(pinnedObject.getEducation() * 100f) + "%" +
            "\nhappiness: " + Math.round(pinnedObject.getHappiness());

        if (pinnedObject.getPartner() != null && pinnedObject.isLivingWithParents()) {
            stats += "\n- living with parents";
        }
        else if (pinnedObject.getHome() == null) {
            stats += "\n- homeless";
        }
    }

    @Override
    protected void updateWindowSize() {
        super.updateWindowSize();
        setContentHeight(getContentHeight() + UI.PADDING + Villager.TEXTURE_SIZE * IMAGE_SCALE + 20);
    }
}
