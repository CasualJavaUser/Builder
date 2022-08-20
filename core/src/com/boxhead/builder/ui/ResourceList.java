package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Resources;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class ResourceList extends UIElement {
    Label[] labels = new Label[Resources.values().length];

    public ResourceList() {
        super(null, new Vector2i(20, Gdx.graphics.getHeight() - 20), true);
        for (int i = 0; i < labels.length; i++) {
            TextureRegion texture = getResourcesTexture(Resources.values()[i]);
            Vector2i labelPosition = new Vector2i(position.x, position.y + 20 * i);

            labels[i] = new Label(texture, labelPosition);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        updateData();
        for (Label label : labels) {
            if (label.isVisible()) label.draw(batch);
        }
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        for (int i = 0; i < labels.length; i++) {
            labels[i].setPosition(position.x, position.y + 20 * i);
        }
    }

    private void updateData() {
        int i = 0, j = labels.length - 1;
        for (Resources resource : Resources.values()) {
            if (World.getStored(resource) != 0) {
                labels[i].setTexture(getResourcesTexture(resource));
                labels[i].setText(World.getStored(resource) + " / " + World.getMaxStorage(resource));
                labels[i].setVisible(true);
                i++;
            } else {
                labels[j].setVisible(false);
                j--;
            }
        }
    }

    private TextureRegion getResourcesTexture(Resources resources) {
        String resourceName = resources.toString().toUpperCase();
        return Textures.get(Textures.Resource.valueOf(resourceName));
    }
}
