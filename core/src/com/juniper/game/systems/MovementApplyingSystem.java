package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.juniper.game.Mappers;
import com.juniper.game.components.client.Movement;
import com.juniper.game.components.shared.Position;

/**
 * Created by Juniperbrew on 13.1.2015.
 */
public class MovementApplyingSystem extends IteratingSystem{

    public MovementApplyingSystem(Family family) {
        super(family);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Movement movement = Mappers.movementM.get(entity);
        Position position = Mappers.positionM.get(entity);

        position.x += movement.deltaX;
        position.y += movement.deltaY;

        movement.deltaX = 0;
        movement.deltaY = 0;

        if(Mappers.playerControlledM.get(entity) != null){

        }
    }
}
