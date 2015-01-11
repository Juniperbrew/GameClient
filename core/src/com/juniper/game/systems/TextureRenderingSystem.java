package com.juniper.game.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.juniper.game.Mappers;
import com.juniper.game.components.Position;

import java.util.Vector;

public class TextureRenderingSystem extends EntitySystem implements EntityListener{

    private Batch batch;
    private Family family;
    private Vector<Entity> entities;

    public TextureRenderingSystem(Family family, Batch batch){
        this(family,batch,0);
    }

    public TextureRenderingSystem(Family family, Batch batch, int priority){
        super(priority);
        this.family = family;
        this.batch = batch;
        entities = new Vector<>();
    }

    @Override
    public void addedToEngine (Engine engine) {
        //FIXME can i somehow use ImmutableArray instead of Vector
        ImmutableArray<Entity> immutableEntities = engine.getEntitiesFor(family);
        for(Entity e : immutableEntities){
            entities.add(e);
        }
    }

    @Override
    public void removedFromEngine (Engine engine) {
        entities.clear();
    }


    @Override
    public void update (float deltaTime) {
        for (Entity entity : entities) {
            Position position = Mappers.positionM.get(entity);
            TextureRegion texture = Mappers.spriteM.get(entity).texture;
            //FIXME figure out a way to batch these calls
            batch.begin();
            batch.draw(texture, position.x,position.y);
            batch.end();
        }
    }

    @Override
    public void entityAdded(Entity entity) {
        System.out.println("Entity (" + Mappers.nameM.get(entity).name + ") with Sprite added");
        entities.add(entity);
    }

    @Override
    public void entityRemoved(Entity entity) {
        System.out.println("Entity (" + Mappers.nameM.get(entity).name + ") with Sprite removed");
        entities.remove(entity);
    }
}
