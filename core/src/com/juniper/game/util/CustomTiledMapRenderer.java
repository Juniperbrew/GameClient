package com.juniper.game.util;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import java.util.ArrayList;

/**
 * Created by Juniperbrew on 17.1.2015.
 */
public class CustomTiledMapRenderer extends OrthogonalTiledMapRenderer {
    public CustomTiledMapRenderer(TiledMap map) {
        super(map);
    }

    public void renderLayers(ArrayList<TiledMapTileLayer> layers) {
        getBatch().setColor(1f, 1f, 1f, 1f);
        beginRender();
        for(TiledMapTileLayer layer: layers){
            super.renderTileLayer(layer);
        }
        endRender();
    }
}
