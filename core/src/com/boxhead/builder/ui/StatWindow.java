package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.GameScreen;
import com.boxhead.builder.NPC;
import com.boxhead.builder.Vector2i;
import com.boxhead.builder.World;

public class StatWindow extends Window{
    private NPC npc = null;
    private Vector3 npcPosition;
    private boolean pinned;
    String job = null;

    public StatWindow(TextureRegion texture) {
        super(texture);
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
        super.draw(batch);
        UI.FONT.draw(batch, job, position.x, position.y);
    }

    private void updatePosition() {
        npcPosition = GameScreen.getCamera().project(new Vector3(npc.getSpritePosition().x * World.TILE_SIZE, npc.getSpritePosition().y * World.TILE_SIZE, 0));
        position = new Vector2i((int)(npcPosition.x + 20/GameScreen.getCamera().zoom), (int)(npcPosition.y + 20/GameScreen.getCamera().zoom));
    }

    private void updateStats() {
        job = npc.getJob().toString().toLowerCase();
    }
}
