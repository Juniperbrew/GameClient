package com.juniper.game.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.juniper.game.Mappers;

import java.util.LinkedList;

/**
 * Created by Juniperbrew on 15.1.2015.
 */
public abstract class ListeningEntitySystem extends EntitySystem implements EntityListener {

    private LinkedList<Entity> entities;
    private Family family;

    public ListeningEntitySystem (Family family) {
        this.family = family;
        entities = new LinkedList<>();
    }

    @Override
    public void update (float deltaTime) {
        for (int i = 0; i < entities.size(); ++i) {
            processEntity(entities.get(i), deltaTime);
        }
    }

    @Override
    public void entityAdded(Entity entity) {
        entities.add(entity);
        System.out.println(Mappers.nameM.get(entity).name + " added to "+this);
    }

    @Override
    public void entityRemoved(Entity entity) {
        entities.remove(entity);
        System.out.println(Mappers.nameM.get(entity).name + " removed from "+this);
    }

    @Override
    public void addedToEngine (Engine engine) {
        //FIXME What is best list to use here, probably arraylist instead of vector
        ImmutableArray<Entity> immutableEntities = engine.getEntitiesFor(family);
        for(Entity e : immutableEntities){
            entities.add(e);
            System.out.println("Entity (" + Mappers.nameM.get(e).name + ") added to "+this);
        }
    }

    @Override
    public void removedFromEngine (Engine engine) {
        entities.clear();
        System.out.println(this + " removed from engine.");
    }

    public LinkedList<Entity> getEntities () {
        return entities;
    }

    protected abstract void processEntity (Entity entity, float deltaTime);
}
