package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;

public class StatWindow extends Window{
    private NPC npc = null;
    private Vector3 npcPosition;
    private boolean pinned;
    String job = null, name = null;
    private int[] stats = null;

    public StatWindow() {
        super(Textures.getUI("stat_window"));
    }

    public void show(NPC npc) {
        show(npc, false);
    }

    public void show(NPC npc, boolean pinned) {
        this.npc = npc;
        this.pinned = pinned;
        updateStats();
        updatePosition();
        setVisible(true);
    }

    @Override
    public void onDown() {
        pinned = false;
        super.onDown();
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (pinned) {
            updatePosition();
        }
        updateStats();
        //super.draw(batch);
        batch.draw(texture, position.x, position.y - 105);
        batch.draw(npc.getTexture(), position.x + 10, position.y + 10, npc.getTexture().getRegionWidth() * 4, npc.getTexture().getRegionHeight() * 4);
        UI.FONT.draw(batch, name, position.x + 10, position.y);
        UI.FONT.draw(batch, "job: " + job, position.x + 10, position.y - 23);
        String stat;
        for (int i = 0; i < NPC.Stats.values().length; i++) {
            stat = NPC.Stats.values()[i].toString().toLowerCase() + ": ";
            UI.FONT.draw(batch, stat + npc.getStats()[i], position.x + 10, position.y - (i+2) * 23);
        }
    }

    private void updatePosition() {
        npcPosition = GameScreen.getCamera().project(new Vector3(npc.getSpritePosition().x * World.TILE_SIZE, npc.getSpritePosition().y * World.TILE_SIZE, 0));
        position = new Vector2i((int)(npcPosition.x + 25/GameScreen.getCamera().zoom), (int)(npcPosition.y + 40/GameScreen.getCamera().zoom));
    }

    private void updateStats() {
        name = npc.getName() + " " + npc.getSurname();
        job = npc.getJob().toString().toLowerCase();
        stats = npc.getStats();
    }
}
