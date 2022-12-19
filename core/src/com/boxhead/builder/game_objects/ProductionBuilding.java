package com.boxhead.builder.game_objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.boxhead.builder.*;
import com.boxhead.builder.ui.TileCircle;
import com.boxhead.builder.ui.UI;
import com.boxhead.builder.ui.UIElement;
import com.boxhead.builder.utils.Vector2i;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProductionBuilding extends EnterableBuilding {
    protected transient Job job;
    protected int jobQuality = 0;
    protected int employeeCapacity, employeesInside = 0;
    protected final Set<NPC> employees;
    protected final Map<NPC, FieldWork> assignedFieldWork;
    protected int productionCounter = 0, productionInterval;
    protected boolean showRange = false;
    protected transient UIElement indicator;

    public ProductionBuilding(Buildings.Type type, Vector2i gridPosition, int employeeCapacity, int productionInterval) {
        super(type, gridPosition);
        this.type = type;
        job = type.getJob();
        this.employeeCapacity = employeeCapacity;
        this.productionInterval = productionInterval;
        employees = new HashSet<>(employeeCapacity, 1f);
        if (job.getPoI() != null) {
            assignedFieldWork = new HashMap<>(employeeCapacity, 1f);
        } else {
            assignedFieldWork = null;
        }
        instantiateIndicator();
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
                job.assign(employee, this);
            }
        }

        if (productionInterval > 0) {
            productionCounter += employeesInside;

            if (productionCounter >= productionInterval) {
                Recipe recipe = job.getRecipe();
                Inventory.Availability availability = inventory.checkStorageAvailability(recipe);

                if (availability == Inventory.Availability.AVAILABLE) {
                    inventory.put(recipe);
                    productionCounter = 0;
                    Logistics.requestTransport(this, recipe);
                }
            }
        }
    }

    public void dissociateFieldWork(NPC employee) {
        assignedFieldWork.remove(employee);
    }

    public void endWorkday() {
        for (NPC employee : employees) {
            employee.clearOrderQueue();
            job.onExit(employee, this);
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

    public int getEmployeeCapacity() {
        return employeeCapacity;
    }

    public Set<NPC> getEmployees() {
        return employees;
    }

    public boolean hasEmployeesInside() {
        return employeesInside != 0;
    }

    public Map<NPC, FieldWork> getAssignedFieldWork() {
        return assignedFieldWork;
    }

    public void hideRangeVisualiser() {
        showRange = false;
    }

    @Override
    public void onClick() {
        super.onClick();
        showRange = true;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.setColor(UI.VERY_TRANSPARENT);
        if (showRange) TileCircle.draw(
                batch,
                Textures.get(Textures.Tile.DEFAULT),  //todo temp texture
                gridPosition.add(entrancePosition).multiply(World.TILE_SIZE),
                job.getRange() * World.TILE_SIZE);
        batch.setColor(UI.DEFAULT_COLOR);

        super.draw(batch);

        if (inventory.checkStorageAvailability(job.getRecipe()) == Inventory.Availability.OUTPUT_FULL) {
            indicator.setTexture(Textures.get(Textures.Ui.FULL_STORAGE));
            indicator.setVisible(true);
        }
        else if (inventory.checkStorageAvailability(job.getRecipe()) == Inventory.Availability.LACKS_INPUT) {
            indicator.setTexture(Textures.get(Textures.Ui.NO_RESOURCES));
            indicator.setVisible(true);
        }
        else {
            indicator.setVisible(false);
        }
        if (indicator.isVisible()) {
            Vector3 screenPos = GameScreen.worldToScreenPosition(
                    gridPosition.x * World.TILE_SIZE + getTexture().getRegionWidth()/2f - indicator.getWidth()/2f * GameScreen.camera.zoom,
                    gridPosition.y * World.TILE_SIZE + getTexture().getRegionHeight() + 5);
            indicator.setGlobalPosition((int)screenPos.x, (int)screenPos.y);
//            batch.draw(indicator.getTexture(), gridPosition.x * World.TILE_SIZE + indicator.getLocalPosition().x,
//                    gridPosition.y * World.TILE_SIZE + indicator.getLocalPosition().y);
        }
    }

    private void instantiateIndicator() {
        indicator = new UIElement(
                Textures.get(Textures.Ui.FULL_STORAGE),
                UI.Layer.BUILDINGS,
                //new Vector2i(texture.getRegionWidth() / 2 - 8, texture.getRegionHeight() + 10),
                new Vector2i(),
                false);
        indicator.addToUI();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(type.name());
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        type = Buildings.Type.valueOf(ois.readUTF());
        texture = type.getTexture();
        job = type.getJob();
        instantiateIndicator();
    }
}
