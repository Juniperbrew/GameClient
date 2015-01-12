package com.juniper.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.esotericsoftware.kryonet.Client;
import com.juniper.game.Mappers;
import com.juniper.game.components.client.PlayerControlled;
import com.juniper.game.components.shared.Position;
import com.juniper.game.network.Network.*;

public class UpdateEntityOnServerSystem extends IteratingSystem {

    Client client;

    public UpdateEntityOnServerSystem(Family family, Client client) {
        this(family, client, 0);
    }

    public UpdateEntityOnServerSystem(Family family, Client client, int priority) {
        super(family, priority);
        this.client = client;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlayerControlled player = Mappers.playerControlledM.get(entity);
        if(player.hasMoved){
            UpdateComponent update = new UpdateComponent();
            Position pos = Mappers.positionM.get(entity);
            long id = Mappers.networkidM.get(entity).id;
            update.networkID = id;
            update.component = pos;
            client.sendTCP(update);
            player.hasMoved = false;
        }
    }
}
