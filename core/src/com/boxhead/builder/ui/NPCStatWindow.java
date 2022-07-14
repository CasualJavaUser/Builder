package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.utils.Vector2i;

public class NPCStatWindow extends Window{
    private NPC npc = null;
    private Vector3 npcPosition;
    private boolean pinned;
    String job = null, name = null;
    private int[] stats = null;
    private Button closeButton;

    public NPCStatWindow() {
        super(Textures.getUI("stat_window"));
        closeButton = new Button(Textures.getUI("close_button"), new Vector2i()) {
            @Override
            public void onClick() {
                close();
            }
        };
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
    public void onHold() {
        pinned = false;
        super.onHold();
        if(closeButton.isClicked()) closeButton.onClick();
        //closeButton.onClick() is called here instead of in the onClick() function because onClick() is called before onHold()
        //and the isDragged variable stayed true despite of the left mouse button not being pressed.
    }

    @Override
    public void onClick() {
        super.onClick();
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (pinned) {
            updatePosition();
        }
        updateStats();
        batch.draw(texture, position.x, position.y);
        batch.draw(npc.getTexture(), position.x + 10, position.y + 115, npc.getTexture().getRegionWidth() * 4, npc.getTexture().getRegionHeight() * 4);
        UI.FONT.draw(batch, name, position.x + 10, position.y + 105);
        UI.FONT.draw(batch, "job: " + job, position.x + 10, position.y - 23 + 105);
        String stat;
        for (int i = 0; i < NPC.Stats.values().length; i++) {
            stat = NPC.Stats.values()[i].toString().toLowerCase() + ": ";
            UI.FONT.draw(batch, stat + npc.getStats()[i], position.x + 10, position.y - (i+2) * 23 + 105);
        }
        closeButton.setPosition(position.x + texture.getRegionWidth() - closeButton.getTexture().getRegionWidth(),
                                position.y + texture.getRegionHeight() - closeButton.getTexture().getRegionHeight());
        closeButton.draw(batch);
    }

    private void updatePosition() {
        npcPosition = GameScreen.getCamera().project(new Vector3(npc.getSpritePosition().x * World.TILE_SIZE, npc.getSpritePosition().y * World.TILE_SIZE, 0));
        position = new Vector2i((int)(npcPosition.x + 20/GameScreen.getCamera().zoom), (int)(npcPosition.y + 10/GameScreen.getCamera().zoom));
    }

    private void updateStats() {
        name = npc.getName() + " " + npc.getSurname();
        job = npc.getJob().toString().toLowerCase();
        stats = npc.getStats();
    }

    private void close() {
        setVisible(false);
        pinned = false;
        isDragged = false;
    }
}
