package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.juniper.game.Mappers;
import com.juniper.game.components.Position;

public class PlayerControlSystem extends IteratingSystem {

    private OrthographicCamera camera;

    public PlayerControlSystem(Family family, OrthographicCamera camera) {
        super(family);
        this.camera = camera;
    }

    public PlayerControlSystem(Family family, OrthographicCamera camera, int priority) {
        super(family, priority);
        this.camera = camera;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        Position pos = Mappers.positionM.get(entity);
        float deltaX = 0;
        float deltaY = 0;
        int moveSpeed = 1000; //pixels per sec

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            deltaX -= moveSpeed*deltaTime;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            deltaX += moveSpeed*deltaTime;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            deltaY += moveSpeed*deltaTime;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            deltaY -= moveSpeed*deltaTime;
        }

        pos.x += deltaX;
        pos.y += deltaY;

        Mappers.playerM.get(entity).hasMoved = true;

        camera.position.set(pos.x, pos.y, 0);
    }
}
