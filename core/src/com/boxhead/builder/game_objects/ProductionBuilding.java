package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.TileCircle;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.ui.UIElement;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.*;
import java.util.stream.Collectors;

public class ProductionBuilding extends StorageBuilding {
    /**
     * How many production cycles worth of input resources to keep.
     */
    private static final int stockCycles = 3;

    private static final Map<Villager, FieldWork> emptyMap = new HashMap<>(0);  //do not modify
    private static final Set<Building> emptySet = new HashSet<>(0);

    protected transient Job job;
    protected float efficiency = 1f;
    protected int jobQuality = 0;
    protected int employeesInside = 0;
    protected final Set<Villager> employees;
    protected final Map<Villager, FieldWork> assignedFieldWork;
    protected final Set<Building> buildingsInRange;
    protected float productionCounter = 0;
    protected boolean showRange = false;
    protected transient UIElement indicator;

    public ProductionBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        this.type = type;
        job = type.job;
        employees = new HashSet<>(type.npcCapacity, 1f);

        if (job.getPoI() != null) {
            assignedFieldWork = new HashMap<>(type.npcCapacity, 1f);
        }
        else assignedFieldWork = emptyMap;

        if (type.range > 0f) {
            buildingsInRange = World.getBuildings().stream()
                    .filter((b) -> b.getCollider().distance(entrancePosition) < type.range && !(b instanceof ConstructionSite))
                    .collect(Collectors.toSet());
        }
        else buildingsInRange = emptySet;
        updateEfficiency();

        instantiateIndicator();
    }

    /**
     * Removes the specified employee from the building.
     *
     * @param villager employee to be removed from the building
     */
    public void removeEmployee(Villager villager) {
        if (!employees.contains(villager))
            throw new IllegalArgumentException("Employee does not work here");
        employees.remove(villager);
    }

    /**
     * Adds the specified employee to the building.
     *
     * @param villager employee to be added to the building
     */
    public void addEmployee(Villager villager) {
        if (employees.contains(villager))
            throw new IllegalArgumentException("Employee already works here");
        if (employees.size() < type.npcCapacity) {
            employees.add(villager);
        }
    }

    public void employeeEnter(Villager villager) {
        if (employees.contains(villager)) {
            employeesInside++;
        }
    }

    public void employeeExit() {
        employeesInside--;
    }

    public boolean isHiring() {
        return employees.size() < type.npcCapacity;
    }

    public boolean canProduce() {
        return hasEmployeesInside() && inventory.checkStorageAvailability(job.getRecipe(this)) == Inventory.Availability.AVAILABLE;
    }

    public void business() {
        for (Villager employee : employees) {
            if (employee.isClockedIn() && !employee.hasOrders()) {
                job.assign(employee, this);
                break;
            }
        }

        if (type.productionInterval > 0) {
            productionCounter += employeesInside * efficiency;

            if (productionCounter >= type.productionInterval) {
                Recipe recipe = job.getRecipe(this);
                Inventory.Availability availability = inventory.checkStorageAvailability(recipe);

                if (availability == Inventory.Availability.AVAILABLE) {
                    inventory.put(recipe);
                    World.updateStoredResources(recipe);
                    productionCounter = 0;
                    Logistics.requestTransport(this, recipe);
                } else if (availability == Inventory.Availability.LACKS_INPUT) {
                    for (Resource resource : recipe.changedResources()) {
                        if (recipe.getChange(resource) < 0 && inventory.getResourceAmount(resource) < stockCycles * -recipe.getChange(resource)) {
                            Logistics.requestTransport(this, resource, recipe.getChange(resource));
                        }
                    }
                }
            }
        }
    }

    public void dissociateFieldWork(Villager employee) {
        assignedFieldWork.remove(employee);
    }

    public void endWorkday() {
        for (Villager employee : employees) {
            employee.clearOrderQueue();
            job.onExit(employee, this);
            employee.giveOrder(Villager.Order.Type.EXIT, this);
            employee.giveOrder(Villager.Order.Type.CLOCK_OUT);
            if (employee.getHome() != null) {
                employee.giveOrder(Villager.Order.Type.GO_TO, employee.getHome());
                employee.giveOrder(Villager.Order.Type.ENTER, employee.getHome());
            }
        }
    }

    public void updateEfficiency() {
        efficiency = type.updateEfficiency.apply(buildingsInRange);
    }

    public Job getJob() {
        return job;
    }

    public float getEfficiency() {
        return efficiency;
    }

    public int getJobQuality() {
        return jobQuality;
    }

    public Set<Villager> getEmployees() {
        return employees;
    }

    public boolean hasEmployeesInside() {
        return employeesInside != 0;
    }

    public Map<Villager, FieldWork> getAssignedFieldWork() {
        return assignedFieldWork;
    }

    public void showRangeVisualiser(boolean show) {
        showRange = show;
    }

    public Set<Building> getBuildingsInRange() {
        return buildingsInRange;
    }

    public boolean isBuildingInRange(Building building) {
        return type.range > 0 && building.getCollider().distance(entrancePosition) < type.range && !building.equals(this);
    }

    @Override
    public void onClick() {
        super.onClick();
        showRange = true;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (showRange) {
            batch.setColor(UI.VERY_TRANSPARENT);
            TileCircle.draw(
                    batch,
                    Textures.get(Textures.Tile.DEFAULT),
                    entrancePosition.multiply(World.TILE_SIZE),
                    type.range * World.TILE_SIZE);
            batch.setColor(UI.DEFAULT_COLOR);
        }

        updateIndicator();

        super.draw(batch);
    }

    protected void instantiateIndicator() {
        indicator = new UIElement(
                Textures.get(Textures.Ui.FULL_OUTPUT),
                UI.Layer.BUILDINGS,
                new Vector2i(),
                false);
        indicator.addToUI();
    }

    private void updateIndicator() {
        switch (inventory.checkStorageAvailability(job.getRecipe(this))) {
            case AVAILABLE: indicator.setVisible(false); break;
            case LACKS_INPUT: indicator.setTexture(Textures.get(Textures.Ui.NO_INPUT));
            case OUTPUT_FULL: indicator.setTexture(Textures.get(Textures.Ui.FULL_OUTPUT));
            default: indicator.setVisible(true);
        }

        if (indicator.isVisible()) {
            Vector2 screenPos = GameScreen.worldToScreenPosition(
                    gridPosition.x * World.TILE_SIZE + getTexture().getRegionWidth() / 2f - indicator.getWidth() / 2f * GameScreen.camera.zoom,
                    gridPosition.y * World.TILE_SIZE + getTexture().getRegionHeight() + 5);
            indicator.setGlobalPosition((int) screenPos.x, (int) screenPos.y);
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(type.name());
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        type = Buildings.Type.valueOf(ois.readUTF());
        textureId = type.texture;
        job = type.job;
        instantiateIndicator();
    }
}
