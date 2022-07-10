package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

public class Textures {
    private static TextureAtlas buildingsAtlas;
    private static TextureAtlas tileAtlas;

    private static HashMap<String, TextureRegion> buildings;
    private static HashMap<String, TextureRegion> tiles;

    public static void initTextures() {
        buildingsAtlas = new TextureAtlas("buildings.atlas");
        tileAtlas = new TextureAtlas("tiles.atlas");
        buildings = new HashMap<>();
        tiles = new HashMap<>();

        for (TextureAtlas.AtlasRegion ar : buildingsAtlas.getRegions()) {
            buildings.put(ar.name, ar);
        }
        for (TextureAtlas.AtlasRegion ar : tileAtlas.getRegions()) {
            tiles.put(ar.name, ar);
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
}