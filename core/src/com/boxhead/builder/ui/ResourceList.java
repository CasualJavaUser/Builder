package com.boxhead.builder.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.boxhead.builder.Resources;
import com.boxhead.builder.Textures;
import com.boxhead.builder.utils.Vector2i;

public class ResourceList extends UIElement {
    Label[] labels = new Label[Resources.values().length];

    public ResourceList() {
        super(null, new Vector2i(20, Gdx.graphics.getHeight() - 20), true);
        for (int i = 0; i < labels.length; i++) {
            labels[i] = new Label(Textures.getResource(Resources.values()[i].toString().toLowerCase()),
                                  new Vector2i(position.x, position.y + 20 * i));
        }
    }

    public void updateData() {
        int i = 0, j = labels.length - 1;
        for(Resources resource : Resources.values()) {
            if (resource.getAmount() != 0) {
                labels[i].setTexture(Textures.getResource(resource.toString().toLowerCase()));
                labels[i].setText(resource.getAmount() + "");
                labels[i].setVisible(true);
                i++;
            } else {
                labels[j].setVisible(false);
                j--;
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        for(Label label : labels) {
            if (label.isVisible()) label.draw(batch);
        }
    }
}
