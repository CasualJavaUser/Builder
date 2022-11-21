package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

public class Textures {
    private static TextureAtlas buildingAtlas;
    private static TextureAtlas tileAtlas;
    private static TextureAtlas uiAtlas;
    private static TextureAtlas npcAtlas;
    private static TextureAtlas resourceAtlas;
    private static TextureAtlas environmentAtlas;

    private static final HashMap<TextureId, TextureRegion> textures = new HashMap<>();

    public static void init() {
        buildingAtlas = new TextureAtlas("buildings.atlas");
        tileAtlas = new TextureAtlas("tiles.atlas");
        uiAtlas = new TextureAtlas("ui.atlas");
        npcAtlas = new TextureAtlas("npcs.atlas");
        resourceAtlas = new TextureAtlas("resources.atlas");
        environmentAtlas = new TextureAtlas("environment.atlas");

        loadTextures(buildingAtlas, Building.values());
        loadTextures(tileAtlas, Tile.values());
        loadTextures(uiAtlas, Ui.values());
        loadTextures(npcAtlas, Npc.values());
        loadTextures(resourceAtlas, Resource.values());
        loadTextures(environmentAtlas, Environment.values());
    }

    public static TextureRegion get(TextureId textureId) {
        if (textures.containsKey(textureId)) {
            return textures.get(textureId);
        } else {
            throw new IllegalArgumentException("Texture not found: " + textureId);
        }
    }

    private static void loadTextures(TextureAtlas textureAtlas, TextureId[] textureIds) {
        for (TextureId textureId : textureIds) {
            TextureAtlas.AtlasRegion atlasRegion = textureAtlas.findRegion(textureId.name().toLowerCase());
            if (atlasRegion == null) {
                throw new IllegalStateException("Texture '" + textureId.name().toLowerCase() + "' not found in texture atlas");
            }
            textures.put(textureId, atlasRegion);
        }
    }

    private interface TextureId {
        String name();
    }

    public enum Building implements TextureId {
        FUNGI, FUNGUS, HOUSE_FUNGUS, WORK_FUNGUS, SERVICE_FUNGUS, STORAGE_FUNGUS, CONSTRUCTION_OFFICE,
        HOUSE_FUNGUS_CS, WORK_FUNGUS_CS, SERVICE_FUNGUS_CS, STORAGE_FUNGUS_CS, CONSTRUCTION_OFFICE_CS
    }

    public enum Tile implements TextureId {
        DEFAULT, DIRT, GRASS, GRASS1, GRASS2, GRASS3, WATER
    }

    public enum Npc implements TextureId {
        FUNGUY
    }

    public enum Ui implements TextureId {
        CLOCK_FACE, CLOSE_BUTTON, FUNGUS, HOME, HOUR_HAND, HOUSE, MINUTE_HAND, NPC, SERVICE, WORKPLACE, NO_STORAGE, FULL_STORAGE, NO_RESOURCES, WORK, REST, DEMOLISH, STORAGE, CONSTRUCTION_OFFICE, WINDOW,
        PAUSE, PLAY, X2SPEED, X3SPEED
    }

    public enum Resource implements TextureId {
        NOTHING, WOOD, IRON, COAL, STEEL, TOOLS
    }

    public enum Environment implements TextureId {
        SMALL_TREE, BIG_TREE, BUSH, STONE1, STONE2
    }
}
