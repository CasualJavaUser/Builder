package com.boxhead.builder;

import com.boxhead.builder.game_objects.*;
import com.boxhead.builder.utils.BidirectionalMap;
import com.boxhead.builder.utils.SortedList;
import com.boxhead.builder.utils.Vector2i;

import java.io.Serializable;
import java.util.*;

public class Logistics {
    /**
     * The smallest amount that is considered worth issuing a transport order.
     */
    public static final int THE_UNIT = NPC.INVENTORY_SIZE;
    /**
     * The lowest priority at which storage space or stored resources are used to fulfill requests.
     */
    public static final int USE_STORAGE = Priority.HIGH.ordinal();

    public static final SortedList<Request> supplyRequests;
    public static final SortedList<Request> outputRequests;
    public static final SortedList<Order> readyOrders;

    public static final Set<StorageBuilding> storages = new HashSet<>();
    public static final Set<ProductionBuilding> transportOffices = new HashSet<>();

    private static final BidirectionalMap<FieldWork, ProductionBuilding> fieldWorkRequests = new BidirectionalMap<>();
    public static final BidirectionalMap<Order, ProductionBuilding> orderRequests = new BidirectionalMap<>();
    public static final Map<NPC, Order> deliveriesInProgress = new HashMap<>();

    static {
        Comparator<Request> requestComparator = Comparator.comparingInt(r -> r.priority);
        Comparator<Order> orderComparator = Comparator.comparingInt(o -> o.priority);
        supplyRequests = new SortedList<>(requestComparator);
        outputRequests = new SortedList<>(requestComparator);
        readyOrders = new SortedList<>(orderComparator);
    }

    public static void requestFieldWork(ProductionBuilding building, FieldWork fieldWork) {
        if (!fieldWorkRequests.containsKey(fieldWork)) {
            fieldWorkRequests.put(fieldWork, building);
        } else {
            Vector2i fieldWorkPos = fieldWork.getGridPosition();
            int newDistance = building.getEntrancePosition().distanceScore(fieldWorkPos);
            int currentDistance = fieldWorkRequests.get(fieldWork).getEntrancePosition().distanceScore(fieldWorkPos);

            if (newDistance < currentDistance) {
                fieldWorkRequests.replace(fieldWork, building);
            }
        }
    }

    public static FieldWork assignedFieldWork(ProductionBuilding building) {
        if (!fieldWorkRequests.containsValue(building))
            return null;

        return fieldWorkRequests.getByValue(building);
    }

    public static void clearFieldWorkRequests() {
        fieldWorkRequests.clear();
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

    /**
     * Creates a new transport request and overrides its priority. Note that this override is not persistent - invoking a
     * non-overriding <code>requestTransport()</code> will reset priority.
     */
    public static void requestTransport(StorageBuilding building, Recipe recipe, int overridePriority) {
        for (Resource resource : recipe.changedResources()) {
            Request request = returningRequestTransport(building, resource, recipe.getChange(resource));
            overrideRequestPriority(request, overridePriority);
        }
    }
    public static void requestTransport(StorageBuilding building, Recipe recipe) {
        for (Resource resource : recipe.changedResources()) {
            requestTransport(building, resource, recipe.getChange(resource));
        }
    }

    /**
     * @param units The sign of this argument determines whether this request is considered an input or an output. Should be kept the same as in Recipes - positive for output and negative for input, zero has no effect.
     */
    public static void requestTransport(StorageBuilding building, Resource resource, int units) {
        returningRequestTransport(building, resource, units);
    }

    private static Request returningRequestTransport(StorageBuilding building, Resource resource, int units) {
        SortedList<Request> list;

        if (units > 0)
            list = outputRequests;
        else if (units < 0)
            list = supplyRequests;
        else
            return null;

        long zipCode = Request.zipCode(building, resource);
        for (Request request : list) {
            if (request.equals(zipCode)) {
                request.amount += Math.abs(units);
                updatePriority(request);
                return request;
            }
        }
        Request request = new Request(resource, building, Math.abs(units));
        request.priorityEnum = calcPriority(request);
        request.priority = request.priorityEnum.ordinal();
        list.add(request);
        return request;
    }

    public static void overrideRequestPriority(Request request, int newPriority) {
        SortedList<Request> list = determineList(request);
        if (request == null || list == null) return;

        list.remove(request);
        request.priority = newPriority;
        request.priorityEnum = Priority.values()[newPriority];
        list.add(request);
    }

    public static void pairRequests() {
        int maxPriority = Math.max(supplyRequests.isEmpty() ? 0 : supplyRequests.get(0).priority,
                outputRequests.isEmpty() ? 0 : outputRequests.get(0).priority);

        if (supplyRequests.isEmpty() || outputRequests.isEmpty()) {
            if (maxPriority >= USE_STORAGE) {
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
                if (currentRequest.amount < THE_UNIT) {
                    supplyIterator++;
                    continue;
                }
                paired = findRequest(outputRequests, currentRequest);

                if (paired.isPresent()) {
                    Request outputRequest = paired.get();

                    addOrder(currentRequest, outputRequest);
                    currentRequest.building.reserveSpace(THE_UNIT);
                    outputRequest.building.reserveResources(currentRequest.resource, THE_UNIT);
                }

                supplyIterator++;
            }

            while (outputIterator < outputRequests.size() && outputRequests.get(outputIterator).priority == p) {
                currentRequest = outputRequests.get(outputIterator);
                if (currentRequest.amount < THE_UNIT) {
                    outputIterator++;
                    continue;
                }
                paired = findRequest(supplyRequests, currentRequest);

                if (paired.isPresent()) {
                    Request supplyRequest = paired.get();

                    addOrder(supplyRequest, currentRequest);
                    currentRequest.building.reserveResources(currentRequest.resource, THE_UNIT);
                    supplyRequest.building.reserveSpace(THE_UNIT);
                }

                outputIterator++;
            }
        }
        supplyRequests.removeIf(request -> request.amount == 0);
        outputRequests.removeIf(request -> request.amount == 0);
        useStorage();
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

        while (it < supplyRequests.size() && supplyRequests.get(it).priority >= USE_STORAGE) {
            Request supplyRequest = supplyRequests.get(it);
            Resource resource = supplyRequest.resource;
            StorageBuilding storage;

            Optional<StorageBuilding> optional = findStoredResources(supplyRequest);
            if (optional.isPresent()) {
                storage = optional.get();

                addOrder(supplyRequest, new Request(resource, storage, THE_UNIT));
                storage.reserveResources(resource, THE_UNIT);
                supplyRequest.building.reserveSpace(THE_UNIT);
            }
            it++;
        }

        it = 0;
        while (it < outputRequests.size() && outputRequests.get(it).priority >= USE_STORAGE) {
            Request outputRequest = outputRequests.get(it);
            Resource resource = outputRequest.resource;
            StorageBuilding storage;

            Optional<StorageBuilding> optional = findStorageSpace(outputRequest);
            if (optional.isPresent()) {
                storage = optional.get();

                addOrder(new Request(resource, storage, THE_UNIT), outputRequest);
                storage.reserveSpace(THE_UNIT);
                outputRequest.building.reserveResources(resource, THE_UNIT);
            }
            it++;
        }
        supplyRequests.removeIf(request -> request.amount == 0);
        outputRequests.removeIf(request -> request.amount == 0);
    }

    private static void updatePriority(Request request) {
        SortedList<Request> list;
        Priority newPriority = calcPriority(request);
        if (newPriority != request.priorityEnum) {
            list = determineList(request);
            if (list != null) {
                list.remove(request);
                request.priorityEnum = newPriority;
                request.priority = newPriority.ordinal();
                list.add(request);
            }
        }
    }

    private static Priority calcPriority(Request request) {
        float fill;
        Inventory inventory = request.building.getInventory();

        if (outputRequests.contains(request)) {
            fill = (float) inventory.getCurrentAmount() / (float) inventory.getMaxCapacity();
        } else if (supplyRequests.contains(request)) {
            fill = 1 - (float) inventory.getCurrentAmount() / (float) inventory.getMaxCapacity();
        } else return Priority.values()[0];

        Priority maxPriority = Priority.values()[Priority.values().length - 1];
        for (Priority priority : Priority.values()) {
            if (fill >= priority.threshold) {
                maxPriority = priority;
            } else break;
        }
        return maxPriority;
    }

    private static Order searchOrders(Request supply, Request output) {
        for (Order order : readyOrders) {
            if (order.resource == supply.resource && order.sender == output.building && order.recipient == supply.building)
                return order;
        }
        return null;
    }

    private static void addOrder(Request supply, Request output) {
        Order order = searchOrders(supply, output);

        if (order == null) {
            readyOrders.add(new Order(output, supply, THE_UNIT));
        } else {
            int maxPriority = Math.max(supply.priority, output.priority);
            if (maxPriority > order.priority) {
                readyOrders.remove(order);
                order.priority = maxPriority;
                readyOrders.add(order);
            }
            order.amount += THE_UNIT;
        }

        supply.amount -= THE_UNIT;
        output.amount -= THE_UNIT;
    }

    private static SortedList<Request> determineList(Request request) {
        if (supplyRequests.contains(request))
            return supplyRequests;

        if (outputRequests.contains(request))
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
                .filter(storage -> storage.getFreeResources(request.resource) >= THE_UNIT)
                .min(Comparator.comparingInt(storage -> storage.getEntrancePosition().distanceScore(request.building.getEntrancePosition())));
    }

    private static Optional<StorageBuilding> findStorageSpace(Request request) {
        return storages.stream()
                .filter(storage -> storage.getInventory().getAvailableCapacity() >= THE_UNIT)
                .min(Comparator.comparingInt(storage -> storage.getEntrancePosition().distanceScore(request.building.getEntrancePosition())));
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

    private static class Request implements Serializable {
        Resource resource;
        StorageBuilding building;
        int amount;
        int priority;
        Priority priorityEnum;

        private Request() {
        }

        private Request(Resource resource, StorageBuilding building, int amount) {
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

    public static class Order extends Request{
        StorageBuilding sender;
        StorageBuilding recipient;

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

            if (other instanceof Order otherOrder) {
                return resource == otherOrder.resource && recipient == otherOrder.recipient && sender == otherOrder.sender;
            }
            return false;
        }
    }
}
