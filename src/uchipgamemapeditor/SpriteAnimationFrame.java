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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class SpriteAnimationFrame
{
    int originalW, originalH;
    int w;
    int h;
    int verticalOffset;    // this DEPENDS ON (but it's not!) how many blank lines we removed from the top. it is used to get the real center
    int horizontalOffset; // this DEPENDS ON (but it's not!)  how many blank lines we removed from the left part. it is used to get the real center
    boolean  indexedPalette = false;
    boolean isMask;
    BufferedImage frameImage;
    
    public SpriteAnimationFrame (BufferedImage originalImage, int x, int y, int width, int height, boolean crop, boolean isMask)
    {
        this.isMask = isMask;
        indexedPalette = false;
        horizontalOffset = 0;
        verticalOffset = 0; 
        BufferedImage tmpImage;
        // get the sprite frame, as originally was identified
        originalW = width;
        originalH = height;
        tmpImage = originalImage.getSubimage(x, y, width, height);       
        if (crop)
        {
            WritableRaster raster = tmpImage.getAlphaRaster();
            if (raster == null) // indexed palette ?
            {
                indexedPalette = true;
                raster = tmpImage.getRaster();
            }    

            int left = 0;
            int top = 0;
            int right = width - 1;
            int bottom = height - 1;
            int minRight = width - 1;
            int minBottom = height - 1;

            top:
            for (; top < bottom; top++)
            {
                for ( x = 0; x < width; x++)
                {
                    if (raster.getSample(x, top, 0) != 0)
                    {
                        minRight = x;
                        minBottom = top;
                        break top;
                    }
                }
            }

            left:
            for (; left < minRight; left++)
            {
                for ( y = height - 1; y > top; y--)
                {
                    if (raster.getSample(left, y, 0) != 0)
                    {
                        minBottom = y;
                        break left;
                    }
                }
            }

            bottom:
            for (; bottom > minBottom; bottom--)
            {
                for ( x = width - 1; x >= left; x--)
                {
                    if (raster.getSample(x, bottom, 0) != 0)
                    {
                        minRight = x;
                        break bottom;
                    }
                }
            }

            right:
            for (; right > minRight; right--)
            {
                for ( y = bottom; y >= top; y--)
                {
                    if (raster.getSample(right, y, 0) != 0)
                    {
                        break right;
                    }
                }
            }
            horizontalOffset = width/2 - left;
            verticalOffset = height/2 - top;
            tmpImage = tmpImage.getSubimage(left, top, right - left + 1, bottom - top + 1);
            width = right - left + 1;
            height = bottom - top + 1;
        }
        frameImage=tmpImage;
     
        w = width;
        h = height;
    }
}
