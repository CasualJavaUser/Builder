package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.World;
import com.boxhead.builder.utils.Vector2i;

public class ResourceList extends UIElement {
    Label[] labels = new Label[Resource.values().length];

    public ResourceList() {
        super(null, new Vector2i(20, Gdx.graphics.getHeight() - 20), true);
        for (int i = 0; i < labels.length; i++) {
            TextureRegion texture = getResourcesTexture(Resource.values()[i]);
            Vector2i labelPosition = new Vector2i(0, 20 * i);

            labels[i] = new Label(texture, this, labelPosition);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        updateData();
        for (Label label : labels) {
            if (label.isVisible()) label.draw(batch);
        }
    }

    private void updateData() {
        int i = 0, j = labels.length - 1;
        for (Resource resource : Resource.values()) {
            if (World.getStored(resource) != 0) {
                labels[i].setTexture(getResourcesTexture(resource));
                labels[i].setText(World.getStored(resource) + "");
                labels[i].setVisible(true);
                i++;
            } else {
                labels[j].setVisible(false);
                j--;
            }
        }
    }

    private TextureRegion getResourcesTexture(Resource resource) {
        String resourceName = resource.toString().toUpperCase();
        return Textures.get(Textures.Resource.valueOf(resourceName));
    }
}
