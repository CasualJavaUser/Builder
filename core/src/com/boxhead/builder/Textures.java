package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

public class Textures {
    private static TextureAtlas buildingAtlas;
    private static TextureAtlas tileAtlas;
    private static TextureAtlas UIAtlas;
    private static TextureAtlas npcAtlas;
    private static TextureAtlas resourceAtlas;

    private static HashMap<String, TextureRegion> buildings;
    private static HashMap<String, TextureRegion> tiles;
    private static HashMap<String, TextureRegion> ui;
    private static HashMap<String, TextureRegion> npcs;
    private static HashMap<String, TextureRegion> resources;

    public static void initTextures() {
        buildingAtlas = new TextureAtlas("buildings.atlas");
        tileAtlas = new TextureAtlas("tiles.atlas");
        UIAtlas = new TextureAtlas("ui.atlas");
        npcAtlas = new TextureAtlas("npcs.atlas");
        //resourceAtlas = new TextureAtlas("resources.atlas");  //TODO make resource.atlas

        buildings = new HashMap<>();
        tiles = new HashMap<>();
        ui = new HashMap<>();
        npcs = new HashMap<>();
        resources = new HashMap<>();

        for (TextureAtlas.AtlasRegion ar : buildingAtlas.getRegions()) {
            buildings.put(ar.name, ar);
        }
        for (TextureAtlas.AtlasRegion ar : tileAtlas.getRegions()) {
            tiles.put(ar.name, ar);
        }
        for (TextureAtlas.AtlasRegion ar : UIAtlas.getRegions()) {
            ui.put(ar.name, ar);
        }
        for (TextureAtlas.AtlasRegion ar : npcAtlas.getRegions()) {
            npcs.put(ar.name, ar);
        }
        for (Resources resource : Resources.values()) {  //TODO temp solution
            resources.put(resource.toString().toLowerCase(), getTile("dirt"));
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

    public static TextureRegion getNPC(String name) {
        if (npcs.get(name) != null) return npcs.get(name);
        else return npcs.get("funguy");
    }

    public static TextureRegion getResource(String name) {
        if (resources.get(name) != null) return resources.get(name);
        else return npcs.get("funguy");
    }
}
