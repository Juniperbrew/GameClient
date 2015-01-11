package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.juniper.game.Mappers;
import com.juniper.game.components.Position;

public class PlayerControlSystem extends IteratingSystem {
    public PlayerControlSystem(Family family) {
        super(family);
    }

    public PlayerControlSystem(Family family, int priority) {
        super(family, priority);
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
        System.out.println("Player moved to " + pos.x + "," + pos.y);
    }
}
