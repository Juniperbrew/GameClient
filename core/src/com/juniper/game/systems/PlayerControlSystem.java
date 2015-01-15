package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.juniper.game.Mappers;
import com.juniper.game.components.client.AnimatedSprite;
import com.juniper.game.components.client.Movement;
import com.juniper.game.components.shared.Position;

public class PlayerControlSystem extends ListeningEntitySystem {

    public PlayerControlSystem(Family family) {
        super(family);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        Position pos = Mappers.positionM.get(entity);
        Movement movement = Mappers.movementM.get(entity);
        int moveSpeed = 300; //pixels per sec

        AnimatedSprite animatedSprite = Mappers.animatedM.get(entity);

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            movement.deltaX -= moveSpeed*deltaTime;
            animatedSprite.setRotation(270);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            movement.deltaX += moveSpeed*deltaTime;
            animatedSprite.setRotation(90);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            movement.deltaY += moveSpeed*deltaTime;
            animatedSprite.setRotation(0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            movement.deltaY -= moveSpeed*deltaTime;
            animatedSprite.setRotation(180);
        }

        //If player moved we update stuff
        if(movement.deltaX != 0 || movement.deltaY != 0){
            animatedSprite.needsStateTimeUpdate = true;
            //Mappers.playerControlledM.get(entity).hasMoved = true;
            Mappers.movementM.get(entity).syncServer = true;
        }
    }
}
