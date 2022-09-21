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
    protected final Job job;
    protected int jobQuality = 0;
    protected int employeeCapacity, employeesInside = 0;
    protected final Set<NPC> employees;
    protected final Map<NPC, FieldWork> assignedFieldWork;
    protected int productionCounter = 0, productionInterval;
    protected UIElement indicator;
    protected int availability = 0;

    private final static int storageDistance = 20;

    public ProductionBuilding(String name, TextureRegion texture, Vector2i gridPosition, Job job, int employeeCapacity, Vector2i entrancePosition, int productionInterval) {
        super(name, texture, gridPosition, entrancePosition);
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

    public ProductionBuilding(String name, TextureRegion texture, Vector2i gridPosition, Job job, int employeeCapacity, Vector2i entrancePosition) {
        super(name, texture, gridPosition, entrancePosition);
        if (job.producesAnyResources())
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
     */
    public void removeEmployee(NPC npc) {
        if (!employees.contains(npc))
            throw new IllegalArgumentException("Employee does not work here");
        employees.remove(npc);
    }

    /**
     * Adds the specified employee to the building.
     *
     * @param npc employee to be added to the building
     */
    public void addEmployee(NPC npc) {
        if (employees.contains(npc))
            throw new IllegalArgumentException("Employee already works here");
        if (employees.size() < employeeCapacity) {
            employees.add(npc);
        }
    }

    public void employeeEnter(NPC npc) {
        if (employees.contains(npc)) {
            employeesInside++;
        }
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
                if (gridPosition.equals(employee.getGridPosition()) && !assignedFieldWork.containsKey(employee)
                        && inventory.getAvailableCapacityFor(Resource.NOTHING) >= NPC.INVENTORY_SIZE) {
                    sendEmployee(employee);
                }
            }
        }

        if (job.producesAnyResources()) {
            Inventory.Availability availability = inventory.checkStorageAvailability(job);

            if (availability == Inventory.Availability.AVAILABLE) {
                productionCounter += employeesInside;
                if (productionCounter >= productionInterval) {
                    inventory.put(job);
                    productionCounter -= productionInterval;
                }
                indicator.setVisible(false);
            } else {
                if (availability == Inventory.Availability.LACKS_INPUT)
                    indicator.setTexture(Textures.get(Textures.Ui.NO_RESOURCES));
                else indicator.setTexture(Textures.get(Textures.Ui.FULL_STORAGE));
                indicator.setVisible(true);
            }
        }
    }

    protected void sendEmployee(NPC npc) {
        FieldWork fieldWork = findPoI();
        if (fieldWork != null) {
            if (fieldWork instanceof Harvestable) {
                inventory.put(Resource.NOTHING, NPC.INVENTORY_SIZE);    //reserve space for whatever the employee brings back
            }
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

    public Job getJob() {
        return job;
    }

    public int getJobQuality() {
        return jobQuality;
    }

    public int getAvailability() {
        return availability;
    }

    public int getEmployeeCapacity() {
        return employeeCapacity;
    }

    public Set<NPC> getEmployees() {
        return employees;
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
