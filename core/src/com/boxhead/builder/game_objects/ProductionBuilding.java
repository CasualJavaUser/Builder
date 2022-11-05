package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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

    public ProductionBuilding(String name, Buildings.Type type, Vector2i gridPosition, Job job, int employeeCapacity, Vector2i entrancePosition, int productionInterval) {
        super(name, type, gridPosition, entrancePosition);
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

    public void business() {
        for (NPC employee : employees) {
            if (employee.getCurrentBuilding() == this && !employee.hasOrders()) {
                job.sequence.assign(employee, this);
            }
        }
    }

    public void dissociateFieldWork(NPC employee) {
        assignedFieldWork.remove(employee);
    }

    public void endWorkday() {
        for (NPC employee : employees) {
            employee.clearOrderQueue();
            job.sequence.onExit(employee, this);
            employee.giveOrder(NPC.Order.Type.EXIT, this);
            if (employee.getHome() != null) {
                employee.giveOrder(NPC.Order.Type.GO_TO, employee.getHome());
                employee.giveOrder(NPC.Order.Type.ENTER, employee.getHome());
            }
        }
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

    public Map<NPC, FieldWork> getAssignedFieldWork() {
        return assignedFieldWork;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        if (indicator.isVisible()) {
            batch.draw(indicator.getTexture(), gridPosition.x * World.TILE_SIZE + indicator.getLocalPosition().x,
                    gridPosition.y * World.TILE_SIZE + indicator.getLocalPosition().y);
        }
    }
}
