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
        try {
            return buildings.get(name);
        } catch (NullPointerException e) {
            System.err.println("Missing texture: " + name);
            return buildings.get("fungus");
        }
    }

    public static TextureRegion getTile(String name) {
        try {
            return tiles.get(name);
        } catch (NullPointerException e) {
            System.err.println("Missing texture: " + name);
            return tiles.get("default");
        }
    }
}
