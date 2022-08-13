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

    private static final HashMap<TextureId, TextureRegion> textures = new HashMap<>();

    public static void initTextures() {
        buildingAtlas = new TextureAtlas("buildings.atlas");
        tileAtlas = new TextureAtlas("tiles.atlas");
        uiAtlas = new TextureAtlas("ui.atlas");
        npcAtlas = new TextureAtlas("npcs.atlas");
        //resourceAtlas = new TextureAtlas("resources.atlas");  //TODO make resource.atlas

        loadTextures(buildingAtlas, Building.values());
        loadTextures(tileAtlas, Tile.values());
        loadTextures(uiAtlas, Ui.values());
        loadTextures(npcAtlas, Npc.values());

        for (Resource resource : Resource.values()) {
            textures.put(resource, get(Tile.DIRT));  //TODO temp solution
        }
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
        FUNGI, FUNGUS, HOUSE_FUNGUS, SERVICE_FUNGUS, WORK_FUNGUS
    }

    public enum Tile implements TextureId {
        DEFAULT, DIRT, GRASS
    }

    public enum Npc implements TextureId {
        FUNGUY
    }

    public enum Ui implements TextureId {
        CLOCK_FACE, CLOSE_BUTTON, FUNGUS, HOME, HOUR_HAND, HOUSE, MINUTE_HAND, NPC, SERVICE, STAT_WINDOW, WORKPLACE
    }

    public enum Resource implements TextureId {
        NOTHING, WOOD, IRON, COAL, STEEL, TOOLS
    }
}
