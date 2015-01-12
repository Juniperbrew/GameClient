package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.juniper.game.Mappers;
import com.juniper.game.components.AnimatedSprite;
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
        int moveSpeed = 300; //pixels per sec

        AnimatedSprite animatedSprite = Mappers.animatedM.get(entity);

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            deltaX -= moveSpeed*deltaTime;
            animatedSprite.setRotation(270);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            deltaX += moveSpeed*deltaTime;
            animatedSprite.setRotation(90);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            deltaY += moveSpeed*deltaTime;
            animatedSprite.setRotation(0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            deltaY -= moveSpeed*deltaTime;
            animatedSprite.setRotation(180);
        }

        //If player moved we update stuff
        if(deltaX != 0 || deltaY != 0){
            pos.x += deltaX;
            pos.y += deltaY;
            animatedSprite.needsStateTimeUpdate = true;
            Mappers.playerControlledM.get(entity).hasMoved = true;
            camera.position.set(pos.x, pos.y, 0);
        }
    }
}
