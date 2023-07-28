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

import static com.boxhead.builder.game_objects.Villager.Order.Type.*;

public class ProductionBuilding extends StorageBuilding {
    /**
     * How many production cycles worth of input resources to keep.
     */
    private static final int stockCycles = 3;
    public static final int shiftsPerJob = 3;

    private static final Map<Villager, FieldWork> emptyMap = new HashMap<>(0);  //do not modify
    private static final Set<Building> emptySet = new HashSet<>(0);

    private static final String notEmployee = "Villager does not work here";

    protected float efficiency = 1f;
    protected int jobQuality = 0;
    protected int employeesInside = 0;
    protected Set<Villager> employees;
    protected final Shift[] shifts;
    protected final Map<Villager, FieldWork> assignedFieldWork;
    protected final Set<Building> buildingsInRange;
    protected float productionCounter = 0;
    protected boolean showRange = false;
    protected transient UIElement indicator;

    public ProductionBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        this.type = type;
        shifts = new Shift[type.jobs.length * shiftsPerJob];
        employees = new HashSet<>(type.workerCapacity);

        if (shiftsPerJob != 3) throw new IllegalStateException();
        if (type.isService()) {
            for (int i = 0; i < type.jobs.length; i++) {
                shifts[i * shiftsPerJob]     = new Shift(type.jobs[i], Job.ShiftTime.THREE_ELEVEN, type.shifts[0] ? type.workersPerShift : 0);
                shifts[i * shiftsPerJob + 1] = new Shift(type.jobs[i], Job.ShiftTime.ELEVEN_SEVEN, type.shifts[1] ? type.workersPerShift : 0);
                shifts[i * shiftsPerJob + 2] = new Shift(type.jobs[i], Job.ShiftTime.SEVEN_THREE,  type.shifts[2] ? type.workersPerShift : 0);
            }
        } else {
            for (int i = 0; i < type.jobs.length; i++) {
                shifts[i * shiftsPerJob]     = new Shift(type.jobs[i], Job.ShiftTime.MIDNIGHT_EIGHT,type.shifts[0] ? type.workersPerShift : 0);
                shifts[i * shiftsPerJob + 1] = new Shift(type.jobs[i], Job.ShiftTime.EIGHT_FOUR,    type.shifts[1] ? type.workersPerShift : 0);
                shifts[i * shiftsPerJob + 2] = new Shift(type.jobs[i], Job.ShiftTime.FOUR_MIDNIGHT, type.shifts[2] ? type.workersPerShift : 0);
            }
        }

        int fieldShifts = 0;
        for (Job job : type.jobs) {
            if (job.getPoI() != null)
                fieldShifts += shiftsPerJob;
        }

        if (fieldShifts > 0) {
            assignedFieldWork = new HashMap<>(fieldShifts * type.workersPerShift);
        } else assignedFieldWork = emptyMap;

        if (type.range > 0f) {
            buildingsInRange = World.getBuildings().stream()
                    .filter((b) -> b.getCollider().distance(entrancePosition) < type.range && !(b instanceof ConstructionSite))
                    .collect(Collectors.toSet());
        } else buildingsInRange = emptySet;
        updateEfficiency();

        instantiateIndicator();
    }

    public void removeEmployee(Villager villager) {
        for (Shift shift : shifts) {
            if (shift.employees.remove(villager)) {
                employees.remove(villager);
                return;
            }
        }
        throw new IllegalArgumentException(notEmployee);
    }

    public void addEmployee(Villager villager) {

        Shift bestShift = Arrays.stream(shifts)
                .filter(shift -> shift.employees.size() < shift.maxEmployees)
                .min(Comparator.comparingDouble(shift -> (double) shift.employees.size() / (double) shift.maxEmployees))
                .orElseThrow();

        bestShift.employees.add(villager);
        employees.add(villager);
    }

    public void employeeEnter(Villager villager) {
        if (employees.contains(villager))
            employeesInside++;
        else
            throw new IllegalArgumentException(notEmployee);
    }

    public void employeeExit(Villager villager) {
        if (employees.contains(villager))
            employeesInside--;
        else
            throw new IllegalArgumentException(notEmployee);
    }

    public boolean isHiring() {
        for (Shift shift : shifts) {
            if (shift.employees.size() < shift.maxEmployees) return true;
        }
        return false;
    }

    public boolean canProduce() {
        return hasEmployeesInside() && inventory.checkStorageAvailability(type.mainJob.getRecipe(this)) == Inventory.Availability.AVAILABLE;
    }

    public void business() {
        for (Shift shift : shifts) {
            Job job = shift.job;
            for (Villager employee : shift.employees) {
                if (employee.isClockedIn())
                    job.continuousTask(employee, this);
                if (employee.isInBuilding(this) && !employee.hasOrders()) {
                    job.assign(employee, this);
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
    }

    public void dissociateFieldWork(Villager employee) {
        assignedFieldWork.remove(employee);
    }

    public void startShift(Job.ShiftTime shiftTime) {
        for (Shift shift : shifts) {
            if (shift.shiftTime != shiftTime)
                continue;

            for (Villager villager : shift.employees) {
                villager.giveOrder(EXIT);
                villager.giveOrder(GO_TO, this);
                villager.giveOrder(CLOCK_IN);
            }
        }
    }

    public void endShift(Job.ShiftTime shiftTime) {
        for (Shift shift : shifts) {
            if (shift.shiftTime != shiftTime)
                continue;

            for (Villager employee : shift.employees) {
                if (employee.isClockedIn()) {
                    employee.clearOrderQueue();
                    shift.job.onExit(employee, this);
                    employee.giveOrder(EXIT, this);
                    employee.giveOrder(CLOCK_OUT);
                    if (employee.getHome() != null) {
                        employee.giveOrder(GO_TO, employee.getHome());
                    }
                }
            }
        }
    }

    public void endShift(Villager employee) {
        for (Shift shift : shifts) {
            if (shift.employees.contains(employee)) {
                employee.clearOrderQueue();
                shift.job.onExit(employee, this);
                employee.giveOrder(EXIT, this);
                employee.giveOrder(CLOCK_OUT);
            }
        }
    }

    public void setShiftActivity(int index, boolean active) {
        for (int i = index; i < shifts.length; i += shiftsPerJob) {
            if (active) {
                shifts[i].maxEmployees = type.workersPerShift;
            } else {
                shifts[i].maxEmployees = 0;
                for (Villager employee : shifts[i].employees) {
                    if (employee.isInBuilding(this)) employeesInside--;
                    employee.looseJob();
                    employees.remove(employee);
                }
                shifts[i].employees.clear();
            }
        }
    }

    public boolean isShiftEnabled(int index) {
        return shifts[index].maxEmployees > 0;
    }

    public static class Shift {
        Job job;
        Job.ShiftTime shiftTime;
        Set<Villager> employees;
        int maxEmployees;

        public Shift(Job job, Job.ShiftTime shiftTime, int maxEmployees) {
            this.job = job;
            this.shiftTime = shiftTime;
            this.maxEmployees = maxEmployees;
            employees = new HashSet<>(maxEmployees);
        }
    }

    public void updateEfficiency() {
        efficiency = type.updateEfficiency.apply(buildingsInRange);
    }

    public Job getJob() {
        return type.mainJob;
    }

    public Shift getShift(Villager employee) {
        for (Shift shift : shifts) {
            if (shift.employees.contains(employee))
                return shift;
        }
        return null;
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
                UI.Layer.WORLD,
                new Vector2i(),
                false);
        indicator.addToUI();
    }

    private void updateIndicator() {
        switch (inventory.checkStorageAvailability(type.mainJob.getRecipe(this))) {
            case AVAILABLE:
                indicator.setVisible(false);
                break;
            case LACKS_INPUT:
                indicator.setTexture(Textures.get(Textures.Ui.NO_INPUT));
            case OUTPUT_FULL:
                indicator.setTexture(Textures.get(Textures.Ui.FULL_OUTPUT));
            default:
                indicator.setVisible(true);
        }

        if (indicator.isVisible()) {
            Vector2 screenPos = GameScreen.worldToScreenPosition(
                    gridPosition.x * World.TILE_SIZE + getTexture().getRegionWidth() / 2f - indicator.getWidth() / 2f * GameScreen.camera.zoom,
                    gridPosition.y * World.TILE_SIZE + getTexture().getRegionHeight() + 5);
            indicator.setGlobalPosition((int) screenPos.x, (int) screenPos.y);
        }
    }

    @Override
    public String toString() {
        return type.name +
                "\njob: " + type.mainJob +
                "\nefficiency: " + efficiency +
                "\njobQuality: " + jobQuality +
                "\nemployees: " + employees.size() + " / " + type.workerCapacity +
                "\nemployeesInside: " + employeesInside +
                "\nassignedFieldWork: " + assignedFieldWork +
                "\nbuildingsInRange: " + buildingsInRange.size() +
                "\nproductionCounter: " + productionCounter +
                "\nshowRange: " + showRange;
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
        instantiateIndicator();
    }
}
