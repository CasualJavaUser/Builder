package com.boxhead.builder;

import com.boxhead.builder.game_objects.*;
import com.boxhead.builder.utils.BidirectionalMap;
import com.boxhead.builder.utils.SortedList;
import com.boxhead.builder.utils.Vector2i;

import java.util.*;

public class Logistics {
    /**
     * The smallest amount that is considered worth issuing a transport order.
     */
    public static final int THE_UNIT = NPC.INVENTORY_SIZE;
    private static final SortedList<Request> supplyRequests;
    private static final SortedList<Request> outputRequests;
    private static final SortedList<Order> readyOrders;

    private static final Set<StorageBuilding> storages = new HashSet<>();
    private static final Set<ProductionBuilding> transportOffices = new HashSet<>();

    private static final BidirectionalMap<FieldWork, ProductionBuilding> FW_requests = new BidirectionalMap<>();
    private static final BidirectionalMap<Order, ProductionBuilding> orderRequests = new BidirectionalMap<>();
    private static final Map<NPC, Order> deliveriesInProgress = new HashMap<>();

    static {
        Comparator<Request> requestComparator = Comparator.comparingInt(r -> r.priority);
        Comparator<Order> orderComparator = Comparator.comparingInt(o -> o.priority);
        supplyRequests = new SortedList<>(requestComparator);
        outputRequests = new SortedList<>(requestComparator);
        readyOrders = new SortedList<>(orderComparator);
    }

    public static void requestFieldWork(ProductionBuilding building, FieldWork fieldWork) {
        if (!FW_requests.containsKey(fieldWork)) {
            FW_requests.put(fieldWork, building);
        } else {
            Vector2i fieldWorkPos = fieldWork.getGridPosition();
            double newDistance = building.getEntrancePosition().distance(fieldWorkPos);
            double currentDistance = FW_requests.get(fieldWork).getEntrancePosition().distance(fieldWorkPos);

            if (newDistance < currentDistance) {
                FW_requests.replace(fieldWork, building);
            }
        }
    }

    public static FieldWork assignedFieldWork(ProductionBuilding building) {
        if (!FW_requests.containsValue(building))
            return null;

        return FW_requests.getByValue(building);
    }

    public static void clearFieldWorkRequests() {
        FW_requests.clear();
    }

    /**
     * Transport order analogue of {@code .requestFieldWork()}
     */
    public static void takeOrder(ProductionBuilding building, Order order) {
        if (!orderRequests.containsKey(order)) {
            orderRequests.put(order, building);
        } else {
            double newDistance = order.distance(building.getEntrancePosition());
            double currentDistance = order.distance(orderRequests.get(order).getEntrancePosition());

            if (newDistance < currentDistance) {
                orderRequests.replace(order, building);
            }
        }
    }

    public static Order assignedOrder(ProductionBuilding building) {
        if (!orderRequests.containsValue(building))
            return null;

        return orderRequests.getByValue(building);
    }

    public static void removeOrder(Order order, int units) {
        order.amount -= units;
        if (order.amount == 0)
            readyOrders.remove(order);
    }

    public static Map<NPC, Order> getDeliveryList() {
        return deliveriesInProgress;
    }

    public static void clearOrderRequests() {
        orderRequests.clear();
    }

    public static void requestTransport(EnterableBuilding building, Recipe recipe) {
        for (Resource resource : recipe.changedResources()) {
            requestTransport(building, resource, recipe.getChange(resource));
        }
    }

    /**
     * @param units The sign of this argument determines whether this request is considered an input or an output. Should be kept the same as in Recipes - positive for output and negative for input, zero has no effect.
     */
    public static void requestTransport(EnterableBuilding building, Resource resource, int units) {
        Request request;
        long zipCode = Request.zipCode(building, resource);
        SortedList<Request> list;

        if (units > 0)
            list = outputRequests;
        else if (units < 0)
            list = supplyRequests;
        else return;

        int index = list.indexOf(zipCode);
        if (index == -1) {
            request = new Request(resource, building, Math.abs(units));
            list.add(request);
        } else {
            request = list.get(index);
            request.amount += Math.abs(units);
        }
        updatePriority(request);
    }

    public static void overrideRequestPriority(EnterableBuilding building, Resource resource, int newPriority) {
        long zipCode = Request.zipCode(building, resource);

        SortedList<Request> list = determineList(zipCode);
        if (list == null) return;

        Request request = list.get(list.indexOf(zipCode));

        request.priority = newPriority;
        request.priorityEnum = Priority.values()[newPriority];
    }

    public static void pairRequests() {
        int maxPriority = Math.max(supplyRequests.isEmpty() ? 0 : supplyRequests.get(0).priority,
                outputRequests.isEmpty() ? 0 : outputRequests.get(0).priority);

        if (supplyRequests.isEmpty() || outputRequests.isEmpty()) {
            if (maxPriority > Priority.LOW.ordinal()) {
                useStorage();
            }
            return;
        }

        int supplyIterator = 0;
        int outputIterator = 0;
        Request currentRequest;
        Optional<Request> paired;
        for (int p = maxPriority; p >= 0; p--) {    //high-priority requests in both supply and output are served first
            while (supplyIterator < supplyRequests.size() && supplyRequests.get(supplyIterator).priority == p) {
                currentRequest = supplyRequests.get(supplyIterator);
                paired = findRequest(outputRequests, currentRequest);

                if (paired.isPresent()) {
                    readyOrders.add(new Order(paired.get(), currentRequest, THE_UNIT));
                    currentRequest.amount -= THE_UNIT;
                }

                supplyIterator++;
            }

            while (outputIterator < outputRequests.size() && outputRequests.get(outputIterator).priority == p) {
                currentRequest = outputRequests.get(outputIterator);
                paired = findRequest(supplyRequests, currentRequest);

                if (paired.isPresent()) {
                    readyOrders.add(new Order(currentRequest, paired.get(), THE_UNIT));
                    currentRequest.amount -= THE_UNIT;
                }

                outputIterator++;
            }
        }
        supplyRequests.removeIf(request -> request.amount == 0);
        outputRequests.removeIf(request -> request.amount == 0);
    }

    /**
     * Analogous to {@code FieldWork.findFieldWork()}.
     */
    public static Optional<Order> findOrder(Vector2i gridPosition) {
        if (readyOrders.isEmpty())
            return Optional.empty();

        return readyOrders.stream()
                .filter(order -> order.priority == readyOrders.get(0).priority)
                .min(Comparator.comparingDouble(order -> order.distance(gridPosition)));
    }

    public static Set<ProductionBuilding> getTransportOffices() {
        return transportOffices;
    }

    public static Set<StorageBuilding> getStorages() {
        return storages;
    }

    private static void useStorage() {
        int it = 0;

        while (it < supplyRequests.size() && supplyRequests.get(it).priority > Priority.LOW.ordinal()) {
            Request request = supplyRequests.get(it);
            Resource resource = request.resource;
            StorageBuilding storage;

            Optional<StorageBuilding> optional = findStoredResources(request);
            if (optional.isPresent()) {
                storage = optional.get();
                storage.reserveResources(resource, THE_UNIT);
                request.building.reserveSpace(THE_UNIT);
                readyOrders.add(new Order(new Request(resource, storage, THE_UNIT), request));
                request.amount -= THE_UNIT;
            }
            it++;
        }

        it = 0;
        while (it < outputRequests.size() && outputRequests.get(it).priority > Priority.LOW.ordinal()) {
            Request request = outputRequests.get(it);
            Resource resource = request.resource;
            StorageBuilding storage;

            Optional<StorageBuilding> optional = findStorageSpace(request);
            if (optional.isPresent()) {
                storage = optional.get();
                storage.reserveSpace(THE_UNIT);
                request.building.reserveResources(resource, THE_UNIT);
                readyOrders.add(new Order(request, new Request(resource, storage, THE_UNIT)));
                request.amount -= THE_UNIT;
            }
            it++;
        }
        supplyRequests.removeIf(request -> request.amount == 0);
        outputRequests.removeIf(request -> request.amount == 0);
    }

    private static void updatePriority(Request request) {
        float fill;
        SortedList<Request> list;
        Inventory inventory = request.building.getInventory();

        if (outputRequests.contains(request)) {
            fill = (float) inventory.getCurrentAmount() / (float) inventory.getMaxCapacity();
            list = outputRequests;
        } else if (supplyRequests.contains(request)) {
            fill = 1 - (float) inventory.getCurrentAmount() / (float) inventory.getMaxCapacity();
            list = supplyRequests;
        } else return;

        list.remove(request);
        for (Priority priority : Priority.values()) {
            if (fill >= priority.threshold) {
                request.priorityEnum = priority;
                request.priority = request.priorityEnum.ordinal();
            } else break;
        }
        list.add(request);
    }

    private static SortedList<Request> determineList(long zipCode) {
        if (supplyRequests.indexOf(zipCode) != -1)
            return supplyRequests;

        if (outputRequests.indexOf(zipCode) != -1)
            return outputRequests;

        return null;
    }

    private static Optional<Request> findRequest(SortedList<Request> list, Request request) {
        return list.stream()
                .filter(req -> req.resource == request.resource)
                .filter(req -> req.amount >= THE_UNIT)
                .max(Comparator.comparingInt(r -> r.priority));
    }

    private static Optional<StorageBuilding> findStoredResources(Request request) {
        return storages.stream()
                .filter(storage -> storage.getStored(request.resource) >= THE_UNIT)
                .min(Comparator.comparingDouble(storage -> storage.getEntrancePosition().distance(request.building.getEntrancePosition())));
    }

    private static Optional<StorageBuilding> findStorageSpace(Request request) {
        return storages.stream()
                .filter(storage -> storage.getRemainingCapacity() >= THE_UNIT)
                .min(Comparator.comparingDouble(storage -> storage.getEntrancePosition().distance(request.building.getEntrancePosition())));
    }

    public enum Priority {
        STANDBY(0.00f),
        LOW(0.25f),
        HIGH(0.75f),
        URGENT(0.90f);

        /**
         * A Request receives priority level corresponding to its issuing building's storage
         * being at least this full (for produced resources) or <i>1 - threshold</i> full (for consumed)
         */
        public final float threshold;

        Priority(float threshold) {
            this.threshold = threshold;
        }
    }

    private static class Request {
        Resource resource;
        EnterableBuilding building;
        int amount;
        int priority;
        Priority priorityEnum;

        private Request() {
        }

        private Request(Resource resource, EnterableBuilding building, int amount) {
            this.resource = resource;
            this.building = building;
            this.amount = amount;
        }

        long zipCode() {
            return zipCode(building, resource);
        }

        static long zipCode(Building building, Resource resource) {
            return ((long) building.getGridPosition().hashCode() << 32) | resource.ordinal();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;

            if (other instanceof Request) {
                return this.zipCode() == ((Request) other).zipCode();
            } else if (other instanceof Long) {
                return this.zipCode() == (long) other;
            } else
                return false;
        }
    }

    public static class Order extends Request {
        EnterableBuilding sender;
        EnterableBuilding recipient;

        private Order(Request sender, Request recipient) {
            if (sender.resource != recipient.resource)
                throw new IllegalArgumentException();

            this.sender = sender.building;
            this.recipient = recipient.building;
            resource = sender.resource;
            priority = Math.max(sender.priority, recipient.priority);
            priorityEnum = Priority.values()[priority];
            amount = Math.min(sender.amount, recipient.amount);
        }

        private Order(Request sender, Request recipient, int amount) {
            if (sender.resource != recipient.resource)
                throw new IllegalArgumentException();

            this.sender = sender.building;
            this.recipient = recipient.building;
            this.amount = amount;
            resource = sender.resource;
            priority = Math.max(sender.priority, recipient.priority);
            priorityEnum = Priority.values()[priority];
        }

        private double distance(Vector2i gridPosition) {
            return sender.getEntrancePosition().distance(gridPosition) +
                    recipient.getEntrancePosition().distance(gridPosition);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;

            if (other instanceof Order) {
                return resource == ((Order) other).resource && recipient == ((Order) other).recipient && sender == ((Order) other).sender;
            }
            return false;
        }
    }
}
