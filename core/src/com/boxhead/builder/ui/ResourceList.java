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
            labels[i] = new Label(texture, this, Vector2i.zero());
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
            if (resource != Resource.NOTHING && World.getStored(resource) != 0) {
                labels[i].setTexture(getResourcesTexture(resource));
                labels[i].setLocalPosition(0, -25 * i);
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
