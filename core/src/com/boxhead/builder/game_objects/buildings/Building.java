package com.boxhead.builder.game_objects.buildings;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.GameObject;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.lang.reflect.Field;
import java.util.*;

public class Building extends GameObject {
    public static class Type {
        public final Textures.Building texture;
        public final String name;
        /**
         * Relative position of the tile from which NPCs can enter. The lower left tile of the building is at (0,0).
         */
        public final Vector2i entrancePosition;
        public final BoxCollider relativeCollider;
        public final Recipe buildCost;

        protected static final Type[] values;

        public static final Type STORAGE_BARN = new Type (
                Textures.Building.STORAGE_BARN,
                "storage barn",
                new Vector2i(2, -1),
                new BoxCollider(0, 0, 5, 4),
                new Recipe(Pair.of(Resource.WOOD, 50))
        );

        static {
            values = initValues(Type.class).toArray(Type[]::new);
        }

        public Type(Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider, Recipe buildCost) {
            this.texture = texture;
            this.name = name;
            this.relativeCollider = relativeCollider;
            this.buildCost = buildCost;
            this.entrancePosition = entrancePosition;
        }

        public static Type[] values() {
            return values;
        }

        public TextureRegion getTexture() {
            return Textures.get(texture);
        }

        public Textures.TextureId getConstructionSite() {
            try {
                return Textures.Building.valueOf(texture.name() + "_CS");
            } catch (IllegalArgumentException e) {
                return texture;
            }
        }

        protected static<T extends Type> List<T> initValues(Class<T> typeClass) {
            try {
                Field[] fields = Arrays.stream(typeClass.getFields())
                        .filter(f -> f.getType().equals(typeClass))
                        .sorted(Comparator.comparing(Field::getName))
                        .toArray(Field[]::new);

                List<T> values = new ArrayList<>(fields.length);

                for (Field field : fields) {
                    values.add(((T) field.get(typeClass)));
                }

                return values;
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        protected static Type getByName(String name) {
            for (Type value : values) {
                if (value.name.equals(name))
                    return value;
            }
            throw new IllegalStateException("type with name \"" + name + "\" not found.");
        }
    }

    private static int nextId = 0;
    private final int id;

    protected transient Type type;
    protected BoxCollider collider;
    /**
     * Absolute position of the tile from which NPCs can enter.
     */
    public final Vector2i entrancePosition;
    public final Inventory inventory;
    private final Map<Pair<Villager, Resource>, Integer> inventoryReservations = new HashMap<>();
    private final Map<Resource, Integer> reservedTotals = new HashMap<>();
    protected String warning = "";

    public enum Indicator {
        NO_INPUT(Textures.Ui.NO_INPUT),
        FULL_OUTPUT(Textures.Ui.FULL_OUTPUT),
        NOT_ACTIVE(Textures.Ui.NOT_ACTIVE),
        DEMOLISHING(Textures.Ui.DEMOLISHING);

        private final Textures.Ui texture;

        Indicator(Textures.Ui texture) {
            this.texture = texture;
        }

        public Textures.Ui getTexture() {
            return texture;
        }
    }

    public Building(Type type, Vector2i gridPosition) {
        this(type, type.texture, gridPosition, 200);
    }

    public Building(Building.Type type, Textures.TextureId texture, Vector2i gridPosition, int storageCapacity) {
        super(texture, gridPosition);
        this.type = type;
        collider = type.relativeCollider.cloneAndTranslate(gridPosition);
        id = nextId;
        nextId++;
        entrancePosition = gridPosition.plus(type.entrancePosition);
        inventory = new Inventory(storageCapacity);
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        checkAndDrawIndicator(batch);
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return type.name;
    }

    public BoxCollider getCollider() {
        return collider;
    }

    public int getId() {
        return id;
    }

    public boolean isMouseOver() {
        return collider.overlaps(GameScreen.getMouseGridPosition());
    }

    public Vector2i getEntrancePosition() {
        return entrancePosition;
    }

    public static Building getByCoordinates(Vector2i gridPosition) {
        for (Building building : World.getBuildings()) {
            if (building.getGridPosition().equals(gridPosition)) {
                return building;
            }
        }
        return null;
    }

    public void reserveResources(Resource resource, int units) {
        reserveResources(null, resource, units);
    }

    public void reserveResources(Villager reservee, Resource resource, int units) {
        if (getFreeResources(resource) >= units) {
            updateReservations(reservee, resource, units);
        }
    }

    public boolean reserveSpace(int units) {
        return reserveSpace(null, units);
    }

    public boolean reserveSpace(Villager reservee, int units) {
        if (inventory.getAvailableCapacity() >= reservedTotals.getOrDefault(Resource.NOTHING, 0) + units) {
            updateReservations(reservee, Resource.NOTHING, units);
            return true;
        }
        return false;
    }

    public void cancelReservation(int units) {
        Pair<Villager, Resource> pair = Pair.of(null, Resource.NOTHING);
        Integer currentlyReserved = inventoryReservations.get(pair);
        if (currentlyReserved == null || currentlyReserved < units)
            throw new IllegalArgumentException("cancelling reservation that wasn't made");

        updateReservations(null, Resource.NOTHING, -units);
    }

    public void cancelReservation(Villager reservee) {
        Pair<Villager, Resource> pair = Pair.of(reservee, Resource.NOTHING);
        Integer reservedUnits = inventoryReservations.get(pair);
        if (reservedUnits == null)
            throw new IllegalArgumentException("cancelling reservation that wasn't made");

        updateReservations(reservee, Resource.NOTHING, -reservedUnits);
    }

    public void transferReservationOwnership(Villager currentReservee, Villager newReservee, Resource resource, int units) {
        Pair<Villager, Resource> pair = Pair.of(currentReservee, resource);
        Integer reservedUnits = inventoryReservations.get(pair);

        if (reservedUnits == null || reservedUnits < units)
            throw new IllegalArgumentException();

        if (reservedUnits > units)
            inventoryReservations.put(pair, reservedUnits - units);
        else
            inventoryReservations.remove(pair);

        pair = Pair.of(newReservee, resource);
        reservedUnits = inventoryReservations.get(pair);
        if (reservedUnits == null)
            inventoryReservations.put(pair, units);
        else
            inventoryReservations.put(pair, reservedUnits + units);
    }

    public void moveReservedResources(Villager reservee, Inventory source, Inventory destination, Resource resource, int movedUnits) {
        source.moveResourcesTo(destination, resource, movedUnits);
        if (this.inventory == source) {
            updateReservations(reservee, resource, -movedUnits);
        } else if (this.inventory == destination) {
            updateReservations(reservee, Resource.NOTHING, -movedUnits);
        } else throw new IllegalArgumentException();
    }

    public boolean hasReserved(Villager villager, Resource resource) {
        return inventoryReservations.containsKey(Pair.of(villager, resource));
    }

    public int getFreeResources(Resource resource) {
        return inventory.getResourceAmount(resource) - reservedTotals.getOrDefault(resource, 0);
    }

    public int getFreeSpace() {
        return inventory.getAvailableCapacity() - reservedTotals.getOrDefault(Resource.NOTHING, 0);
    }

    public int getReservedBy(Villager reservee, Resource resource) {
        Pair<Villager, Resource> pair = Pair.of(reservee, resource);
        return inventoryReservations.getOrDefault(pair, 0);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void emptyOccupants(){}

    private void updateReservations(Villager reservee, Resource resource, int units) {
        Pair<Villager, Resource> pair = Pair.of(reservee, resource);
        int reserved = inventoryReservations.getOrDefault(pair, 0) + units;
        if (reserved == 0)
            inventoryReservations.remove(pair);
        else
            inventoryReservations.put(pair, reserved);

        reserved = reservedTotals.getOrDefault(resource, 0) + units;
        if (reserved == 0)
            reservedTotals.remove(resource);
        else
            reservedTotals.put(resource, reserved);
    }

    protected void checkAndDrawIndicator(SpriteBatch batch) {
        if (inventory.isFull()) {
            drawIndicator(Indicator.NO_INPUT, batch);
            warning = "inventory full";
        }
    }

    protected void drawIndicator(Indicator indicator, SpriteBatch batch) {
        batch.setColor(UI.DEFAULT_UI_COLOR);
        batch.draw(
                Textures.get(indicator.getTexture()),
                ((float) gridPosition.x + (float) collider.getWidth() / 2f) * World.TILE_SIZE - 32 * GameScreen.camera.zoom,
                (gridPosition.y + collider.getHeight()) * World.TILE_SIZE,
                0,
                0,
                64,
                64,
                GameScreen.camera.zoom,
                GameScreen.camera.zoom,
                0
        );
        batch.setColor(UI.DEFAULT_COLOR);
    }

    public String getInfo() {
        StringBuilder info = new StringBuilder();
        info.append(inventory.getDisplayedAmount()).append(" / ").append(inventory.getMaxCapacity());
        for (Resource resource : inventory.getStoredResources()) {
            if (resource != Resource.NOTHING) {
                info.append("\n").append(resource.toString().toLowerCase()).append(": ").append(inventory.getResourceAmount(resource));
            }
        }
        return info.toString();
    }

    public String getWarning() {
        return warning;
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(type.getClass().getName());
        oos.writeUTF(type.name);
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        try {
            Class<? extends Type> typeClass = (Class<? extends Type>) Class.forName(ois.readUTF());
            type = typeClass.cast(typeClass.getDeclaredMethod("getByName", String.class).invoke(typeClass, ois.readUTF()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        textureId = type.texture;
    }
}
