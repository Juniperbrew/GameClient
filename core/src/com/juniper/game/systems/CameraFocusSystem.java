package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.juniper.game.Mappers;
import com.juniper.game.components.shared.Position;

/**
 * Created by Juniperbrew on 14.1.2015.
 */
public class CameraFocusSystem extends IteratingSystem {

    private OrthographicCamera camera;

    public CameraFocusSystem(Family family, OrthographicCamera camera) {
        super(family);
        this.camera = camera;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        //This should only run for PlayerControlled entity
        Position position = Mappers.positionM.get(entity);
        camera.position.set(position.x,position.y,0);
    }
}
