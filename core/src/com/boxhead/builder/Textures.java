package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.Animation;
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
        PLANTATION, PLANTATION_CS,
        RANCH, RANCH_CS,
        PUB, PUB_CS,
        HOSPITAL, HOSPITAL_CS,
        SCHOOL, SCHOOL_CS,
        WATERMILL, WATERMILL_CS,
        FISHING_HUT, FISHING_HUT_CS,

        FUNGUS, SERVICE_FUNGUS, SERVICE_FUNGUS_CS
    }

    public enum Tile implements TextureId {
        DEFAULT,
        FARMLAND,
        DIRT1, DIRT2, DIRT3,
        GRASS1, GRASS2, GRASS3,
        WATER1, WATER2,
        BANK_T, BANK_R, BANK_B, BANK_L, BANK_TL, BANK_BR, BANK_BL, BANK_TR, BANK_TRO, BANK_BRO, BANK_BLO, BANK_TLO,
        BRIDGE, BANK_T_BRIDGE, BANK_R_BRIDGE, BANK_B_BRIDGE, BANK_L_BRIDGE, BANK_TL_BRIDGE, BANK_BR_BRIDGE,
        BANK_BL_BRIDGE, BANK_TR_BRIDGE, BANK_TRO_BRIDGE, BANK_BRO_BRIDGE, BANK_BLO_BRIDGE, BANK_TLO_BRIDGE,
        PATH_CROSS, PATH_LR, PATH_TB, PATH_TL, PATH_TR, PATH_BR, PATH_BL, PATH_T_L, PATH_T_T, PATH_T_R, PATH_T_B
    }

    public enum Npc implements TextureId {
        IDLE0, IDLE1, COW, PIG
    }

    public enum NpcAnimation implements TextureId {
        WALK0, CHOPPING0, HAMMERING0, HARVESTING0, MINING0, SOWING0,
        WALK1, CHOPPING1, HAMMERING1, HARVESTING1, MINING1, SOWING1,
        COW, PIG
    }

    public enum Ui implements TextureId {
        SMALL_BUTTON, WIDE_BUTTON, BIG_BUTTON, WINDOW, MENU_WINDOW, WIDE_AREA, GO_BUTTON,
        CLOSE_BUTTON, TEXT_FIELD, WIDE_TEXT_FIELD, LEFT_ARROW, RIGHT_ARROW, POWER_BUTTON,

        NOT_ACTIVE, DEMOLISHING, FULL_OUTPUT, NO_INPUT, SHIFT_DIVIDER,

        FUNGUS, HAMMER, NPC, WORK, REST, DEMOLISH, SHIFTS, GRAPH_BUTTON, PAUSE_GAME,
        CLOCK_FACE, HOUR_HAND, MINUTE_HAND, PAUSE, PLAY, X2SPEED, X3SPEED,

        INFRASTRUCTURE_TAB, HOUSING_TAB, RESOURCES_TAB, SERVICES_TAB, DIVIDER,
        HOUSE, AXE, PICKAXE, PICKAXE_WITH_STONE, BIG_HAMMER, BARN, CARRIAGE, SERVICE, CONSTRUCTION_OFFICE, HOE, MUG,
        PATH, REMOVE_PATH, BRIDGE, CROSS, BOOK, COW, BUILD, FISHING_ROD, WHEEL,

        SAVE, LOAD, DELETE,

        TIMELINE_SEGMENT, TIMELINE_TOP, CHECK_BOX, CHECK, GRAPH
    }

    public enum Resource implements TextureId {
        NOTHING, WOOD, STONE, IRON, COAL, STEEL, TOOLS, GRAIN, ALCOHOL, MILK, MEAT, FISH
    }

    public enum Environment implements TextureId {
        FENCE_T, FENCE_TL, FENCE_L, FENCE_BL, FENCE_B, FENCE_BR, FENCE_R, FENCE_TR
    }

    public enum Harvestables implements TextureId {
        PINE_TREE, OAK_TREE,
        ROCK1, ROCK2, ROCK3, WHEAT
    }
}
