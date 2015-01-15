package com.juniper.game;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.juniper.game.components.client.AnimatedSprite;
import com.juniper.game.components.client.Movement;
import com.juniper.game.components.shared.*;
import com.juniper.game.systems.ListeningEntitySystem;
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
    TextureAtlas textureAtlas;
    String mapName;

    private long networkIDCounter;

    public GdxWorldData(String mapName){
        this.mapName = mapName;
        engine = new Engine();
        engine.addEntityListener(this);
        entities = new Vector<>();
        entityIDs = new HashMap<>();
        playerList = new Vector<>();
        textureAtlas = new TextureAtlas(Gdx.files.internal("data/spritesheetindexed.atlas"));
    }

    public void addFamilyListener(Family family, EntityListener listener){
        engine.addEntityListener(family, listener);
    }

    public void toggleSystem(int index){
        EntitySystem system = engine.getSystems().get(index);
        if(system == null){
            System.out.println("No system with index "+index+" in map "+mapName);
            return;
        }
        system.setProcessing(!system.checkProcessing());
        System.out.println(system + " is now " + (system.checkProcessing() ? " enabled." : " disabled."));
    }

    public void toggleAllSystems(boolean enabled){
        for(EntitySystem system:engine.getSystems()){
            system.setProcessing(enabled);
        }
        System.out.println((enabled?"Enabled ":"Disabled ")+" all systems on map "+mapName);
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

    public ImmutableArray<Entity> getEntitiesInFamily(Family family){
        return engine.getEntitiesFor(family);
    }

    public void updateEntities(HashMap<Long,Component[]> updatedEntities){
        //FIXME this method really needs some work

        Set<Long> updateIDList = updatedEntities.keySet();
        //Loop through all entities sent in update
            for(long id : updateIDList){
            //The changed local entity
            Entity e = getEntityWithID(id);
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
                        Position updatedPosition = (Position) updatedComponent;
                        Position currentPosition = Mappers.positionM.get(e);
                        //Store the movement change in the movement component
                        //Every entity that changes position needs this component so we can let the game crash here if someone doesnt have it
                        Mappers.movementM.get(e).deltaX = updatedPosition.x - currentPosition.x;
                        Mappers.movementM.get(e).deltaY = updatedPosition.y - currentPosition.y;

                        //If the moved entity has an animation
                        if(Mappers.animatedM.get(e) != null){
                            //FIXME this seems like an awkwards way to animated networked sprites
                            AnimatedSprite animatedSprite = Mappers.animatedM.get(e);
                            if(updatedPosition.x > currentPosition.x){
                                animatedSprite.setRotation(90);
                                Mappers.animatedM.get(e).needsStateTimeUpdate = true;
                            }else if(updatedPosition.x < currentPosition.x){
                                animatedSprite.setRotation(270);
                                Mappers.animatedM.get(e).needsStateTimeUpdate = true;
                            }else if(updatedPosition.y > currentPosition.y){
                                animatedSprite.setRotation(0);
                                Mappers.animatedM.get(e).needsStateTimeUpdate = true;
                            }else if(updatedPosition.y < currentPosition.y){
                                animatedSprite.setRotation(180);
                                Mappers.animatedM.get(e).needsStateTimeUpdate = true;
                            }else{
                                Mappers.animatedM.get(e).needsStateTimeUpdate = false;
                            }
                        }
                    }else if(updatedComponent instanceof MapName){
                        Mappers.mapM.get(e).map = ((MapName) updatedComponent).map;
                    }else if(updatedComponent instanceof Name){
                        Mappers.nameM.get(e).name = ((Name) updatedComponent).name;
                    }else if(updatedComponent instanceof Player){
                        //If a player doesn't have an animated component we add one
                        if(Mappers.animatedM.get(e)==null){
                            e.add(new AnimatedSprite(textureAtlas));
                        }
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
        //If entity doesn't have a Movement component we give it one
        //FIXME This component will be needed on both server and client for collision handling but the data will not be shared so it's probably best to assign these separately in both also there might be a better place where to add it
        if(Mappers.movementM.get(e) == null){
            e.add(new Movement());
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

    public void dumpData(){

        /*
        public HashMap<Long,Entity> entityIDs;
        public Vector<Entity> entities;
        public Vector<String> playerList;
        HashMap<String,TextureRegion> entityNameToTextureMapping;
        private TiledMap map;
        public MapLayer objectLayer;
        TextureAtlas textureAtlas;*/


        System.out.println("-----------------------------------------");
        System.out.println("#"+mapName+": "+engine);
        ImmutableArray<EntitySystem> systems = engine.getSystems();
        for (int i = 0; i < systems.size(); i++) {
            EntitySystem system = systems.get(i);
            System.out.println("  "+i+"#"+system+": "+system.checkProcessing());
            if(system instanceof ListeningEntitySystem){
                ListeningEntitySystem s = (ListeningEntitySystem) system;
                for(Entity e:s.getEntities()){
                    System.out.println("    >"+EntityToString.convert(e));
                }
            }
        }
        System.out.println();
        System.out.println("Entities:");
        for(Entity e:entities){
            System.out.println(">"+EntityToString.convert(e));
        }
        System.out.println("-----------------------------------------");
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
