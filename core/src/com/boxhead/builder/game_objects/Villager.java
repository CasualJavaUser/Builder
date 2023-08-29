package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.boxhead.builder.*;
import com.boxhead.builder.game_objects.buildings.*;
import com.boxhead.builder.ui.Clickable;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.utils.Vector2i;

import java.io.*;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Optional;

import static com.boxhead.builder.Stat.TIREDNESS;
import static com.boxhead.builder.Stat.values;

public class Villager extends NPC implements Clickable {
    public static final String[] NAMES = {"Benjamin", "Ove", "Sixten", "Sakarias", "Joel", "Alf", "Gustaf", "Arfast", "Rolf", "Martin"};
    public static final String[] SURNAMES = {"Ekström", "Engdahl", "Tegnér", "Palme", "Axelsson", "Ohlin", "Ohlson", "Lindholm", "Sandberg", "Holgersson"};

    public static final int INVENTORY_SIZE = 10;
    public static final int WORKING_AGE = 16;
    public static final int AGE_OF_CONSENT = 18;
    public static final int INFERTILITY_AGE = 45;
    public static final int RETIREMENT_AGE = 65;
    public static final float AVERAGE_NUM_OF_CHILDREN = 2;

    private final String name, surname;
    private final boolean gender;
    /**
     * age in ticks
     */
    private long age = 0;
    private final long dayOfDecease;
    private float education = 0f;
    private final float[] stats = new float[Stat.values().length];
    private final int skin;
    protected transient Textures.NpcAnimation[] animations = new Textures.NpcAnimation[Animation.values().length];

    private final Villager[] parents = new Villager[2];
    private Villager partner = null;

    private ProductionBuilding workplace = null;
    private ResidentialBuilding home = null;
    private SchoolBuilding school = null;
    private Building buildingIsIn = null;
    private Building destinationBuilding = null;
    /**
     * is at work or school
     */
    private boolean clockedIn = false;

    private final LinkedList<Order> orderList = new LinkedList<>();

    private final Inventory inventory = new Inventory(INVENTORY_SIZE);

    public enum Animation {
        WALK,
        CHOPPING,
        HAMMERING,
        HARVESTING,
        MINING,
        SOWING
    }

    public Villager(Vector2i gridPosition) {
        this(World.getRandom().nextInt(2), World.getRandom().nextBoolean(), gridPosition);
    }

    private Villager(int skin, boolean gender, Vector2i gridPosition) {
        super(Textures.Npc.valueOf("IDLE" + skin), gridPosition);
        this.skin = skin;
        name = NAMES[(int) (Math.random() * NAMES.length)];
        surname = SURNAMES[(int) (Math.random() * SURNAMES.length)];
        this.gender = gender;
        dayOfDecease = World.calculateDate(World.getRandom().nextInt(10 * World.YEAR) + (RETIREMENT_AGE + 5) * World.YEAR);

        for (int i = 0; i < stats.length; i++) {
            stats[i] = Stat.values()[i].initVal;
        }

        initAnimations();
    }

    private void initAnimations() {
        for (int i = 0; i < animations.length; i++) {
            animations[i] = Enum.valueOf(Textures.NpcAnimation.class, Animation.values()[i].name() + skin);
        }
        currentAnimation = animations[0];
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (!isInBuilding()) {
            super.draw(batch);
        }
    }

    public void seekHouse() {
        if (partner != null) {
            if (partner.home != null && !partner.isLivingWithParents())
                switchHouses(partner.home);
            else if (home == null || isLivingWithParents()) {
                Optional<ResidentialBuilding> bestHouseOptional = World.getBuildings().stream()
                        .filter(building -> building instanceof ResidentialBuilding)
                        .map(building -> (ResidentialBuilding) building)
                        .filter(ResidentialBuilding::isEmpty)
                        .min(Comparator.comparingInt(building -> building.getGridPosition().distanceScore(gridPosition)));

                bestHouseOptional.ifPresent(this::switchHouses);
            }
        } else if (!isLivingWithParents()) {
            for (Villager parent : parents) {
                if (parent != null && parent.home != null) {
                    switchHouses(parent.home);
                    return;
                }
            }

            Optional<ResidentialBuilding> bestHouseOptional = World.getBuildings().stream()
                    .filter(building -> building instanceof ResidentialBuilding)
                    .map(building -> (ResidentialBuilding) building)
                    .filter(ResidentialBuilding::isEmpty)
                    .min(Comparator.comparingInt(building -> building.getGridPosition().distanceScore(gridPosition)));

            bestHouseOptional.ifPresent(this::switchHouses);
        }
    }

    private void switchHouses(ResidentialBuilding newHome) {
        if (home == null) {
            newHome.addResident(this);
            home = newHome;
        } else {
            home.removeResident(this);
            home = newHome;
            home.addResident(this);
        }
    }

    public boolean isLivingWithParents() {
        if (home == null)
            return false;

        for (Villager parent : parents) {
            if (parent == null || parent.home == null)
                continue;

            if (parent.home.equals(home))
                return true;
        }
        return false;
    }

    private boolean seekJob() {
        Optional<ProductionBuilding> bestJobOptional = World.getBuildings().stream()
                .filter(building -> building instanceof ProductionBuilding)
                .map(building -> (ProductionBuilding) building)
                .filter(ProductionBuilding::isHiring)
                .max(Comparator.comparing(ProductionBuilding::getJobQuality));

        if (bestJobOptional.isPresent()) {
            final ProductionBuilding bestJob = bestJobOptional.get();

            if (workplace == null) {
                bestJob.addEmployee(this);
                workplace = bestJob;
            } else if (bestJob.getJobQuality() > workplace.getJobQuality()) {
                workplace.removeEmployee(this);
                bestJob.addEmployee(this);
                workplace = bestJob;
            }

            if (school != null) {
                school.endStudentShift(this);
                school.removeStudent(this);
            }

            return true;
        }
        return false;
    }

    private boolean seekSchool() {
        Optional<SchoolBuilding> bestSchoolOptional = World.getBuildings().stream()
                .filter(building -> building instanceof SchoolBuilding)
                .map(building -> (SchoolBuilding) building)
                .filter(SchoolBuilding::isRecruiting)
                .max(Comparator.comparing(school -> school.getEmployees().size()));

        if (bestSchoolOptional.isPresent()) {
            final SchoolBuilding bestSchool = bestSchoolOptional.get();

            if (school == null) {
                bestSchool.addStudent(this);
                school = bestSchool;
            } else if (bestSchool.getEmployees().size() > school.getEmployees().size()) {
                school.removeStudent(this);
                bestSchool.addStudent(this);
                school = bestSchool;
            }

            if (workplace != null) {
                workplace.endShift(this);
                workplace.removeEmployee(this);
            }

            return true;
        }
        return false;
    }

    public void seekJobOrSchool() {
        if (isEducationPreferable()) {
            if (!seekSchool())
                seekJob();
        } else {
            if (!seekJob())
                seekSchool();
        }
    }

    public boolean isEducationPreferable() {
        return education < 1f;  //TODO education preferability algorithm
    }

    public float getHappiness() {
        float happiness = 0;
        for (Stat stat : values()) {
            if (stat.isIncreasing)
                happiness += 100 - stats[stat.ordinal()];
            else
                happiness += stats[stat.ordinal()];
        }
        happiness = happiness / stats.length;

        if (partner != null && isLivingWithParents())
            happiness -= 20;
        else if (home == null) {
            happiness -= 30;
        }

        return happiness;
    }

    public ServiceBuilding seekNearestService(Service service) {
        Optional<ServiceBuilding> bestServiceOptional = World.getBuildings().stream()
                .filter(building -> building instanceof ServiceBuilding serviceBuilding && serviceBuilding.getType().service == service)
                .map(building -> (ServiceBuilding) building)
                .filter(ServiceBuilding::canProvideService)
                .filter(ServiceBuilding::hasFreeSpaces)
                .min(Comparator.comparingInt(building -> building.getGridPosition().distanceScore(gridPosition)));

        return bestServiceOptional.orElse(null);
    }

    public Villager getPartner() {
        return partner;
    }

    public boolean getGender() {
        return gender;
    }

    public void findPartner() {
        Optional<Villager> partnerOptional = World.getVillagers().stream()
                .filter(villager -> villager.partner == null)
                .filter(villager -> villager.gender == !gender)
                .filter(villager -> villager.ageInYears() >= AGE_OF_CONSENT && villager.ageInYears() < INFERTILITY_AGE)
                .min(Comparator.comparingLong(villager -> Math.abs(villager.age - age)));

        partnerOptional.ifPresent(villager -> {
            partner = villager;
            villager.partner = this;
        });
    }

    public void reproduce() {
        if (home == null || partner == null || buildingIsIn != home)
            throw new IllegalStateException();

        Villager child = new Villager(this.gridPosition.clone());
        child.home = this.home;
        child.parents[0] = this;
        child.parents[1] = partner;
        home.addResident(child);
        child.buildingIsIn = home;
        Debug.log(child.name + " " + child.surname + " (" + child.getId() + ") was born.");
    }

    public void enterBuilding(Building building) {
        if (gridPosition.equals(building.getEntrancePosition())) {
            gridPosition.set(building.getGridPosition());
            buildingIsIn = building;
        }
    }

    public void exitBuilding() {
        Building building = Building.getByCoordinates(gridPosition);
        if (building != null) {
            gridPosition.set(building.getEntrancePosition());
        }
        buildingIsIn = null;
    }

    public void educate(float rate) {
        education += rate;
        if (education > 1f) education = 1f;
    }

    public float getEducation() {
        return education;
    }

    public void endShift() {
        if (workplace != null)
            workplace.endShift(this);
        else if (school != null)
            school.endStudentShift(this);
    }

    public boolean hasJob() {
        return workplace != null || school != null;
    }

    @Override
    public void wander() {
        if (World.getRandom().nextInt(360) == 0) {
            Vector2i randomPos = randomPosInRange(5);
            if (World.isNavigable(randomPos))
                giveOrder(randomPos);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean isMouseOver() {
        Vector2 mousePos = GameScreen.getMouseWorldPosition();
        return mousePos.x >= spritePosition.x * World.TILE_SIZE && mousePos.x < (spritePosition.x * World.TILE_SIZE + currentTexture.getRegionWidth()) &&
                mousePos.y >= spritePosition.y * World.TILE_SIZE && mousePos.y < (spritePosition.y * World.TILE_SIZE + currentTexture.getRegionHeight());
    }

    @Override
    public void onClick() {
        UI.showNPCStatWindow(this);
    }

    public static abstract class Order implements Serializable {
        abstract void execute();

        public enum Type {
            GO_TO,
            ENTER,
            EXIT,
            CLOCK_IN,
            CLOCK_OUT,
            PUT_RESOURCES_TO_BUILDING,
            PUT_RESERVED_RESOURCES,
            TAKE_RESERVED_RESOURCES,
            REQUEST_TRANSPORT,
            END_DELIVERY,
        }
    }

    public void executeOrders() {
        if (!orderList.isEmpty()) {
            orderList.getFirst().execute();
        }
    }

    public void clearOrderQueue() {
        orderList.clear();
    }

    public void giveOrder(Vector2i tile) {
        orderList.addLast(new Order() {
            @Override
            void execute() {
                if (pathfinding == null || pathfinding.isDone() && (path == null || !path[path.length - 1].equals(tile))) {
                    navigateTo(tile);
                } else if (followPath()) {
                    orderList.removeFirst();
                }
            }
        });
    }

    public void giveOrder(Order.Type type, Building building) {
        switch (type) {
            case GO_TO -> {
                destinationBuilding = building;
                giveOrder(building.getEntrancePosition());
                giveOrder(Order.Type.ENTER, building);
            }
            case ENTER -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (gridPosition.equals(building.getEntrancePosition())) {
                        if (building == workplace) {
                            workplace.employeeEnter(Villager.this);
                        }
                        enterBuilding(building);
                    }
                    destinationBuilding = null;
                    orderList.removeFirst();
                }
            });
            case EXIT -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (gridPosition.equals(building.getGridPosition())) {
                        if (building == workplace) {
                            workplace.employeeExit(Villager.this);
                        }
                        if (building instanceof ServiceBuilding serviceBuilding && serviceBuilding.getGuests().contains(Villager.this)) {
                            serviceBuilding.guestExit(Villager.this);
                        }
                        gridPosition.set(building.getEntrancePosition());
                        buildingIsIn = null;
                    }
                    orderList.removeFirst();
                }
            });
        }
    }

    public void giveOrder(ServiceBuilding serviceBuilding) {
        giveOrder(Order.Type.GO_TO, serviceBuilding);
        orderList.addLast(new Order() {
            @Override
            void execute() {
                serviceBuilding.guestEnter(Villager.this);
                orderList.removeFirst();
            }
        });
    }

    public void giveOrder(Order.Type type, FieldWork fieldWork) {
        switch (type) {
            case GO_TO -> {
                orderList.addLast(new Order() {
                    @Override
                    void execute() {
                        if (pathfinding == null || pathfinding.isDone() && (path == null || fieldWork.getCollider().distance(path[path.length - 1]) > Math.sqrt(2d))) {
                            navigateTo(fieldWork.getCollider());
                        } else if (followPath()) {
                            orderList.removeFirst();
                        }
                    }
                });
                giveOrder(Order.Type.ENTER, fieldWork);
            }
            case ENTER -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (fieldWork.getCollider().distance(gridPosition) <= Math.sqrt(2d)) {
                        fieldWork.setWork(Villager.this);
                    }
                    orderList.removeFirst();
                }
            });
            case EXIT -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    fieldWork.dissociateWorker(Villager.this);
                    orderList.removeFirst();
                }
            });
        }
    }

    public void giveOrder(Order.Type type) {
        switch (type) {
            case EXIT ->  //if building is known, giveOrder(Order.Type, StorageBuilding) should be used instead
                    orderList.addLast(new Order() {
                        @Override
                        void execute() {
                            exitBuilding();
                            orderList.removeFirst();
                        }
                    });
            case END_DELIVERY -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    Logistics.getDeliveryList().remove(Villager.this);
                    orderList.removeFirst();
                }
            });
            case CLOCK_IN -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    clockedIn = true;
                    orderList.removeFirst();
                }
            });
            case CLOCK_OUT -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    clockedIn = false;
                    orderList.removeFirst();
                }
            });
        }
    }

    public void giveOrder(Order.Type type, Resource resource, int amount) {
        switch (type) {
            case PUT_RESOURCES_TO_BUILDING -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (buildingIsIn == null || !inventory.hasResourceAmount(resource, amount))
                        throw new IllegalStateException();

                    inventory.moveResourcesTo(buildingIsIn.getInventory(), resource, amount);
                    orderList.removeFirst();
                }
            });
            case TAKE_RESERVED_RESOURCES -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (buildingIsIn == null)
                        throw new IllegalStateException();

                    buildingIsIn.moveReservedResources(Villager.this, buildingIsIn.inventory, inventory, resource, amount);
                    orderList.removeFirst();
                }
            });
            case PUT_RESERVED_RESOURCES -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (buildingIsIn == null)
                        throw new IllegalStateException();

                    buildingIsIn.moveReservedResources(Villager.this, inventory, buildingIsIn.inventory, resource, amount);
                    orderList.removeFirst();
                }
            });
            case REQUEST_TRANSPORT -> orderList.addLast(new Order() {
                @Override
                void execute() {
                    if (buildingIsIn == null)
                        throw new IllegalStateException();

                    Logistics.requestTransport(buildingIsIn, resource, amount);
                    orderList.removeFirst();
                }
            });
            default -> throw new IllegalArgumentException();
        }
    }

    public void giveOrder(Harvestable harvestable) {
        orderList.addLast(new Order() {
            @Override
            void execute() {
                World.placeFieldWork(harvestable);
                orderList.removeFirst();
            }
        });
    }

    public void progressStats() {
        for (Stat stat : values()) {
            stats[stat.ordinal()] += stat.rate;
        }

        if (clockedIn) {
            stats[TIREDNESS.ordinal()] += TIREDNESS.rate;
        } else if (home != null && home.equals(buildingIsIn)) {
            stats[TIREDNESS.ordinal()] -= TIREDNESS.rate * 2;
        }
    }

    public void fulfillNeeds() {
        for (int i = 0; i < stats.length; i++) {
            Stat stat = Stat.values()[i];
            float threshold = isWorkTime() ? stat.critical : stat.mild;

            boolean condition;
            if (stat.isIncreasing) {
                condition = stats[i] >= threshold;  //for stats that increase over time (e.g. hunger)
            } else {
                condition = stats[i] <= threshold;  //for stats that decrease over time (e.g. health)
            }

            boolean alreadyFulfilling = (buildingIsIn != null && buildingIsIn instanceof ServiceBuilding serviceIsIn && serviceIsIn.getType().service.getEffects().containsKey(stat))
                    || (destinationBuilding != null && destinationBuilding instanceof ServiceBuilding destService && destService.getType().service.getEffects().containsKey(stat));

            if (condition && !alreadyFulfilling) {
                stat.fulfillNeed(this);
                return;
            }
        }
    }

    public void quitJob() {
        if (buildingIsIn == workplace || destinationBuilding == workplace) {
            workplace.endShift(this);
        }
        workplace = null;
    }

    public void quitSchool() {
        if (buildingIsIn == school || destinationBuilding == school) {
            school.endStudentShift(this);
        }
        school = null;
    }

    public ProductionBuilding getWorkplace() {
        return workplace;
    }

    public SchoolBuilding getSchool() {
        return school;
    }

    public boolean hasWorkplace() {
        return workplace != null;
    }

    public boolean hasSchool() {
        return school != null;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public Job getJob() {
        if (workplace == null)
            return Jobs.UNEMPLOYED;
        else
            return workplace.getType().job;
    }

    public boolean isWorkTime() {
        if (workplace == null) return false;

        return workplace.getShift(this).getShiftTime().overlaps(World.getTime());
    }

    public ResidentialBuilding getHome() {
        return home;
    }

    public float[] getStats() {
        return stats;
    }

    public boolean isInBuilding() {
        return buildingIsIn != null;
    }

    public boolean isInBuilding(Building building) {
        return buildingIsIn == building;
    }

    public boolean isClockedIn() {
        return clockedIn;
    }

    public Building getCurrentBuilding() {
        return buildingIsIn;
    }

    public boolean hasOrders() {
        return !orderList.isEmpty();
    }

    @Override
    public String toString() {
        String homeType = null;
        String workplaceType = null;
        if (home != null) homeType = home.getType().toString();
        if (workplace != null) workplaceType = workplace.getType().toString();

        String s = name + " " + surname +
                "\ngender: " + (gender ? "female" : "male") +
                "\nage: " + ageInYears() +
                "\npartner: " + (partner != null ? partner.name + " " + partner.surname : null) +
                "\nhappiness: " + getHappiness();

        String stat;
        for (int i = 0; i < Stat.values().length; i++) {
            stat = Stat.values()[i].toString().toLowerCase() + ": " + (int) stats[i];
            s = s.concat("\n" + stat);
        }

        s += "\neducation: " + education +
                "\norder list size: " + orderList.size() +
                "\nclocked in: " + clockedIn +
                "\nis education preferable: " + isEducationPreferable() +
                "\nhome: " + homeType +
                "\nworkplace: " + workplaceType +
                "\nis in building: " + isInBuilding() +
                "\nbuilding is in: " + (buildingIsIn != null ? buildingIsIn.getName() + " (id: " + buildingIsIn.getId() + ")" : "") +
                "\ninventory: " + inventory;

        return s;
    }

    public void setAnimation(Animation animation, boolean flipped) {
        setAnimation(animation);
        prevPosition.add(flipped ? 1 : -1, 0);
    }

    public void setAnimation(Animation animation) {
        currentAnimation = animations[animation.ordinal()];
    }

    public void incrementAge() {
        age++;
    }

    /**
     * Only for testing purposes!!!
     */
    public void setAge(long age) {
        this.age = age;
    }

    public long ageInYears() {
        return age / World.YEAR;
    }

    public long ageInTicks() {
        return age;
    }

    public long getDayOfDecease() {
        return dayOfDecease;
    }

    public void retire() {
        if (workplace != null)
            workplace.removeEmployee(this);
        if (school != null)
            school.removeStudent(this);
    }

    public void die() {
        if (home != null)
            home.removeResident(this);

        if (partner != null) {
            partner.partner = null;
            partner = null;
        }

        retire();
        World.getVillagers().remove(this);
        Debug.log(name + " " + surname + " (" + getId() + ") died at the age of " + ageInYears() + ".");
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        textureId = Textures.Npc.valueOf("IDLE" + skin);
        initAnimations();
        currentTexture = getTexture();
    }
}
