package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Textures {
    private static TextureAtlas buildingAtlas;
    private static TextureAtlas tileAtlas;
    private static TextureAtlas uiAtlas;
    private static TextureAtlas npcAtlas;
    private static TextureAtlas resourceAtlas;
    private static TextureAtlas environmentAtlas;

    private static final HashMap<TextureId, TextureRegion> textures = new HashMap<>();
    private static final HashMap<TextureId, TextureRegion[]> bundles = new HashMap<>();
    private static final HashMap<TextureId, Animation<TextureRegion>> animations = new HashMap<>();

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
        loadTextures(environmentAtlas, Harvestables.values());

        loadBundles(environmentAtlas, Harvestables.values());

        loadAnimations(npcAtlas, NpcAnimation.values());
    }

    public static TextureRegion get(TextureId textureId) {
        if (textures.containsKey(textureId)) {
            return textures.get(textureId);
        } else {
            throw new IllegalArgumentException("Texture not found: " + textureId);
        }
    }

    public static TextureRegion[] getBundle(TextureId textureId) {
        if (textures.containsKey(textureId)) {
            return bundles.get(textureId);
        } else {
            throw new IllegalArgumentException("Texture not found: " + textureId);
        }
    }

    public static Animation<TextureRegion> getAnimation(TextureId animationId) {
        if (animations.containsKey(animationId)) {
            return animations.get(animationId);
        } else {
            throw new IllegalArgumentException("Texture not found: " + animationId);
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

    private static void loadBundles(TextureAtlas textureAtlas, TextureId[] textureIds) {
        for (TextureId textureId : textureIds) {
            TextureRegion[] bundle = textureAtlas.findRegions(textureId.name().toLowerCase()).toArray();
            if (bundle == null) {
                throw new IllegalStateException("Bundle '" + textureId.name().toLowerCase() + "' not found in texture atlas");
            }
            bundles.put(textureId, bundle);
        }
    }

    private static void loadAnimations(TextureAtlas textureAtlas, TextureId[] textureIds) {
        for (TextureId textureId : textureIds) {
            Animation<TextureRegion> animation = new Animation<>(.15f, textureAtlas.findRegions(textureId.name().toLowerCase()), Animation.PlayMode.LOOP);
            if (animation.getKeyFrames().length <= 0) {
                throw new IllegalStateException("Animation '" + textureId.name().toLowerCase() + "' not found in texture atlas");
            }
            animations.put(textureId, animation);
        }
    }

    public interface TextureId {
        String name();
    }

    public enum Building implements TextureId {
        LOG_CABIN, LOG_CABIN_CS,
        LUMBERJACKS_HUT, LUMBERJACKS_HUT_CS,
        MINE, MINE_CS,
        BUILDERS_HUT, BUILDERS_HUT_CS,
        STORAGE_BARN, STORAGE_BARN_CS,
        CARRIAGE_HOUSE, CARRIAGE_HOUSE_CS,
        STONE_GATHERERS_SHACK, STONE_GATHERERS_SHACK_CS,
        TOOL_SHACK, TOOL_SHACK_CS,

        FUNGUS, SERVICE_FUNGUS, SERVICE_FUNGUS_CS
    }

    public enum Tile implements TextureId {
        DEFAULT,
        DIRT,
        GRASS1, GRASS2, GRASS3,
        WATER1, WATER2
    }

    public enum Npc implements TextureId {
        IDLE0, IDLE1
    }

    public enum NpcAnimation implements TextureId {
        WALK_LEFT0, WALK_RIGHT0, WALK_LEFT1, WALK_RIGHT1
    }

    public enum Ui implements TextureId {
        //buttons
        FUNGUS,
        HAMMER, NPC, WORK, REST, DEMOLISH, PAUSE_GAME,
        HOUSE, AXE, PICKAXE, PICKAXE_WITH_STONE, BIG_HAMMER, BARN, CARRIAGE, BUILD,
        SAVE, LOAD, DELETE,
        INFRASTRUCTURE_TAB, HOUSING_TAB, RESOURCES_TAB, SERVICES_TAB,

        DIVIDER,
        SMALL_BUTTON, BIG_BUTTON,
        CLOCK_FACE, HOUR_HAND, MINUTE_HAND, PAUSE, PLAY, X2SPEED, X3SPEED,
        FULL_OUTPUT, NO_INPUT,

        SERVICE, CONSTRUCTION_OFFICE,
        WINDOW, MENU_WINDOW, WIDE_AREA,
        CLOSE_BUTTON, TEXT_FIELD
    }

    public enum Resource implements TextureId {
        NOTHING, WOOD, STONE, IRON, COAL, STEEL, TOOLS, GRAIN, ALCOHOL
    }

    public enum Environment implements TextureId {
        //PINE_TREE, ROCK1, ROCK2, ROCK3,
        FENCE_T, FENCE_TL, FENCE_L, FENCE_BL, FENCE_B, FENCE_BR, FENCE_R, FENCE_TR
    }

    public enum Harvestables implements TextureId {
        PINE_TREE, ROCK1, ROCK2, ROCK3, WHEAT
    }
}
