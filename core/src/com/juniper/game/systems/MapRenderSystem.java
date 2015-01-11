package com.juniper.game.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

/**
 * Created by Juniperbrew on 11.1.2015.
 */
public class MapRenderSystem extends EntitySystem {

    private OrthographicCamera camera;
    private OrthogonalTiledMapRenderer tiledMapRenderer;

    public MapRenderSystem(OrthogonalTiledMapRenderer tiledMapRenderer, OrthographicCamera camera){
        super(0);
        this.camera = camera;
        this.tiledMapRenderer = tiledMapRenderer;
    }

    @Override
    public void update(float deltaTime){

        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
    }
}
