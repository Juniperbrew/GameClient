package com.juniper.game.util;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;
import com.juniper.game.Global;

/**
 * Created by Juniperbrew on 13.1.2015.
 */
public class MapMask {

    public boolean[][] mask;
    public int height;
    public int width;

    public MapMask(int height, int width, Array<TiledMapTileLayer> layers, String propertyKey) {
        this.height = height;
        this.width = width;

        mask = new boolean[height][width];
        generate(layers, propertyKey);
    }

    public boolean atGrid( final int x, final int y )
    {
        if ( x >= width || x < 0 || y < 0 || y >= height  ) return false;
        return mask[y][x];
    }

    public boolean atScreen( final int x, final int y)
    {
        return atGrid((int)(x / Global.TILE_SIZE),(int)(y / Global.TILE_SIZE));
    }

    private void generate(Array<TiledMapTileLayer> layers, String propertyKey) {
        for (TiledMapTileLayer layer : layers) {
            for (int ty = 0; ty < height; ty++) {
                for (int tx = 0; tx < width; tx++) {
                    final TiledMapTileLayer.Cell cell = layer.getCell(tx, ty);
                    if ( cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey(propertyKey)) {
                        mask[ty][tx] = true;
                    }
                }
            }
        }
    }
}
