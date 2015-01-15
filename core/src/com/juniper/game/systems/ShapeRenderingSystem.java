package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.juniper.game.Mappers;
import com.juniper.game.components.shared.Bounds;
import com.juniper.game.components.shared.Position;

public class ShapeRenderingSystem extends ListeningEntitySystem {

    ShapeRenderer shapeRenderer;
    OrthographicCamera camera;

    public ShapeRenderingSystem(Family family, ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        super(family);
        this.shapeRenderer = shapeRenderer;
        this.camera = camera;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Position position = Mappers.positionM.get(entity);
        Bounds bounds = Mappers.boundsM.get(entity);
        //FIXME figure out a way to batch these calls

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(position.x, position.y, bounds.width, bounds.height);
        shapeRenderer.end();
    }
}
