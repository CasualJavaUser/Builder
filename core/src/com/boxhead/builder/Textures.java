package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

public class Textures {
    private static TextureAtlas buildingAtlas;
    private static TextureAtlas tileAtlas;
    private static TextureAtlas iconAtlas;

    private static HashMap<String, TextureRegion> buildings;
    private static HashMap<String, TextureRegion> tiles;
    private static HashMap<String, TextureRegion> icons;

    public static void initTextures() {
        buildingAtlas = new TextureAtlas("buildings.atlas");
        tileAtlas = new TextureAtlas("tiles.atlas");
        iconAtlas = new TextureAtlas("icons.atlas");

        buildings = new HashMap<>();
        tiles = new HashMap<>();
        icons = new HashMap<>();

        for (TextureAtlas.AtlasRegion ar : buildingAtlas.getRegions()) {
            buildings.put(ar.name, ar);
        }
        for (TextureAtlas.AtlasRegion ar : tileAtlas.getRegions()) {
            tiles.put(ar.name, ar);
        }
        for (TextureAtlas.AtlasRegion ar : iconAtlas.getRegions()) {
            icons.put(ar.name, ar);
        }
    }

    public static TextureRegion getBuilding(String name) {
        if (buildings.get(name) != null) return buildings.get(name);
        else return buildings.get("fungus");
    }

    public static TextureRegion getTile(String name) {
        if (tiles.get(name) != null) return tiles.get(name);
        else return tiles.get("default");
    }

    public static TextureRegion getIcon(String name) {
        if (icons.get(name) != null) return icons.get(name);
        else return icons.get("house");
    }
}
