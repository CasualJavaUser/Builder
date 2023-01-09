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
        LOG_CABIN, LOG_CABIN_CS,
        LUMBERJACK_HUT, LUMBERJACK_HUT_CS,
        MINE, MINE_CS,
        FUNGI, FUNGUS, HOUSE_FUNGUS, WORK_FUNGUS, SERVICE_FUNGUS, STORAGE_FUNGUS, CONSTRUCTION_OFFICE,
        HOUSE_FUNGUS_CS, WORK_FUNGUS_CS, SERVICE_FUNGUS_CS, STORAGE_FUNGUS_CS, CONSTRUCTION_OFFICE_CS
    }

    public enum Tile implements TextureId {
        DEFAULT,
        DIRT,
        GRASS1, GRASS2, GRASS3,
        WATER1, WATER2
    }

    public enum Npc implements TextureId {
        FUNGUY
    }

    public enum Ui implements TextureId {
        //buttons
        FUNGUS,
        HAMMER, NPC, WORK, REST, DEMOLISH,
        HOUSE, AXE, PICKAXE, BIG_HAMMER, STORAGE, BUILD,
        SAVE, LOAD, DELETE,

        DIVIDER,
        SMALL_BUTTON, BIG_BUTTON,
        CLOCK_FACE, HOUR_HAND, MINUTE_HAND, PAUSE, PLAY, X2SPEED, X3SPEED,

        SERVICE, CONSTRUCTION_OFFICE,
        NO_STORAGE, FULL_STORAGE, NO_RESOURCES, WINDOW, MENU_WINDOW, WIDE_AREA,
        CLOSE_BUTTON, TEXT_FIELD
    }

    public enum Resource implements TextureId {
        NOTHING, WOOD, STONE, IRON, COAL, STEEL, TOOLS
    }

    public enum Environment implements TextureId {
        PINE_TREE, ROCK1, ROCK2, ROCK3
    }
}
