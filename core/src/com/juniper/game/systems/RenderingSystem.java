package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.juniper.game.Mappers;
import com.juniper.game.components.Position;

public class RenderingSystem extends IteratingSystem {

    ShapeRenderer shapeRenderer;

    public RenderingSystem(Family family, ShapeRenderer shapeRenderer) {
        super(family);
        this.shapeRenderer = shapeRenderer;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Position position = Mappers.positionM.get(entity);
        shapeRenderer.rect(position.x, position.y, 10, 10);
    }
}
