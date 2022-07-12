package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

public class Textures {
    private static TextureAtlas buildingAtlas;
    private static TextureAtlas tileAtlas;
    private static TextureAtlas UIAtlas;

    private static HashMap<String, TextureRegion> buildings;
    private static HashMap<String, TextureRegion> tiles;
    private static HashMap<String, TextureRegion> ui;

    public static void initTextures() {
        buildingAtlas = new TextureAtlas("buildings.atlas");
        tileAtlas = new TextureAtlas("tiles.atlas");
        UIAtlas = new TextureAtlas("ui.atlas");

        buildings = new HashMap<>();
        tiles = new HashMap<>();
        ui = new HashMap<>();

        for (TextureAtlas.AtlasRegion ar : buildingAtlas.getRegions()) {
            buildings.put(ar.name, ar);
        }
        for (TextureAtlas.AtlasRegion ar : tileAtlas.getRegions()) {
            tiles.put(ar.name, ar);
        }
        for (TextureAtlas.AtlasRegion ar : UIAtlas.getRegions()) {
            ui.put(ar.name, ar);
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

    public static TextureRegion getUI(String name) {
        if (ui.get(name) != null) return ui.get(name);
        else return ui.get("house");
    }
}
