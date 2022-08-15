package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.UIElement;
import com.boxhead.builder.utils.Vector2i;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProductionBuilding extends EnterableBuilding {
    protected final Jobs job;
    protected int jobQuality = 0;
    protected int employeeCapacity, employeesInside = 0;
    protected final Set<NPC> employees;
    protected final Map<NPC, FieldWork> assignedFieldWork;
    protected int productionCounter = 0, productionInterval;
    protected StorageBuilding storage = null;
    protected UIElement indicator;
    protected int availability = 0;

    private final static int storageDistance = 20;

    public ProductionBuilding(String name, TextureRegion texture, Jobs job, int employeeCapacity, Vector2i entrancePosition, int productionInterval) {
        super(name, texture, entrancePosition);
        this.job = job;
        this.employeeCapacity = employeeCapacity;
        this.productionInterval = productionInterval;
        employees = new HashSet<>(employeeCapacity, 1f);
        if (job.getPoI() != null) {
            assignedFieldWork = new HashMap<>(employeeCapacity, 1f);
        } else {
            assignedFieldWork = null;
        }
        indicator = new UIElement(null, new Vector2i(texture.getRegionWidth() / 2 - 8, texture.getRegionHeight() + 10));
    }

    public ProductionBuilding(String name, TextureRegion texture, Jobs job, int employeeCapacity, Vector2i entrancePosition) {
        super(name, texture, entrancePosition);
        if (job.getResources()[0] != Resources.NOTHING)
            throw new IllegalArgumentException("this constructor requires the building not to produce anything");

        this.job = job;
        this.employeeCapacity = employeeCapacity;
        employees = new HashSet<>(employeeCapacity, 1f);
        if (job.getPoI() != null) {
            assignedFieldWork = new HashMap<>(employeeCapacity, 1f);
        } else {
            assignedFieldWork = null;
        }
        indicator = new UIElement(null, new Vector2i(texture.getRegionWidth() / 2 - 8, texture.getRegionHeight() + 10));
    }

    /**
     * Removes the specified employee from the building.
     *
     * @param npc employee to be removed from the building
     * @return true if the set of employees changed as a result of the call
     */
    public boolean removeEmployee(NPC npc) {
        return employees.remove(npc);
    }

    /**
     * Adds the specified employee to the building.
     *
     * @param npc employee to be added to the building
     * @return true if the set of employees changed as a result of the call
     */
    public boolean addEmployee(NPC npc) {
        if (employees.size() < employeeCapacity) {
            return employees.add(npc);
        }
        return false;
    }

    public boolean employeeEnter(NPC npc) {
        if (employees.contains(npc)) {
            employeesInside++;
            return true;
        }
        return false;
    }

    public void employeeExit() {
        employeesInside--;
    }

    public boolean isHiring() {
        return employees.size() < employeeCapacity;
    }

    public void produceResources() {
        if (job.getPoI() != null) {
            for (NPC employee : employees) {
                if (employee != null && gridPosition.equals(employee.getGridPosition())) {
                    sendEmployee(employee);
                }
            }
        }

        if (job.getResources()[0] != Resources.NOTHING) {
            if (storage != null) {
                availability = storage.checkStorageAvailability(job);
                if (availability == 0) {
                    productionCounter += employeesInside;
                    if (productionCounter >= productionInterval) {
                        storage.addToStorage(job);
                        productionCounter -= productionInterval;
                    }
                    indicator.setVisible(false);
                } else {
                    if (availability == -1)
                        indicator.setTexture(Textures.get(Textures.Tile.GRASS));  //TODO temp textures
                    else indicator.setTexture(Textures.get(Textures.Tile.DIRT));
                    indicator.setVisible(true);
                    storage = getClosestStorage();
                }
            } else {
                availability = 2;  //availability is set to 2 so that the appropriate information can be displayed in the BuildingStatWindow.
                indicator.setTexture(Textures.get(Textures.Tile.DEFAULT));
                indicator.setVisible(true);
                storage = getClosestStorage();
            }
        }
    }

    protected void sendEmployee(NPC npc) {
        FieldWork fieldWork = findPoI();
        if (fieldWork != null) {
            fieldWork.assignWorker(npc);
            assignedFieldWork.put(npc, fieldWork);
            npc.giveOrder(NPC.Order.Type.EXIT, this);
            npc.giveOrder(NPC.Order.Type.GO_TO, fieldWork);
            npc.giveOrder(NPC.Order.Type.ENTER, fieldWork);
        }
    }

    public void dissociateFieldWork(NPC employee) {
        assignedFieldWork.remove(employee);
    }

    public void endWorkday() {
        if (job.getPoI() == null) {
            for (NPC employee : employees) {
                employee.giveOrder(NPC.Order.Type.EXIT, this);
                employee.giveOrder(NPC.Order.Type.GO_TO, employee.getHome());
            }
        } else {
            for (NPC employee : employees) {
                employee.clearOrderQueue();
                if (assignedFieldWork.containsKey(employee)) {
                    employee.giveOrder(NPC.Order.Type.EXIT, assignedFieldWork.get(employee));
                    assignedFieldWork.remove(employee);
                } else {
                    employee.giveOrder(NPC.Order.Type.EXIT, this);
                }
                if (employee.getHome() != null) {
                    employee.giveOrder(NPC.Order.Type.GO_TO, employee.getHome());
                    employee.giveOrder(NPC.Order.Type.ENTER, employee.getHome());
                }
            }
        }
    }

    protected FieldWork findPoI() {
        for (Building building : World.getBuildings()) {
            if (building instanceof FieldWork && ((FieldWork) building).isFree() && ((FieldWork) building).getCharacteristic() == job.getPoI()) {
                return (FieldWork) building;
            }
        }
        for (Harvestable harvestable : World.getHarvestables()) {
            if (harvestable.isFree() && harvestable.getCharacteristic() == job.getPoI()) {
                return harvestable;
            }
        }
        return null;    //no FieldWork available
    }

    public Jobs getJob() {
        return job;
    }

    public int getJobQuality() {
        return jobQuality;
    }

    public int getAvailability() {
        return availability;
    }

    public StorageBuilding getStorage() {
        return storage;
    }

    /**
     * @return the closest available StorageBuilding. If there StorageBuildings in range but none are available then the closest one is returned.
     */
    private StorageBuilding getClosestStorage() {
        StorageBuilding closest = null;
        double distance = storageDistance;
        boolean isAvailable = false;
        for (Building storageBuilding : World.getBuildings()) {
            if (storageBuilding instanceof StorageBuilding) {
                if (gridPosition.distance(storageBuilding.getGridPosition()) <= distance &&
                        (!isAvailable || (((StorageBuilding) storageBuilding).checkStorageAvailability(job) == 0))) {
                    //isAvailable -> (((StorageBuilding)storageBuilding).checkStorageAvailability(job) == 0)
                    distance = gridPosition.distance(storageBuilding.getGridPosition());
                    closest = (StorageBuilding) storageBuilding;
                    if ((((StorageBuilding) storageBuilding).checkStorageAvailability(job) == 0)) isAvailable = true;
                }
            }
        }
        return closest;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        if (indicator.isVisible()) {
            batch.draw(indicator.getTexture(), gridPosition.x * World.TILE_SIZE + indicator.getPosition().x,
                    gridPosition.y * World.TILE_SIZE + indicator.getPosition().y);
        }
    }
}
