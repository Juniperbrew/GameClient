package com.juniper.game.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.juniper.game.GdxWorldData;
import com.juniper.game.util.CustomTiledMapRenderer;

import java.util.ArrayList;

/**
 * Created by Juniperbrew on 11.1.2015.
 */
public class BottomMapRenderSystem extends EntitySystem {

    private OrthographicCamera camera;
    private CustomTiledMapRenderer tiledMapRenderer;
    private GdxWorldData gdxWorldData;

    public BottomMapRenderSystem(CustomTiledMapRenderer tiledMapRenderer, OrthographicCamera camera, GdxWorldData gdxWorldData){
        super(0);
        this.camera = camera;
        this.tiledMapRenderer = tiledMapRenderer;
        this.gdxWorldData = gdxWorldData;
    }

    @Override
    public void update(float deltaTime){

        ArrayList<TiledMapTileLayer> bottomLayers = new ArrayList<>();
        camera.update();
        tiledMapRenderer.setView(camera);
        for(MapLayer layer :gdxWorldData.getActiveMap().getLayers()){
            if(layer instanceof TiledMapTileLayer){
                if(!layer.getName().equals("Top")){
                    bottomLayers.add((TiledMapTileLayer) layer);
                }
            }

        }
        tiledMapRenderer.renderLayers(bottomLayers);
    }
}
