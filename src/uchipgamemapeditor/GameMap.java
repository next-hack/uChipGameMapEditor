/*
*  uChip Game Map Editor. A simple editor and integrated utility for tile-based games.
*
*  Copyright 2019-2020 Nicola Wrachien (next-hack.com)
*
*  This file is part of uChip Simple VGA Console Kernel Library.
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program  is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*  tl;dr
*  Do whatever you want, this program is free! Though we won't
*  reject donations https://next-hack.com/index.php/donate/ :)
*
*/
package uchipgamemapeditor;

import com.google.gson.Gson;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JOptionPane;

public class GameMap
{
    // note! This claass has both (mapSizeX, mapSizeY and map[][]) as well as (width, height and data[]) for a partial compatibility with maps created by Unity? (maybe).
    private int mapSizeX = 100;
    private int mapSizeY = 100; 
    private short [][] map;
    private long [] data;
    private short width;
    private short height;
    static public final short TILE_MASK = 0x3FFF;
    static public final short TILE_PRIORITY_MASK = 0x4000;
    public GameMap(int sizeX, int sizeY)
    {
        mapSizeX = sizeX;
        mapSizeY = sizeY;
        map = new short[mapSizeX][mapSizeY];       
    }
    private GameMap(int sizeX, int sizeY, short [][] oldMap)
    {
        mapSizeX = sizeX;
        mapSizeY = sizeY;
        //map = oldMap.clone();
        map = new short[mapSizeX][mapSizeY];  
        /*for (int x = 0; x < mapSizeX; x++)
        {
            for (int y = 0; y < mapSizeY; y++)
            {
                map[x][y] = oldMap[x][y];
            }
        }       */
        for (int i = 0; i < oldMap.length; i++) {
            System.arraycopy(oldMap[i], 0, map[i], 0, oldMap[0].length);
}
    }
    
    public GameMap getCopy()
    {
        GameMap copy = new GameMap(mapSizeX , mapSizeY, map);
        return copy;
    }
    public int getTile(int x, int y)
    {
        return map[x][y];
    }
    public void setTile(int x, int y, short tile)
    {
        map[x][y] = tile;
    }
    public byte getPriority( int x, int y)
    {
        return (byte) (((map[x][y] & TILE_PRIORITY_MASK) == TILE_PRIORITY_MASK) ? 1 : 0);
    }
    public void setPriority (int x, int y, int pri)
    {
        if (pri != 0)
            map[x][y] |= TILE_PRIORITY_MASK;
        else
            map[x][y] = (short) (map[x][y] & TILE_MASK);
    }
    public int getSizeX ()
    {
        return mapSizeX;
    }
    public int getSizeY()
    {
        return mapSizeY;
    }
    public GameMap(GameMap oldMap, int sizeX, int sizeY)
    {
        mapSizeX = sizeX;
        mapSizeY = sizeY;
        map = new short[mapSizeX][mapSizeY];           
        int minX = Integer.min(sizeX, oldMap.getSizeX());
        int minY = Integer.min(sizeY, oldMap.getSizeY());
        for (int x = 0; x < minX; x++)
        {
            for (int y = 0; y < minY; y++)
            {
                map[x][y] = oldMap.map[x][y];
            }
        }
    } 
    public static GameMap loadFormFile( String fileName) throws IOException
    {
        GameMap gameMap;
        String gsonString = new String (Files.readAllBytes(Paths.get(fileName)));
        Gson gson = new Gson();
        // this is only used to quickly load other map types (e.g. for Unity).
        gameMap =  gson.fromJson(gsonString ,GameMap.class); 
        if (gameMap.data != null)       // Unity compatibility?
        {
            gameMap.mapSizeX = gameMap.width;
            gameMap.mapSizeY = gameMap.height;
            gameMap.map = new short[gameMap.width][gameMap.height]; 
            for (int x = 0; x < gameMap.width; x++)
            {
                for (int y = 0; y < gameMap.height; y++)
                {
                    long icon = Long.max(0, (gameMap.data[x + y * gameMap.width] & 0x7FFF) - 1);
                    gameMap.map[x][y] = (short) icon ;
                }
            }            
        }
        gameMap.data = null;    // remove old array
        return gameMap;
    }
    public boolean saveToFile(String fileName)
    {
        String jsonString = new Gson().toJson(this);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8")))
        {
            writer.write(jsonString);
            return true;
        } 
        catch (Exception e)
        {
            USVCMapEditorUtilities.infoBox("Cannot save file", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
}
