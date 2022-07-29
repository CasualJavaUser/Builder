package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.ui.UIElement;
import com.boxhead.builder.utils.Vector2i;

public class ProductionBuilding extends EnterableBuilding {
    protected Jobs job;
    protected int jobQuality = 0;
    protected int employeeCapacity, employeeCount = 0, employeesInside = 0;
    protected NPC[] employees;
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
        employees = new NPC[employeeCapacity];
        indicator = new UIElement(null, new Vector2i(texture.getRegionWidth() / 2 - 8, texture.getRegionHeight() + 10));
    }

    /**
     * Removes the specified employee from the building.
     *
     * @param npc employee to be removed from the building
     * @return true if the array of employees changed as a result of the call
     */
    public boolean removeEmployee(NPC npc) {
        if (employeeCount > 0) {
            for (int i = 0; i < employees.length; i++) {
                if (employees[i] == npc) {
                    employees[i] = null;
                    employeeCount--;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds the specified employee to the building.
     *
     * @param npc employee to be added to the building
     * @return true if the array of employees changed as a result of the call
     */
    public boolean addEmployee(NPC npc) {
        if (employeeCount < employeeCapacity) {
            for (int i = 0; i < employees.length; i++) {
                if (employees[i] == null) {
                    employees[i] = npc;
                    employeeCount++;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean employeeEnter(NPC npc) {
        for (NPC employee : employees) {
            if (employee == npc) {
                employeesInside++;
                return true;
            }
        }
        return false;
    }

    public void employeeExit() {
        employeesInside--;
    }

    public void produceResources() {
        if (job.getPoI() != null) {
            for (NPC employee : employees) {
                if (employee != null && employee.isInBuilding() && gridPosition.equals(employee.getGridPosition())) {
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
                    if (availability == -1) indicator.setTexture(Textures.getTile("grass"));  //TODO temp textures
                    else indicator.setTexture(Textures.getTile("dirt"));
                    indicator.setVisible(true);
                    storage = getClosestStorage();
                }
            } else {
                availability = 2;  //availability is set to 2 so that the appropriate information can be displayed in the BuildingStatWindow.
                indicator.setTexture(Textures.getTile("default"));
                indicator.setVisible(true);
                storage = getClosestStorage();
            }
        }
    }

    protected void sendEmployee(NPC npc) {
        if (findPoI() != null) {
            npc.exitBuilding();
            npc.navigateTo(job.getPoI());
            npc.setDestination(NPC.Pathfinding.Destination.FIELD_WORK);
        }
    }

    protected void recallEmployees() {
        for (NPC employee : employees) {
            if (employee == null) continue;

            if (employee.getDestination() == NPC.Pathfinding.Destination.FIELD_WORK) {
                FieldWork harvestable = Harvestable.getByCoordinates(employee.getDestinationTile());
                if (harvestable != null) {
                    harvestable.dissociateWorker(employee);
                } else {
                    EnterableBuilding building = EnterableBuilding.getByCoordinates(employee.getDestinationTile());
                    if (building instanceof FieldWork) {
                        ((FieldWork) building).dissociateWorker(employee);
                    }
                }
                employee.navigateTo(employee.getHome());
                employee.setDestination(NPC.Pathfinding.Destination.HOME);
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

    public int getEmployeeCapacity() {
        return employeeCapacity;
    }

    public int getEmployeeCount() {
        return employeeCount;
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
