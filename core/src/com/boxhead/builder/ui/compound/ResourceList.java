package com.boxhead.builder.ui.compound;

import com.boxhead.builder.Resource;
import com.boxhead.builder.Textures;
import com.boxhead.builder.ui.*;

public class ResourceList extends BoxPane {
    private final TextArea[] resourceCounts = new TextArea[Resource.values().length];

    public ResourceList() {
        Pane grid = new GridPane(2, Resource.values().length-1, 16, 16);
        for (int i = 1; i < Resource.values().length; i++) {
            Resource resource = Resource.values()[i];
            grid.addUIComponent(new DrawableComponent(Textures.get(Textures.Resource.valueOf(resource.name()))));
            TextArea textArea = new TextArea(resource.getStored() + "", 16, TextArea.Align.LEFT);
            grid.addUIComponent(textArea);
            resourceCounts[i] = textArea;
        }
        addUIComponent(grid);
    }

    public void updateResourceCount(Resource resource) {
        resourceCounts[resource.ordinal()].setText(resource.getStored() + "");
    }
}
