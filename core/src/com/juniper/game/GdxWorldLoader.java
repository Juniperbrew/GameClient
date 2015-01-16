package com.juniper.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import java.io.File;
import java.util.*;

public class GdxWorldLoader {

	public GdxWorldLoader(){

	}

	public static GdxWorldData loadWorld(String startingMapName){
		String resFolderPath = (System.getProperty("user.dir") + File.separator + "res" + File.separator);

		GdxWorldData gdxWorldData = new GdxWorldData(startingMapName);

		System.out.println("#Loading world from: " + resFolderPath);
		TiledMap startingMap = loadMap(resFolderPath, startingMapName);
		gdxWorldData.setActiveMap(startingMap);

		loadObjects(gdxWorldData, startingMapName);

		System.out.println("#Properties of map: " + startingMapName);
		gdxWorldData.printEntities();
		return gdxWorldData;
	}

	private static TiledMap loadMap(String resFolderPath, String mapName){
		System.out.println("#Loading map: " + mapName);
		TiledMap tiledMap = new TmxMapLoader().load(mapName);

		MapProperties mapProperties = tiledMap.getProperties();

		int tileHeight = (int) mapProperties.get("tileheight");
		int tileWidth = (int) mapProperties.get("tilewidth");
		int mapWidth = (int) mapProperties.get("width");
		int mapHeight = (int) mapProperties.get("height");

		return tiledMap;
	}

	private static void loadObjects(GdxWorldData gdxWorldData, String mapName){

		System.out.println("#Checking layers in map: " + mapName);
		TiledMapTileSet tileSet = gdxWorldData.getActiveMap().getTileSets().getTileSet(0);
		MapLayers mapLayers = gdxWorldData.getActiveMap().getLayers();

		for(MapLayer layer: mapLayers){

			MapProperties layerProperties = layer.getProperties();
			System.out.println("Checking layers: " + layer.getName());
			if(layer.getName().equalsIgnoreCase("object layer")){
				gdxWorldData.objectLayer = layer;
				System.out.println("#Found object layer#");
			}
			System.out.println(layer.getClass());
			printMapProperties(layerProperties);
		}
		System.out.println();

		System.out.println("#Loading objects in map: " + mapName);
		MapObjects mapObjects = gdxWorldData.objectLayer.getObjects();
		Iterator<MapObject> objIterator = mapObjects.iterator();
		int objectCount = 0;
		int collisionCount = 0;
		int entityCount = 0;
		int messageCount = 0;
		int teleportCount = 0;
		int spawnCount = 0;
		int exitCount = 0;
		int tileObjectCount = 0;
		int noTypeCount = 0;

		System.out.println("#Listing objects in map: " + mapName);
		while(objIterator.hasNext()){
			objectCount++;
			MapObject obj = objIterator.next();
			MapProperties objProperties = obj.getProperties();
			System.out.println("#"+obj.getName()+"#");
			printMapProperties(objProperties);
			System.out.println();
			String objectType = (String) objProperties.get("type");

			if(objProperties.get("gid")!=null){
				tileObjectCount++;
			}
			if(objectType == null){
				noTypeCount++;
				continue;
			}
			if(objectType.equalsIgnoreCase("collision")){
				collisionCount++;
			}else if(objectType.equalsIgnoreCase("entity")){
				entityCount++;
			}else if(objectType.equalsIgnoreCase("message")){
				messageCount++;
			}else if(objectType.equalsIgnoreCase("teleport")){
				teleportCount++;
			}else if(objectType.equalsIgnoreCase("spawn")){
				spawnCount++;
			}else if(objectType.equalsIgnoreCase("exit")){
				exitCount++;
			}
		}
		System.out.println("Objects with a tile: " + tileObjectCount);
		System.out.println("Collision count: " + collisionCount);
		System.out.println("Entity count: " + entityCount);
		System.out.println("Message count: " + messageCount);
		System.out.println("Teleport count: " + teleportCount);
		System.out.println("Spawn count: " + spawnCount);
		System.out.println("Exit count: " + exitCount);
		System.out.println("Objects without type: " + noTypeCount);
		System.out.println("Unsupported objects: " + (objectCount-(collisionCount+entityCount+messageCount+teleportCount+spawnCount+exitCount+noTypeCount)));
		System.out.println("Total object count: " + objectCount);
		System.out.println();
	}

	public static void printMapProperties(MapProperties properties){
		Iterator<String> keys = properties.getKeys();
		while(keys.hasNext()){
			String key = keys.next();
			System.out.println(key + ": " + properties.get(key));
		}
	}
}