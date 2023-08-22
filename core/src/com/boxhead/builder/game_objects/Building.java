package com.boxhead.builder.game_objects;

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

    private static int nextId = 0;
    private final int id;

    public Building (Buildings.Type type, Vector2i gridPosition) {
        this(type, type.texture, gridPosition);
    }

    public Building (Buildings.Type type, Textures.TextureId texture, Vector2i gridPosition) {
        super(texture, gridPosition);
        this.type = type;
        collider = type.relativeCollider.cloneAndTranslate(gridPosition);
        id = nextId;
        nextId++;
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

    public int getId() {
        return id;
    }

    @Override
    public boolean isMouseOver() {
        return collider.overlaps(GameScreen.getMouseGridPosition());
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
        textureId = type.texture;
    }
}
