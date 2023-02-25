package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.Clickable;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;

public class Building extends GameObject implements Clickable {
    protected transient Buildings.Type type;
    protected BoxCollider collider;

    public Building (Buildings.Type type, Vector2i gridPosition) {
        this(type, type.getTexture(), gridPosition);
    }

    public Building (Buildings.Type type, TextureRegion texture, Vector2i gridPosition) {
        super(texture, gridPosition);
        this.type = type;
        collider = type.relativeCollider.cloneAndTranslate(gridPosition);
    }

    public String getName() {
        return type.name;
    }

    public BoxCollider getCollider() {
        return collider;
    }

    public Buildings.Type getType() {
        return type;
    }

    @Override
    public boolean isMouseOver() {
        int colliderX = collider.getGridPosition().x;
        int colliderY = collider.getGridPosition().y;
        Vector2 mousePos = GameScreen.getMouseWorldPosition();

        return mousePos.x >= colliderX * World.TILE_SIZE && mousePos.x < (colliderX * World.TILE_SIZE + collider.getWidth() * World.TILE_SIZE)
                && mousePos.y >= colliderY * World.TILE_SIZE && mousePos.y < (colliderY * World.TILE_SIZE + collider.getHeight() * World.TILE_SIZE);
    }

    @Override
    public void onClick() {
        UI.showBuildingStatWindow(this);
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(type.name());
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        type = Buildings.Type.valueOf(ois.readUTF());
        texture = type.getTexture();
    }
}
