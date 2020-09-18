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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 *
 * @author PETN
 */
class TileMapPainter extends Component
{
    int preferredSizeX = 100;
    int preferredSizeY = 100;
    //
    int tileWidth=16,tileHeight=16;
    private int nTilesX, nTilesY;
    int screenX, screenY;
    boolean drawGrid = true;
    boolean showSelection = false;
    boolean showScreenSize = false;
    boolean showTilePriority = false;
    boolean showTileProps = false;
    int selectedX1 = 0;
    int selectedY1 = 0;
    int selectedX2 = 0;
    int selectedY2 = 0;
    int [][] multipleSelection = null; 
    TileProperties [] listOfTileProperties = new TileProperties[0]; // to avoid null pointer
    //
    BufferedImage bufferedImage = null;
    BufferedImage previewSelectionImage = null;
    int previewSelectionX = 0;
    int previewSelectionY = 0;
    //
    GameMap gameMap;
    // 
    //
    int [] tileProps;
    byte [] tilePriorities;
    public void setMap(GameMap map)
    {
        gameMap = map;
    }
    public void setBufferedImage(BufferedImage bi)
    {
        bufferedImage = bi;
    }    
    void setNumberOfTiles (int nx, int ny)
    {
        tileProps = new int[ny * nx];
        tilePriorities = new byte [ny * nx];
    }
    void setTileSize(int x, int y)
    {
        if (x == 0 || y == 0)
            return;
        tileWidth = x;
        tileHeight = y;
    }
    int [] getTileProperties()
    {
        return tileProps;
    }
    byte [] getTilePriorities()
    {
        return tilePriorities;
    }
    static BufferedImage deepCopy (BufferedImage bi)
    {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage (cm, raster, isAlphaPremultiplied, null);
    }
    public void setPreferredSize(int w, int h)
    {
        preferredSizeX = w;
        preferredSizeY = h;
    }
    public void setPreferredSize(Dimension d)
    {
        preferredSizeX = d.width;
        preferredSizeY = d.height;
    }    
    public Dimension getPreferredSize()
    {
        return new Dimension(preferredSizeX, preferredSizeY);
    }
    public void paint(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getPreferredSize();
        if (bufferedImage != null)
        {
            nTilesX = bufferedImage.getWidth() / tileWidth;
            nTilesY = bufferedImage.getHeight() / tileHeight;
            g2.drawImage(bufferedImage,
                    0, 0,  size.width , size.height,
                    0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(),
                    null);
            // now we must draw the grid.
            // 
            if (previewSelectionImage != null) // restore old image
            {
                //bufferedImage.getGraphics().drawImage(previewSelectionImage, previewSelectionX, previewSelectionY, null); 
                int x1 = (int) ( ((double) size.width)/bufferedImage.getWidth(null) * previewSelectionX + 0.5);
                int y1 = (int) ( ((double) size.height)/ bufferedImage.getHeight(null) * previewSelectionY + 0.5);
                int x2 = (int) ( ((double) size.width)/bufferedImage.getWidth(null) * (previewSelectionX + previewSelectionImage.getWidth(null) ) + 0.5);
                int y2 = (int) ( ((double) size.height)/ bufferedImage.getHeight(null) * (previewSelectionY + previewSelectionImage.getHeight(null) ) + 0.5);            
                g2.drawImage(previewSelectionImage,
                    x1, y1, x2 ,y2 ,
                    0, 0, previewSelectionImage.getWidth(null), previewSelectionImage.getHeight(null),
                    null);                    
            }
        }
        if (drawGrid)
        {
            float alpha = 0.2f;
            g2.setPaint(new Color (1,1,0,alpha));

            for (int ny = 0; ny < nTilesY; ny++)
            {
                if (showScreenSize && ny % screenY == 0)
                {
                    g2.setPaint(new Color (1,0,0,alpha));
                    int y = (int) ( ny*((double) size.height) / nTilesY + 0.5);
                    g2.drawLine(0, y , size.width, y);                                                 
                    g2.setPaint(new Color (1,1,0,alpha));                    
                }
                else
                {
                    int y = (int) ( ny*((double) size.height) / nTilesY + 0.5);
                    g2.drawLine(0, y , size.width, y);                             
                }
            }
            for (int nx = 0; nx < nTilesX; nx++)
            {
                if (showScreenSize && nx % screenX == 0)
                {
                    g2.setPaint(new Color (1,0,0,alpha));
                    int x = (int) ( nx*((double) size.width) / nTilesX + 0.5);
                    g2.drawLine(x, 0, x, size.height);            
                    g2.setPaint(new Color (1,1,0,alpha));                    
                }
                else
                {
                    int x = (int) ( nx*((double) size.width) / nTilesX + 0.5);
                    g2.drawLine(x, 0, x, size.height);            
                }

            }            
        }
        if (showTileProps)
        {
            float alpha = 0.5f;
            for (int ny = 0; ny < nTilesY; ny++)
            {
                for (int nx = 0; nx < nTilesX; nx++)
                {
                    Color c;
                    int y = (int) (ny * ((double) size.height) / nTilesY + 0.5);
                    int x = (int) ( nx*((double) size.width) / nTilesX + 0.5);
                    int tileType = tileProps[ny * nTilesX + nx];
                    if (tileType >= listOfTileProperties.length)
                        c = new Color (0, 0, 0, 0);
                    else
                        c = new Color (((listOfTileProperties[tileType].color >> 16) & 0xFF)/255.0f, ((listOfTileProperties[tileType].color  >> 8) & 0xFF)/255.0f, (listOfTileProperties[tileType].color & 0xFF)/255.0f, alpha);
                    g2.setPaint(c);
                    g2.fillRect(x, y, (int) (( (double) size.width) / nTilesX + 0.5), (int) (((double) size.height) / nTilesY + 0.5));
                }
            }  
        }
        if (showTilePriority)
        {
            float alpha = 0.5f;
            for (int ny = 0; ny < nTilesY; ny++)
            {
                for (int nx = 0; nx < nTilesX; nx++)
                {
                    Color c;
                    int y = (int) (ny * ((double) size.height) / nTilesY + 0.5);
                    int x = (int) ( nx*((double) size.width) / nTilesX + 0.5);
                    int tilePri = gameMap.getPriority(nx,ny);
                    if (tilePri != 0)
                    {
                        c = new Color (1, 1, 1, alpha);
                        g2.setPaint(c);
                        g2.fillRect(x, y, (int) (( (double) size.width) / nTilesX + 0.5), (int) (((double) size.height) / nTilesY + 0.5));
                    }    
                }
            }  
        }
        if (showSelection)
        {
            if (selectedX1 > selectedX2)
            {
                int t = selectedX1;
                selectedX1 = selectedX2;
                selectedX2 = t;
            }
            if (selectedY1 > selectedY2)
            {
                int t = selectedY1;
                selectedY1 = selectedY2;
                selectedY2 = t;
            }
            g2.setPaint(new Color (1,1,1,0.75f));
            int x1 =  (int) ( selectedX1*((double) size.width) / nTilesX + 0.5);
            int x2 = (int) ( (selectedX2 + 1)*((double) size.width) / nTilesX + 0.5) - 1;
            int y1 = (int) ( (selectedY1 ) *((double) size.height) / nTilesY + 0.5) ;
            int y2 = (int) ( (selectedY2 + 1)*((double) size.height) / nTilesY + 0.5) - 1;
            g2.drawRect( x1, y1 ,x2 - x1,y2 - y1 );
        }
        if (multipleSelection != null)
        {
            for (int i = 0; i < multipleSelection.length; i++)
            {
                g2.setPaint(new Color (1,1,0,0.75f));
                int x1 =  (int) ( multipleSelection[i][0]*((double) size.width) / nTilesX + 0.5);
                int x2 = (int) ( (multipleSelection[i][0] + multipleSelection[i][2])*((double) size.width) / nTilesX + 0.5) - 1;
                int y1 = (int) ( (multipleSelection[i][1] ) *((double) size.height) / nTilesY + 0.5) ;
                int y2 = (int) ( (multipleSelection[i][1] + multipleSelection[i][3])*((double) size.height) / nTilesY + 0.5) - 1;
                g2.drawRect( x1, y1 ,x2 - x1,y2 - y1 );                
            }
        }
        g2.dispose();
    }
}

