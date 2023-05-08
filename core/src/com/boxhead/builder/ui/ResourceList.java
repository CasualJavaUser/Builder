package com.boxhead.builder.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Recipe;
import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class ResourceList extends UIElement {
    Label[] labels = new Label[Resource.values().length];

    public ResourceList(UIElement parent, UI.Layer layer, Vector2i position) {
        super(null, parent, layer, position, true);
        for (int i = 0; i < labels.length; i++) {
            TextureRegion texture = getResourcesTexture(Resource.values()[i]);
            labels[i] = new Label(texture, this, layer, Vector2i.zero());
            labels[i].setVisible(false);
            labels[i].setText("0");
            labels[i].setTexture(getResourcesTexture(Resource.values()[i]));
        }
    }

    public void initData() {
        int y = 0;
        for (Resource resource : Resource.values()) {
            if (resource == Resource.NOTHING) continue;
            if (World.getStored(resource) > 0) {
                labels[y+1].setVisible(true);
                labels[y+1].setText(World.getStored(resource) + "");
                labels[y+1].setLocalPosition(0, -25 * y);
                y++;
            }

        }
    }

    public void updateData(Resource resource, int amount) {
        Label label = labels[resource.ordinal()];
        if (label.getText().equals("0")) {
            label.setVisible(true);
            organiseLabels();
        }

        label.setText(World.getStored(resource) + "");

        if (label.getText().equals("0")) {
            label.setVisible(false);
            organiseLabels();
        }
    }

    public void updateData(Recipe recipe) {
        for (Resource resource : recipe.changedResources()) {
            Label label = labels[resource.ordinal()];
            if (label.getText().equals("0")) {
                label.setVisible(true);
                organiseLabels();
            }

            label.setText(World.getStored(resource) + "");

            if (label.getText().equals("0")) {
                label.setVisible(false);
                organiseLabels();
            }
        }
    }

    private void organiseLabels() {
        int y = 0;
        for (Label label : labels) {
            if (label.isVisible()) {
                label.setLocalPosition(0, -25 * y);
                y++;
            }
        }
    }

    private TextureRegion getResourcesTexture(Resource resource) {
        String resourceName = resource.toString().toUpperCase();
        return Textures.get(Textures.Resource.valueOf(resourceName));
    }

    @Override
    public void addToUI() {
        super.addToUI();
        for (Label label : labels) {
            label.addToUI();
        }
    }
}
