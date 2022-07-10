package com.boxhead.builder;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ProductionBuilding extends EnterableBuilding {

    protected Jobs job;
    protected int jobQuality = 0;
    protected int employeeCapacity, employeeCount = 0, employeesInside = 0;
    protected NPC[] employees;

    public ProductionBuilding(TextureRegion texture, Jobs job, int employeeCapacity, Vector2i entrancePosition) {
        super(texture, entrancePosition);
        this.job = job;
        this.employeeCapacity = employeeCapacity;
        employees = new NPC[employeeCapacity];
    }

    /**
     * Removes the specified employee from the building.
     * @param npc employee to be removed from the building
     * @return true if the array of employees changed as a result of the call
     */
    public boolean removeEmployee(NPC npc) {
        boolean b = false;
        if (employeeCount > 0) {
            for (int i = 0; i < employees.length; i++) {
                if (employees[i] == npc) {
                    employees[i] = null;
                    employeeCount--;
                    b = true;
                    break;
                }
            }
        }
        return b;
    }

    /**
     * Adds the specified employee to the building.
     * @param npc employee to be added to the building
     * @return true if the array of employees changed as a result of the call
     */
    public boolean addEmployee(NPC npc) {
        boolean b = false;
        if (employeeCount < employeeCapacity) {
            for (int i = 0; i < employees.length; i++) {
                if (employees[i] == null) {
                    employees[i] = npc;
                    employeeCount++;
                    b = true;
                    break;
                }
            }
        }
        return b;
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
        World.resourceStorage[job.getProduct().getIndex()] += employeesInside;
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
}
