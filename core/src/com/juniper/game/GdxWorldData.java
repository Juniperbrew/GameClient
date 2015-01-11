package com.juniper.game;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.juniper.game.components.*;
import com.juniper.game.util.EntityToString;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;


public class GdxWorldData implements EntityListener {

    private Engine engine;
	public HashMap<Long,Entity> entityIDs;
    public Vector<Entity> entities;
    public Vector<String> playerList;
    HashMap<String,TextureRegion> entityNameToTextureMapping;
    private TiledMap map;
    public MapLayer objectLayer;

    private long networkIDCounter;

    public GdxWorldData(Engine engine){
        this.engine = engine;
        engine.addEntityListener(this);
        entities = new Vector<>();
        entityIDs = new HashMap<>();
        playerList = new Vector<>();
        entityNameToTextureMapping = new HashMap<>();
    }

    public void addFamilyListener(Family family, EntityListener listener){
        engine.addEntityListener(family, listener);
    }

    public void setActiveMap(TiledMap map){
        this.map = map;
    }

    public TiledMap getActiveMap(){
        return map;
    }

    public void addSystem(EntitySystem system){
        engine.addSystem(system);
    }

    public int getEntityCount(){
        return entityIDs.size();
    }

    public Vector<Entity> getEntities(){
        return entities;
    }

    public void printEntities(){
        System.out.println("###-------------------------------------------------------------###");
        for(Entity entity : entities){
            System.out.println(EntityToString.convert(entity));
        }
        System.out.println("###-------------------------------------------------------------###");
    }

    public Entity getEntityWithID(long id){
        return entityIDs.get(id);
    }

    public Vector<String> getEntitiesAsString(){
        Vector<String> entitiesAsString = new Vector<>();
        for (Entity e : entities) {
            entitiesAsString.add(EntityToString.convert(e));
        }
        return entitiesAsString;
    }

    public void updateEntities(HashMap<Long,Component[]> updatedEntities){

        Set<Long> updateIDList = updatedEntities.keySet();

        //Loop through all entities sent in update
            for(long id : updateIDList){
            //The changed local entity
            Entity e = getEntityWithID(id);
            //Get the new components
            Component[] updatedComponents = updatedEntities.get(id);
            if(e == null) {
                //If the entity doesnt exist in local store we add it
                Entity newEntity = new Entity();
                //Add new components
                for(Component updatedComponent : updatedComponents){
                    newEntity.add(updatedComponent);
                }
                addEntity(newEntity);
            }else{
                //FIXME trying to update components instead of replacing, might cause bugs now
                //Update components
                for(Component updatedComponent : updatedComponents){
                    if(updatedComponent instanceof Position){
                        Position pos = Mappers.positionM.get(e);
                        pos.x = ((Position) updatedComponent).x;
                        pos.y = ((Position) updatedComponent).y;
                    }else if(updatedComponent instanceof MapName){
                        Mappers.mapM.get(e).map = ((MapName) updatedComponent).map;
                    }else if(updatedComponent instanceof Name){
                        Mappers.nameM.get(e).name = ((Name) updatedComponent).name;
                    }
                }
            }
        }

        //Remove all entities not sent in update
        //Copy id list to avoid ConcurrentModificationException caused by calls to entityRemoved(Entity e)
        //FIXME is this a good idea
        Vector<Long> idListCopy = new Vector<>();
        for(long id : entityIDs.keySet()){
            idListCopy.add(id);
        }

        for(long id : idListCopy){
            if (updateIDList.contains(id)) {
                //This entity was sent in update
            } else {
                //We dont need to remove ID here since it's removed in the call to listeners entityRemoved()
                //iter.remove();
                engine.removeEntity(getEntityWithID(id));

                System.out.println("Removed one entity from local list, this should never happen on server");
            }
        }

    }

    public void addEntity(Entity e){
        //If entity doesnt have a networkID we give it one
        if(e.getComponent(NetworkID.class) == null){
            e.add(new NetworkID(networkIDCounter));
            networkIDCounter++;
            System.out.println("Giving entity a network ID, this should never be called on client");
        }
    engine.addEntity(e);
    }


    public void removeAllEntities(){
        engine.removeAllEntities();
        networkIDCounter = 0;

        entityIDs.clear();
        entities.clear();

    }

    public void updateWorld(float deltaTime){
        engine.update(deltaTime);
    }

	@Override
	public void entityAdded(Entity entity) {
        String map = Mappers.mapM.get(entity).map;
        System.out.println("Added " + EntityToString.convert(entity) + " to map " + map);

        //Add to id list
        entityIDs.put(Mappers.networkidM.get(entity).id, entity);

        //If entity is player we add his name to playerlist
        if(entity.getComponent(Player.class) != null){
            playerList.add(Mappers.nameM.get(entity).name);
        }
        //Add to entity list
        entities.add(entity);
    }

	@Override
	public void entityRemoved(Entity entity) {
        String map = Mappers.mapM.get(entity).map;
        System.out.println("Removed " + EntityToString.convert(entity) + " from map " + map);

        //Remove from id list
        entityIDs.remove(Mappers.networkidM.get(entity).id);

        //If entity is player we remove him from playerlist
        if(entity.getComponent(Player.class) != null){
            playerList.remove(Mappers.nameM.get(entity).name);
        }
        //Remove from entity list
        entities.remove(entity);
	}
}
