package com.boxhead.builder.game_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.boxhead.builder.GameScreen;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.WorldObject;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

public class GameObject implements WorldObject, Serializable {
    @Serial
    private static final long serialVersionUID = 3L;
    public static final Comparator<GameObject> gridPositionComparator = Comparator.comparingLong(go -> go.getGridPosition().gridHash());

    protected final Vector2i gridPosition;
    protected transient Textures.TextureId textureId;

    public GameObject(Textures.TextureId textureId, Vector2i gridPosition) {
        this.gridPosition = gridPosition;
        this.textureId = textureId;
    }

    public GameObject(Textures.TextureId textureId) {
        this.textureId = textureId;
        gridPosition = new Vector2i();
    }

    @Override
    public Vector2i getGridPosition() {
        return gridPosition;
    }

    public void setGridPosition(Vector2i gridPosition) {
        this.gridPosition.set(gridPosition);
    }

    public TextureRegion getTexture() {
        return Textures.get(textureId);
    }

    public void draw(SpriteBatch batch) {
            batch.draw(getTexture(), gridPosition.x * World.TILE_SIZE, gridPosition.y * World.TILE_SIZE);
    }

    protected BoxCollider getDefaultCollider() {
        return new BoxCollider(gridPosition, getTexture().getRegionWidth() / World.TILE_SIZE, getTexture().getRegionHeight() / World.TILE_SIZE);
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(textureId.getClass().getSimpleName());
        oos.writeUTF(textureId.name());
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        String className = ois.readUTF();
        String fieldName = ois.readUTF();

        try {
        for (Class<?> declaredClass : Textures.class.getDeclaredClasses()) {
            if (declaredClass.getSimpleName().equals(className)) {
                //textureId = (Textures.TextureId) declaredClass.getDeclaredField(fieldName).get(declaredClass);
                //break;
                for (Field declaredField : declaredClass.getDeclaredFields()) {
                    if (declaredField.getName().equals(fieldName)) {
                        textureId = (Textures.TextureId) declaredField.get(declaredClass);
                        return;
                    }
                }
            }
        }
        throw new IllegalStateException("Texture '" + fieldName + "' not found in enum '" + className + "'");
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Texture '" + fieldName + "' not found in enum '" + className + "'");
        }
    }
}
