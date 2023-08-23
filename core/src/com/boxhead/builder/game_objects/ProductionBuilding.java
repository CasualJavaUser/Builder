package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Circle;
import com.boxhead.builder.utils.Vector2i;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.boxhead.builder.game_objects.Villager.Order.Type.*;

public class ProductionBuilding extends StorageBuilding {
    /**
     * How many production cycles worth of input resources to keep.
     */
    private static final int STOCK_CYCLES = 3;
    public static final int SHIFTS_PER_JOB = 3;
    protected static final Job.ShiftTime[] DEFAULT_SHIFT_TIMES =
            new Job.ShiftTime[] {Job.ShiftTime.MIDNIGHT_EIGHT, Job.ShiftTime.EIGHT_FOUR, Job.ShiftTime.FOUR_MIDNIGHT};
    protected static final Job.ShiftTime[] SERVICE_SHIFT_TIMES =
            new Job.ShiftTime[] {Job.ShiftTime.THREE_ELEVEN, Job.ShiftTime.ELEVEN_SEVEN, Job.ShiftTime.SEVEN_THREE};

    private static final Map<Villager, FieldWork> emptyMap = new HashMap<>(0);  //do not modify
    private static final Set<Building> emptySet = new HashSet<>(0);

    private static final String notEmployee = "Villager does not work here";

    protected float efficiency = 1f;
    protected int jobQuality = 0;
    protected int employeesInside = 0;
    protected final Set<Villager> employees;
    protected int employeeCapacity;
    protected final Shift[] shifts;
    protected final Map<Villager, FieldWork> assignedFieldWork;
    protected final Set<Building> buildingsInRange;
    protected float productionCounter = 0;
    protected boolean showRange = false;

    public ProductionBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        this.type = type;
        shifts = new Shift[SHIFTS_PER_JOB];
        employees = new HashSet<>(type.maxEmployeeCapacity * SHIFTS_PER_JOB);
        employeeCapacity = type.maxEmployeeCapacity;

        if (SHIFTS_PER_JOB != 3) throw new IllegalStateException();
        if (type.isService()) {
            for (int i = 0; i < SHIFTS_PER_JOB; i++) {
                shifts[i] = new Shift(SERVICE_SHIFT_TIMES[i], type.getShiftActivity(i) ? employeeCapacity : 0);
            }
        } else {
            for (int i = 0; i < SHIFTS_PER_JOB; i++) {
                shifts[i] = new Shift(DEFAULT_SHIFT_TIMES[i], type.getShiftActivity(i) ? employeeCapacity : 0);
            }
        }

        if (type.job.getPoI() != null) {
            assignedFieldWork = new HashMap<>(SHIFTS_PER_JOB * type.maxEmployeeCapacity);
        } else assignedFieldWork = emptyMap;

        if (type.range > 0f) {
            buildingsInRange = World.getBuildings().stream()
                    .filter((b) -> b.getCollider().distance(entrancePosition) < type.range && !(b instanceof ConstructionSite))
                    .collect(Collectors.toSet());
        } else buildingsInRange = emptySet;
        updateEfficiency();
    }

    public void removeEmployee(Villager villager) {
        for (Shift shift : shifts) {
            if (shift.employees.remove(villager)) {
                employees.remove(villager);
                villager.quitJob();
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
        return hasEmployeesInside() && inventory.checkStorageAvailability(type.job.getRecipe(this)) == Inventory.Availability.AVAILABLE;
    }

    public void business() {
        for (Shift shift : shifts) {
            Job job = type.job;
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
                            if (recipe.getChange(resource) < 0 && inventory.getResourceAmount(resource) < STOCK_CYCLES * -recipe.getChange(resource)) {
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
            if (shift.shiftTime == shiftTime) {
                for (Villager villager : shift.employees) {
                    villager.giveOrder(EXIT);
                    villager.giveOrder(GO_TO, this);
                    villager.giveOrder(CLOCK_IN);
                }
                break;
            }
        }
    }

    public void endShift(Job.ShiftTime shiftTime) {
        for (Shift shift : shifts) {
            if (shift.shiftTime == shiftTime) {
                for (Villager employee : shift.employees) {
                    if (employee.isClockedIn()) {
                        employee.clearOrderQueue();
                        type.job.onExit(employee, this);
                        employee.giveOrder(EXIT, this);
                        employee.giveOrder(CLOCK_OUT);
                        if (employee.getHome() != null) {
                            employee.giveOrder(GO_TO, employee.getHome());
                        }
                    }
                }
                break;
            }
        }
    }

    public void endShift(Villager employee) {
        for (Shift shift : shifts) {
            if (shift.employees.contains(employee)) {
                employee.clearOrderQueue();
                type.job.onExit(employee, this);
                employee.giveOrder(EXIT, this);
                employee.giveOrder(CLOCK_OUT);
                break;
            }
        }
    }

    public void setShiftActivity(int index, boolean active) {
        if (active) {
            shifts[index].maxEmployees = employeeCapacity;
        } else {
            shifts[index].maxEmployees = 0;
            for (Villager employee : shifts[index].employees) {
                employee.quitJob();
                employees.remove(employee);
            }
            shifts[index].employees.clear();
        }
    }

    public void setEmployeeCapacity(int capacity) {
        employeeCapacity = capacity;
        for (int i = 0; i < SHIFTS_PER_JOB; i++) {
            if (!type.getShiftActivity(i))
                continue;

            shifts[i].maxEmployees = capacity;
            int toFire = shifts[i].employees.size() - capacity;
            for (int j = 0; j < toFire; j++) {
                Villager employee = shifts[i].employees.stream().findFirst().get();
                employee.quitJob();
                employees.remove(employee);
                shifts[i].employees.remove(employee);
            }
        }
    }

    public int getEmployeeCapacity() {
        return employeeCapacity;
    }

    public static class Shift implements Serializable {
        Job.ShiftTime shiftTime;
        Set<Villager> employees;
        int maxEmployees;

        public Shift(Job.ShiftTime shiftTime, int maxEmployees) {
            this.shiftTime = shiftTime;
            this.maxEmployees = maxEmployees;
            employees = new HashSet<>(maxEmployees);
        }

        public Set<Villager> getEmployees() {
            return employees;
        }

        public int getMaxEmployees() {
            return maxEmployees;
        }

        public Job.ShiftTime getShiftTime() {
            return shiftTime;
        }
    }

    public void updateEfficiency() {
        efficiency = type.updateEfficiency.apply(buildingsInRange);
    }

    public Job getJob() {
        return type.job;
    }

    public Shift getShift(Villager employee) {
        for (Shift shift : shifts) {
            if (shift.employees.contains(employee))
                return shift;
        }
        return null;
    }

    public Shift getShift(int index) {
        if (index >= shifts.length)
            throw new IllegalStateException();

        return shifts[index];
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

    public int getEmployeesInside() {
        return employeesInside;
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
            Circle.draw(
                    batch,
                    Textures.Tile.DEFAULT,
                    entrancePosition,
                    type.range);
            batch.setColor(UI.DEFAULT_COLOR);
        }

        super.draw(batch);

        drawIndicator(batch);
    }

    protected void drawIndicator(SpriteBatch batch) {
        TextureRegion texture;
        switch (inventory.checkStorageAvailability(type.job.getRecipe(this))) {
            case LACKS_INPUT:
                texture = Textures.get(Textures.Ui.NO_INPUT); break;
            case OUTPUT_FULL:
                texture = Textures.get(Textures.Ui.FULL_OUTPUT); break;
            default:
                return;
        }
        batch.draw(
                texture,
                ((float)gridPosition.x + (float)collider.getWidth() / 2f) * World.TILE_SIZE - 32 * GameScreen.camera.zoom,
                (gridPosition.y + collider.getHeight()) * World.TILE_SIZE,
                0,
                0,
                64,
                64,
                GameScreen.camera.zoom,
                GameScreen.camera.zoom,
                0
        );
    }

    @Override
    public String toString() {
        return type.name +
                "\njob: " + type.job +
                "\nefficiency: " + efficiency +
                "\njobQuality: " + jobQuality +
                "\nshifts: " + type.getShiftActivity(0) + ", " + type.getShiftActivity(1) + ", " + type.getShiftActivity(2) +
                "\nemployees: " + employees.size() + " / " + (employeeCapacity * SHIFTS_PER_JOB) +
                "\nemployees per shift: " + employeeCapacity + " / " + type.maxEmployeeCapacity +
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
    }
}
