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
*  This program really needs a clean-up, bugfix, etc. 
*/
package uchipgamemapeditor;

import com.google.gson.Gson;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import static uchipgamemapeditor.USVCMapEditorUtilities.createFileChooser;
import static uchipgamemapeditor.USVCMapEditorUtilities.infoBox;
import static uchipgamemapeditor.USVCMapEditorUtilities.loadImage;
import static uchipgamemapeditor.USVCMapEditorUtilities.questionBox;

public class MapEditorMainFrame extends javax.swing.JFrame
{
    final static boolean DEBUG = true;
    int [] tileProps;       // tile properties
    UndoableState undoableState = new UndoableState();  // for undo/redo operations
    //
    final int MAX_NUMBER_OF_SPRITES = 127; // the number of sprites is always 127 at most
    final int MAX_NUMBER_OF_WAYPOINTS = 65535; 
    int nTilesX, nTilesY;       // number of tiles in the tile image along x and y.
    // tile size X,Y
    int tileSizeX = 16;         
    int tileSizeY = 16;
    // Painters for map and tile window, currentTile, sprite and current sprite.
    TileMapPainter mapPainter;
    TileMapPainter tilePainter;
    TileMapPainter currentTilePainter;
    TileMapPainter spritePainter;
    TileMapPainter currentSpritePainter;
    // 
    JPanel jPanelMapArea;
    JPanel jPanelTileArea;
    JPanel jPanelSpriteArea;
    // Sprites
    SpriteCollection sprites;
    // current operation and selected tiles 
    int currentPlaceOperation = UndoableState.PLACE_TILE;
    int currentSelectedSpriteImage = 0;
    int currentSelectedTile = 0;
    int currentSelectedTileX1 = 0;
    int currentSelectedTileX2 = 0;
    int currentSelectedTileY1 = 0;
    int currentSelectedTileY2 = 0;
    int [][] currentTileBlock;
    // uSVC RGB tables.
    static final byte [] uChipVGAred = new byte[256];
    static final byte [] uChipVGAgreen = new byte[256];
    static final byte [] uChipVGAblue = new byte[256];
    //
    int screenX = 10;       // number of tiles displayable on screen in X
    int screenY = 8;        // number of tiles displayable on screen in Y
    // instances of other built-in editors.
    PaletteRemapEditor paletteRemapEditor;
    SpriteSetBuilder spriteSetBuilder;
    TileProperties [] listOfTileProperties;
    // fileChooser names. Global so that they remember the last selected file.
    String mapFileName = "";
    String exportMapToCfileFileName = "";
    String exportTilesToCfileFileName = "";
    String createTileSetFromDirectoryFileName = "";
    String loadTilePictureFileName = "";
    String loadSpriteSetFileName = "";   
    String exportSpriteListFileName = "";
    String exportWaypointListFileName = "";
    String exportSpriteDataFileName = "";
    String tilePropsFileName = "";
    String exportTilePropsNoDuplicatesFileName = "";
    String exportTrigTableFileName = "";
    String spritePositionFileName = "";
    String waypointListFileName = "";
    String usvcPackagerBinaryFileName = "";
    String usvcPackagerMetaFileName = "";
    String usvcPackagerImageFileName = "";
    String usvcPackagerOutputFileName = "";
    String usvcPackagerDirectory = "";
    String wavFileName = "";
    String convertedWavFileName = "";
    String wavVariableName = "";
    // 
    MidiConvertOptions midiConvertOptions = new MidiConvertOptions();
    //
    // REMOVE ME ***
    File exportPicToUSVCImageFile;
    File exportPicToUSVCSaveFile;
    // 
    void setTileSize(int x, int y)
    {
        if (x == 0 || y == 0)       // just to avoid errors
            return;
        tileSizeX = x;
        tileSizeY = y;
    } 
    private void calculate8BitPalette()
    {
        // build uChipVGA palette.
        // Notice: given the tile mode, rounting, and resistor values, let's say that p0..7 is the our byte. This is routed:
        // p0 = r0
        // p1 = r2
        // p2 = r1
        // p3 = b0
        // p4 = g0
        // p5 = b1
        // p6 = g1
        // p7 = g2
        // due to resistor values, these are the approximate levels for a 3-bit component (red, green): 0, 32, 71, 103, 151, 184, 222, 255
        // and these are for a 2-bit component (blue): 0, 86, 180, 255
        // Note: we COULD use an IndexColorModel, but it is buggy as hell, and there is no way to turn off dither.
        final int [] redGreenThresholds =  { (0 + 32)/2, (32 +71) / 2, (71 +103) / 2, (103+151) / 2, (151 + 184) /2 , (184 + 222) / 2, (222 + 255) / 2};
        final int [] blueThresholds = {(0 + 86) / 2, (86+ 180) / 2, (180 + 255) / 2};
        for (int i = 0; i < 256; i++)
        {

            if ( i > redGreenThresholds[6])
            {
                uChipVGAred[i] = 7;
                uChipVGAgreen[i] = 7;
            }
            else if ( i > redGreenThresholds[5])
            {
                uChipVGAred[i] = 6;
                uChipVGAgreen[i] = 6;
            }                            
            else if ( i > redGreenThresholds[4])
            {
                uChipVGAred[i] = 5;
                uChipVGAgreen[i] = 5;
            } 
            else if ( i > redGreenThresholds[3])
            {
                uChipVGAred[i] = 4;
                uChipVGAgreen[i] = 4;
            } 
            else if ( i > redGreenThresholds[2])
            {
                uChipVGAred[i] = 3;
                uChipVGAgreen[i] = 3;
            } 
            else if ( i > redGreenThresholds[1])
            {
                uChipVGAred[i] = 2;
                uChipVGAgreen[i] = 2;
            } 
            else if ( i > redGreenThresholds[0])
            {
                uChipVGAred[i] = 1;
                uChipVGAgreen[i] = 1;
            } 
            else 
            {
                uChipVGAred[i] = 0;
                uChipVGAgreen[i] = 0;
            } 
            if ( i > blueThresholds[2])
            {
                uChipVGAblue[i] = 3;
            }
            else if ( i > blueThresholds[1])
            {
                uChipVGAblue[i] = 2;
            }
            else if ( i > blueThresholds[0])
            {
                uChipVGAblue[i] = 1;
            }
            else 
            {
                uChipVGAblue[i] = 0;
            }
        }         
    }
    /*
     * Creates new form MapEditorMainFrame
     */
    public MapEditorMainFrame()
    {
        // common uChipVGA color table
        calculate8BitPalette();
        // init undo list operations.
        undoableState.spritePositionList = new  ArrayList <SpritePosition> ();
        undoableState.spritePositionList.add(new SpritePosition (16,16, 0));
        undoableState.waypointList = new ArrayList <Waypoint> ();
        undoableState.waypointList.add(new Waypoint(16, 16, 16, 16, 0, 0));
        initComponents();
        // register undo actions
        Action undoAction = new AbstractAction() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                undo();
            }
        };
        Action redoAction = new AbstractAction() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                redo();
            }
        };
        getRootPane().getActionMap().put("undo", undoAction);
        getRootPane().getActionMap().put("redo", redoAction);
        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "undo");
        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "redo");
        // set the UI current place operation stat. currentPlaceOperation is initialized to be "place tile".
        setCurrentPlaceOperation(currentPlaceOperation);
        sprites = new SpriteCollection(); // create an empty sprite list.
        // init tile area
        tilePainter = new TileMapPainter();
        tilePainter.bufferedImage = new BufferedImage(16,16,BufferedImage.TYPE_4BYTE_ABGR) ;  // create an empty 16x16 tile.
        jPanelTileArea = new JPanel();
        jPanelTileArea.setLayout(new BorderLayout());
        tilePainter.setPreferredSize( tilePainter.bufferedImage.getWidth(),tilePainter.bufferedImage.getHeight());
        jPanelTileArea.setPreferredSize(new Dimension( tilePainter.bufferedImage.getWidth(),tilePainter.bufferedImage.getHeight()));
        jPanelTileArea.add(tilePainter);
        jScrollPaneTileArea.setViewportView(jPanelTileArea);
        TilesMouseListener tml = new TilesMouseListener();
        jPanelTileArea.addMouseMotionListener(tml);
        jPanelTileArea.addMouseListener(tml);
        nTilesX = tilePainter.bufferedImage.getWidth() / tileSizeX;
        nTilesY = tilePainter.bufferedImage.getHeight() / tileSizeY;
        tilePainter.setNumberOfTiles(nTilesX, nTilesY);
        tileProps = tilePainter.getTileProperties();
        setTileWindowZoom();
        // initial game map
        undoableState.map = new GameMap(100,100);
        // init map area
        mapPainter = new TileMapPainter();
        mapPainter.screenX = screenX;
        mapPainter.screenY = screenY;
        mapPainter.showScreenSize = true;
        updateMapPainterMapData();
        jPanelMapArea = new JPanel();
        jPanelMapArea.setLayout(new BorderLayout());
        mapPainter.setPreferredSize(undoableState.map.getSizeX() * tileSizeX ,undoableState.map.getSizeY() * tileSizeY);
        jPanelMapArea.setPreferredSize(new Dimension(undoableState.map.getSizeX() * tileSizeX ,undoableState.map.getSizeY() * tileSizeY));
        jPanelMapArea.add(mapPainter);
        MapMouseListener mml = new MapMouseListener();
        jPanelMapArea.addMouseListener(mml);
        jPanelMapArea.addMouseMotionListener (mml);
        jScrollPaneMapArea.setViewportView(jPanelMapArea);
        drawMap();
        setMapWindowZoom();
        // init current Tile
        currentTilePainter = new TileMapPainter();
        currentTilePainter.bufferedImage = getTileBlockByCoordinates(currentSelectedTileX1, currentSelectedTileY1, currentSelectedTileX2, currentSelectedTileY2, true);
        jPanelCurrentTile.add(currentTilePainter);
        redrawCurrentTilePanel();
        spritePainter = new TileMapPainter();
        spritePainter.showScreenSize = false;
        spritePainter.bufferedImage = new BufferedImage(32, 32 ,BufferedImage.TYPE_INT_RGB);
        jPanelSpriteArea = new JPanel();
        jPanelSpriteArea.setLayout(new BorderLayout());
        spritePainter.setPreferredSize(32 ,32);
        jPanelSpriteArea.setPreferredSize(new Dimension(32 ,32));
        jPanelSpriteArea.add(spritePainter);
        // 
        jPanelSpriteArea.addMouseListener(new SpriteMouseListener());
        jScrollPaneSpriteArea.setViewportView(jPanelSpriteArea);
        spritePainter.repaint();
        //
        currentSpritePainter = new TileMapPainter();
        currentSpritePainter.bufferedImage = new BufferedImage( jPanelCurrentSprite.getWidth(), jPanelCurrentSprite.getHeight() ,BufferedImage.TYPE_4BYTE_ABGR);
        jPanelCurrentSprite.add(currentSpritePainter);
        currentSpritePainter.repaint();
        // set some default tile properties
        listOfTileProperties = new TileProperties[6];
        listOfTileProperties[0] = new TileProperties("Normal", "TILEPROP_NORMAL", 0);
        listOfTileProperties[1] = new TileProperties("Solid", "TILEPROP_SOLID", 0xFF0000);
        listOfTileProperties[2] = new TileProperties("Jumpable Over", "TILEPROP_JUMPABLEOVER", 0x0000FF);
        listOfTileProperties[3] = new TileProperties("Solid where not transparent", "TILEPROP_COMPLEX", 0x00FF00);
        listOfTileProperties[4] = new TileProperties("Ladder", "TILEPROP_LADDER", 0xFF00FF);
        listOfTileProperties[5] = new TileProperties("Harmful", "TILEPROP_HARM", 0x00FFFF);
        setListOfTileProperties(listOfTileProperties);
    }
    boolean deleteWaypoint(int waypointNumber)
    {
        // boundary check
        // note: there should always be at least one wp
        if (waypointNumber >= undoableState.waypointList.size() || waypointNumber < 0 || undoableState.waypointList.size() < 2) 
        {
           return false;
        }
        // TODO fixme: we must try to avoid breaking a linked list.
        undoableState.waypointList.remove(waypointNumber);
        for (int i = 0; i < undoableState.waypointList.size(); i++)
        {
            Waypoint wp = undoableState.waypointList.get(i);
            int nWp1 = wp.nextWaypoint1;
            int nWp2 = wp.nextWaypoint2;
            // boundary check;
            if (nWp1 < 0)
                nWp1 = 0;
            if (nWp2 < 0)
                nWp2 = 0;
            if (nWp1 >= undoableState.waypointList.size())
                nWp1 = 0;
            if (nWp2 >= undoableState.waypointList.size())
                nWp2 = 0;
            wp.nextWaypoint1 = nWp1;
            wp.nextWaypoint2 = nWp2;
        }
        return true;
    }
    void setCurrentPlaceOperation(int placeOp)
    {
        currentPlaceOperation = placeOp;
        switch (placeOp)
        {
            case UndoableState.PLACE_SPRITE:
                jPanelCurrentTile.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
                jPanelCurrentSprite.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                jToggleButtonPlaceWaypoints.setSelected(false);
                break;
            case UndoableState.PLACE_TILE:
                jPanelCurrentTile.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
                jPanelCurrentSprite.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
                jToggleButtonPlaceWaypoints.setSelected(false);
                break;
            case UndoableState.PLACE_WAYPOINT:
                jPanelCurrentTile.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
                jPanelCurrentSprite.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
                jToggleButtonPlaceWaypoints.setSelected(true);
                break;
        }
                
    }
    private BufferedImage getTileByIndex(int index)
    {
        return getTileByIndexFromImage(index & GameMap.TILE_MASK, nTilesX,  tilePainter.bufferedImage); 
    }
    private BufferedImage getTileByIndexFromImage (int index, int tilesPerRow,  BufferedImage image)
    {
        index = index & GameMap.TILE_MASK;
        int column =  index % tilesPerRow;
        int row = index / tilesPerRow;
        return image.getSubimage(column * tileSizeX, row*tileSizeY, tileSizeX, tileSizeY);        
    }
    private BufferedImage getTileBlockByCoordinates(int x1, int y1, int x2, int y2, boolean fromTileWindow)
    {
        int maxX, maxY;
        if (fromTileWindow)
        {
            maxX = nTilesX;
            maxY = nTilesY;
        }
        else
        {
            maxX = undoableState.map.getSizeX();
            maxY = undoableState.map.getSizeY();
        }
        // let's make some order
        if (y2 < y1)
        {
            int t = y1;
            y1 = y2;
            y2 = t;
        }
        if (x2 < x1)
        {
            int t = x1;
            x1 = x2;
            x2 = t;
        }
        // now range check
        if (x1 < 0)
            x1 = 0;
        if (x2 < 0)
            x2 = 0;
        if (y1 < 0)
            y1 = 0;
        if (y2 < 0)
            y2 = 0;
        if (x1 >= maxX)
        {
            x1 = maxX - 1;
        }
        if (x2 >= maxX)
        {
            x2 = maxX - 1;
        }
        if (y1 >= maxY)
        {
            y1 = maxY - 1;
        }
        if (y2 >= maxY)
        {
            y2 = maxY - 1;
        }
        // based on x1, x2, y1, y2, let's create an array of tiles
        currentTileBlock = new int[y2 - y1 + 1][x2 - x1 + 1];
        for (int y = y1; y <= y2; y++)
        {
            for (int x = x1; x <= x2; x++)
            {
                if (fromTileWindow)
                    currentTileBlock[y - y1][x - x1] = getTileIndexByNxy(x, y);
                else
                    currentTileBlock[y - y1][x - x1] = undoableState.map.getTile(x, y);
            }
        }
        if (fromTileWindow)          
            return tilePainter.bufferedImage.getSubimage(x1 * tileSizeX, y1 * tileSizeY, tileSizeX*(1 + x2 - x1), (1 + y2 - y1) * tileSizeY);
        else
        {
            BufferedImage image = new BufferedImage( tileSizeX * (x2 - x1 + 1), tileSizeY * (y2 - y1 + 1), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            for (int y = y1; y <= y2; y++)
            {
                for (int x = x1; x <= x2; x++)
                {
                    g2d.drawImage(getTileByIndex(currentTileBlock[y - y1][x - x1]), (x - x1) * tileSizeX, (y - y1) * tileSizeY, null);
                }
            }         
            g2d.dispose();
            return image;
        }
    }    
    private int getTileIndexByNxy(int nx, int ny)
    {
        return ny * nTilesX + nx;
    }
    private void drawMap()
    {
        Graphics g = mapPainter.bufferedImage.getGraphics();
        int mapSizeX = undoableState.map.getSizeX();
        int mapSizeY = undoableState.map.getSizeY();
        g.clearRect(0, 0, tileSizeX * mapSizeX, tileSizeY * mapSizeY);
        for (int x = 0; x < mapSizeX; x++)
        {
            for (int y = 0; y < mapSizeY; y++)
            {
                BufferedImage img = getTileByIndex(undoableState.map.getTile(x,y));
                g.drawImage(img, x * tileSizeX, y * tileSizeY,  tileSizeX, tileSizeY, null);
            }
        }
        g.dispose();
        drawSpritesAndWaypoints();
        System.gc();    // hint the jvm that we might want to do a garbage collection to prevent the memory from growing too much.
    }
    void drawWaypoints()
    {
        final int arrowLength = 8;
        final double arrowSemiAngle = 30.0 / 180.0 * Math.PI;
        if (jCheckBoxShowWaypoints.isSelected())
        {
            Graphics2D g = (Graphics2D) mapPainter.bufferedImage.getGraphics();
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            try
            {
                // this might fail when data must be initialized
                for (int i = 0; i < undoableState.waypointList.size(); i++)
                {
                    Waypoint wp = undoableState.waypointList.get(i);
                    if ((Integer) jSpinnerCurrentWaypoint.getValue() == i)
                    {
                        g.setPaint(Color.red);
                    }
                    else
                    {
                        g.setPaint(Color.white);
                    }
                    g.drawOval(wp.x - wp.rx, wp.y - wp.ry, 2 * wp.rx - 1, 2 * wp.ry - 1);
                    if (jCheckBoxShowNextWaypoints.isSelected())
                    {
                        int n;
                        n = wp.nextWaypoint1;
                        if (n >= 0 && n < undoableState.waypointList.size())
                        {
                            Waypoint wpn = undoableState.waypointList.get(n);
                            g.setPaint(Color.yellow);
                            g.drawLine(wp.x, wp.y, wpn.x, wpn.y);                                                        
                            int dx = wpn.x - wp.x;
                            int dy = wpn.y - wp.y;
                            if (dx != 0 || dy != 0)
                            {
                                double angle = Math.atan2(dy, dx);
                                g.drawLine(wpn.x, wpn.y, (int) (wpn.x - Math.cos(angle - arrowSemiAngle) * arrowLength), (int) (wpn.y - Math.sin(angle - arrowSemiAngle) * arrowLength));
                                g.drawLine(wpn.x, wpn.y, (int) (wpn.x - Math.cos(angle + arrowSemiAngle) * arrowLength), (int) (wpn.y - Math.sin(angle + arrowSemiAngle) * arrowLength));
                            }
                        }
                        n = wp.nextWaypoint2;
                        if (n >= 0 && n < undoableState.waypointList.size())
                        {
                            Waypoint wpn = undoableState.waypointList.get(n);
                            g.setPaint(Color.cyan);
                            g.drawLine(wp.x, wp.y, wpn.x, wpn.y); 
                            int dx = wpn.x - wp.x;
                            int dy = wpn.y - wp.y;
                            if (dx != 0 || dy != 0)
                            {
                                double angle = Math.atan2(dy, dx);
                                g.drawLine(wpn.x, wpn.y, (int) (wpn.x - Math.cos(angle - arrowSemiAngle) * arrowLength), (int) (wpn.y - Math.sin(angle - arrowSemiAngle) * arrowLength));
                                g.drawLine(wpn.x, wpn.y, (int) (wpn.x - Math.cos(angle + arrowSemiAngle) * arrowLength), (int) (wpn.y - Math.sin(angle + arrowSemiAngle) * arrowLength));
                            }                            
                        }
                    }
                    if (jCheckBoxShowWaypointNumbers.isSelected())
                    {
                        if ((Integer) jSpinnerCurrentWaypoint.getValue() == i)
                            g.setPaint(Color.red);
                        else
                            g.setPaint(Color.white);
                        g.setBackground(Color.black);
                        String s = "" + i;
                        FontMetrics fm = g.getFontMetrics();
                        int x = wp.x + fm.stringWidth(s) / 2;
                        int y = wp.y + fm.getAscent() / 2;
                        g.clearRect(x, y - fm.getAscent() , fm.stringWidth(s), fm.getAscent());
                        g.drawString(s, x, y);                        
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                // do not bother
            }
            g.dispose();
        }                
    }
    void drawSpritesAndWaypoints()
    {
        if (jCheckBoxShowSprites.isSelected())
        {
            Graphics2D g = (Graphics2D) mapPainter.bufferedImage.getGraphics();
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            try
            {
                // this might fail when data must be initialized
                for (int i = 0; i < undoableState.spritePositionList.size(); i++)
                {
                    SpritePosition sp = undoableState.spritePositionList.get(i);
                    if (sprites.entityArray.size() <= sp.entityNumber)  // avoid init problems
                        continue;
                    Entity ent = sprites.entityArray.get(sp.entityNumber);
                    BufferedImage sImg = ent.entityDisplayImage;
                    g.drawImage(sImg,sp.x - ent.entityDisplayHorizontalOffset, sp.y - ent.entityDisplayVerticalOffset,sImg.getWidth() , sImg.getHeight(), null);
                    if (jCheckBoxShowSpriteNumbers.isSelected())
                    {
                        if ((Integer) jSpinnerCurrentSprite.getValue() == i)
                            g.setPaint(Color.red);
                        else
                            g.setPaint(Color.white);
                        //
                        g.setBackground(Color.black);
                        String s = "" + i;
                        FontMetrics fm = g.getFontMetrics();
                        int x = sp.x + fm.stringWidth(s) / 2;
                        int y = sp.y + fm.getAscent() / 2;
                        g.clearRect(x, y - fm.getAscent() , fm.stringWidth(s), fm.getAscent());
                        g.drawString(s, x, y);                        
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                // do not bother
            }
            g.dispose();
        }        
        drawWaypoints();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jLabel5 = new javax.swing.JLabel();
        jPanelCurrentTile = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jSpinnerCurrentSprite = new javax.swing.JSpinner();
        jButtonFindSprite = new javax.swing.JButton();
        jPanelCurrentSprite = new javax.swing.JPanel();
        jScrollPaneSpriteArea = new javax.swing.JScrollPane();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jCheckBoxShowSprites = new javax.swing.JCheckBox();
        jCheckBoxShowSpriteNumbers = new javax.swing.JCheckBox();
        jButtonDeleteSprite = new javax.swing.JButton();
        jCheckBoxShowTileType = new javax.swing.JCheckBox();
        jCheckBoxSetTileType = new javax.swing.JCheckBox();
        jComboBoxTileType = new javax.swing.JComboBox<String>();
        jCheckBoxAutoIncrementSpriteNumber = new javax.swing.JCheckBox();
        jButtonEditType = new javax.swing.JButton();
        jCheckBoxShowTilePriority = new javax.swing.JCheckBox();
        jCheckBoxSetTilePriority = new javax.swing.JCheckBox();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        jInternalFrameMap = new javax.swing.JInternalFrame();
        jScrollPaneMapArea = new javax.swing.JScrollPane();
        jLabel1 = new javax.swing.JLabel();
        jSliderZoomMap = new javax.swing.JSlider();
        jInternalFrameTiles = new javax.swing.JInternalFrame();
        jScrollPaneTileArea = new javax.swing.JScrollPane();
        jLabel4 = new javax.swing.JLabel();
        jSliderZoomTiles = new javax.swing.JSlider();
        jInternalFrame1 = new javax.swing.JInternalFrame();
        jCheckBoxShowWaypoints = new javax.swing.JCheckBox();
        jCheckBoxShowWaypointNumbers = new javax.swing.JCheckBox();
        jCheckBoxAutoIncrementWaypointNumber = new javax.swing.JCheckBox();
        jCheckBoxShowNextWaypoints = new javax.swing.JCheckBox();
        jToggleButtonPlaceWaypoints = new javax.swing.JToggleButton();
        jSpinnerNextWaypoint2 = new javax.swing.JSpinner();
        jLabel14 = new javax.swing.JLabel();
        jSpinnerNextWaypoint1 = new javax.swing.JSpinner();
        jButtonDeleteWaypoint = new javax.swing.JButton();
        jButtonFindWaypoint = new javax.swing.JButton();
        jSpinnerCurrentWaypoint = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jSpinnerWaypointSizeX = new javax.swing.JSpinner();
        jSpinnerWaypointSizeY = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemLoadMap = new javax.swing.JMenuItem();
        jMenuItemSaveMap = new javax.swing.JMenuItem();
        jMenuItemSaveMapAs = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExportMapToCfile = new javax.swing.JMenuItem();
        jMenuItemExportTilesToCfile = new javax.swing.JMenuItem();
        jMenuItemExportTilesToCFileNoDuplicates = new javax.swing.JMenuItem();
        jMenuItemExportTilePropsNoDuplicate = new javax.swing.JMenuItem();
        jCheckBoxMenuItemMarkSemiTransparent = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExportSpriteData = new javax.swing.JMenuItem();
        jMenuItemExportSpriteList = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExportWaypointsToCFile = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuItemUndo = new javax.swing.JMenuItem();
        jMenuItemRedo = new javax.swing.JMenuItem();
        jMenuCheckBoxItemShowTileSelectionOnMap = new javax.swing.JMenu();
        jCheckBoxMenuItemShowGridOnMap = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemShowGridOnTiles = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemShowScreenSize = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuItemShowTileSelectionOnMap = new javax.swing.JCheckBoxMenuItem();
        jMenuMap = new javax.swing.JMenu();
        jMenuItemChangeMapAndScreenSizes = new javax.swing.JMenuItem();
        jMenuTiles = new javax.swing.JMenu();
        jMenuItemCreateTileSetFromDirectory = new javax.swing.JMenuItem();
        jMenuItemLoadTilePicture = new javax.swing.JMenuItem();
        jMenuItemLoadTileProps = new javax.swing.JMenuItem();
        jMenuItemSaveTileProps = new javax.swing.JMenuItem();
        jMenuSprites = new javax.swing.JMenu();
        jMenuItemLoadSpriteSet = new javax.swing.JMenuItem();
        jMenuItemCreateSpriteSet = new javax.swing.JMenuItem();
        jMenuItemLoadSpritePositions = new javax.swing.JMenuItem();
        jMenuItemSaveSpritePositions = new javax.swing.JMenuItem();
        jMenuUtilities = new javax.swing.JMenu();
        jMenuItemExportTrigTable = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItemExportWav = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuWaypoints = new javax.swing.JMenu();
        jMenuItemLoadWaypoints = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuSpecial = new javax.swing.JMenu();
        jMenuItemBackgroundRainbowEditor = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("uChip game map editor");
        setMinimumSize(new java.awt.Dimension(320, 240));
        addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentResized(java.awt.event.ComponentEvent evt)
            {
                formComponentResized(evt);
            }
        });

        jLabel5.setText("Current Tile");

        jPanelCurrentTile.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanelCurrentTile.setMaximumSize(new java.awt.Dimension(100, 100));
        jPanelCurrentTile.setLayout(new java.awt.BorderLayout(100, 100));

        jLabel6.setText("Current Sprite:");

        jSpinnerCurrentSprite.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1, 1));
        jSpinnerCurrentSprite.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerCurrentSpriteStateChanged(evt);
            }
        });

        jButtonFindSprite.setText("Find Sprite");
        jButtonFindSprite.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonFindSpriteActionPerformed(evt);
            }
        });

        jPanelCurrentSprite.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanelCurrentSprite.setMaximumSize(new java.awt.Dimension(100, 100));
        jPanelCurrentSprite.setLayout(new java.awt.BorderLayout(100, 100));

        jLabel7.setText("Sprites:");

        jLabel8.setText("Current Sprite:");

        jCheckBoxShowSprites.setSelected(true);
        jCheckBoxShowSprites.setText("Show Sprites on Map");
        jCheckBoxShowSprites.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxShowSpritesActionPerformed(evt);
            }
        });

        jCheckBoxShowSpriteNumbers.setSelected(true);
        jCheckBoxShowSpriteNumbers.setText("Show Sprite Numbers");
        jCheckBoxShowSpriteNumbers.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxShowSpriteNumbersActionPerformed(evt);
            }
        });

        jButtonDeleteSprite.setText("Delete Sprite");
        jButtonDeleteSprite.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonDeleteSpriteActionPerformed(evt);
            }
        });

        jCheckBoxShowTileType.setText("Show Tile Types");
        jCheckBoxShowTileType.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxShowTileTypeActionPerformed(evt);
            }
        });

        jCheckBoxSetTileType.setText("Set Tile Type");

        jComboBoxTileType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "normal", "solid", "jumpableOver", "solid where not transparent", "ladder", "harmful" }));

        jCheckBoxAutoIncrementSpriteNumber.setText("Automatically increment Sprite Number");

        jButtonEditType.setText("Edit Types");
        jButtonEditType.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonEditTypeActionPerformed(evt);
            }
        });

        jCheckBoxShowTilePriority.setText("Show Tile Priority");
        jCheckBoxShowTilePriority.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxShowTilePriorityActionPerformed(evt);
            }
        });

        jCheckBoxSetTilePriority.setText("Set/delete priority (left/right click)");
        jCheckBoxSetTilePriority.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxSetTilePriorityActionPerformed(evt);
            }
        });

        jDesktopPane1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jInternalFrameMap.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        jInternalFrameMap.setIconifiable(true);
        jInternalFrameMap.setMaximizable(true);
        jInternalFrameMap.setResizable(true);
        jInternalFrameMap.setTitle("Level Map");
        jInternalFrameMap.setNormalBounds(new java.awt.Rectangle(10, 10, 620, 550));
        jInternalFrameMap.setVisible(true);
        jInternalFrameMap.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentResized(java.awt.event.ComponentEvent evt)
            {
                jInternalFrameMapComponentResized(evt);
            }
        });

        jScrollPaneMapArea.setToolTipText("Click/drag: draw current Tile. Shift+Click and drag: select block from map. Ctrl+click and drag: fill area with repeated copies of current block.");
        jScrollPaneMapArea.setMaximumSize(new java.awt.Dimension(500, 32767));

        jLabel1.setText("Zoom:");

        jSliderZoomMap.setMinimum(1);
        jSliderZoomMap.setValue(10);
        jSliderZoomMap.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSliderZoomMapStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jInternalFrameMapLayout = new javax.swing.GroupLayout(jInternalFrameMap.getContentPane());
        jInternalFrameMap.getContentPane().setLayout(jInternalFrameMapLayout);
        jInternalFrameMapLayout.setHorizontalGroup(
            jInternalFrameMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInternalFrameMapLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInternalFrameMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneMapArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jInternalFrameMapLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSliderZoomMap, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jInternalFrameMapLayout.setVerticalGroup(
            jInternalFrameMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInternalFrameMapLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneMapArea, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jInternalFrameMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1)
                    .addComponent(jSliderZoomMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8))
        );

        jDesktopPane1.add(jInternalFrameMap);
        jInternalFrameMap.setBounds(0, 0, 430, 350);

        jInternalFrameTiles.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        jInternalFrameTiles.setIconifiable(true);
        jInternalFrameTiles.setMaximizable(true);
        jInternalFrameTiles.setResizable(true);
        jInternalFrameTiles.setTitle("Tiles");
        jInternalFrameTiles.setName(""); // NOI18N
        jInternalFrameTiles.setNormalBounds(new java.awt.Rectangle(500, 444, 500, 444));
        jInternalFrameTiles.setVisible(true);
        jInternalFrameTiles.addComponentListener(new java.awt.event.ComponentAdapter()
        {
            public void componentResized(java.awt.event.ComponentEvent evt)
            {
                jInternalFrameTilesComponentResized(evt);
            }
        });

        jScrollPaneTileArea.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabel4.setText("Zoom:");

        jSliderZoomTiles.setMinimum(1);
        jSliderZoomTiles.setValue(10);
        jSliderZoomTiles.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSliderZoomTilesStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jInternalFrameTilesLayout = new javax.swing.GroupLayout(jInternalFrameTiles.getContentPane());
        jInternalFrameTiles.getContentPane().setLayout(jInternalFrameTilesLayout);
        jInternalFrameTilesLayout.setHorizontalGroup(
            jInternalFrameTilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInternalFrameTilesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInternalFrameTilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneTileArea)
                    .addGroup(jInternalFrameTilesLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSliderZoomTiles, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jInternalFrameTilesLayout.setVerticalGroup(
            jInternalFrameTilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInternalFrameTilesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneTileArea, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jInternalFrameTilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(jSliderZoomTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jDesktopPane1.add(jInternalFrameTiles);
        jInternalFrameTiles.setBounds(430, 0, 500, 350);

        jInternalFrame1.setIconifiable(true);
        jInternalFrame1.setMaximizable(true);
        jInternalFrame1.setTitle("Waypoints");
        jInternalFrame1.setVisible(true);

        jCheckBoxShowWaypoints.setText("Show Waypoints on Map");
        jCheckBoxShowWaypoints.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxShowWaypointsActionPerformed(evt);
            }
        });

        jCheckBoxShowWaypointNumbers.setText("Show Waypoint Numbers");
        jCheckBoxShowWaypointNumbers.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxShowWaypointNumbersActionPerformed(evt);
            }
        });

        jCheckBoxAutoIncrementWaypointNumber.setText("Automatically Increment  Waypoint Number");

        jCheckBoxShowNextWaypoints.setText("Show connections");
        jCheckBoxShowNextWaypoints.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jCheckBoxShowNextWaypointsStateChanged(evt);
            }
        });

        jToggleButtonPlaceWaypoints.setText("Place Waypoints");
        jToggleButtonPlaceWaypoints.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jToggleButtonPlaceWaypointsActionPerformed(evt);
            }
        });

        jSpinnerNextWaypoint2.setModel(new javax.swing.SpinnerNumberModel(0, 0, 0, 1));
        jSpinnerNextWaypoint2.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerNextWaypoint2StateChanged(evt);
            }
        });

        jLabel14.setText("Next Waypoint 2:");

        jSpinnerNextWaypoint1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 0, 1));
        jSpinnerNextWaypoint1.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerNextWaypoint1StateChanged(evt);
            }
        });

        jButtonDeleteWaypoint.setText("Delete Waypoint");
        jButtonDeleteWaypoint.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonDeleteWaypointActionPerformed(evt);
            }
        });

        jButtonFindWaypoint.setText("Find Waypoint");
        jButtonFindWaypoint.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonFindWaypointActionPerformed(evt);
            }
        });

        jSpinnerCurrentWaypoint.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1, 1));
        jSpinnerCurrentWaypoint.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerCurrentWaypointStateChanged(evt);
            }
        });

        jLabel11.setText("Current Waypoint:");

        jLabel12.setText("Waypoint Size: (x by y)");

        jSpinnerWaypointSizeX.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(16), Integer.valueOf(1), null, Integer.valueOf(1)));
        jSpinnerWaypointSizeX.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerWaypointSizeXStateChanged(evt);
            }
        });

        jSpinnerWaypointSizeY.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(16), Integer.valueOf(1), null, Integer.valueOf(1)));
        jSpinnerWaypointSizeY.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerWaypointSizeYStateChanged(evt);
            }
        });

        jLabel13.setText("Next Waypoint 1:");

        javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
        jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
        jInternalFrame1Layout.setHorizontalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInternalFrame1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jInternalFrame1Layout.createSequentialGroup()
                        .addGroup(jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSpinnerCurrentWaypoint, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addGap(18, 18, 18)
                                .addComponent(jSpinnerWaypointSizeX, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                .addComponent(jButtonFindWaypoint)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonDeleteWaypoint))
                            .addGroup(jInternalFrame1Layout.createSequentialGroup()
                                .addComponent(jSpinnerWaypointSizeY, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSpinnerNextWaypoint1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSpinnerNextWaypoint2, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel11)
                    .addGroup(jInternalFrame1Layout.createSequentialGroup()
                        .addComponent(jCheckBoxShowWaypoints)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBoxShowWaypointNumbers)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBoxAutoIncrementWaypointNumber)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBoxShowNextWaypoints)))
                .addGap(18, 18, 18)
                .addComponent(jToggleButtonPlaceWaypoints)
                .addContainerGap())
        );
        jInternalFrame1Layout.setVerticalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInternalFrame1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jInternalFrame1Layout.createSequentialGroup()
                        .addGroup(jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBoxShowWaypoints)
                            .addComponent(jCheckBoxShowWaypointNumbers)
                            .addComponent(jCheckBoxAutoIncrementWaypointNumber)
                            .addComponent(jCheckBoxShowNextWaypoints))
                        .addGap(15, 15, 15)
                        .addGroup(jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jSpinnerCurrentWaypoint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11)
                            .addComponent(jButtonFindWaypoint)
                            .addComponent(jButtonDeleteWaypoint))
                        .addGap(15, 15, 15)
                        .addGroup(jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(jSpinnerNextWaypoint1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14)
                            .addComponent(jSpinnerNextWaypoint2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpinnerWaypointSizeY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpinnerWaypointSizeX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12)))
                    .addComponent(jToggleButtonPlaceWaypoints, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jDesktopPane1.add(jInternalFrame1);
        jInternalFrame1.setBounds(0, 360, 930, 150);

        jMenu1.setText("File");

        jMenuItemLoadMap.setText("Load Map");
        jMenuItemLoadMap.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemLoadMapActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemLoadMap);

        jMenuItemSaveMap.setText("Save Map");
        jMenuItemSaveMap.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemSaveMapActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemSaveMap);

        jMenuItemSaveMapAs.setText("Save Map As...");
        jMenuItemSaveMapAs.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemSaveMapAsActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemSaveMapAs);
        jMenu1.add(jSeparator1);

        jMenuItemExportMapToCfile.setText("Export Map To C File");
        jMenuItemExportMapToCfile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExportMapToCfileActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemExportMapToCfile);

        jMenuItemExportTilesToCfile.setText("Export Tiles To C File");
        jMenuItemExportTilesToCfile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExportTilesToCfileActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemExportTilesToCfile);

        jMenuItemExportTilesToCFileNoDuplicates.setText("Export Tiles To C File (no duplicate tiles)");
        jMenuItemExportTilesToCFileNoDuplicates.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExportTilesToCFileNoDuplicatesActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemExportTilesToCFileNoDuplicates);

        jMenuItemExportTilePropsNoDuplicate.setText("Export Tile Props To C File (no duplicate tiles)");
        jMenuItemExportTilePropsNoDuplicate.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExportTilePropsNoDuplicateActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemExportTilePropsNoDuplicate);

        jCheckBoxMenuItemMarkSemiTransparent.setSelected(true);
        jCheckBoxMenuItemMarkSemiTransparent.setText("Mark SemiTransparent Tiles");
        jMenu1.add(jCheckBoxMenuItemMarkSemiTransparent);
        jMenu1.add(jSeparator2);

        jMenuItemExportSpriteData.setText("Export Sprite Data To C File");
        jMenuItemExportSpriteData.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExportSpriteDataActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemExportSpriteData);

        jMenuItemExportSpriteList.setText("Export Sprite Positions To C File");
        jMenuItemExportSpriteList.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExportSpriteListActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemExportSpriteList);
        jMenu1.add(jSeparator3);

        jMenuItemExportWaypointsToCFile.setText("Export Waypoint To C File");
        jMenuItemExportWaypointsToCFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExportWaypointsToCFileActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemExportWaypointsToCFile);

        jMenuBar1.add(jMenu1);

        jMenuEdit.setText("Edit");

        jMenuItemUndo.setText("Undo");
        jMenuItemUndo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemUndoActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemUndo);

        jMenuItemRedo.setText("Redo");
        jMenuItemRedo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemRedoActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuItemRedo);

        jMenuBar1.add(jMenuEdit);

        jMenuCheckBoxItemShowTileSelectionOnMap.setText("View");

        jCheckBoxMenuItemShowGridOnMap.setSelected(true);
        jCheckBoxMenuItemShowGridOnMap.setText("Show Grid on Map");
        jCheckBoxMenuItemShowGridOnMap.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxMenuItemShowGridOnMapActionPerformed(evt);
            }
        });
        jMenuCheckBoxItemShowTileSelectionOnMap.add(jCheckBoxMenuItemShowGridOnMap);

        jCheckBoxMenuItemShowGridOnTiles.setSelected(true);
        jCheckBoxMenuItemShowGridOnTiles.setText("Show Grid on Tiles");
        jCheckBoxMenuItemShowGridOnTiles.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxMenuItemShowGridOnTilesActionPerformed(evt);
            }
        });
        jMenuCheckBoxItemShowTileSelectionOnMap.add(jCheckBoxMenuItemShowGridOnTiles);

        jCheckBoxMenuItemShowScreenSize.setSelected(true);
        jCheckBoxMenuItemShowScreenSize.setText("Show Screen Size on Map");
        jCheckBoxMenuItemShowScreenSize.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxMenuItemShowScreenSizeActionPerformed(evt);
            }
        });
        jMenuCheckBoxItemShowTileSelectionOnMap.add(jCheckBoxMenuItemShowScreenSize);

        jCheckBoxMenuItemShowTileSelectionOnMap.setSelected(true);
        jCheckBoxMenuItemShowTileSelectionOnMap.setText("Show Tile Selection On Map");
        jMenuCheckBoxItemShowTileSelectionOnMap.add(jCheckBoxMenuItemShowTileSelectionOnMap);

        jMenuBar1.add(jMenuCheckBoxItemShowTileSelectionOnMap);

        jMenuMap.setText("Map");

        jMenuItemChangeMapAndScreenSizes.setText("Set Map and Screen Size");
        jMenuItemChangeMapAndScreenSizes.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemChangeMapAndScreenSizesActionPerformed(evt);
            }
        });
        jMenuMap.add(jMenuItemChangeMapAndScreenSizes);

        jMenuBar1.add(jMenuMap);

        jMenuTiles.setText("Tiles");

        jMenuItemCreateTileSetFromDirectory.setText("Create tileset from directory");
        jMenuItemCreateTileSetFromDirectory.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemCreateTileSetFromDirectoryActionPerformed(evt);
            }
        });
        jMenuTiles.add(jMenuItemCreateTileSetFromDirectory);

        jMenuItemLoadTilePicture.setText("Load Tile Image");
        jMenuItemLoadTilePicture.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemLoadTilePictureActionPerformed(evt);
            }
        });
        jMenuTiles.add(jMenuItemLoadTilePicture);

        jMenuItemLoadTileProps.setText("Load Tile Properties");
        jMenuItemLoadTileProps.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemLoadTilePropsActionPerformed(evt);
            }
        });
        jMenuTiles.add(jMenuItemLoadTileProps);

        jMenuItemSaveTileProps.setText("Save Tile Properties");
        jMenuItemSaveTileProps.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemSaveTilePropsActionPerformed(evt);
            }
        });
        jMenuTiles.add(jMenuItemSaveTileProps);

        jMenuBar1.add(jMenuTiles);

        jMenuSprites.setText("Sprites");

        jMenuItemLoadSpriteSet.setText("Load Sprite Set");
        jMenuItemLoadSpriteSet.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemLoadSpriteSetActionPerformed(evt);
            }
        });
        jMenuSprites.add(jMenuItemLoadSpriteSet);

        jMenuItemCreateSpriteSet.setText("Open Sprite Set Creator");
        jMenuItemCreateSpriteSet.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemCreateSpriteSetActionPerformed(evt);
            }
        });
        jMenuSprites.add(jMenuItemCreateSpriteSet);

        jMenuItemLoadSpritePositions.setText("Load Sprite Positions");
        jMenuItemLoadSpritePositions.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemLoadSpritePositionsActionPerformed(evt);
            }
        });
        jMenuSprites.add(jMenuItemLoadSpritePositions);

        jMenuItemSaveSpritePositions.setText("Save Sprite Positions");
        jMenuItemSaveSpritePositions.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemSaveSpritePositionsActionPerformed(evt);
            }
        });
        jMenuSprites.add(jMenuItemSaveSpritePositions);

        jMenuBar1.add(jMenuSprites);

        jMenuUtilities.setText("Utilities");

        jMenuItemExportTrigTable.setText("Export Math Table");
        jMenuItemExportTrigTable.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExportTrigTableActionPerformed(evt);
            }
        });
        jMenuUtilities.add(jMenuItemExportTrigTable);

        jMenuItem3.setText("Export Midi To C");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenuUtilities.add(jMenuItem3);

        jMenuItemExportWav.setText("Export Wav to C");
        jMenuItemExportWav.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExportWavActionPerformed(evt);
            }
        });
        jMenuUtilities.add(jMenuItemExportWav);

        jMenuItem2.setText("Create USVC game file");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenuUtilities.add(jMenuItem2);

        jMenuBar1.add(jMenuUtilities);

        jMenuWaypoints.setText("Waypoints");

        jMenuItemLoadWaypoints.setText("Load Waypoints");
        jMenuItemLoadWaypoints.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemLoadWaypointsActionPerformed(evt);
            }
        });
        jMenuWaypoints.add(jMenuItemLoadWaypoints);

        jMenuItem1.setText("Save Waypoints");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenuWaypoints.add(jMenuItem1);

        jMenuBar1.add(jMenuWaypoints);

        jMenuSpecial.setText("Special");

        jMenuItemBackgroundRainbowEditor.setText("Palette Remap Editor");
        jMenuItemBackgroundRainbowEditor.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemBackgroundRainbowEditorActionPerformed(evt);
            }
        });
        jMenuSpecial.add(jMenuItemBackgroundRainbowEditor);

        jMenuBar1.add(jMenuSpecial);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jDesktopPane1)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jLabel5))
                            .addComponent(jPanelCurrentTile, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jPanelCurrentSprite, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPaneSpriteArea)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCheckBoxShowSprites)
                                .addGap(26, 26, 26)
                                .addComponent(jCheckBoxShowSpriteNumbers)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxAutoIncrementSpriteNumber)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSpinnerCurrentSprite, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonFindSprite, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonDeleteSprite))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCheckBoxShowTileType)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonEditType, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jCheckBoxSetTileType)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBoxTileType, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jCheckBoxShowTilePriority)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCheckBoxSetTilePriority)))
                        .addContainerGap(59, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jDesktopPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addGap(11, 11, 11)
                        .addComponent(jPanelCurrentTile, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPaneSpriteArea)
                            .addComponent(jPanelCurrentSprite, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxShowSprites)
                    .addComponent(jCheckBoxShowSpriteNumbers)
                    .addComponent(jCheckBoxAutoIncrementSpriteNumber)
                    .addComponent(jLabel8)
                    .addComponent(jSpinnerCurrentSprite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonFindSprite)
                    .addComponent(jButtonDeleteSprite))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxShowTileType)
                    .addComponent(jButtonEditType)
                    .addComponent(jCheckBoxSetTileType)
                    .addComponent(jComboBoxTileType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxShowTilePriority)
                    .addComponent(jCheckBoxSetTilePriority))
                .addGap(25, 25, 25))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    void setMapWindowZoom()
    {
        double zoom = jSliderZoomMap.getValue() / 10.0;
        int imageW = (int)(undoableState.map.getSizeX() * tileSizeX);
        int imageH = (int) (undoableState.map.getSizeY() * tileSizeY);
        // the jpanel cannot be smaller than the jScrollPane width or height
        int minW = jScrollPaneMapArea.getWidth();
        int minH = jScrollPaneMapArea.getHeight();
        //
        double minZoomX = ((double)minW)/imageW;
        double minZoomY = ((double)minH)/imageH;
        zoom = Double.max(zoom,Double.max(minZoomX,minZoomY));
        int w,h;
        w = (int)(imageW * zoom + 0.5);
        h = (int) (imageH * zoom + 0.5);
        jPanelMapArea.setPreferredSize(new Dimension(w,h));
        mapPainter.setPreferredSize(w,h);
        jScrollPaneMapArea.setViewportView(jPanelMapArea);
        mapPainter.repaint();         
    }
    void setTileWindowZoom()
    {
        double zoom = jSliderZoomTiles.getValue() / 10.0;
        int imageW = tilePainter.bufferedImage.getWidth();
        int imageH = tilePainter.bufferedImage.getHeight();
        // the jpanel cannot be smaller than the jScrollPane width or height
        int minW = jScrollPaneTileArea.getWidth();
        int minH = jScrollPaneTileArea.getHeight();
        //
        double minZoomX = ((double)minW)/imageW;
        double minZoomY = ((double)minH)/imageH;
        zoom = Double.max(zoom,Double.max(minZoomX,minZoomY));
        int w,h;
        w = (int)(imageW * zoom + 0.5);
        h = (int) (imageH * zoom + 0.5);
        jPanelTileArea.setPreferredSize(new Dimension(w,h));
        tilePainter.setPreferredSize(w,h);
        jScrollPaneTileArea.setViewportView(jPanelTileArea);
        tilePainter.repaint();        
    }
    private void jSliderZoomTilesStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSliderZoomTilesStateChanged
    {//GEN-HEADEREND:event_jSliderZoomTilesStateChanged
        setTileWindowZoom();

    }//GEN-LAST:event_jSliderZoomTilesStateChanged

    private void jMenuItemLoadMapActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemLoadMapActionPerformed
    {//GEN-HEADEREND:event_jMenuItemLoadMapActionPerformed
        try
        {
            undoableState.saveState(UndoableState.PLACE_TILE);
            final JFileChooser loadMapFC = new JFileChooser();
            loadMapFC.setSelectedFile(new File (mapFileName));
            if (loadMapFC.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                mapFileName = loadMapFC.getSelectedFile().getAbsolutePath();
                undoableState.map = GameMap.loadFormFile(mapFileName);
                updateMapPainterMapData();
                drawMap();
                setMapWindowZoom();
            }
        }
        catch (Exception e)
        {
            infoBox("Cannot load map!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemLoadMapActionPerformed
  
    private void jMenuItemSaveMapAsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemSaveMapAsActionPerformed
    {//GEN-HEADEREND:event_jMenuItemSaveMapAsActionPerformed
        final CustomFileChooser saveMapFC = new CustomFileChooser();
        saveMapFC.setSelectedFile(new File(mapFileName));
        if (saveMapFC.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            mapFileName = saveMapFC.getSelectedFile().getAbsolutePath();
            undoableState.map.saveToFile(mapFileName);
        }        
    }//GEN-LAST:event_jMenuItemSaveMapAsActionPerformed
    void saveSpriteListToCFile(Writer writer) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append("// Put the following lines in a header file!\r\n");
        sb.append("#include <stdint.h>\r\n");
        sb.append("#define NUMBER_OF_SPRITES "+undoableState.spritePositionList.size()+"\r\n");
        sb.append("typedef struct\r\n{\r\n");
        sb.append("\tuint16_t posX;\r\n");
        sb.append("\tuint16_t posY;\r\n");
        sb.append("\tuint8_t entityNumber;\r\n");
        sb.append("} spritePos_t\r\n");
        sb.append("extern const spritePos_t initialSpritePositions[NUMBER_OF_SPRITES];\r\n");
        //
        sb.append("// Put the following lines in the C file!\r\n");
        sb.append("#include <stdint.h>\r\n");
        sb.append("const spritePos_t initialSpritePositions[NUMBER_OF_SPRITES] = \r\n{\r\n");
        for (int i = 0; i < undoableState.spritePositionList.size(); i++)
        {
            SpritePosition sp = undoableState.spritePositionList.get(i);
            sb.append( "\t{ .posX = " + String.format(Locale.ROOT,"0x%04X, ",sp.x ) +
                        " .posY = " + String.format(Locale.ROOT,"0x%04X, ",sp.y ) + 
                        " .entityNumber = " + String.format(Locale.ROOT,"0x%02X },\r\n",sp.entityNumber));
        }
        String s = sb.toString();
        writer.write(s.substring(0, s.lastIndexOf(","))); 
        writer.write("\r\n};\r\n");
    }
    void saveWaypointListToCFile(Writer writer, ExportWaypointListDialog d) throws Exception
    {
        int mode = d.getExportMode();
        String variableName = d.getExportVariableName();
        StringBuilder sb = new StringBuilder();
        sb.append("// Put the following lines in a header file!\r\n");
        sb.append("#include <stdint.h>\r\n");
        if (mode == ExportWaypointListDialog.EXPORT_AS_ARRAY)
        {
            sb.append("#define NUMBER_OF_WAYPOINTS "+undoableState.waypointList.size()+"\r\n");        
            sb.append("extern const waypoint_t " + variableName + "[NUMBER_OF_WAYPOINTS];\r\n");
        }
        else
        {
           sb.append("extern const waypoint_t " + variableName + "0;\r\n"); // only the first waypoint need to be global.
        }
        //
        sb.append("// Put the following lines in the C file!\r\n");
        sb.append("// Put the following lines in the C file! Remember to include the header too!\r\n");
        sb.append("#include <stdint.h>\r\n");
        if (mode == ExportWaypointListDialog.EXPORT_AS_ARRAY)
        {
            sb.append("const waypoint_t " + variableName + "[NUMBER_OF_WAYPOINTS] = \r\n{");            
            for (int i = 0; i < undoableState.waypointList.size(); i++)
            {
                Waypoint wp = undoableState.waypointList.get(i);
                sb.append( "\r\n\t{\r\n\t\t.x = " + String.format(Locale.ROOT,"0x%04X, ", wp.x ) +
                            "\r\n\t\t.y = " + String.format(Locale.ROOT,"0x%04X, ", wp.y ) + 
                            "\r\n\t\t.halfWidth = " + String.format(Locale.ROOT,"0x%04X, ", wp.rx) + 
                            "\r\n\t\t.halfHeight = " + String.format(Locale.ROOT,"0x%04X, ", wp.ry) + 
                            "\r\n\t\t.normR2 = " + String.format(Locale.ROOT,"0x%08X, ", wp.rx * wp.rx * wp.ry * wp.ry) + 
                            "\r\n\t\t.pNextWaypoint1 = &" + variableName + "[" + String.format(Locale.ROOT,"%d]," , wp.nextWaypoint1) + 
                            "\r\n\t\t.pNextWaypoint2 = &" + variableName + "[" + String.format(Locale.ROOT,"%d]", wp.nextWaypoint2) + 
                            "\r\n\t}," );
            }
            String s = sb.toString();
            writer.write(s.substring(0, s.lastIndexOf(","))); // remove final comma 
            writer.write("\r\n};\r\n");
            
        }
        else
        {
            for (int i = 0; i < undoableState.waypointList.size(); i++)
            {
                Waypoint wp = undoableState.waypointList.get(i);
                sb.append( "const waypoint_t " + variableName + i +" = \r\n{\r\n\t.x = " + String.format(Locale.ROOT,"0x%04X, ", wp.x ) +
                            "\r\n\t.y = " + String.format(Locale.ROOT,"0x%04X, ", wp.y ) + 
                            "\r\n\t.halfWidth = " + String.format(Locale.ROOT,"0x%04X, ", wp.rx) + 
                            "\r\n\t.halfHeight = " + String.format(Locale.ROOT,"0x%04X, ", wp.ry) + 
                            "\r\n\t.normR2 = " + String.format(Locale.ROOT,"0x%08X, ", wp.rx * wp.rx * wp.ry * wp.ry) + 
                            "\r\n\t.pNextWaypoint1 = &wp[" + String.format(Locale.ROOT,"%d, " , wp.nextWaypoint1) + 
                            "\r\n\t.pNextWaypoint2 = &wp[" + String.format(Locale.ROOT,"%d ", wp.nextWaypoint2) + 
                            "\r\n};\r\n" );
            }
            String s = sb.toString();
            writer.write(s); 
        }
    }    
    void saveMapToCFile(Writer writer, ExportMapSettings ems) throws Exception
    {
        final int MAX_ENTRIES_PER_LINE = 8;
        GameMap mapToBeSaved = ems.getGameMap();
        int mapSizeX = mapToBeSaved.getSizeX();
        int mapSizeY = mapToBeSaved.getSizeY();
        writer.write("// put the following lines in a .h file and include it in your main.h!\n\n");
        writer.write("#include <stdint.h>\r\n");
        writer.write("#define MAPSIZEX" + ems.getLevelName().toUpperCase() + " " + mapSizeX + "\r\n");
        writer.write("#define MAPSIZEY" + ems.getLevelName().toUpperCase() + " "+ mapSizeY + "\r\n");
        if (ems.usesMetaTiles())
        {
            writer.write("#define NUMBER_OF_METATILES" + ems.getLevelName().toUpperCase() + " " + ems.getNumberOfMetaTiles() + "\r\n");
        }
        StringBuilder sb;
        String s;
        if (ems.isXbyYarraySelected())
        {
            writer.write("extern const uint16_t"+ ems.getMapName() + ems.getLevelName() +"[MAPSIZEY"+ ems.getLevelName().toUpperCase() + "][MAPSIZEX"+ ems.getLevelName().toUpperCase() +"];\r\n");
        }
        else
        {
            writer.write("extern const uint16_t "+ ems.getMapName() + ems.getLevelName() +"[MAPSIZEY"+ ems.getLevelName().toUpperCase() + " * MAPSIZEX"+ ems.getLevelName().toUpperCase() +"];\r\n");            
        }
        if (ems.usesMetaTiles())
        {
        
            writer.write("extern const uint16_t " + ems.getMetaTileName() + ems.getLevelName() +"[4 * NUMBER_OF_METATILES"+ ems.getLevelName().toUpperCase() + "];\r\n");
        }
        writer.write("// put the following lines in a .c file\n\n");
        writer.write("#include \"the header in which you put the lines before.h\"\r\n");        
        if (ems.isXbyYarraySelected())
        {
            writer.write("const uint16_t"+ ems.getMapName() + ems.getLevelName() +"[MAPSIZEY"+ ems.getLevelName().toUpperCase() + "][MAPSIZEX"+ ems.getLevelName().toUpperCase() +"] = \r\n{\r\n");
            for (int y = 0; y < mapSizeY; y++)
            {
                sb = new StringBuilder("\t{\r\n\t\t");
                for (int x = 0; x < mapSizeX; x++ )
                {
                    if (x < mapSizeX - 1)
                    {
                        if (x % MAX_ENTRIES_PER_LINE == (MAX_ENTRIES_PER_LINE - 1))
                            sb.append(String.format(Locale.ROOT,"0x%04X,\r\n\t\t", 0xFFFF & mapToBeSaved.getTile(x, y)));
                        else
                            sb.append(String.format(Locale.ROOT,"0x%04X, ", 0xFFFF &  mapToBeSaved.getTile(x, y)));   
                    }
                    else
                        sb.append(String.format(Locale.ROOT,"0x%04X\r\n", 0xFFFF &  mapToBeSaved.getTile(x, y)));    
                }
                writer.write(sb.toString());
                if (y < mapSizeY - 1)
                    writer.write("\t},\r\n");                               
                else
                    writer.write("\t}\r\n};\r\n");                            
            }            
        }
        else
        {
            sb =  new StringBuilder("");
            writer.write("const uint16_t "+ ems.getMapName() + ems.getLevelName() +"[MAPSIZEY"+ ems.getLevelName().toUpperCase() + " * MAPSIZEX"+ ems.getLevelName().toUpperCase() +"] = \r\n{");
            for (int i = 0; i < mapSizeX*mapSizeY; i++)
            {
                sb.append("\r\n\t");
                for (int j = 0; j < MAX_ENTRIES_PER_LINE - 1 && i < mapSizeX*mapSizeY; j++)
                {
                    int x = i % mapSizeX;
                    int y = i / mapSizeX;
                    sb.append(String.format(Locale.ROOT,"0x%04X, ",0xFFFF &  mapToBeSaved.getTile(x, y)));   
                    i++;
                }
                int x = i % mapSizeX;
                int y = i / mapSizeX;
                if (i < mapSizeX*mapSizeY)
                    sb.append(String.format(Locale.ROOT,"0x%04X,",0xFFFF &  mapToBeSaved.getTile(x, y)));   
            }
            s = sb.toString();
            writer.write(s.substring(0, s.lastIndexOf(",")));
            writer.write("\r\n};\r\n");                            
        }
        if (ems.usesMetaTiles())
        {
            writer.write ("//meta tiles\r\n");
            LinkedHashMap<ArrayList<Integer>,Integer> metaHashMap = ems.getMetaHashMap();
            writer.write("const uint16_t " + ems.getMetaTileName() + ems.getLevelName() +"[4 * NUMBER_OF_METATILES"+ ems.getLevelName().toUpperCase() + "] = \r\n{");
            Iterator<ArrayList<Integer>> it = metaHashMap.keySet().iterator();
            sb = new StringBuilder();
            while (it.hasNext())
            {
                ArrayList<Integer> al = it.next();
                sb.append(String.format(Locale.ROOT,"\r\n\t0x%04X, ",0xFFFF &  al.get(0)));
                sb.append(String.format(Locale.ROOT,"0x%04X, ",0xFFFF &  al.get(1)));
                sb.append(String.format(Locale.ROOT,"0x%04X, ",0xFFFF &  al.get(2)));
                sb.append(String.format(Locale.ROOT,"0x%04X,",0xFFFF &  al.get(3)));
            }
            s = sb.toString();
            s = s.substring(0, s.lastIndexOf(","));
            writer.write(s);
            writer.write("\r\n};\r\n");
        }
        writer.write("// put the following as an entry to a level_t array\r\n");
  	writer.write("{\r\n\t.pGameMap = (uint16_t*) " + ems.getMapName() + ems.getLevelName() + ",\r\n");
  	writer.write("\t.pMetaTiles = " + ems.getMetaTileName()+ ems.getLevelName() + ",\r\n");
        writer.write("\t.mapSizeX = MAPSIZEX"+ ems.getLevelName().toUpperCase() + ",\r\n");
  	writer.write("\t.mapSizeY = MAPSIZEY"+ ems.getLevelName().toUpperCase() + ",\r\n");
  	writer.write("\t.pTiles = &tileData[0][0], // note change this if you are using a different tile set for each level!\r\n");
  	writer.write("\t.numberOfTilesToCopyInRam = RAMTILES, // note change this if you are using a different tile set for each level!\r\n");
  	writer.write("\t.pixelSizeX = TILE_SIZE_X * MAPSIZEX" + ems.getLevelName().toUpperCase() + (ems.usesMetaTiles() ? " * 2" : "" ) + ",\r\n");
  	writer.write("\t.pixelSizeY = TILE_SIZE_Y * MAPSIZEY" + ems.getLevelName().toUpperCase() + (ems.usesMetaTiles() ? " * 2" : "" ) + ",\r\n");        
  	writer.write("\t.tileSizeX = TILE_SIZE_X" + (ems.usesMetaTiles() ? " * 2" : "" ) + ",\r\n");
  	writer.write("\t.tileSizeY = TILE_SIZE_Y" + (ems.usesMetaTiles() ? " * 2" : "" ) + ",\r\n");
        writer.write("\t.useMetaTiles = 1,\r\n");
  	writer.write("\t.pInitialSpritePositions = &initialSpritePositions" + ems.getLevelName() + "[0],\r\n");
  	writer.write("\t.numberOfSpritePositions = NUMBER_OF_SPRITES"+ ems.getLevelName().toUpperCase() + ",\r\n");
  	writer.write("\t.useMetaTiles = " + (ems.usesMetaTiles() ? "1" : "0" ) + ",\r\n");        
        writer.write("};\r\n");    
        infoBox ("Map Exported to C file", "Notice", JOptionPane.INFORMATION_MESSAGE);        
    }
    private void jMenuItemExportMapToCfileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExportMapToCfileActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExportMapToCfileActionPerformed
        final CustomFileChooser exportMapToCfileFC = new CustomFileChooser();    
        exportMapToCfileFC.setSelectedFile(new File(exportMapToCfileFileName));
        ExportMapSettings ems = new ExportMapSettings(this, true, undoableState.map);
        ems.setVisible(true);
        if (!ems.confirmed)
        {
            infoBox("Operation aborted!", "Information", JOptionPane.INFORMATION_MESSAGE);    
            return;
        }
        //
        if (exportMapToCfileFC.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            exportMapToCfileFileName = exportMapToCfileFC.getSelectedFile().getAbsolutePath();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(exportMapToCfileFileName), "utf-8")))
            {
                saveMapToCFile(writer, ems);
            } 
            catch (Exception e)
            {
                e.printStackTrace();
                infoBox("Cannot export map ", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }        
    }//GEN-LAST:event_jMenuItemExportMapToCfileActionPerformed

    private void jCheckBoxMenuItemShowGridOnMapActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxMenuItemShowGridOnMapActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxMenuItemShowGridOnMapActionPerformed
        mapPainter.drawGrid = jCheckBoxMenuItemShowGridOnMap.isSelected();
        mapPainter.repaint();
    }//GEN-LAST:event_jCheckBoxMenuItemShowGridOnMapActionPerformed

    private void jCheckBoxMenuItemShowGridOnTilesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxMenuItemShowGridOnTilesActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxMenuItemShowGridOnTilesActionPerformed
        tilePainter.drawGrid = jCheckBoxMenuItemShowGridOnTiles.isSelected();
        tilePainter.repaint();
    }//GEN-LAST:event_jCheckBoxMenuItemShowGridOnTilesActionPerformed

    private void jMenuItemSaveMapActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemSaveMapActionPerformed
    {//GEN-HEADEREND:event_jMenuItemSaveMapActionPerformed
        if (mapFileName.equals(""))
        {
            jMenuItemSaveMapAsActionPerformed(evt);
        }
        else
            undoableState.map.saveToFile(mapFileName);
    }//GEN-LAST:event_jMenuItemSaveMapActionPerformed
    public static short[] convertToRGB565 (BufferedImage image)
    {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return ((DataBufferUShort)newImage.getRaster().getDataBuffer()).getData();        
    }
    public static byte[] convertTouChipVGA8bpp (BufferedImage image)
    {
        // why should we create a new buffered image ? Because we want the image to be ARGB, while the original could be of any type.
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);       
        int [] dataBuffer =  ((DataBufferInt)newImage.getRaster().getDataBuffer()).getData();
        byte [] buffer = new byte[dataBuffer.length];
        for (int i = 0; i < buffer.length; i++)
        {
            
            int red = uChipVGAred[(dataBuffer[i] >> 16)  & 0xFF];
            int green = uChipVGAgreen[(dataBuffer[i] >> 8 ) & 0xFF];
            int blue = uChipVGAblue[(dataBuffer[i] >> 0 ) & 0xFF];
            // now that we have the uChipVGAred-uChipVGAgreen-b (3-3-2) components, we can create the uChip-palette
            buffer[i] = (byte) USVCMapEditorUtilities.USVCRGBto8bit(red, green, blue);
        }
        g2d.dispose();
        return buffer;        
    }    
    public static byte[] convertTouChipVGA4bpp (BufferedImage image)
    {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] dataBuffer = new int[0];
        dataBuffer = image.getData().getPixels(0, 0, width, height, (int [] )null);        
        byte [] buffer = new byte [dataBuffer.length];
        for (int i = 0; i < buffer.length; i++)
        {
            buffer[i] = (byte) dataBuffer[i];
        }
        return buffer;        
    }       
    boolean exportTileImageToCfile(BufferedImage image, ExportTileAsCfileDialog d, String fileName, int nDifferent, int tilesPerRow, ArrayList<Integer> listOfUniqueIndexes)
    {
        
        final int MAX_ENTRIES_PER_LINE = 4;  
        int maxIndex = nDifferent;
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(fileName), "utf-8")))
        {
            writer.write("//Put the following lines to a .h files and include it in main.h!\r\n");
            writer.write("#include <stdint.h>\r\n");
            writer.write("#define MAXTILEINDEX " + maxIndex + "\r\n");
            writer.write("#define TILESIZEX " + tileSizeX + "\r\n");
            writer.write("#define TILESIZEY " + tileSizeY + "\r\n");
            boolean bigEndian = d.getEndianess() == ExportTileAsCfileDialog.BIGENDIAN;
            switch (d.getExportMode() )
            {
                case ExportTileAsCfileDialog.FASTDMAMODE:
                    writer.write("extern extern const uint8_t tileData[MAXTILEINDEX][TILESIZEX * TILESIZEY * 2];\r\n");
                    break;
                case ExportTileAsCfileDialog.TILEARRAYMODE:
                    writer.write("extern const uint8_t tileData[MAXTILEINDEX][TILESIZEX * TILESIZEY * 2];\r\n");
                    break;
                case ExportTileAsCfileDialog.TILEARRAYMODEUCHIPVGA8BIT:                     
                    writer.write("extern const uint8_t tileData[MAXTILEINDEX][TILESIZEX * TILESIZEY ];\r\n");            
                    break;                    
                case ExportTileAsCfileDialog.TILEARRAYMODEUCHIPVGA4BIT:                     
                    writer.write("extern const uint8_t tileData[MAXTILEINDEX][TILESIZEX * TILESIZEY / 2 ];\r\n");
                    break;
                case ExportTileAsCfileDialog.TILEARRAYMODEUCHIPVGA2BIT:                     
                    writer.write("extern const uint8_t tileData[MAXTILEINDEX][TILESIZEX * TILESIZEY / 4 ]\r\n");
                    break;
            }
            writer.write("//Put the following lines in a .c file\r\n");
            writer.write("#include \"the header file in which you put the previous lines.h\"\r\n");            
            switch (d.getExportMode() )
            {
                case ExportTileAsCfileDialog.FASTDMAMODE:
                    writer.write("const uint8_t tileData[MAXTILEINDEX][TILESIZEX * TILESIZEY * 2] = \r\n{\r\n");
                    //
                    for (int i = 0; i < maxIndex;  i++ )
                    {
                        StringBuilder sb = new StringBuilder("\t{\r\n\t\t");
                        short [] data = convertToRGB565(getTileByIndexFromImage(listOfUniqueIndexes != null ? listOfUniqueIndexes.get(i) : i, tilesPerRow , image)); 
                        for (int x = 0; x < tileSizeX; x+=2)
                        {
                            for (int y = 0 ; y < tileSizeY; y++ )
                            {
                                int hibyte0, lobyte0, hibyte1, lobyte1;
                                // get the 32-bit word
                                if (bigEndian)
                                {
                                    hibyte0 = (0xFF00 & data [x + y * tileSizeX]) >> 8;
                                    lobyte0 = 0xFF & data [x + y * tileSizeX];
                                    hibyte1 = (0xFF00 & data [x + 1 + y * tileSizeX]) >> 8;
                                    lobyte1 = 0xFF & data [x + 1  + y * tileSizeX];
                                }
                                else
                                {
                                    lobyte0 = (0xFF00 & data [x + y * tileSizeX]) >> 8;
                                    hibyte0 = 0xFF & data [x + y * tileSizeX];
                                    lobyte1 = (0xFF00 & data [x + 1 + y * tileSizeX]) >> 8;
                                    hibyte1 = 0xFF & data [x + 1  + y * tileSizeX];                           
                                }
                                if (x == (tileSizeX - 2) && y == (tileSizeY -1 ))
                                {
                                    // last tile data
                                    sb.append(String.format(Locale.ROOT,"0x%02X, 0x%02X, 0x%02X, 0x%02X\r\n\t",hibyte0, lobyte0, hibyte1, lobyte1));
                                }
                                else
                                {
                                    //
                                    if (y % MAX_ENTRIES_PER_LINE == (MAX_ENTRIES_PER_LINE - 1))
                                        sb.append(String.format(Locale.ROOT,"0x%02X, 0x%02X, 0x%02X, 0x%02X,\r\n\t\t",hibyte0, lobyte0, hibyte1, lobyte1));
                                    else
                                        sb.append(String.format(Locale.ROOT,"0x%02X, 0x%02X, 0x%02X, 0x%02X, ",hibyte0, lobyte0, hibyte1, lobyte1));
                                }

                            }
                        }
                        writer.write(sb.toString());
                        if (i < maxIndex - 1)
                            writer.write("},\r\n");                               
                        else
                            writer.write("}\r\n};\r\n");                               
                    }

                    break;
                case ExportTileAsCfileDialog.TILEARRAYMODE:
                    writer.write("const uint8_t tileData[MAXTILEINDEX][TILESIZEX * TILESIZEY * 2] = \r\n{\r\n");
                    for (int i = 0; i < maxIndex;  i++ )
                    {
                        StringBuilder sb = new StringBuilder("\t{\r\n\t\t");
                        short [] data = convertToRGB565(getTileByIndexFromImage(listOfUniqueIndexes != null ? listOfUniqueIndexes.get(i) : i, tilesPerRow , image)); 
                        for (int y = 0 ; y < tileSizeY; y++ )
                        {
                            for (int x = 0; x < tileSizeX; x+=2)
                            {                                    
                                int hibyte0, lobyte0, hibyte1, lobyte1;
                                // get the 32-bit word
                                if (bigEndian)
                                {
                                    hibyte0 = (0xFF00 & data [x + y * tileSizeX]) >> 8;
                                    lobyte0 = 0xFF & data [x + y * tileSizeX];
                                    hibyte1 = (0xFF00 & data [x + 1 + y * tileSizeX]) >> 8;
                                    lobyte1 = 0xFF & data [x + 1  + y * tileSizeX];
                                }
                                else
                                {
                                    lobyte0 = (0xFF00 & data [x + y * tileSizeX]) >> 8;
                                    hibyte0 = 0xFF & data [x + y * tileSizeX];
                                    lobyte1 = (0xFF00 & data [x + 1 + y * tileSizeX]) >> 8;
                                    hibyte1 = 0xFF & data [x + 1  + y * tileSizeX];                           
                                }
                                if (x == (tileSizeX - 2) && y == (tileSizeY -1 ))
                                {
                                    // last tile data
                                    sb.append(String.format(Locale.ROOT,"0x%02X, 0x%02X, 0x%02X, 0x%02X\r\n\t",hibyte0, lobyte0, hibyte1, lobyte1));
                                }
                                else
                                {
                                    //
                                    if (x % MAX_ENTRIES_PER_LINE == (MAX_ENTRIES_PER_LINE - 1))
                                        sb.append(String.format(Locale.ROOT,"0x%02X, 0x%02X, 0x%02X, 0x%02X,\r\n\t\t",hibyte0, lobyte0, hibyte1, lobyte1));
                                    else
                                        sb.append(String.format(Locale.ROOT,"0x%02X, 0x%02X, 0x%02X, 0x%02X, ",hibyte0, lobyte0, hibyte1, lobyte1));
                                }

                            }
                        }
                        writer.write(sb.toString());
                        if (i < maxIndex - 1)
                            writer.write("},\r\n");                               
                        else
                            writer.write("}\r\n};\r\n");                               
                    }

                    break;
                case ExportTileAsCfileDialog.TILEARRAYMODEUCHIPVGA8BIT:                     
                    writer.write("const uint8_t tileData[MAXTILEINDEX][TILESIZEX * TILESIZEY ] = \r\n{\r\n");
                    for (int i = 0; i < maxIndex;  i++ )
                    {
                        StringBuilder sb = new StringBuilder("\t{\r\n\t\t");
                        byte [] data = convertTouChipVGA8bpp(getTileByIndexFromImage(listOfUniqueIndexes != null ? listOfUniqueIndexes.get(i) : i, tilesPerRow , image)); 
                        for (int y = 0 ; y < tileSizeY; y++ )
                        {
                            for (int x = 0; x < tileSizeX; x++)
                            {                                    
                                int value;
                                value = data [x + y * tileSizeX];
                                if (x == (tileSizeX - 1) && y == (tileSizeY -1 ))
                                {
                                    // last tile data
                                    sb.append(String.format(Locale.ROOT,"0x%02X\r\n\t",value & 0xFF));
                                }
                                else
                                { 
                                    //
                                    if (x % MAX_ENTRIES_PER_LINE == (MAX_ENTRIES_PER_LINE - 1))
                                        sb.append(String.format(Locale.ROOT,"0x%02X,\r\n\t\t",value & 0xFF));
                                    else
                                        sb.append(String.format(Locale.ROOT,"0x%02X, ",value & 0xFF));
                                }

                            }
                        }
                        writer.write(sb.toString());
                        if (i < maxIndex - 1)
                            writer.write("},\r\n");                               
                        else
                            writer.write("}\r\n};\r\n");                               
                    }                    
                    break;
                case ExportTileAsCfileDialog.TILEARRAYMODEUCHIPVGA4BIT:
                    {
                        // Here we need to export the palette too.
                        if (image.getColorModel().getPixelSize() != 4)
                        {
                            infoBox("Tile image should be in 4-bpp format.", "Error", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                        // get the palette
                        int [] rgbs = new int[16];
                        ((IndexColorModel) image.getColorModel()).getRGBs(rgbs);
                        writer.write("const uint8_t tilePalette[16] = \r\n{");
                        for (int i = 0; i < rgbs.length; i++)
                        {
                            // TODO: GENERATE EXTENDED PALETTE (1k)
                            int red = uChipVGAred[(rgbs[i] >> 16)  & 0xFF];
                            int green = uChipVGAgreen[(rgbs[i] >> 8 ) & 0xFF];
                            int blue = uChipVGAblue[(rgbs[i] >> 0 ) & 0xFF];
                            // now that we have the uChipVGAred-uChipVGAgreen-b (3-3-2) components, we can create the uChip-palette
                            int paletteEntry = (byte) ((red & 1) | ((red & 4) >> 1) | ((red & 2) << 1)  | ((blue & 1) << 3) | ((green & 1) << 4) | ((blue & 2) << 4) | ((green & 2) << 5) | ((green & 4) << 5) ) ;
                            String entry = String.format(Locale.ROOT,"0x%02X\t",paletteEntry & 0xFF);
                            if (i != 15)
                              writer.write(entry + ",");
                            else
                              writer.write(entry + "};\r\n");                       
                        }
                        writer.write("const uint8_t tileData[MAXTILEINDEX][TILESIZEX * TILESIZEY / 2 ] = \r\n{\r\n");
                        for (int i = 0; i < maxIndex;  i++ )
                        {
                            StringBuilder sb = new StringBuilder("\t{\r\n\t\t");
                            BufferedImage tile = getTileByIndexFromImage(listOfUniqueIndexes != null ? listOfUniqueIndexes.get(i) : i, tilesPerRow , image);
                            byte [] data = convertTouChipVGA4bpp(tile); 

                            for (int y = 0 ; y < tileSizeY; y++ )
                            {
                                for (int x = 0; x < tileSizeX; x += 2)
                                {                                    
                                    int value, valueLo, valueHi;
                                    valueLo = 0x0F & data [x + y * tileSizeX ];
                                    valueHi = 0x0F & data [x +1 + y * tileSizeX ];
                                    value = valueLo | (valueHi << 4);
                                    if (x == (tileSizeX - 2) && y == (tileSizeY - 1 ))
                                    {
                                        // last tile data
                                        sb.append(String.format(Locale.ROOT,"0x%02X\r\n\t",value & 0xFF));
                                    }
                                    else
                                    { 
                                        if (x % MAX_ENTRIES_PER_LINE == (MAX_ENTRIES_PER_LINE - 1))
                                            sb.append(String.format(Locale.ROOT,"0x%02X,\r\n\t\t",value & 0xFF));
                                        else
                                            sb.append(String.format(Locale.ROOT,"0x%02X, ",value & 0xFF));
                                    }

                                }
                            }
                            writer.write(sb.toString());
                            if (i < maxIndex - 1)
                                writer.write("},\r\n");                               
                            else
                                writer.write("}\r\n};\r\n");                               
                        }                    
                    }
                    break;

                case ExportTileAsCfileDialog.TILEARRAYMODEUCHIPVGA2BIT:
                    {
                        // Here we need to export the palette too.
                        if (image.getColorModel().getPixelSize() != 2)
                        {
                            infoBox("Tile image should be in 2-bpp format.", "Error", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                        // get the palette
                        int [] rgbs = new int[4];
                        ((IndexColorModel) image.getColorModel()).getRGBs(rgbs);
                        writer.write("const uint8_t tilePalette[4] = \r\n{");
                        for (int i = 0; i < rgbs.length; i++)
                        {
                            // TODO: GENERATE EXTENDED PALETTE (1k)
                            int red = uChipVGAred[(rgbs[i] >> 16)  & 0xFF];
                            int green = uChipVGAgreen[(rgbs[i] >> 8 ) & 0xFF];
                            int blue = uChipVGAblue[(rgbs[i] >> 0 ) & 0xFF];
                            // now that we have the uChipVGAred-uChipVGAgreen-b (3-3-2) components, we can create the uChip-palette
                            int paletteEntry = (byte) ((red & 1) | ((red & 4) >> 1) | ((red & 2) << 1)  | ((blue & 1) << 3) | ((green & 1) << 4) | ((blue & 2) << 4) | ((green & 2) << 5) | ((green & 4) << 5) ) ;
                            String entry = String.format(Locale.ROOT,"0x%02X\t",paletteEntry & 0xFF);
                            if (i != 3)
                              writer.write(entry + ",");
                            else
                              writer.write(entry + "};\r\n");                       
                        }
                        writer.write("const uint8_t tileData[MAXTILEINDEX][TILESIZEX * TILESIZEY / 4 ] = \r\n{\r\n");
                        for (int i = 0; i < maxIndex;  i++ )
                        {
                            StringBuilder sb = new StringBuilder("\t{\r\n\t\t");
                            BufferedImage tile = getTileByIndexFromImage(listOfUniqueIndexes != null ? listOfUniqueIndexes.get(i) : i, tilesPerRow , image);
                            byte [] data = convertTouChipVGA4bpp(tile); 
                            for (int y = 0 ; y < tileSizeY; y++ )
                            {
                                for (int x = 0; x < tileSizeX; x += 4)
                                {                                    
                                    int value, value0, value1, value2, value3;
                                    value0 = 0x03 & data [x + y * tileSizeX ];
                                    value1 = 0x03 & data [x + 1 + y * tileSizeX ];
                                    value2 = 0x03 & data [x + 2 + y * tileSizeX ];
                                    value3 = 0x03 & data [x + 3 + y * tileSizeX ];
                                    value = value0 | (value1 << 2) | (value2 << 4) | (value3 << 6);
                                    if (x == (tileSizeX - 4) && y == (tileSizeY - 1 ))
                                    {
                                        // last tile data
                                        sb.append(String.format(Locale.ROOT,"0x%02X\r\n\t",value & 0xFF));
                                    }
                                    else
                                    { 
                                        if (x % MAX_ENTRIES_PER_LINE == (MAX_ENTRIES_PER_LINE - 1))
                                            sb.append(String.format(Locale.ROOT,"0x%02X,\r\n\t\t",value & 0xFF));
                                        else
                                            sb.append(String.format(Locale.ROOT,"0x%02X, ",value & 0xFF));
                                    }

                                }
                            }
                            writer.write(sb.toString());
                            if (i < maxIndex - 1)
                                writer.write("},\r\n");                               
                            else
                                writer.write("}\r\n};\r\n");                               
                        }                    

                    }                        
                    break;                    
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private void jMenuItemExportTilesToCfileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExportTilesToCfileActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExportTilesToCfileActionPerformed
        ExportTileAsCfileDialog d = new ExportTileAsCfileDialog(this, true);
        d.setVisible(true);
        if (d.confirmed)
        {
            final CustomFileChooser exportTilesToCfileFC = new CustomFileChooser();    
            exportTilesToCfileFC.setSelectedFile(new File(exportTilesToCfileFileName));
            if (exportTilesToCfileFC.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                exportTilesToCfileFileName = exportTilesToCfileFC.getSelectedFile().getAbsolutePath(); 
                if (exportTileImageToCfile(tilePainter.bufferedImage, d, exportTilesToCfileFileName, nTilesX * nTilesY, nTilesX,null))
                    infoBox ("Tiles Exported to C file", "Notice", JOptionPane.INFORMATION_MESSAGE);
                else
                    infoBox("Cannot export tiles ", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                 infoBox ("Export aborted.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            }  
        }
        d.dispose();  // free mem
    }//GEN-LAST:event_jMenuItemExportTilesToCfileActionPerformed
    void updateMapPainterMapData()
    {
        mapPainter.bufferedImage = new BufferedImage(undoableState.map.getSizeX() * tileSizeX, undoableState.map.getSizeY() * tileSizeY ,BufferedImage.TYPE_INT_RGB);
        mapPainter.setNumberOfTiles(undoableState.map.getSizeX() , undoableState.map.getSizeY());
        mapPainter.setMap(undoableState.map);
        mapPainter.setPreferredSize(undoableState.map.getSizeX() * tileSizeX ,undoableState.map.getSizeY() * tileSizeY);        
    }
    private void jMenuItemChangeMapAndScreenSizesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemChangeMapAndScreenSizesActionPerformed
    {//GEN-HEADEREND:event_jMenuItemChangeMapAndScreenSizesActionPerformed
        ChangeMapSizeDialog d = new ChangeMapSizeDialog(this, true);
        d.setMapSizeX(undoableState.map.getSizeX());
        d.setMapSizeY(undoableState.map.getSizeY());
        d.setScreenSizeX(screenX);        
        d.setScreenSizeY(screenY);        
        d.setVisible(true);
        if (d.confirmed)
        {
            undoableState.saveState(UndoableState.PLACE_TILE);
            if (d.isCopyMapRequested())
                undoableState.map = new GameMap(undoableState.map, d.getMapSizeX(), d.getMapSizeY());
            else
                undoableState.map = new GameMap( d.getMapSizeX(), d.getMapSizeY());
            screenX = d.getScreenSizeX();
            screenY = d.getScreenSizeY();
            mapPainter.screenX = screenX;
            mapPainter.screenY = screenY;
            updateMapPainterMapData();
            jPanelMapArea.setPreferredSize(new Dimension(undoableState.map.getSizeX() * tileSizeX ,undoableState.map.getSizeY() * tileSizeY));        
            jScrollPaneMapArea.setViewportView(jPanelMapArea);
            drawMap();
            setMapWindowZoom();
        }
        d.dispose();  // free mem
    }//GEN-LAST:event_jMenuItemChangeMapAndScreenSizesActionPerformed

    private void jCheckBoxMenuItemShowScreenSizeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxMenuItemShowScreenSizeActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxMenuItemShowScreenSizeActionPerformed
        mapPainter.showScreenSize = jCheckBoxMenuItemShowScreenSize.isSelected();
        jScrollPaneMapArea.setViewportView(jPanelMapArea);
        drawMap();
        mapPainter.repaint();        
    }//GEN-LAST:event_jCheckBoxMenuItemShowScreenSizeActionPerformed
    public static byte[] hashImage(BufferedImage imageToHash) 
    {
        try 
        {
            byte [] data = ((DataBufferByte)imageToHash.getRaster().getDataBuffer()).getData();          
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(data);
            return md.digest();
        } 
        catch (NoSuchAlgorithmException e) 
        {
            e.printStackTrace();
        }
        return null;
    }

    boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) 
    {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) 
        {
            byte [] data1 = ((DataBufferByte)img1.getRaster().getDataBuffer()).getData();          
            byte [] data2 = ((DataBufferByte)img1.getRaster().getDataBuffer()).getData();          
            for (int i = 0; i < data1.length; i++)
                if (data1[i] != data2[i])
                    return false;
        } 
        else 
        {
            return false;
        }
        return true;
    }    
    boolean isBufferedImagePartiallyTransparent(BufferedImage img) 
    {
        for (int x = 0; x < img.getWidth(); x++) 
        {
            for (int y = 0; y < img.getHeight(); y++) 
            {
                int c = img.getRGB(x, y);
                if ( (c & 0xFF000000) != 0xFF000000)
                    return true;
            }
        }
        return false;
    }      
    int removeDuplicateTiles(int nDifferent, int startIndex, BufferedImage backgroundImg, BufferedImage img, HashMap <String,ArrayList <Integer>> hashMap, ArrayList<BufferedImage> bufferedImageArrayList, ArrayList <Short> conversionMap, int tileSizeX, int tileSizeY)
    {   // first version
        int nx = img.getWidth() / tileSizeX;
        int ny = img.getHeight() / tileSizeY;                
        for (int y = 0; y< ny; y++)
        {
            for (int x = 0; x < nx; x++)
            {
                int equivalentTileNumber = 0;
                // get a s8btile
                BufferedImage tmpSubtile = img.getSubimage(x * tileSizeX, y*tileSizeY, tileSizeX, tileSizeY); 
                // Create empty compatible image
                BufferedImage subtile = new BufferedImage( tileSizeX, tileSizeY ,BufferedImage.TYPE_4BYTE_ABGR);
                // Copy data into the new, empty image
                if (backgroundImg != null)
                {
                    subtile.getGraphics().drawImage(backgroundImg, 0, 0,  tileSizeX, tileSizeY, null);                                     
                    subtile.getGraphics().drawImage(tmpSubtile, 0, 0,  tileSizeX, tileSizeY, null);      
                }
                else
                {
                    Graphics2D graphics = (Graphics2D) subtile.getGraphics();
                    graphics.setComposite(AlphaComposite.Clear); 
                    graphics.fillRect(0, 0, tileSizeX, tileSizeY); 
                    graphics.setComposite(AlphaComposite.SrcOver);
                    subtile.getGraphics().drawImage(tmpSubtile, 0, 0,  tileSizeX, tileSizeY, null);  
                    graphics.dispose();
                }
                // get the hash of the subtile
                String hash = USVCMapEditorUtilities.bytesToHex(hashImage(subtile));
                // 
                boolean semiTransparent = true;
                if (!isBufferedImagePartiallyTransparent(subtile))
                 {
                    semiTransparent = false;
                }
                if (hashMap.containsKey(hash))
                {
                    // possible found
                    boolean alreadyPresent = false;
                    ArrayList <Integer> al = hashMap.get(hash);
                    for (int idx = 0; idx < al.size(); idx++)
                    {
                        // get the image pointed by the arrayList of images having the same hashcode.
                        equivalentTileNumber = al.get(idx);
                        BufferedImage oldTile = bufferedImageArrayList.get(equivalentTileNumber);                     
                        if (bufferedImagesEqual(oldTile, subtile))
                        {
                            alreadyPresent = true;
                            break;
                        }
                    }
                    if (!alreadyPresent)
                    {
                        // same hash, but actually different image. Add a new image to the same list.
                        al.add(nDifferent);
                        equivalentTileNumber = nDifferent;
                        bufferedImageArrayList.add(subtile);
                        nDifferent++;
                    }
                }
                else
                {
                    // different hash. Therefore different image
                    ArrayList <Integer> al = new ArrayList<Integer>();
                    al.add(nDifferent);
                    equivalentTileNumber = nDifferent;
                    bufferedImageArrayList.add(subtile);
                    hashMap.put(hash, al);
                    nDifferent++;                            
                }
                if (semiTransparent && jCheckBoxMenuItemMarkSemiTransparent.isSelected())
                    equivalentTileNumber |= 0x8000;
                conversionMap.add( startIndex + y * nx + x, (short) equivalentTileNumber);   
            }
        }        
        return nDifferent;
    }
    int removeDuplicateTiles(int nDifferent, int startIndex, BufferedImage backgroundImg, BufferedImage img, HashMap <String,ArrayList <Integer>> hashMap, ArrayList<BufferedImage> bufferedImageArrayList, ArrayList <Short> conversionMap, int tileSizeX, int tileSizeY, ArrayList <Integer> listOfUniqueIndexes)
    {   // overloaded version.
        int nx = img.getWidth() / tileSizeX;
        int ny = img.getHeight() / tileSizeY;                
        for (int y = 0; y< ny; y++)
        {
            for (int x = 0; x < nx; x++)
            {
                int equivalentTileNumber = 0;
                // get a s8btile
                BufferedImage tmpSubtile = img.getSubimage(x * tileSizeX, y*tileSizeY, tileSizeX, tileSizeY); 
                // Create empty compatible image
                BufferedImage subtile = new BufferedImage( tileSizeX, tileSizeY ,BufferedImage.TYPE_4BYTE_ABGR);
                // Copy data into the new, empty image
                if (backgroundImg != null)
                {
                    subtile.getGraphics().drawImage(backgroundImg, 0, 0,  tileSizeX, tileSizeY, null);                                     
                    subtile.getGraphics().drawImage(tmpSubtile, 0, 0,  tileSizeX, tileSizeY, null);      
                }
                else
                {
                    Graphics2D graphics = (Graphics2D) subtile.getGraphics();
                    graphics.setComposite(AlphaComposite.Clear); 
                    graphics.fillRect(0, 0, tileSizeX, tileSizeY); 
                    graphics.setComposite(AlphaComposite.SrcOver);
                    subtile.getGraphics().drawImage(tmpSubtile, 0, 0,  tileSizeX, tileSizeY, null);  
                    graphics.dispose();
                }
                // get the hash of the subtile
                String hash = USVCMapEditorUtilities.bytesToHex(hashImage(subtile));
                // 
                boolean semiTransparent = true;
                if (!isBufferedImagePartiallyTransparent(subtile))
                {
                    semiTransparent = false;
                }
                if (hashMap.containsKey(hash))
                {
                    // possible found
                    boolean alreadyPresent = false;
                    ArrayList <Integer> al = hashMap.get(hash);
                    for (int idx = 0; idx < al.size(); idx++)
                    {
                        // get the image pointed by the arrayList of images having the same hashcode.
                        equivalentTileNumber = al.get(idx);
                        BufferedImage oldTile = bufferedImageArrayList.get(equivalentTileNumber);                     
                        if (bufferedImagesEqual(oldTile, subtile))
                        {
                            alreadyPresent = true;
                            break;
                        }
                    }
                    if (!alreadyPresent)
                    {
                        // same hash, but actually different image. Add a new image to the same list.
                        al.add(nDifferent);
                        equivalentTileNumber = nDifferent;
                        bufferedImageArrayList.add(subtile);
                        listOfUniqueIndexes.add(nx * y + x);
                        nDifferent++;
                    }
                }
                else
                {
                    // different hash. Therefore different image
                    ArrayList <Integer> al = new ArrayList<Integer>();
                    al.add(nDifferent);
                    equivalentTileNumber = nDifferent;
                    bufferedImageArrayList.add(subtile);
                    hashMap.put(hash, al);
                    listOfUniqueIndexes.add(nx * y + x);                    
                    nDifferent++;                            
                }
                if (semiTransparent && jCheckBoxMenuItemMarkSemiTransparent.isSelected())
                    equivalentTileNumber |= 0x8000;
                conversionMap.add( startIndex + y * nx + x, (short) equivalentTileNumber);            
            }
        }        
        return nDifferent;
    }

    private void jMenuItemCreateTileSetFromDirectoryActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemCreateTileSetFromDirectoryActionPerformed
    {//GEN-HEADEREND:event_jMenuItemCreateTileSetFromDirectoryActionPerformed
        // 1) get the list
        // 2) Split the tiles in 16x16 subtiles
        // 3) create hash for each subtile
        // 4) compare each image with the n-1 remaining. If the image has the same hash, then compare pixelbypixel.
        final JFileChooser createTileSetFromDirectoryFC = new JFileChooser();
        createTileSetFromDirectoryFC.setSelectedFile(new File(createTileSetFromDirectoryFileName));
        createTileSetFromDirectoryFC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        ArrayList<BufferedImage> bufferedImageArrayList = new ArrayList<BufferedImage>();
        ArrayList <Short> conversionMap = new ArrayList <Short>();
        if (createTileSetFromDirectoryFC.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            SetTileSizesDialog d = new SetTileSizesDialog(this, true);
            d.setTileSizes(tileSizeX, tileSizeY);
            d.setVisible(true);
            if (!d.confirmed)
            {
                infoBox("Operation aborted!", "Info", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();  // free mem
                return;
            }
            int tileWidth = d.getSizeX();
            int tileHeight = d.getSizeY();
            String path = createTileSetFromDirectoryFC.getSelectedFile().getAbsolutePath();
            createTileSetFromDirectoryFileName = path;      // we use path as alias for brevity.
            if (!path.endsWith(File.separator))
               path += File.separator;
            int i = 1;
            int nDifferent = 0;
            BufferedImage img;
            HashMap <String,ArrayList <Integer>> hashMap = new HashMap <String, ArrayList <Integer>>();        
            // load first Tile 0. This will be the background over which all the other tiles will be painted on.
            BufferedImage backgroundImg = loadImage(path+"0.png");
            if (backgroundImg != null)
            {
                // add the first image
                String hash = USVCMapEditorUtilities.bytesToHex(hashImage(backgroundImg));
                ArrayList <Integer> al = new ArrayList<Integer>();
                al.add(nDifferent);
                bufferedImageArrayList.add(backgroundImg);
                hashMap.put(hash, al);
                nDifferent++;  
            }
            int startIndex = 0;            
            while (( img =  loadImage(path+i+".png")) != null )
            {
                // 
                nDifferent = removeDuplicateTiles(nDifferent, startIndex, backgroundImg, img,  hashMap, bufferedImageArrayList, conversionMap,tileWidth, tileHeight);
                int nx = img.getWidth() / tileWidth;
                int ny = img.getHeight() / tileHeight;                
                startIndex += nx * ny;
                i++; 
            }  
            // now we have enough information. Let's save new pictures in a big one.
            final int tilesPerRow = 16;
            int pictureSize = tileHeight * (int) Math.ceil(nDifferent / ((double) tilesPerRow));
            // now create a new buffered image
            BufferedImage tilesImage = new BufferedImage(tilesPerRow * tileWidth, pictureSize ,BufferedImage.TYPE_INT_ARGB);
            i = 0;
            int y = 0;
            Graphics g = tilesImage.getGraphics();
            while (i < nDifferent)
            {
                for (int x = 0; x < tilesPerRow && i < nDifferent ; x++)
                {
                        g.drawImage(bufferedImageArrayList.get(i), x * tileWidth, y * tileHeight,  tileWidth, tileHeight, null);      
                        i++;
                }
                y++;
            }
            g.dispose();
            File outputfile = new File(path+"tiles.png");
            try
            {
                
                ImageIO.write(tilesImage, "png", outputfile);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            d.dispose();  // free mem
        }      
    }//GEN-LAST:event_jMenuItemCreateTileSetFromDirectoryActionPerformed
    private void jMenuItemLoadTilePictureActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemLoadTilePictureActionPerformed
    {//GEN-HEADEREND:event_jMenuItemLoadTilePictureActionPerformed
        // First let's load the image...
        try
        {
            final JFileChooser loadTilePictureFC = new JFileChooser();
            loadTilePictureFC.setSelectedFile(new File ( loadTilePictureFileName));
            if (loadTilePictureFC.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                int oldTileNumber = nTilesX * nTilesY;
                SetTileSizesDialog dialog = new SetTileSizesDialog(this, true);
                dialog.setTileSizes(tileSizeX, tileSizeY);
                dialog.setVisible(true);
                if (!dialog.confirmed)
                {
                    dialog.dispose();
                    infoBox("Operation aborted!", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                loadTilePictureFileName = loadTilePictureFC.getSelectedFile().getAbsolutePath();
                tilePainter.bufferedImage = loadImage(loadTilePictureFileName);                
                jScrollPaneTileArea.setViewportView(jPanelTileArea);
                tilePainter.repaint();
                setTileSize(dialog.getSizeX(), dialog.getSizeY());
                nTilesX = tilePainter.bufferedImage.getWidth() / tileSizeX;
                nTilesY = tilePainter.bufferedImage.getHeight() / tileSizeY;
                tilePainter.setNumberOfTiles (nTilesX , nTilesY);
                tileProps =  tilePainter.getTileProperties();
                tilePainter.setTileSize(tileSizeX, tileSizeY);
                mapPainter.setTileSize(tileSizeX, tileSizeY);
                currentTilePainter.setTileSize(tileSizeX, tileSizeY);
                updateMapPainterMapData();
                mapPainter.previewSelectionImage = null;
                if (oldTileNumber > nTilesX * nTilesY)
                {
                    infoBox("The tiles have been loaded successful, however the current map uses a larger number of tiles, so it cannot be drawn!", "Warning", JOptionPane.WARNING_MESSAGE);
                }
                else
                    drawMap();
                jScrollPaneTileArea.setViewportView(jPanelTileArea);
                setTileWindowZoom();
                setMapWindowZoom();
                dialog.dispose();
            }
        }
        catch (Exception e)
        {
            infoBox("Cannot load tile picture!", "Error", JOptionPane.ERROR_MESSAGE);
        }        
    }//GEN-LAST:event_jMenuItemLoadTilePictureActionPerformed
    public GameMap createReducedMap(GameMap inputMap)
    {
        HashMap <String,ArrayList <Integer>> hashMap = new HashMap <String, ArrayList <Integer>>();        
        ArrayList<BufferedImage> bufferedImageArrayList = new ArrayList<BufferedImage>();
        ArrayList <Short> conversionMap = new ArrayList <Short>();        
        int nDifferent;
        nDifferent = removeDuplicateTiles(0, 0, null, tilePainter.bufferedImage, hashMap, bufferedImageArrayList, conversionMap, tileSizeX, tileSizeY);
        // now we convert the map.
        GameMap convertedMap = new GameMap( undoableState.map, undoableState.map.getSizeX(), undoableState.map.getSizeY());
        int sx =  undoableState.map.getSizeX();
        int sy =  undoableState.map.getSizeY();
        for (int y = 0; y < sy; y++)
        {
            for (int x = 0; x < sx; x++)
            {
                convertedMap.setTile(x, y, (short) (conversionMap.get(undoableState.map.getTile(x, y) & GameMap.TILE_MASK ) | (undoableState.map.getPriority(x, y) != 0 ? GameMap.TILE_PRIORITY_MASK : 0)));
            }
        }
        return convertedMap;
    }
    private void jCheckBoxShowSpritesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxShowSpritesActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxShowSpritesActionPerformed
        drawMap();
        mapPainter.repaint();

    }//GEN-LAST:event_jCheckBoxShowSpritesActionPerformed

    private void jMenuItemLoadSpriteSetActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemLoadSpriteSetActionPerformed
    {//GEN-HEADEREND:event_jMenuItemLoadSpriteSetActionPerformed
        if (USVCMapEditorUtilities.questionYesNo("This operation will clear all the undo history! Continue?", "Warning!") == JOptionPane.YES_OPTION)
        {    
            undoableState.clear();
            try
            {
                final JFileChooser loadSpriteSetFC = new JFileChooser();   
                loadSpriteSetFC.setSelectedFile(new File (loadSpriteSetFileName));
                if (loadSpriteSetFC.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                {
                    loadSpriteSetFileName = loadSpriteSetFC.getSelectedFile().getAbsolutePath();
                    int dotPos = loadSpriteSetFileName.lastIndexOf(".");
                    String jsonSpriteSet = loadSpriteSetFileName.substring(0,dotPos+1)+"json";
                    //
                    sprites.readAtlasFile(loadSpriteSetFileName,jsonSpriteSet);
                    spritePainter.setNumberOfTiles(sprites.numberOfEntities, 1);
                    spritePainter.tileWidth = sprites.maxWidth;
                    spritePainter.tileHeight = sprites.maxHeight;
                    spritePainter.bufferedImage = sprites.painterImage;
                    //
                    Dimension d = spritePainter.getSize();
                    Dimension newDim = new Dimension( sprites.painterImage.getWidth()* d.height / sprites.painterImage.getHeight(), d.height);
                    jPanelSpriteArea.setPreferredSize(newDim);
                    spritePainter.setPreferredSize(newDim.width, newDim.height);
                    jScrollPaneSpriteArea.setViewportView(jPanelSpriteArea);
                    spritePainter.repaint();                
                    drawMap();
                    setMapWindowZoom();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                infoBox("Cannot load sprites!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }//GEN-LAST:event_jMenuItemLoadSpriteSetActionPerformed
    void redrawCurrentTilePanel()
    {
        // let's redraw the current block so that the aspect ratio of the original tiles is kept.
        // to do this, let's check the aspect ratio
        Dimension d = currentTilePainter.getSize();
        Dimension newDim;
        if (currentTilePainter.bufferedImage.getWidth()* d.height / currentTilePainter.bufferedImage.getHeight() < d.width)
            newDim= new Dimension( currentTilePainter.bufferedImage.getWidth()* d.height / currentTilePainter.bufferedImage.getHeight(), d.height);
        else
            newDim= new Dimension( d.width, currentTilePainter.bufferedImage.getHeight()* d.width / currentTilePainter.bufferedImage.getWidth());            
        currentTilePainter.setPreferredSize(newDim);
        currentTilePainter.repaint();                        
    }
    private void jMenuItemExportSpriteDataActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExportSpriteDataActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExportSpriteDataActionPerformed
        ExportSpriteToCFileDialog esd = new ExportSpriteToCFileDialog(this, true);
        esd.setVisible(true);
        if (esd.confirmed)
        {
            final CustomFileChooser exportSpriteDataFC = new CustomFileChooser();
            exportSpriteDataFC.setSelectedFile(new File (exportSpriteDataFileName));
            if (exportSpriteDataFC.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                exportSpriteDataFileName = exportSpriteDataFC.getSelectedFile().getAbsolutePath();
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(exportSpriteDataFileName), "utf-8")))
                {
                    sprites.exportToCFile(writer, esd.getExportMode());
                } 
                catch (Exception e)
                {
                    infoBox("Cannot export sprite data ", "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }        
        }
        else
            infoBox("Operation aborted", "Notice", JOptionPane.INFORMATION_MESSAGE);
        esd.dispose();
    }//GEN-LAST:event_jMenuItemExportSpriteDataActionPerformed

    private void jCheckBoxShowSpriteNumbersActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxShowSpriteNumbersActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxShowSpriteNumbersActionPerformed
        drawMap();
        mapPainter.repaint();
        
    }//GEN-LAST:event_jCheckBoxShowSpriteNumbersActionPerformed

    private void jSpinnerCurrentSpriteStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerCurrentSpriteStateChanged
    {//GEN-HEADEREND:event_jSpinnerCurrentSpriteStateChanged
        SpinnerNumberModel model =  (SpinnerNumberModel) jSpinnerCurrentSprite.getModel();
        model.setMaximum(Integer.min(MAX_NUMBER_OF_SPRITES, undoableState.spritePositionList.size()));
        if (jCheckBoxShowSpriteNumbers.isSelected() && jCheckBoxShowSprites.isSelected())
        {
            drawSpritesAndWaypoints();   // just redraw sprites and possibly wp
            mapPainter.repaint();
        }
    }//GEN-LAST:event_jSpinnerCurrentSpriteStateChanged

    private void jMenuItemExportSpriteListActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExportSpriteListActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExportSpriteListActionPerformed
        final CustomFileChooser exportSpriteListFC = new CustomFileChooser();    
        exportSpriteListFC.setSelectedFile(new File (exportSpriteListFileName));
        if (exportSpriteListFC.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            exportSpriteListFileName = exportSpriteListFC.getSelectedFile().getAbsolutePath();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(exportSpriteListFileName), "utf-8")))
            {
                saveSpriteListToCFile(writer);
            } 
            catch (Exception e)
            {
                infoBox("Cannot export sprite positions ", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }        
    }//GEN-LAST:event_jMenuItemExportSpriteListActionPerformed

    private void jCheckBoxShowTileTypeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxShowTileTypeActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxShowTileTypeActionPerformed
        tilePainter.showTileProps = jCheckBoxShowTileType.isSelected();
        tilePainter.repaint();
    }//GEN-LAST:event_jCheckBoxShowTileTypeActionPerformed

    private void jMenuItemLoadTilePropsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemLoadTilePropsActionPerformed
    {//GEN-HEADEREND:event_jMenuItemLoadTilePropsActionPerformed
        try
        {
            JFileChooser loadTilePropsFC = new JFileChooser();    
            loadTilePropsFC.setSelectedFile(new File(tilePropsFileName));
            if (loadTilePropsFC.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                tilePropsFileName = loadTilePropsFC.getSelectedFile().getAbsolutePath();
                String gsonString = new String (Files.readAllBytes(Paths.get(tilePropsFileName)));
                Gson gson = new Gson();
                TileProps tilePropsClass = gson.fromJson(gsonString,TileProps.class); 
                if (tilePropsClass.tileProps.length == tileProps.length )
                {
                   for (int i = 0; i < tileProps.length; i++)
                   {
                           tileProps[i] = tilePropsClass.tileProps[i];
                   }
                   setListOfTileProperties(tilePropsClass.listOfTileProperties);
                }
                else
                {
                    if (questionBox("Cannot load tile properties: sizes do not match!\nDo you still want to load tile property types?", "Warning", JOptionPane.QUESTION_MESSAGE | JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)            
                        setListOfTileProperties(tilePropsClass.listOfTileProperties);
                }
                tilePainter.repaint();
            }
        }
        catch (Exception e)
        {
            infoBox("Cannot load tile properties!", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_jMenuItemLoadTilePropsActionPerformed

    private AudioFormat getOutFormat(AudioFormat baseFormat)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    class TileProps
    {
        int [] tileProps;
        TileProperties [] listOfTileProperties;
        public TileProps(int [] p, TileProperties [] l)
        {
            tileProps = p;
            listOfTileProperties = l;
        }
    }
    private void jMenuItemSaveTilePropsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemSaveTilePropsActionPerformed
    {//GEN-HEADEREND:event_jMenuItemSaveTilePropsActionPerformed
        CustomFileChooser saveTilePropsFC = new CustomFileChooser();
        saveTilePropsFC.setSelectedFile(new File(tilePropsFileName));
        if (saveTilePropsFC.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            tilePropsFileName = saveTilePropsFC.getSelectedFile().getAbsolutePath();
            String jsonString = new Gson().toJson(new TileProps(tileProps, listOfTileProperties));
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tilePropsFileName), "utf-8")))
            {
                writer.write(jsonString);
            } 
            catch (Exception e)
            {
                infoBox("Cannot save file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }        
    }//GEN-LAST:event_jMenuItemSaveTilePropsActionPerformed

    private void jMenuItemExportTilePropsNoDuplicateActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExportTilePropsNoDuplicateActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExportTilePropsNoDuplicateActionPerformed
        // TODO: we should check for props too when we remove duplicated tiles...
        // To simplify the map creation process, some tiles might be prearranged in templates.
        // This leads to repeated tiles in the tileset. 
        // To save memory, we can call this function, which eliminates the duplicates and exports a map
        // with new indexes.
        HashMap <String,ArrayList <Integer>> hashMap = new HashMap <String, ArrayList <Integer>>();        
        ArrayList<BufferedImage> bufferedImageArrayList = new ArrayList<BufferedImage>();
        ArrayList <Short> conversionMap = new ArrayList <Short>();        
        int nDifferent;
        nDifferent = removeDuplicateTiles(0, 0, null, tilePainter.bufferedImage, hashMap, bufferedImageArrayList, conversionMap, tileSizeX, tileSizeY);
        // now we convert the props.
        int [] convertedProps = new int[nDifferent];
        for (int t = 0; t < conversionMap.size(); t++)
        {
            convertedProps[conversionMap.get(t) & 0x7FFF ] = tileProps[t];
        }
        final int MAX_ENTRIES_PER_LINE = 16;
        final CustomFileChooser exportTilePropsNoDuplicatesFC = new CustomFileChooser(); 
        exportTilePropsNoDuplicatesFC.setSelectedFile(new File (exportTilePropsNoDuplicatesFileName));
        if (exportTilePropsNoDuplicatesFC.showSaveDialog(this) == CustomFileChooser.APPROVE_OPTION)
        {
            exportTilePropsNoDuplicatesFileName = exportTilePropsNoDuplicatesFC.getSelectedFile().getAbsolutePath();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(exportTilePropsNoDuplicatesFileName), "utf-8")))
            {
                StringBuffer sb = new StringBuffer();
                String s;
                writer.write ("// put this to an header file!\r\n");
                writer.write ("#include <stdint.h>\r\n");
                writer.write("#define NUMTILES "+nDifferent+"\r\n");
                for (int i = 0; i < nDifferent; i++)
                {
                    if (i % MAX_ENTRIES_PER_LINE == (MAX_ENTRIES_PER_LINE - 1))
                        sb.append(String.format(Locale.ROOT,"0x%02X, \r\n\t", convertedProps[i]));
                    else
                        sb.append(String.format(Locale.ROOT,"0x%02X, ",convertedProps[i]));                         
                }
                for (int i = 0; i < listOfTileProperties.length; i++)
                {
                    writer.write("#define " + listOfTileProperties[i].cName + " " + i +"\r\n");
                }
                writer.write("// Put this to a C file!\r\n");

                writer.write("const int8_t tileProps[NUMTILES] = \r\n{\r\n\t");
                s = sb.toString();
                writer.write(s.substring(0, s.lastIndexOf(","))); 
                writer.write("\r\n};\r\n");
            } 
            catch (Exception e)
            {
                infoBox("Cannot export tile properties ", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }                 
    }//GEN-LAST:event_jMenuItemExportTilePropsNoDuplicateActionPerformed
    BufferedImage createImageAt(int x, int y, int w, int h)
    {
        BufferedImage img = new BufferedImage(undoableState.map.getSizeX() * tileSizeX, undoableState.map.getSizeY() * tileSizeY ,BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        int mapSizeX = undoableState.map.getSizeX();
        int mapSizeY = undoableState.map.getSizeY();
        g.clearRect(0, 0, tileSizeX * mapSizeX, tileSizeY * mapSizeY);
        for (int tx = 0; tx < mapSizeX; tx++)
        {
            for (int ty = 0; ty < mapSizeY; ty++)
            {
               g.drawImage(getTileByIndex(undoableState.map.getTile(tx,ty)), tx * tileSizeX, ty * tileSizeY,  tileSizeX, tileSizeY, null);
            }
        }
        g.dispose();
        return img.getSubimage(x, y, w, h);
    }
    private void jMenuItemExportTilesToCFileNoDuplicatesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExportTilesToCFileNoDuplicatesActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExportTilesToCFileNoDuplicatesActionPerformed
        
        int nDifferent = 0;
        HashMap <String,ArrayList <Integer>> hashMap = new HashMap <>();        
        ArrayList<BufferedImage> bufferedImageArrayList = new ArrayList<>();
        ArrayList <Short> conversionMap = new ArrayList <>();        
        ArrayList <Integer> listOfUniqueIndexes = new ArrayList <>();    
        //    
        nDifferent = removeDuplicateTiles(nDifferent, 0, null, tilePainter.bufferedImage,  hashMap, bufferedImageArrayList, conversionMap,tileSizeX, tileSizeY, listOfUniqueIndexes);
        System.out.println("Different found: "+nDifferent);
        // now we have enough information. Let's save new pictures in a big one.
        ExportTileAsCfileDialog d = new ExportTileAsCfileDialog(this, true);
        d.setVisible(true);
        if (!d.confirmed)
        {
            d.dispose();
            return;  
        }
        final CustomFileChooser exportTilesToCfileFC = new CustomFileChooser(); 
        exportTilesToCfileFC.setSelectedFile(new File (exportTilesToCfileFileName));
        if (exportTilesToCfileFC.showSaveDialog(this) == CustomFileChooser.APPROVE_OPTION)
        {
            exportTilesToCfileFileName = exportTilesToCfileFC.getSelectedFile().getAbsolutePath();
            if (exportTileImageToCfile(tilePainter.bufferedImage, d, exportTilesToCfileFileName, nDifferent, nTilesX, listOfUniqueIndexes ))

                infoBox ("Tiles Exported to C file", "Notice", JOptionPane.INFORMATION_MESSAGE);
            else
                infoBox("Cannot export tiles ", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
             infoBox ("Export aborted.", "Notice", JOptionPane.INFORMATION_MESSAGE);
        }       
        d.dispose();
    }//GEN-LAST:event_jMenuItemExportTilesToCFileNoDuplicatesActionPerformed

    private void jButtonFindSpriteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonFindSpriteActionPerformed
    {//GEN-HEADEREND:event_jButtonFindSpriteActionPerformed

        int currentSprite = (Integer) jSpinnerCurrentSprite.getValue();
        SpritePosition sp = undoableState.spritePositionList.get(currentSprite);
        if (currentSprite >= undoableState.spritePositionList.size())
            return;        
        if (sp != null)
        {
            JScrollBar vSb = jScrollPaneMapArea.getVerticalScrollBar();
            JScrollBar hSb = jScrollPaneMapArea.getHorizontalScrollBar();
            int maxV = vSb.getMaximum();
            int maxH = hSb.getMaximum();
            // now, let's compute how much is displayed on the screen
            Dimension d = jPanelMapArea.getSize();
            double zoom =  d.width / (undoableState.map.getSizeX() * tileSizeX); // the smaller d, the larger the map region is shown onscreen, the smaller the zoom level
            int visibleMapWidth =  (undoableState.map.getSizeX() * tileSizeX) * jScrollPaneMapArea.getWidth()  / d.width  ; 
            int visibleMapHeight =  (undoableState.map.getSizeY() * tileSizeY) * jScrollPaneMapArea.getHeight() / d.height ; 
            int xPos, yPos;
            xPos = maxH * (sp.x - visibleMapWidth / 2) / (undoableState.map.getSizeX() * tileSizeX);
            yPos = maxV * (sp.y - visibleMapHeight / 2) / (undoableState.map.getSizeY() * tileSizeY);
            // range check
            if (xPos < 0) xPos = 0;
            if (xPos > maxH) xPos = maxH;
            if (yPos < 0) yPos = 0;
            if (yPos > maxV) yPos = maxV;
            hSb.setValue(xPos);
            vSb.setValue(yPos);
        }       
    }//GEN-LAST:event_jButtonFindSpriteActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentResized
    {//GEN-HEADEREND:event_formComponentResized
        setMapWindowZoom();
        setTileWindowZoom();
    }//GEN-LAST:event_formComponentResized

    private void jButtonDeleteSpriteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonDeleteSpriteActionPerformed
    {//GEN-HEADEREND:event_jButtonDeleteSpriteActionPerformed
        // todo fixme: we should decrease the maximum on the jSpinnerCurrentSprite
        int currentSprite = (Integer) jSpinnerCurrentSprite.getValue();
        if (undoableState.spritePositionList.size() <= currentSprite)
            return;
        SpritePosition sp = undoableState.spritePositionList.get(currentSprite);
        if (sp != null)
        {
            undoableState.spritePositionList.remove(currentSprite);
            SpinnerNumberModel model =  (SpinnerNumberModel) jSpinnerCurrentSprite.getModel();
            model.setMaximum(Integer.min(MAX_NUMBER_OF_SPRITES, undoableState.spritePositionList.size()));            
            
        }
        drawMap();          // will redraw sprites and waypoints too
        setMapWindowZoom();
    }//GEN-LAST:event_jButtonDeleteSpriteActionPerformed

    private void jMenuItemLoadSpritePositionsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemLoadSpritePositionsActionPerformed
    {//GEN-HEADEREND:event_jMenuItemLoadSpritePositionsActionPerformed
        undoableState.saveState(UndoableState.PLACE_SPRITE);
        try
        {
            final JFileChooser spritePositionsFC = new JFileChooser();
            spritePositionsFC.setSelectedFile(new File (spritePositionFileName));
            if (spritePositionsFC.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                spritePositionFileName = spritePositionsFC.getSelectedFile().getAbsolutePath();
                String gsonString = new String (Files.readAllBytes(Paths.get(spritePositionFileName)));
                Gson gson = new Gson();       
                /*SpritePosition [] newSpritePositions  =*/
                SpritePosition[] newSpritePositions = gson.fromJson(gsonString, SpritePosition[].class); 
                undoableState.spritePositionList.clear();
                for (int i = 0; i < newSpritePositions.length; i++)
                {
                    undoableState.spritePositionList.add(i, newSpritePositions[i]);
                }               
                drawMap();      // will redraw sprites and waypoints too
                setMapWindowZoom();
            }
        }
        catch (Exception e)
        {
            infoBox("Cannot load sprite positions!", "Error", JOptionPane.ERROR_MESSAGE);
        }        
    }//GEN-LAST:event_jMenuItemLoadSpritePositionsActionPerformed

    private void jMenuItemSaveSpritePositionsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemSaveSpritePositionsActionPerformed
    {//GEN-HEADEREND:event_jMenuItemSaveSpritePositionsActionPerformed
        final CustomFileChooser spritePositionsFC = new CustomFileChooser();
        spritePositionsFC.setSelectedFile(new File (spritePositionFileName));
        if (spritePositionsFC.showSaveDialog(this) == CustomFileChooser.APPROVE_OPTION)
        {
            spritePositionFileName = spritePositionsFC.getSelectedFile().getAbsolutePath();
            String jsonString = new Gson().toJson(undoableState.spritePositionList/*.toArray()*/);
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(spritePositionFileName), "utf-8")))
            {
                writer.write(jsonString);
            } 
            catch (Exception e)
            {
                infoBox("Cannot save file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }         
    }//GEN-LAST:event_jMenuItemSaveSpritePositionsActionPerformed
    void setListOfTileProperties(TileProperties [] tp)
    {
        jComboBoxTileType.removeAllItems();
        for (int i = 0; i < tp.length; i++)
            jComboBoxTileType.addItem(tp[i].name);
        listOfTileProperties = tp;
        tilePainter.listOfTileProperties = tp;
        tilePainter.repaint();
    }
    private void jButtonEditTypeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonEditTypeActionPerformed
    {//GEN-HEADEREND:event_jButtonEditTypeActionPerformed
        TilePropertiesDialog tilePropertiesDialog = new TilePropertiesDialog(this, true, listOfTileProperties);
        tilePropertiesDialog.setVisible(true);  
        if (tilePropertiesDialog.confirmed)
        {    
            setListOfTileProperties(tilePropertiesDialog.getTileProperties());
        }
        tilePropertiesDialog.dispose();
    }//GEN-LAST:event_jButtonEditTypeActionPerformed

    private void jButtonDeleteWaypointActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonDeleteWaypointActionPerformed
    {//GEN-HEADEREND:event_jButtonDeleteWaypointActionPerformed
        int currentWaypoint = (Integer) jSpinnerCurrentWaypoint.getValue();
        if (undoableState.waypointList.size() <= currentWaypoint)
            return;
        if (deleteWaypoint(currentWaypoint))
        {
            SpinnerNumberModel model =  (SpinnerNumberModel) jSpinnerCurrentWaypoint.getModel();
            model.setMaximum(Integer.min(MAX_NUMBER_OF_WAYPOINTS, undoableState.waypointList.size()));          
            model =  (SpinnerNumberModel) jSpinnerNextWaypoint1.getModel();
            model.setMaximum(Integer.min(MAX_NUMBER_OF_WAYPOINTS, undoableState.waypointList.size()));            
            model =  (SpinnerNumberModel) jSpinnerNextWaypoint2.getModel();
            model.setMaximum(Integer.min(MAX_NUMBER_OF_WAYPOINTS, undoableState.waypointList.size()));            
            drawMap();      
            setMapWindowZoom();            
        }

    }//GEN-LAST:event_jButtonDeleteWaypointActionPerformed

    private void jToggleButtonPlaceWaypointsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jToggleButtonPlaceWaypointsActionPerformed
    {//GEN-HEADEREND:event_jToggleButtonPlaceWaypointsActionPerformed
        setCurrentPlaceOperation(jToggleButtonPlaceWaypoints.isSelected() ? UndoableState.PLACE_WAYPOINT : UndoableState.PLACE_TILE);
        if (jToggleButtonPlaceWaypoints.isSelected())
            jCheckBoxShowWaypoints.setSelected(true);
    }//GEN-LAST:event_jToggleButtonPlaceWaypointsActionPerformed
    void updateWaypointSpinners()
    {
        int number = (Integer) jSpinnerCurrentWaypoint.getValue();
        if (number >= 0 && number < undoableState.waypointList.size())
        {
            Waypoint wp = undoableState.waypointList.get(number);
            if (wp != null)
            {
                jSpinnerNextWaypoint1.setValue(wp.nextWaypoint1);
                jSpinnerNextWaypoint2.setValue(wp.nextWaypoint2);
                jSpinnerWaypointSizeX.setValue(wp.rx);
                jSpinnerWaypointSizeY.setValue(wp.ry);
            }
        }         
    }
    private void jSpinnerCurrentWaypointStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerCurrentWaypointStateChanged
    {//GEN-HEADEREND:event_jSpinnerCurrentWaypointStateChanged
        updateWaypointSpinners();
        if (jCheckBoxShowWaypointNumbers.isSelected() && jCheckBoxShowWaypoints.isSelected())
        {
            drawSpritesAndWaypoints();   
            mapPainter.repaint();
        }       
    }//GEN-LAST:event_jSpinnerCurrentWaypointStateChanged

    private void jSpinnerWaypointSizeXStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerWaypointSizeXStateChanged
    {//GEN-HEADEREND:event_jSpinnerWaypointSizeXStateChanged
        int currentWaypointNumber = (Integer) jSpinnerCurrentWaypoint.getValue();
        if (currentWaypointNumber >= 0 && currentWaypointNumber < undoableState.waypointList.size())
        {
            // TODO: save only the first time in case the user keeps changing the size.
            Waypoint wp = undoableState.waypointList.get(currentWaypointNumber);
            int newValue = (Integer) jSpinnerWaypointSizeX.getValue();
            if (newValue != wp.rx)
            {
                undoableState.saveState(UndoableState.PLACE_WAYPOINT);
                wp.rx = newValue; 
            }
            drawMap();
            mapPainter.repaint();
        }        
    }//GEN-LAST:event_jSpinnerWaypointSizeXStateChanged

    private void jSpinnerWaypointSizeYStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerWaypointSizeYStateChanged
    {//GEN-HEADEREND:event_jSpinnerWaypointSizeYStateChanged
        int currentWaypointNumber = (Integer) jSpinnerCurrentWaypoint.getValue();
        if (currentWaypointNumber >= 0 && currentWaypointNumber < undoableState.waypointList.size())
        {
            // TODO: save only the first time in case the user keeps changing the size
            Waypoint wp = undoableState.waypointList.get(currentWaypointNumber);
            int newValue = (Integer) jSpinnerWaypointSizeY.getValue();
            if (newValue != wp.ry)
            {
                undoableState.saveState(UndoableState.PLACE_WAYPOINT);
                wp.ry = newValue; 
            }            
            mapPainter.repaint();
        }          
    }//GEN-LAST:event_jSpinnerWaypointSizeYStateChanged

    private void jSpinnerNextWaypoint1StateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerNextWaypoint1StateChanged
    {//GEN-HEADEREND:event_jSpinnerNextWaypoint1StateChanged
        int currentWaypointNumber = (Integer) jSpinnerCurrentWaypoint.getValue();
        if (currentWaypointNumber >= 0 && currentWaypointNumber < undoableState.waypointList.size())
        {
            // TODO: save state only once. 
            Waypoint wp = undoableState.waypointList.get(currentWaypointNumber);
            int newValue = (Integer) jSpinnerNextWaypoint1.getValue();
            if (newValue != wp.nextWaypoint1 )
            {
                undoableState.saveState(UndoableState.PLACE_WAYPOINT);
                wp.nextWaypoint1 = newValue; 
            }              
            drawMap();
            mapPainter.repaint();
        }           
    }//GEN-LAST:event_jSpinnerNextWaypoint1StateChanged

    private void jSpinnerNextWaypoint2StateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerNextWaypoint2StateChanged
    {//GEN-HEADEREND:event_jSpinnerNextWaypoint2StateChanged
        int currentWaypointNumber = (Integer) jSpinnerCurrentWaypoint.getValue();
        if (currentWaypointNumber >= 0 && currentWaypointNumber < undoableState.waypointList.size())
        {
            // TODO: save state only once.
            Waypoint wp = undoableState.waypointList.get(currentWaypointNumber);
            int newValue = (Integer) jSpinnerNextWaypoint2.getValue();
            if (newValue != wp.nextWaypoint2  )
            {
                undoableState.saveState(UndoableState.PLACE_WAYPOINT);
                wp.nextWaypoint2  = newValue; 
            }              
            drawMap();
            mapPainter.repaint();
        }           
    }//GEN-LAST:event_jSpinnerNextWaypoint2StateChanged

    private void jButtonFindWaypointActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonFindWaypointActionPerformed
    {//GEN-HEADEREND:event_jButtonFindWaypointActionPerformed
        int currentWaypointNumber = (Integer) jSpinnerCurrentWaypoint.getValue();
        if (currentWaypointNumber >= undoableState.waypointList.size())
            return;
        Waypoint wp = undoableState.waypointList.get(currentWaypointNumber);
        if (wp != null)
        {
            JScrollBar vSb = jScrollPaneMapArea.getVerticalScrollBar();
            JScrollBar hSb = jScrollPaneMapArea.getHorizontalScrollBar();
            int maxV = vSb.getMaximum();
            int maxH = hSb.getMaximum();
            // now, let's compute how much is displayed on the screen
            Dimension d = jPanelMapArea.getSize();
            double zoom =  d.width / (undoableState.map.getSizeX() * tileSizeX); // the smaller d, the larger the map region is shown onscreen, the smaller the zoom level
            int visibleMapWidth =  (undoableState.map.getSizeX() * tileSizeX) * jScrollPaneMapArea.getWidth()  / d.width  ; 
            int visibleMapHeight =  (undoableState.map.getSizeY() * tileSizeY) * jScrollPaneMapArea.getHeight() / d.height ; 
            int xPos, yPos;
            xPos = maxH * (wp.x - visibleMapWidth / 2) / (undoableState.map.getSizeX() * tileSizeX);
            yPos = maxV * (wp.y - visibleMapHeight / 2) / (undoableState.map.getSizeY() * tileSizeY);
            // range check
            if (xPos < 0) xPos = 0;
            if (xPos > maxH) xPos = maxH;
            if (yPos < 0) yPos = 0;
            if (yPos > maxV) yPos = maxV;
            hSb.setValue(xPos);
            vSb.setValue(yPos);
        }               
    }//GEN-LAST:event_jButtonFindWaypointActionPerformed

    private void jCheckBoxShowWaypointsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxShowWaypointsActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxShowWaypointsActionPerformed
        drawMap();
        mapPainter.repaint();
    }//GEN-LAST:event_jCheckBoxShowWaypointsActionPerformed

    private void jCheckBoxShowWaypointNumbersActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxShowWaypointNumbersActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxShowWaypointNumbersActionPerformed
        drawMap();
        mapPainter.repaint();
    }//GEN-LAST:event_jCheckBoxShowWaypointNumbersActionPerformed

    private void jCheckBoxShowNextWaypointsStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jCheckBoxShowNextWaypointsStateChanged
    {//GEN-HEADEREND:event_jCheckBoxShowNextWaypointsStateChanged
        drawMap();
        mapPainter.repaint();
    }//GEN-LAST:event_jCheckBoxShowNextWaypointsStateChanged

    private void jMenuItemLoadWaypointsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemLoadWaypointsActionPerformed
    {//GEN-HEADEREND:event_jMenuItemLoadWaypointsActionPerformed
        try
        {
            final JFileChooser waypointListFC = new JFileChooser();    
            waypointListFC.setSelectedFile(new File (waypointListFileName));
            if (waypointListFC.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                waypointListFileName = waypointListFC.getSelectedFile().getAbsolutePath();
                String gsonString = new String (Files.readAllBytes(Paths.get(waypointListFileName)));
                Gson gson = new Gson();       
                Waypoint[] newWaypoints = gson.fromJson(gsonString, Waypoint[].class); 
                undoableState.waypointList.clear();
                for (int i = 0; i < newWaypoints.length; i++)
                {
                    undoableState.waypointList.add(i, newWaypoints[i]);
                }    
                SpinnerNumberModel model =  (SpinnerNumberModel) jSpinnerCurrentWaypoint.getModel();
                model.setMaximum(Integer.min(MAX_NUMBER_OF_WAYPOINTS, undoableState.waypointList.size()));          
                model =  (SpinnerNumberModel) jSpinnerNextWaypoint1.getModel();
                model.setMaximum(Integer.min(MAX_NUMBER_OF_WAYPOINTS, undoableState.waypointList.size()));            
                model =  (SpinnerNumberModel) jSpinnerNextWaypoint2.getModel();
                model.setMaximum(Integer.min(MAX_NUMBER_OF_WAYPOINTS, undoableState.waypointList.size()));            
                drawMap();      // will redraw sprites and waypoints too
                setMapWindowZoom();
            }
        }
        catch (Exception e)
        {
            infoBox("Cannot load waypoints!", "Error", JOptionPane.ERROR_MESSAGE);
        }         
    }//GEN-LAST:event_jMenuItemLoadWaypointsActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem1ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem1ActionPerformed
        final CustomFileChooser waypointListFC = new CustomFileChooser(); 
        waypointListFC.setSelectedFile(new File (waypointListFileName));
        if (waypointListFC.showSaveDialog(this) == CustomFileChooser.APPROVE_OPTION)
        {
            waypointListFileName = waypointListFC.getSelectedFile().getAbsolutePath();
            String jsonString = new Gson().toJson(undoableState.waypointList);
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(waypointListFileName), "utf-8")))
            {
                writer.write(jsonString);       
            } 
            catch (Exception e)
            {
                infoBox("Cannot save file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }           
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItemExportWaypointsToCFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExportWaypointsToCFileActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExportWaypointsToCFileActionPerformed
        ExportWaypointListDialog d = new ExportWaypointListDialog(this, true);
        d.setVisible(true);
        if (d.confirmed)
        {
            final CustomFileChooser exportWaypointListFC = new CustomFileChooser();        
            exportWaypointListFC.setSelectedFile(new File (exportWaypointListFileName));
            if (exportWaypointListFC.showSaveDialog(this) == CustomFileChooser.APPROVE_OPTION)
            {
                exportWaypointListFileName = exportWaypointListFC.getSelectedFile().getAbsolutePath();
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(exportWaypointListFileName), "utf-8")))
                {
                    saveWaypointListToCFile(writer, d);
                } 
                catch (Exception e)
                {
                    infoBox("Cannot export waypoints!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }    
        }
        else
            infoBox("Operation aborted!", "Notice", JOptionPane.INFORMATION_MESSAGE);
        d.dispose();
    }//GEN-LAST:event_jMenuItemExportWaypointsToCFileActionPerformed

    private void jMenuItemExportTrigTableActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExportTrigTableActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExportTrigTableActionPerformed
        final ExportTableDialog exportTableDialog = new ExportTableDialog(this, true);
        exportTableDialog.setVisible(true);
        if (exportTableDialog.confirmed)
        {
            final int steps = exportTableDialog.getNumberOfEntries();
            final CustomFileChooser exportTrigTableFC = new CustomFileChooser();  
            exportTrigTableFC.setSelectedFile(new File (exportTrigTableFileName));
            if (exportTrigTableFC.showSaveDialog(this) == CustomFileChooser.APPROVE_OPTION)
            {
                exportTrigTableFileName = exportTrigTableFC.getSelectedFile().getAbsolutePath();
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(exportTrigTableFileName), "utf-8")))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("// Put the following lines in a header file!\r\n");
                    sb.append("#include <stdint.h>\r\n");
                    sb.append("#define NUMBER_OF_TRIG_ENTRIES "+steps+"\r\n");
                    sb.append("extern const int16_t " + exportTableDialog.getTableName() + "[NUMBER_OF_TRIG_ENTRIES];\r\n");
                    //
                    sb.append("// Put the following lines in the C file!\r\n");
                    sb.append("#include <stdint.h>\r\n");
                    sb.append("const int16_t " + exportTableDialog.getTableName() + "[NUMBER_OF_TRIG_ENTRIES] = \r\n{\r\n");
                    for (int i = 0; i < steps; i++)
                    {
                        double functionValue;
                        int fixedValue;
                        // we need to convert this to an 1.15 float.
                        switch (exportTableDialog.getFunctionType())
                        {
                            case ExportTableDialog.SQRT:
                                functionValue = Math.sqrt(((double)i)/steps);
                                fixedValue = ((int) (functionValue * 65536)) & 0xFFFF;
                                break;
                            case ExportTableDialog.SINE:
                                functionValue = Math.sin(2*Math.PI * i / steps);
                                if (functionValue == 1)
                                    fixedValue = 32767;
                                else
                                    fixedValue = ((int) (functionValue * 32768)) & 0xFFFF;
                                break;
                            case ExportTableDialog.COSINE:
                                functionValue = Math.cos(2*Math.PI * i / steps);
                                if (functionValue == 1)
                                    fixedValue = 32767;
                                else
                                    fixedValue = ((int) (functionValue * 32768)) & 0xFFFF;
                                break;
                            case ExportTableDialog.ATAN:
                                functionValue = Math.atan(  ( 2.0 * i - steps) / steps) / Math.PI;
                                fixedValue = (int) ( 32768 * 4 * functionValue );
                                break;
                            default:
                                // should not reach this!
                                fixedValue = 0;
                                break;
                        }
                        if ((i & 15) == 0)
                        sb.append("\r\n\t");
                        sb.append(String.format(Locale.ROOT,"0x%04X, ",fixedValue & 65535));
                    }
                    String s = sb.toString();
                    writer.write(s.substring(0, s.lastIndexOf(",")));
                    writer.write("\r\n};\r\n");
                }
                catch (Exception e)
                {
                    infoBox("Cannot export table ", "Error", JOptionPane.ERROR_MESSAGE);
                    exportTableDialog.dispose();
                    return;
                }
                infoBox("Operation successful", "Information", JOptionPane.INFORMATION_MESSAGE);
                exportTableDialog.dispose();
                return;
            }
        }
        infoBox("Operation aborted", "Information", JOptionPane.INFORMATION_MESSAGE);
        exportTableDialog.dispose();
    }//GEN-LAST:event_jMenuItemExportTrigTableActionPerformed

    private void jCheckBoxShowTilePriorityActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxShowTilePriorityActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxShowTilePriorityActionPerformed
        mapPainter.showTilePriority = jCheckBoxShowTilePriority.isSelected();
        mapPainter.repaint();        
    }//GEN-LAST:event_jCheckBoxShowTilePriorityActionPerformed

    private void jMenuItemBackgroundRainbowEditorActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemBackgroundRainbowEditorActionPerformed
    {//GEN-HEADEREND:event_jMenuItemBackgroundRainbowEditorActionPerformed
        if (paletteRemapEditor == null)
            paletteRemapEditor = new PaletteRemapEditor();
        paletteRemapEditor.setVisible(true);
    }//GEN-LAST:event_jMenuItemBackgroundRainbowEditorActionPerformed

    private void jInternalFrameTilesComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_jInternalFrameTilesComponentResized
    {//GEN-HEADEREND:event_jInternalFrameTilesComponentResized
        setTileWindowZoom();
    }//GEN-LAST:event_jInternalFrameTilesComponentResized

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem2ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem2ActionPerformed
        
        USVCPackagerDialog upd = new USVCPackagerDialog(null, true, usvcPackagerBinaryFileName, usvcPackagerMetaFileName, usvcPackagerImageFileName, usvcPackagerOutputFileName, usvcPackagerDirectory);
        upd.setVisible(true);
        if (upd.confirmed == true)
        {
            usvcPackagerBinaryFileName = upd.getBinaryFileName();
            usvcPackagerMetaFileName = upd.getMetaFileName();
            usvcPackagerImageFileName = upd.getImageFileName();
            usvcPackagerOutputFileName = upd.getOutputFileName();
            usvcPackagerDirectory = upd.getDirectory();
            boolean success = UsvcPackager.createPackage(usvcPackagerBinaryFileName, usvcPackagerMetaFileName, usvcPackagerImageFileName, usvcPackagerOutputFileName);
            if (success)
               infoBox("Package created at "+usvcPackagerOutputFileName, "Done!", JOptionPane.INFORMATION_MESSAGE);
            else
                infoBox("uSVC Game packaging failed", "Error", JOptionPane.ERROR_MESSAGE);
        }
        upd.dispose();        
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem3ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem3ActionPerformed
        MidiFileConvertDialog mfcd = new MidiFileConvertDialog(this, true);
        mfcd.setOptions(midiConvertOptions);
        mfcd.setVisible(true);
        if (mfcd.confirmed)
        {
            midiConvertOptions = mfcd.getOptions();
            MidiConverter mc = new MidiConverter(midiConvertOptions);
            try 
            {
                mc.convertSong();
                infoBox("Midi exported!", "Done", JOptionPane.INFORMATION_MESSAGE);
            }
            catch (Exception e)
            {
                e.printStackTrace();
               infoBox("Cannot export midi file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed
    
    private void jMenuItemCreateSpriteSetActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemCreateSpriteSetActionPerformed
    {//GEN-HEADEREND:event_jMenuItemCreateSpriteSetActionPerformed
        if (spriteSetBuilder == null)
            spriteSetBuilder = new SpriteSetBuilder();
        spriteSetBuilder.setVisible(true);
    }//GEN-LAST:event_jMenuItemCreateSpriteSetActionPerformed
    void checkForSpritesAndWaypointsAfterUndoOrRedo()
    {
        // sprites
        int maxSprite = undoableState.spritePositionList.size();
        int currentSprite = (Integer) jSpinnerCurrentSprite.getValue();
        SpinnerNumberModel model =  (SpinnerNumberModel) jSpinnerCurrentSprite.getModel();
        model.setMaximum(Integer.min(MAX_NUMBER_OF_SPRITES, maxSprite));
        if (currentSprite > maxSprite)
            jSpinnerCurrentSprite.setValue(maxSprite);
        // waypoints
        int currentWaypoint = (Integer) jSpinnerCurrentWaypoint.getValue();
        int maxWaypoint = undoableState.waypointList.size();
        if (currentWaypoint > maxWaypoint)
            jSpinnerCurrentWaypoint.setValue(maxWaypoint);
        model =  (SpinnerNumberModel) jSpinnerCurrentWaypoint.getModel();
        model.setMaximum(Integer.min(MAX_NUMBER_OF_WAYPOINTS, maxWaypoint));          
        model =  (SpinnerNumberModel) jSpinnerNextWaypoint1.getModel();
        model.setMaximum(Integer.min(MAX_NUMBER_OF_WAYPOINTS, maxWaypoint));            
        model =  (SpinnerNumberModel) jSpinnerNextWaypoint2.getModel();
        model.setMaximum(Integer.min(MAX_NUMBER_OF_WAYPOINTS, maxWaypoint));
        updateWaypointSpinners();
    }
    void redo()
    {
        undoableState.redo();
        checkForSpritesAndWaypointsAfterUndoOrRedo();
        updateMapPainterMapData();
        drawMap();
        setMapWindowZoom();
    }
    void undo()
    {
        undoableState.undo();
        checkForSpritesAndWaypointsAfterUndoOrRedo();
        updateMapPainterMapData();
        drawMap();
        setMapWindowZoom();
    }
    private void jCheckBoxSetTilePriorityActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxSetTilePriorityActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxSetTilePriorityActionPerformed
        if (jCheckBoxSetTilePriority.isSelected())
            currentPlaceOperation = UndoableState.PLACE_TILE;
    }//GEN-LAST:event_jCheckBoxSetTilePriorityActionPerformed

    private void jMenuItemUndoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemUndoActionPerformed
    {//GEN-HEADEREND:event_jMenuItemUndoActionPerformed
        undo();
    }//GEN-LAST:event_jMenuItemUndoActionPerformed

    private void jMenuItemRedoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemRedoActionPerformed
    {//GEN-HEADEREND:event_jMenuItemRedoActionPerformed
        redo();
    }//GEN-LAST:event_jMenuItemRedoActionPerformed

    private void jInternalFrameMapComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_jInternalFrameMapComponentResized
    {//GEN-HEADEREND:event_jInternalFrameMapComponentResized
        setMapWindowZoom();
    }//GEN-LAST:event_jInternalFrameMapComponentResized

    private void jSliderZoomMapStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSliderZoomMapStateChanged
    {//GEN-HEADEREND:event_jSliderZoomMapStateChanged
        setMapWindowZoom();
    }//GEN-LAST:event_jSliderZoomMapStateChanged

    private void jMenuItemExportWavActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExportWavActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExportWavActionPerformed
       
        final JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File (wavFileName));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            wavFileName = fc.getSelectedFile().getAbsolutePath();           
            // let's convert already the wave. If conversion fails, we won't bother the user for asking variable name.
            try 
            {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(wavFileName)));
                if (ais != null) 
                {
                    
                    final AudioFormat baseFormat = ais.getFormat();
                    AudioFormat decodeFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,        // signed values
                            baseFormat.getSampleRate(),             // keep sample rate.
                            8,                                      // uSVC supports only 8 bit
                            1,                                      // only one channel
                            1,                                      // frame = 1 byte
                            baseFormat.getSampleRate(),             // frame rate.
                            false                                   // little endian
                    );
                    AudioInputStream dais = AudioSystem.getAudioInputStream(decodeFormat, ais);
                    // Now it has been successfully decoded. We can ask for save file 
                    ExportWavDialog ewd = new ExportWavDialog(this, true, wavVariableName);
                    ewd.setVisible(true);
                    if (ewd.confirmed)
                    {
                        wavVariableName = ewd.getVariableName();
                        final CustomFileChooser sfc = new CustomFileChooser(); 
                        sfc.setDialogTitle("Select exported wav C file name");
                        sfc.setSelectedFile(new File (convertedWavFileName));
                        if (sfc.showSaveDialog(this) == CustomFileChooser.APPROVE_OPTION)
                        {
                            convertedWavFileName = sfc.getSelectedFile().getAbsolutePath();
                            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(convertedWavFileName), "utf-8")))
                            {
                                StringBuilder sb = new StringBuilder("const char "+wavVariableName + "[" + wavVariableName.toUpperCase()+"_NUM_ELEMENTS] = \r\n{\r\n\t");
                                int numSamples = 0;
                                while (dais.available() > 0)
                                {
                                    byte b = (byte) dais.read();
                                    sb.append(b + ",");
                                    numSamples++;
                                    if (numSamples % 16 == 0)
                                        sb.append("\r\n\t");
                                }
                                if (numSamples % 16 == 0)
                                {
                                    sb.deleteCharAt(sb.length() -1 );
                                    sb.append("};\r\n");
                                }
                                else
                                    sb.append("\r\n};\r\n");
                                
                                writer.write ("// put this to the soundData header file!\r\n");
                                writer.write ("#include <stdint.h> //only if not already included!\r\n");
                                writer.write("#define "+ wavVariableName.toUpperCase()+"_NUM_ELEMENTS " + numSamples + "\r\n");
                                writer.write("#define "+ wavVariableName.toUpperCase()+"_SPS " + ((int)decodeFormat.getSampleRate()) + "\r\n");
                                writer.write("extern const char "+wavVariableName + "[" + wavVariableName.toUpperCase()+"_NUM_ELEMENTS];\r\n");
                                writer.write("// Put this the soundData C file!\r\n");
                                writer.write ("#include \"soundData header file where you have placed all the defines\" //only if not already included!\r\n");
                                writer.write(sb.toString()); 
                                infoBox("Wave exported to C file!", "Done", JOptionPane.INFORMATION_MESSAGE);                                                        
                            } 
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                infoBox("Cannot export wave file!", "Error", JOptionPane.ERROR_MESSAGE);
                            }                
                        }    
                    }
                    else
                        infoBox("Operation aborted.", "Notice", JOptionPane.INFORMATION_MESSAGE);                        
                    dais.close();
                    ais.close();
                }
            }
            catch (Exception e)
            {
                infoBox("Cannot open wave file!", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_jMenuItemExportWavActionPerformed
    private class TilesMouseListener extends MouseAdapter
    {
        boolean isSelecting = false;
        public void mouseExited(MouseEvent e)
        {
            if (jCheckBoxSetTileType.isSelected())
                return;
            updateTilePainterSelection(); 
        }
        private void updateTilePainterSelection()
        {
            boolean changed = tilePainter.selectedX1 != currentSelectedTileX1 || tilePainter.selectedX2 != currentSelectedTileX2 || tilePainter.selectedY1 != currentSelectedTileY1 || tilePainter.selectedY2 != currentSelectedTileY2 || tilePainter.showSelection != isSelecting;
            if (changed)
            {
                tilePainter.selectedX1 = currentSelectedTileX1;
                tilePainter.selectedX2 = currentSelectedTileX2;
                tilePainter.selectedY1 = currentSelectedTileY1;
                tilePainter.selectedY2 = currentSelectedTileY2;
                tilePainter.showSelection = isSelecting;
                tilePainter.repaint();
            }
        }
        public void mousePressed (MouseEvent e)
        {
            Point p =  e.getPoint();
            int mx = p.x;
            int my = p.y;
            // now get jPanelTileArea size
            Dimension d = jPanelTileArea.getSize();
            // get current zoom factor            
            double zoom =  d.width / ((double) nTilesX * tileSizeX);
            int xTile = (int) (p.x / (zoom *tileSizeX )); 
            int yTile = (int) (p.y / (zoom *tileSizeY ));
            if (xTile < 0)
                xTile = 0;
            if (yTile < 0)
                yTile = 0;
            if (xTile >= nTilesX)
                xTile = nTilesX - 1;
            if (yTile >= nTilesY)
                yTile = nTilesY - 1;
            if (jCheckBoxSetTileType.isSelected())
            {
                // 
                tileProps[getTileIndexByNxy(xTile, yTile)] = jComboBoxTileType.getSelectedIndex();
                isSelecting = false;
                tilePainter.repaint();
                return;
            }
            setCurrentPlaceOperation(UndoableState.PLACE_TILE);
            isSelecting = true;
            // get mouse coordinates relative to jPanelTileArea
            currentSelectedTileX1 = xTile;
            currentSelectedTileY1 = yTile;
            currentSelectedTileX2 = xTile;
            currentSelectedTileY2 = yTile;
            // 
            currentSelectedTile = getTileIndexByNxy(xTile, yTile);
            currentTilePainter.bufferedImage = getTileByIndex(currentSelectedTile);     
            redrawCurrentTilePanel();
            //        
            updateTilePainterSelection();
        }    
        public void mouseReleased (MouseEvent e)
        {
            if (jCheckBoxSetTileType.isSelected())
            {
                isSelecting = false;
                return;
            }            
            
            setCurrentPlaceOperation(UndoableState.PLACE_TILE);
            isSelecting = false;
            // get mouse coordinates relative to jPanelTileArea
            Point p =  e.getPoint();
            // now get jPanelTileArea size
            Dimension d = jPanelTileArea.getSize();
            // get current zoom factor            
            double zoom =  d.width / ((double) nTilesX * tileSizeX);
            int xTile = (int) (p.x / (zoom *tileSizeX )); 
            int yTile = (int) (p.y / (zoom *tileSizeY ));
            if (currentSelectedTileX1 > xTile)
                xTile = currentSelectedTileX1;
            if (currentSelectedTileY1 > yTile)
                yTile = currentSelectedTileY1;
            currentSelectedTileX2 = xTile;
            currentSelectedTileY2 = yTile;
            currentSelectedTile = getTileIndexByNxy(xTile, yTile);
            currentTilePainter.bufferedImage = getTileBlockByCoordinates(currentSelectedTileX1, currentSelectedTileY1, currentSelectedTileX2, currentSelectedTileY2, true);     
            redrawCurrentTilePanel();
            // and now compute which tile has been selected.
            updateTilePainterSelection();
        }   
        public void mouseDragged(MouseEvent e)
        {
            if (jCheckBoxSetTileType.isSelected())
            {
                mousePressed(e);
                isSelecting = false;
                return;
            }            
            setCurrentPlaceOperation(UndoableState.PLACE_TILE);            
            if (isSelecting)
            {
                Point p =  e.getPoint();
                // now get jPanelTileArea size
                Dimension d = jPanelTileArea.getSize();
                int dw = d.width;
                int dh = d.height;
                // get current zoom factor            
                double zoom =  d.width / ((double) nTilesX * tileSizeX);
                int xTile = (int) (p.x / (zoom *tileSizeX )); 
                int yTile = (int) (p.y / (zoom *tileSizeY ));
                if (currentSelectedTileX1 > xTile)
                    xTile = currentSelectedTileX1;
                if (currentSelectedTileY1 > yTile)
                    yTile = currentSelectedTileY1;
                boolean changed = currentSelectedTileX2 != xTile || currentSelectedTileY2 != yTile;
                if (changed)
                {
                    currentSelectedTileX2 = xTile;
                    currentSelectedTileY2 = yTile;
                    currentSelectedTile = getTileIndexByNxy(xTile, yTile);
                    currentTilePainter.bufferedImage  = getTileBlockByCoordinates(currentSelectedTileX1, currentSelectedTileY1, currentSelectedTileX2, currentSelectedTileY2, true);     
                    redrawCurrentTilePanel();
                    updateTilePainterSelection();   
                }
            }
            else
            {
                mousePressed(e);
            }
        }
    }
    
    private class SpriteMouseListener extends MouseAdapter
    {   
        public void mouseClicked (MouseEvent e)
        {
            setCurrentPlaceOperation(UndoableState.PLACE_SPRITE);           
            // get mouse coordinates relative to jPanelTileArea
            Point p =  e.getPoint();
            // now get jPanelSpriteArea size
            Dimension d = spritePainter.getPreferredSize();//jPanelSpriteArea.getSize();
            // get current zoom factor            
            double zoom =  d.width / ((double) sprites.numberOfEntities * sprites.maxWidth);
            int nEntity = (int) (p.x / (zoom *sprites.maxWidth )); 
            if (nEntity >= sprites.numberOfEntities)
                nEntity = sprites.numberOfEntities -1;
            currentSelectedSpriteImage = nEntity;
            if (sprites.painterImage != null)   // 
            {
                Graphics2D g2d = (Graphics2D) currentSpritePainter.bufferedImage.getGraphics();
                g2d.clearRect(0, 0, currentSpritePainter.bufferedImage.getWidth(), currentSpritePainter.bufferedImage.getHeight());
                // get the sprite image
                Entity ent = sprites.entityArray.get(nEntity);
                BufferedImage sImg = ent.entityDisplayImage;
                // now calculate the maximum zoom we can have without distortion
                double zoomX = currentSpritePainter.bufferedImage.getWidth() / ((double) sImg.getWidth());
                double zoomY = currentSpritePainter.bufferedImage.getHeight() / ((double) sImg.getHeight());
                double minZoom = Math.min(zoomX, zoomY);
                g2d.drawImage(sImg,currentSpritePainter.bufferedImage.getWidth() / 2 - (int) (minZoom * sImg.getWidth() / 2)  , 
                                   currentSpritePainter.bufferedImage.getHeight() / 2 - (int) (minZoom * sImg.getHeight() / 2),
                                   (int) (minZoom * sImg.getWidth()) ,
                                   (int) (minZoom * sImg.getHeight()), null);           
                //currentSpritePainter.bufferedImage = sprites.painterImage.getSubimage(xSprite * sprites.maxWidth, 0, sprites.maxWidth, sprites.maxHeight);     
                currentSpritePainter.repaint();
                g2d.dispose();
            }
        }
    }
    private class MapMouseListener extends MouseAdapter
    {
        boolean isSelecting = false;
        boolean isFilling = false;
        public void mouseReleased (MouseEvent e)
        {
            isSelecting = false;

            if (isFilling)
            {
                isFilling = false;
                // get mouse coordinates relative to jPanelTileArea
                Point p =  e.getPoint();
                int mx = p.x;
                int my = p.y;
                // now get jPanelTileArea size
                Dimension d = jPanelMapArea.getSize();
                int dw = d.width;
                int dh = d.height;
                // get current zoom factor            
                double zoom =  d.width / ((double) undoableState.map.getSizeX() * tileSizeX);
                int xTile = (int) (p.x / (zoom *tileSizeX )); 
                int yTile = (int) (p.y / (zoom *tileSizeY ));
                if (currentSelectedTileX1 > xTile)
                    xTile = currentSelectedTileX1;
                if (currentSelectedTileY1 > yTile)
                    yTile = currentSelectedTileY1;
                currentSelectedTileX2 = xTile;
                currentSelectedTileY2 = yTile;
                // now fill the selected area with the currently selected block
                Graphics g = mapPainter.bufferedImage.getGraphics();            
                for (int x = currentSelectedTileX1; x <= currentSelectedTileX2; x++ )
                {
                    for (int y = currentSelectedTileY1; y <= currentSelectedTileY2; y++)
                    {
                        if (x < undoableState.map.getSizeX() && y < undoableState.map.getSizeY())
                        {
                            int index = currentTileBlock[(y - currentSelectedTileY1) % currentTileBlock.length][(x - currentSelectedTileX1) % currentTileBlock[0].length];//getTileIndexByNxy(currentSelectedTileX1 + x, currentSelectedTileY1 +y );
                            undoableState.map.setTile(x, y, (short) index) ;
                            g.clearRect(x * tileSizeX, y * tileSizeY, tileSizeX, tileSizeY);
                            g.drawImage(getTileByIndex(index), x * tileSizeX, y * tileSizeY,  tileSizeX, tileSizeY, null);
                        }                    
                    }
                }
                g.dispose();
            }
            else if (currentPlaceOperation == UndoableState.PLACE_SPRITE)
            {
                if (jCheckBoxAutoIncrementSpriteNumber.isSelected())
                {
                    SpinnerNumberModel model =  (SpinnerNumberModel) jSpinnerCurrentSprite.getModel();
                    int currentSprite = (Integer) jSpinnerCurrentSprite.getValue();                
                    model.setValue(currentSprite + 1);
                }
            }
            else if (currentPlaceOperation == UndoableState.PLACE_WAYPOINT)
            {
                if (jCheckBoxAutoIncrementWaypointNumber.isSelected())
                {
                    SpinnerNumberModel model =  (SpinnerNumberModel) jSpinnerCurrentWaypoint.getModel();
                    int currentWaypoint = (Integer) jSpinnerCurrentWaypoint.getValue();                
                    model.setValue(currentWaypoint + 1);                 
                }
            }            
            updateMapPainterSelection(); 
        }           
        public void mouseExited(MouseEvent e)
        {
            mapPainter.previewSelectionImage = null;
            mapPainter.repaint();            
            updateMapPainterSelection(); 
        }
        private void updateMapPainterSelection()
        {
            boolean changed = mapPainter.selectedX1 != currentSelectedTileX1 || mapPainter.selectedX2 != currentSelectedTileX2 || mapPainter.selectedY1 != currentSelectedTileY1 
                    || mapPainter.selectedY2 != currentSelectedTileY2 || mapPainter.showSelection != (isSelecting || isFilling);
            if (changed)
            {
                mapPainter.selectedX1 = currentSelectedTileX1;
                mapPainter.selectedX2 = currentSelectedTileX2;
                mapPainter.selectedY1 = currentSelectedTileY1;
                mapPainter.selectedY2 = currentSelectedTileY2;
                mapPainter.showSelection = isSelecting || isFilling;
                mapPainter.repaint();
            }
        }
        public void handleMouseDown(MouseEvent e)
        {
              // get mouse coordinates relative to jPanelTileArea
            Point p =  e.getPoint();
            // now get jPanelTileArea size
            Dimension d = jPanelMapArea.getSize();
            // get current zoom factor            
            int mapSizeX = undoableState.map.getSizeX();
            int mapSizeY = undoableState.map.getSizeY();
            double zoom =  d.width / ((double) mapSizeX  * tileSizeX);
            int xTile = (int) (p.x / (zoom *tileSizeX )); 
            int yTile = (int) (p.y / (zoom *tileSizeY )); 
            if (xTile < 0)
                xTile = 0;
            if (yTile < 0)
                yTile = 0;
            if (jCheckBoxSetTilePriority.isSelected())
            {                
                if ((e.getButton() == MouseEvent.BUTTON1) || 0 != (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK))
                    undoableState.map.setPriority(xTile, yTile, 1);
                else if ((e.getButton() == MouseEvent.BUTTON3) || 0 != (e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK))
                     undoableState.map.setPriority(xTile, yTile, 0);                  
                isSelecting = false;
                mapPainter.repaint();
                return;
            }                        
            if (currentPlaceOperation == UndoableState.PLACE_TILE)
            {
                if (e.isShiftDown()) // selection
                {
                    isSelecting = true;
                    currentSelectedTileX1 = xTile;
                    currentSelectedTileY1 = yTile;
                    currentSelectedTileX2 = xTile;
                    currentSelectedTileY2 = yTile;
                    // 
                    //
                    currentSelectedTile = undoableState.map.getTile(xTile, yTile);
                    currentTilePainter.bufferedImage = getTileByIndex(currentSelectedTile);     
                    redrawCurrentTilePanel();
                    updateMapPainterSelection();
                }
                else if (e.isControlDown())
                {
                    isSelecting = false;
                    isFilling = true;
                    currentSelectedTileX1 = xTile;
                    currentSelectedTileY1 = yTile;
                    currentSelectedTileX2 = xTile;
                    currentSelectedTileY2 = yTile;                   
                    updateMapPainterSelection();
                }
                else
                {
                    isSelecting = false;
                    isFilling = false;
                    // draw current selected block
                    int blockW = /*currentSelectedTileX2 - currentSelectedTileX1 + 1*/ currentTileBlock[0].length;
                    int blockH = /*currentSelectedTileY2 - currentSelectedTileY1 + 1*/currentTileBlock.length;
                    Graphics g = mapPainter.bufferedImage.getGraphics();            
                    for (int x = 0; x < blockW; x++ )
                    {
                        for (int y = 0; y < blockH; y++)
                        {
                            int xx = xTile + x;
                            int yy = yTile + y;
                            if (xx < mapSizeX && yy < mapSizeY)
                            {
                                int index = currentTileBlock[y][x];//getTileIndexByNxy(currentSelectedTileX1 + x, currentSelectedTileY1 +y );
                                undoableState.map.setTile(xx,yy, (short) index) ;
                                g.clearRect(xx * tileSizeX, yy * tileSizeY, tileSizeX, tileSizeY);
                                g.drawImage(getTileByIndex(index), xx * tileSizeX, yy * tileSizeY,  tileSizeX, tileSizeY, null);
                            }                    
                        }
                    }
                    g.dispose();
                    updateMapPainterSelection();
                }
            }
            else if (currentPlaceOperation == UndoableState.PLACE_SPRITE)
            {
                // we need to remove the old sprite position, redraw the area around it, and 
                // place the new sprite. Then, draw the sprites
                // get which sprite was drawn and where
                int currentSprite = (Integer) jSpinnerCurrentSprite.getValue();
                if (undoableState.spritePositionList.size() > currentSprite)
                {
                    SpritePosition pos = undoableState.spritePositionList.get(currentSprite);
                    int xSprite = pos.x;
                    int ySprite = pos.y;
                    int entity = pos.entityNumber;
                    // get the image for the sprite
                    if (sprites.entityArray.size() > entity)
                    {
                        Entity ent = sprites.entityArray.get(entity);
                        //
                        xTile = (xSprite - ent.entityDisplayHorizontalOffset) /  tileSizeX - 1  ; 
                        yTile = (ySprite - ent.entityDisplayVerticalOffset)  / tileSizeY - 1 ; 
                        // draw current selected block
                        int blockW = ent.entityDisplayImage.getWidth();
                        int blockH = ent.entityDisplayImage.getHeight();
                        Graphics g = mapPainter.bufferedImage.getGraphics();            
                        for (int x = 0; x < blockW; x++ )
                        {
                            for (int y = 0; y < blockH; y++)
                            {
                                int xx = xTile + x;
                                int yy = yTile + y;
                                if (xx < mapSizeX && yy < mapSizeY && xx >= 0 && yy >= 0)
                                {
                                    g.clearRect(xx * tileSizeX, yy * tileSizeY, tileSizeX, tileSizeY);
                                    g.drawImage(getTileByIndex(undoableState.map.getTile(xx, yy)), xx * tileSizeX, yy * tileSizeY,  tileSizeX, tileSizeY, null);
                                }                    
                            }
                        }  
                        g.dispose();
                        // now place the new sprite by setting the new coordinates and entity number
                        //ent = sprites.entityArray.get(currentSelectedSpriteImage); 
                        pos.x = (int) (p.x / zoom  ); 
                        pos.y = (int) (p.y / zoom  );             
                        pos.entityNumber = currentSelectedSpriteImage;
                    }
                }
                else
                {
                    // let's add a new sprite
                    undoableState.spritePositionList.add(currentSprite, new SpritePosition ((int) (p.x / (zoom  )), (int) (p.y / (zoom)), currentSelectedSpriteImage) );
                    SpinnerNumberModel model =  (SpinnerNumberModel) jSpinnerCurrentSprite.getModel();
                    model.setMaximum(Integer.min(MAX_NUMBER_OF_SPRITES, undoableState.spritePositionList.size()));
                }
            }
            else if (currentPlaceOperation == UndoableState.PLACE_WAYPOINT)
            {
                // we need to remove the old waypoint position, redraw the area around it, and 
                // place the new waypoint. Then, draw the waypoints and sprites.
                // get which sprite was drawn and where
                int currentWaypointNumber = (Integer) jSpinnerCurrentWaypoint.getValue();
                int newRx =(Integer) jSpinnerWaypointSizeX.getValue();
                int newRy = (Integer) jSpinnerWaypointSizeY.getValue();
                int newDest1 = (Integer) jSpinnerNextWaypoint1.getValue();
                int newDest2 = (Integer) jSpinnerNextWaypoint2.getValue();
                if (undoableState.waypointList.size() > currentWaypointNumber)
                {
                    Waypoint wp = undoableState.waypointList.get(currentWaypointNumber);
                    int xWp = wp.x;
                    int yWp = wp.y;
                    xTile = (xWp - wp.rx - 1) /  tileSizeX - 1  ; 
                    yTile = (yWp - wp.ry - 1)  / tileSizeY - 1 ; 
                    // draw current selected block
                    int blockW = wp.rx + 2;
                    int blockH = wp.ry + 2;
                    Graphics g = mapPainter.bufferedImage.getGraphics();            
                    for (int x = 0; x < blockW; x++ )
                    {
                        for (int y = 0; y < blockH; y++)
                        {
                            int xx = xTile + x;
                            int yy = yTile + y;
                            if (xx < mapSizeX && yy < mapSizeY && xx >= 0 && yy >= 0)
                            {
                                g.clearRect(xx * tileSizeX, yy * tileSizeY, tileSizeX, tileSizeY);
                                g.drawImage(getTileByIndex(undoableState.map.getTile(xx, yy)), xx * tileSizeX, yy * tileSizeY,  tileSizeX, tileSizeY, null);
                            }                    
                        }
                    }  
                    g.dispose();
                    // now place the new wayèpoint
                    wp.x = (int) (p.x / zoom  ); 
                    wp.y = (int) (p.y / zoom  );             
                }
                else
                {
                    // let's add a new waypoint
                    undoableState.waypointList.add(currentWaypointNumber, new Waypoint ((int) (p.x / (zoom  )), (int) (p.y / (zoom)), newRx, newRy, newDest1, newDest2) );
                    SpinnerNumberModel model =  (SpinnerNumberModel) jSpinnerCurrentWaypoint.getModel();
                    model.setMaximum(Integer.min(MAX_NUMBER_OF_WAYPOINTS, undoableState.waypointList.size()));
                    model =  (SpinnerNumberModel) jSpinnerNextWaypoint1.getModel();
                    model.setMaximum(Integer.min(MAX_NUMBER_OF_WAYPOINTS, undoableState.waypointList.size()));
                    model =  (SpinnerNumberModel) jSpinnerNextWaypoint2.getModel();
                    model.setMaximum(Integer.min(MAX_NUMBER_OF_WAYPOINTS, undoableState.waypointList.size()));                    
                }
            }
            //
            mapPainter.previewSelectionImage = null;
            if (jCheckBoxShowNextWaypoints.isSelected() && jCheckBoxShowWaypoints.isSelected())
                drawMap();
            else
               drawSpritesAndWaypoints();          // just redraw sprites and waypoints
            mapPainter.repaint();
          
        }
        public void mousePressed (MouseEvent e)
        {
            undoableState.saveState(currentPlaceOperation);
            handleMouseDown(e);
        }
        public void mouseDragged (MouseEvent e)
        {
            if (jCheckBoxSetTilePriority.isSelected())
            {
                handleMouseDown(e);
                isSelecting = false;
                return;
            }            
            Point p =  e.getPoint();
            // now get jPanelTileArea size
            Dimension d = jPanelMapArea.getSize();
            // get current zoom factor            
            double zoom =  d.width / ((double) undoableState.map.getSizeX() * tileSizeX);
            int xTile = (int) (p.x / (zoom *tileSizeX )); 
            int yTile = (int) (p.y / (zoom *tileSizeY ));
            if (currentSelectedTileX1 > xTile)
                xTile = currentSelectedTileX1;
            if (currentSelectedTileY1 > yTile)
                yTile = currentSelectedTileY1;
            boolean changed = currentSelectedTileX2 != xTile || currentSelectedTileY2 != yTile;
            if (isSelecting)
            {

                if (changed)
                {
                    currentSelectedTileX2 = xTile;
                    currentSelectedTileY2 = yTile;
                    //currentSelectedTile = getTileIndexByNxy(xTile, yTile);
                    currentTilePainter.bufferedImage  = getTileBlockByCoordinates(currentSelectedTileX1, currentSelectedTileY1, currentSelectedTileX2, currentSelectedTileY2, false);     
                    redrawCurrentTilePanel();
                    updateMapPainterSelection();   
                }
            }
            else if (isFilling)
            {
                if (changed)
                {
                    currentSelectedTileX2 = xTile;
                    currentSelectedTileY2 = yTile;
                     updateMapPainterSelection();   
                }                
            }
            else
            {
                handleMouseDown(e);
            }
        }      
        @Override
        public void mouseMoved(MouseEvent e)
        {
            if (!jCheckBoxMenuItemShowTileSelectionOnMap.isSelected())
                return;
            Point p =  e.getPoint();
            // now get jPanelTileArea size
            Dimension d = jPanelMapArea.getSize();
            // get current zoom factor            
            int mapSizeX = undoableState.map.getSizeX();
            int mapSizeY = undoableState.map.getSizeY();
            double zoom =  d.width / ((double) mapSizeX  * tileSizeX);
            if (currentPlaceOperation == UndoableState.PLACE_TILE)
            {
                int xTile = (int) (p.x / (zoom *tileSizeX )); 
                int yTile = (int) (p.y / (zoom *tileSizeY )); 
                // draw current selected block
                int blockW = currentTileBlock[0].length;//currentSelectedTileX2 - currentSelectedTileX1 + 1;
                int blockH = currentTileBlock.length;//currentSelectedTileY2 - currentSelectedTileY1 + 1;
                Graphics g = mapPainter.bufferedImage.getGraphics();            
                if (blockW + xTile > mapSizeX)
                    blockW = mapSizeX - xTile;
                if (blockH + yTile > mapSizeY)
                    blockH = mapSizeY - yTile;
                BufferedImage oldImage = new BufferedImage( blockW*tileSizeX, blockH*tileSizeY, BufferedImage.TYPE_4BYTE_ABGR);
                oldImage.getGraphics().drawImage(mapPainter.bufferedImage.getSubimage(xTile * tileSizeX, yTile * tileSizeY, blockW*tileSizeX, blockH*tileSizeY), 0, 0, null);
                mapPainter.previewSelectionImage = oldImage;        // this image will be restored after repaint.
                mapPainter.previewSelectionX = xTile * tileSizeX;
                mapPainter.previewSelectionY = yTile * tileSizeY;
                Graphics2D g2d = oldImage.createGraphics();
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
                g2d.setComposite(ac);
                for (int x = 0; x < blockW; x++ )
                {
                    for (int y = 0; y < blockH; y++)
                    {
                        int xx = xTile + x;
                        int yy = yTile + y;
                        if (xx < mapSizeX && yy < mapSizeY)
                        {
                            int index = currentTileBlock[y][x];
                            g2d.drawImage(getTileByIndex(index), x * tileSizeX, y * tileSizeY,  tileSizeX, tileSizeY, null);                              
                        }                    
                    }
                }
                g.dispose();
                g2d.dispose();
                drawSpritesAndWaypoints();          // just redraw sprites
                mapPainter.repaint();
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDeleteSprite;
    private javax.swing.JButton jButtonDeleteWaypoint;
    private javax.swing.JButton jButtonEditType;
    private javax.swing.JButton jButtonFindSprite;
    private javax.swing.JButton jButtonFindWaypoint;
    private javax.swing.JCheckBox jCheckBoxAutoIncrementSpriteNumber;
    private javax.swing.JCheckBox jCheckBoxAutoIncrementWaypointNumber;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemMarkSemiTransparent;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowGridOnMap;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowGridOnTiles;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowScreenSize;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowTileSelectionOnMap;
    private javax.swing.JCheckBox jCheckBoxSetTilePriority;
    private javax.swing.JCheckBox jCheckBoxSetTileType;
    private javax.swing.JCheckBox jCheckBoxShowNextWaypoints;
    private javax.swing.JCheckBox jCheckBoxShowSpriteNumbers;
    private javax.swing.JCheckBox jCheckBoxShowSprites;
    private javax.swing.JCheckBox jCheckBoxShowTilePriority;
    private javax.swing.JCheckBox jCheckBoxShowTileType;
    private javax.swing.JCheckBox jCheckBoxShowWaypointNumbers;
    private javax.swing.JCheckBox jCheckBoxShowWaypoints;
    private javax.swing.JComboBox<String> jComboBoxTileType;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JInternalFrame jInternalFrameMap;
    private javax.swing.JInternalFrame jInternalFrameTiles;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuCheckBoxItemShowTileSelectionOnMap;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItemBackgroundRainbowEditor;
    private javax.swing.JMenuItem jMenuItemChangeMapAndScreenSizes;
    private javax.swing.JMenuItem jMenuItemCreateSpriteSet;
    private javax.swing.JMenuItem jMenuItemCreateTileSetFromDirectory;
    private javax.swing.JMenuItem jMenuItemExportMapToCfile;
    private javax.swing.JMenuItem jMenuItemExportSpriteData;
    private javax.swing.JMenuItem jMenuItemExportSpriteList;
    private javax.swing.JMenuItem jMenuItemExportTilePropsNoDuplicate;
    private javax.swing.JMenuItem jMenuItemExportTilesToCFileNoDuplicates;
    private javax.swing.JMenuItem jMenuItemExportTilesToCfile;
    private javax.swing.JMenuItem jMenuItemExportTrigTable;
    private javax.swing.JMenuItem jMenuItemExportWav;
    private javax.swing.JMenuItem jMenuItemExportWaypointsToCFile;
    private javax.swing.JMenuItem jMenuItemLoadMap;
    private javax.swing.JMenuItem jMenuItemLoadSpritePositions;
    private javax.swing.JMenuItem jMenuItemLoadSpriteSet;
    private javax.swing.JMenuItem jMenuItemLoadTilePicture;
    private javax.swing.JMenuItem jMenuItemLoadTileProps;
    private javax.swing.JMenuItem jMenuItemLoadWaypoints;
    private javax.swing.JMenuItem jMenuItemRedo;
    private javax.swing.JMenuItem jMenuItemSaveMap;
    private javax.swing.JMenuItem jMenuItemSaveMapAs;
    private javax.swing.JMenuItem jMenuItemSaveSpritePositions;
    private javax.swing.JMenuItem jMenuItemSaveTileProps;
    private javax.swing.JMenuItem jMenuItemUndo;
    private javax.swing.JMenu jMenuMap;
    private javax.swing.JMenu jMenuSpecial;
    private javax.swing.JMenu jMenuSprites;
    private javax.swing.JMenu jMenuTiles;
    private javax.swing.JMenu jMenuUtilities;
    private javax.swing.JMenu jMenuWaypoints;
    private javax.swing.JPanel jPanelCurrentSprite;
    private javax.swing.JPanel jPanelCurrentTile;
    private javax.swing.JScrollPane jScrollPaneMapArea;
    private javax.swing.JScrollPane jScrollPaneSpriteArea;
    private javax.swing.JScrollPane jScrollPaneTileArea;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JSlider jSliderZoomMap;
    private javax.swing.JSlider jSliderZoomTiles;
    private javax.swing.JSpinner jSpinnerCurrentSprite;
    private javax.swing.JSpinner jSpinnerCurrentWaypoint;
    private javax.swing.JSpinner jSpinnerNextWaypoint1;
    private javax.swing.JSpinner jSpinnerNextWaypoint2;
    private javax.swing.JSpinner jSpinnerWaypointSizeX;
    private javax.swing.JSpinner jSpinnerWaypointSizeY;
    private javax.swing.JToggleButton jToggleButtonPlaceWaypoints;
    // End of variables declaration//GEN-END:variables
}

