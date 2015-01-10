package com.juniper.game.systems;


import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.juniper.game.components.Position;
import com.juniper.game.Mappers;

import java.util.Random;

public class AIRandomMovementSystem extends IteratingSystem {

    Random rng;

    public AIRandomMovementSystem(Family family) {
        super(family);
        rng = new Random();
    }

    public  AIRandomMovementSystem (Family family, int priority) {
        super(family, priority);
        rng = new Random();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Position position = Mappers.positionM.get(entity);

        position.x += ((rng.nextFloat()*5)-2.5f)*deltaTime;
        position.y += ((rng.nextFloat()*5)-2.5f)*deltaTime;
    }
}
