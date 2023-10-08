package com.boxhead.builder.game_objects.buildings;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.Villager;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Circle;
import com.boxhead.builder.utils.Pair;
import com.boxhead.builder.utils.Vector2i;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.boxhead.builder.game_objects.Villager.Order.Type.*;

public class ProductionBuilding extends Building {
    public static class Type extends Building.Type {
        public final Job job;
        public final int maxEmployeeCapacity, productionInterval, range;
        public final Function<Set<Building>, Float> updateEfficiency;
        protected final boolean[] shifts = new boolean[]{false, true, false};

        protected static Type[] values;

        public static final Type LUMBERJACKS_HUT = new Type(
                Textures.Building.LUMBERJACKS_HUT,
                "lumberjack's hut",
                new Vector2i(1, -1),
                new BoxCollider(0, 0, 4, 3),
                new Recipe(Pair.of(Resource.WOOD, 20)),
                Jobs.LUMBERJACK,
                1,
                0,
                15
        );
        public static final Type MINE = new Type(
                Textures.Building.MINE,
                "mine",
                new Vector2i(1, -1),
                new BoxCollider(0, 0, 3, 2),
                new Recipe(Pair.of(Resource.WOOD, 40),
                        Pair.of(Resource.STONE, 20)),
                Jobs.MINER,
                2,
                300,
                10,
                (buildingsInRange) -> {
                    float efficiency = 1f - buildingsInRange.size() / 3f;
                    if (efficiency < 0) efficiency = 0;
                    return efficiency;
                }
        );
        public static final Type BUILDERS_HUT = new Type(
                Textures.Building.BUILDERS_HUT,
                "builder's hut",
                new Vector2i(1, -1),
                new BoxCollider(0, 0, 4, 2),
                new Recipe(Pair.of(Resource.WOOD, 20)),
                Jobs.BUILDER,
                3
        );
        public static final Type TRANSPORT_OFFICE = new Type(
                Textures.Building.CARRIAGE_HOUSE,
                "carriage house",
                new Vector2i(1, -1),
                new BoxCollider(0, 0, 5, 2),
                new Recipe(Pair.of(Resource.WOOD, 20)),
                Jobs.CARRIER,
                5
        );
        public static final Type STONE_GATHERERS = new Type(
                Textures.Building.STONE_GATHERERS_SHACK,
                "stone gatherer's shack",
                new Vector2i(1, -1),
                new BoxCollider(0, 0, 4, 2),
                new Recipe(Pair.of(Resource.WOOD, 30)),
                Jobs.STONEMASON,
                2,
                0,
                15
        );

        static {
            BUILDERS_HUT.shifts[2] = true;
            values = initValues(Type.class).toArray(Type[]::new);
        }

        protected Type(
                Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider,
                Recipe buildCost, Job job, int maxEmployeeCapacity, int productionInterval, int range,
                Function<Set<Building>, Float> updateEfficiency
        ) {
            super(texture, name, entrancePosition, relativeCollider, buildCost);
            this.job = job;
            this.maxEmployeeCapacity = maxEmployeeCapacity;
            this.productionInterval = productionInterval;
            this.range = range;
            this.updateEfficiency = updateEfficiency;
        }

        protected Type(
                Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider,
                Recipe buildCost, Job job, int maxEmployeeCapacity, int productionInterval, int range
        ) {
            this(texture, name, entrancePosition, relativeCollider, buildCost, job, maxEmployeeCapacity, productionInterval, range, (b) -> 1f);
        }

        protected Type(
                Textures.Building texture, String name, Vector2i entrancePosition, BoxCollider relativeCollider,
                Recipe buildCost, Job job, int maxEmployeeCapacity
        ) {
            this(texture, name, entrancePosition, relativeCollider, buildCost, job, maxEmployeeCapacity, 0, 0, (b) -> 1f);
        }

        public static Type[] values() {
            return values;
        }

        public void setShiftActivity(int index, boolean active) {
            shifts[index] = active;

            for (Building building : World.getBuildings()) {
                if (building.type == this) {
                    if (building instanceof ConstructionSite)
                        continue;
                    ((ProductionBuilding) building).setShiftActivity(index, active);
                }
            }
        }

        public boolean getShiftActivity(int index) {
            return shifts[index];
        }

        protected static Type getByName(String name) {
            for (Type value : values) {
                if (value.name.equals(name))
                    return value;
            }
            throw new IllegalStateException();
        }
    }

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

    protected boolean active = true;
    protected float efficiency = 1f;
    protected int jobQuality = 0;
    protected int employeesInside = 0;
    protected final Set<Villager> employees;
    /**
     * Number of employees in each active shift
     */
    protected int employeeCapacity;
    protected final Shift[] shifts;
    protected final Map<Villager, FieldWork> assignedFieldWork;
    protected final Set<Building> buildingsInRange;
    protected float productionCounter = 0;
    protected boolean showRange = false;

    public ProductionBuilding(Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        this.type = type;
        shifts = new Shift[SHIFTS_PER_JOB];
        employees = new HashSet<>(type.maxEmployeeCapacity * SHIFTS_PER_JOB);
        employeeCapacity = type.maxEmployeeCapacity;

        if (SHIFTS_PER_JOB != 3) throw new IllegalStateException();
        if (type instanceof ServiceBuilding.Type) {
            for (int i = 0; i < SHIFTS_PER_JOB; i++) {
                shifts[i] = new Shift(SERVICE_SHIFT_TIMES[i], employeeCapacity, type.getShiftActivity(i));
            }
        } else {
            for (int i = 0; i < SHIFTS_PER_JOB; i++) {
                shifts[i] = new Shift(DEFAULT_SHIFT_TIMES[i], employeeCapacity, type.getShiftActivity(i));
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

    @Override
    public Type getType() {
        return ((Type) type);
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
                .filter(shift -> shift.active && shift.employees.size() < employeeCapacity)
                .min(Comparator.comparingDouble(shift -> (double) shift.employees.size() / (double) employeeCapacity))
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
            if (shift.active && shift.employees.size() < employeeCapacity) return true;
        }
        return false;
    }

    public boolean canProduce() {
        return hasEmployeesInside() &&
                inventory.checkStorageAvailability(getType().job.getRecipe(this)) == Inventory.Availability.AVAILABLE;
    }

    public void business() {
        for (Shift shift : shifts) {
            if (!shift.active) continue;

            Job job = getType().job;
            for (Villager employee : shift.employees) {
                if (employee.isClockedIn())
                    job.continuousTask(employee, this);
                if (employee.isInBuilding(this) && !employee.hasOrders()) {
                    job.assign(employee, this);
                }
            }

            if (getType().productionInterval > 0) {
                productionCounter += employeesInside * efficiency;

                if (productionCounter >= getType().productionInterval) {
                    Recipe recipe = job.getRecipe(this);
                    Inventory.Availability availability = inventory.checkStorageAvailability(recipe);

                    if (availability == Inventory.Availability.AVAILABLE) {
                        inventory.put(recipe);
                        Resource.updateStoredResources(recipe);
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
        employee.setAnimation(Villager.Animation.WALK);
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
                        getType().job.onExit(employee, this);
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
                getType().job.onExit(employee, this);
                employee.giveOrder(EXIT, this);
                employee.giveOrder(CLOCK_OUT);
                break;
            }
        }
    }

    public void setShiftActivity(int index, boolean active) {
        Shift shift = shifts[index];
        shift.active = active;

        if (!active) {
            for (Villager employee : shift.employees) {
                employee.quitJob();
            }
            employees.removeAll(shift.employees);
            shift.employees.clear();
        }
    }

    public void setEmployeeCapacity(int capacity) {
        employeeCapacity = capacity;
        for (int i = 0; i < SHIFTS_PER_JOB; i++) {
            if (!getType().getShiftActivity(i))
                continue;

            int toFire = shifts[i].employees.size() - capacity;
            for (int j = 0; j < toFire; j++) {
                Villager employee = shifts[i].employees.stream().findFirst().get();
                employee.quitJob();
                employees.remove(employee);
                shifts[i].employees.remove(employee);
            }
        }
    }

    public void switchBuildingActivity() {
        if (!active) {
            for (int i = 0; i < SHIFTS_PER_JOB; i++) {
                setShiftActivity(i, getType().getShiftActivity(i));
            }
        } else {
            for (int i = 0; i < SHIFTS_PER_JOB; i++) {
                setShiftActivity(i, false);
            }
        }
        active = !active;
    }

    public boolean isActive() {
        return active;
    }

    public int getEmployeeCapacity() {
        return employeeCapacity;
    }

    public static class Shift implements Serializable {
        Job.ShiftTime shiftTime;
        Set<Villager> employees;
        boolean active;

        public Shift(Job.ShiftTime shiftTime, int maxEmployees, boolean startActive) {
            this.shiftTime = shiftTime;
            this.active = startActive;
            employees = new HashSet<>(maxEmployees);
        }

        public Set<Villager> getEmployees() {
            return employees;
        }

        public Job.ShiftTime getShiftTime() {
            return shiftTime;
        }
    }

    public void updateEfficiency() {
        efficiency = getType().updateEfficiency.apply(buildingsInRange);
    }

    public Job getJob() {
        return getType().job;
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
            throw new ArrayIndexOutOfBoundsException();

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
        return getType().range > 0 && building.getCollider().distance(entrancePosition) < getType().range && !building.equals(this);
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
                    getType().range);
            batch.setColor(UI.DEFAULT_COLOR);
        }

        super.draw(batch);

        drawIndicator(batch);
    }

    protected void drawIndicator(SpriteBatch batch) {
        TextureRegion texture;
        switch (inventory.checkStorageAvailability(getType().job.getRecipe(this))) {
            case LACKS_INPUT:
                texture = Textures.get(Textures.Ui.NO_INPUT); break;
            case OUTPUT_FULL:
                texture = Textures.get(Textures.Ui.FULL_OUTPUT); break;
            default:
                if (!active) texture = Textures.get(Textures.Ui.NOT_ACTIVE);
                else return;
        }
        drawIndicator(texture, batch);
    }

    @Override
    public String toString() {
        return type.name +
                "\njob: " + getType().job +
                "\nefficiency: " + efficiency +
                "\njobQuality: " + jobQuality +
                "\nshifts: " + getType().getShiftActivity(0) + ", " + getType().getShiftActivity(1) + ", " + getType().getShiftActivity(2) +
                "\nemployees: " + employees.size() + " / " + (employeeCapacity * SHIFTS_PER_JOB) +
                "\nemployees per shift: " + employeeCapacity + " / " + getType().maxEmployeeCapacity +
                "\nemployeesInside: " + employeesInside +
                "\nassignedFieldWork: " + assignedFieldWork +
                "\nbuildingsInRange: " + buildingsInRange.size() +
                "\nproductionCounter: " + productionCounter +
                "\nshowRange: " + showRange;
    }
}
