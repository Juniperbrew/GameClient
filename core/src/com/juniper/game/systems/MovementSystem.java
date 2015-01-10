package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.juniper.game.components.Position;
import com.juniper.game.components.Velocity;
import com.juniper.game.*;

public class MovementSystem extends IteratingSystem {

    public MovementSystem(Family family) {
        super(family);
    }

    public MovementSystem(Family family, int priority) {
        super(family, priority);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        System.out.println("Processing movement");
        Position position = Mappers.positionM.get(entity);
        Velocity velocity = Mappers.velocityM.get(entity);

        if(velocity == null) {
            return;
        }
        position.x += velocity.vectorX*deltaTime;
        position.y += velocity.vectorY*deltaTime;
    }
}
