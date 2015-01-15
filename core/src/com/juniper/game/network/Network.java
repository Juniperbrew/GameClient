package com.juniper.game.network;

import java.util.HashMap;
import java.util.Vector;

import com.badlogic.ashley.core.Component;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.juniper.game.components.shared.*;

public class Network {

	static public final int portTCP = 54555;
	static public final int portUDP = 54556;

    // This registers objects that are going to be sent over the network.
    static public void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(Message.class);
        kryo.register(SyncEntities.class);
        kryo.register(Spawn.class);
        kryo.register(Vector.class);
        kryo.register(MoveTo.class);
        kryo.register(GoToMap.class);

		kryo.register(com.badlogic.ashley.core.Component[].class);
		kryo.register(Component.class);
		kryo.register(Object[].class);
		kryo.register(Name.class);
		kryo.register(MapName.class);
		kryo.register(Position.class);
		kryo.register(Velocity.class);
		kryo.register(Health.class);
		kryo.register(NetworkID.class);
		kryo.register(Player.class);
		kryo.register(java.util.HashMap.class);
		kryo.register(Register.class);
		kryo.register(SyncPlayerList.class);
		kryo.register(TileID.class);
		kryo.register(UpdateEntity.class);
		kryo.register(UpdateComponent.class);
		kryo.register(Bounds.class);
		//FIXME These caused a really annoying bug when server serialized a Player class with a boolean field while client Player class didnt have that
    }

	static public class SyncPlayerList{
		public Vector<String> playerList;
	}

	static public class Register {
		public String connectionName;
	}

	static public class Message{
		public String text;
	}

	static public class UpdateEntity{
		public long networkID;
		public Component[] entity;
	}

	static public class UpdateComponent{
		public long networkID;
		public Component component;
	}

	static public class SyncEntities{
		public HashMap<Long,Component[]> entities;
	}

	static public class MoveTo{
		public int x;
		public int y;
	}

	static public class GoToMap{
		public String mapName;
	}

	static public class Spawn{
		public String name;
		public String mapName;
		public int x;
		public int y;
		public long networkID;
	}
}
