package com.boxhead.builder.game_objects;

import com.boxhead.builder.utils.BoxCollider;
import com.boxhead.builder.utils.Vector2i;

public class FarmBuilding extends ProductionBuilding {

    private BoxCollider fieldCollider;

    public FarmBuilding(Buildings.Type type, Vector2i gridPosition) {
        super(type, gridPosition);
        fieldCollider = new BoxCollider();
    }

    public void setFieldCollider(BoxCollider collider) {
        fieldCollider = collider;
    }

    public BoxCollider getFieldCollider() {
        return fieldCollider;
    }

    public int getFieldWidth() {
        return fieldCollider.getWidth();
    }

    public int getFieldHeight() {
        return fieldCollider.getHeight();
    }

    public Vector2i getFieldGridPosition() {
        return fieldCollider.getGridPosition();
    }
}
