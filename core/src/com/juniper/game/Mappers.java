package com.juniper.game;

import com.badlogic.ashley.core.ComponentMapper;
import com.juniper.game.components.client.AnimatedSprite;
import com.juniper.game.components.client.PlayerControlled;
import com.juniper.game.components.client.Sprite;
import com.juniper.game.components.shared.*;

public class Mappers {
    public static final ComponentMapper<Position> positionM = ComponentMapper.getFor(Position.class);
    public static final ComponentMapper<Velocity> velocityM = ComponentMapper.getFor(Velocity.class);
    public static final ComponentMapper<Name> nameM = ComponentMapper.getFor(Name.class);
    public static final ComponentMapper<MapName> mapM = ComponentMapper.getFor(MapName.class);
    public static final ComponentMapper<NetworkID> networkidM = ComponentMapper.getFor(NetworkID.class);
    public static final ComponentMapper<Player> playerM = ComponentMapper.getFor(Player.class);
    public static final ComponentMapper<Sprite> spriteM = ComponentMapper.getFor(Sprite.class);
    public static final ComponentMapper<TileID> tileidM = ComponentMapper.getFor(TileID.class);
    public static final ComponentMapper<AnimatedSprite> animatedM = ComponentMapper.getFor(AnimatedSprite.class);
    public static final ComponentMapper<PlayerControlled> playerControlledM = ComponentMapper.getFor(PlayerControlled.class);
}