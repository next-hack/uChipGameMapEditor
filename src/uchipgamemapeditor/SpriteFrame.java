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
class SpriteFrame
{
    String filename;
    SizeCoordinate frame;
    boolean rotated;
    boolean trimmed;
    SizeCoordinate spriteSourceSize;
    SizeCoordinate sourceSize;
    boolean isMask;
    Pivot pivot;
    int getFrameNumber()
    {
        return Integer.parseInt(filename.substring(filename.lastIndexOf("-")+1));
    }
    String getAnimationName()
    {
        // there are some sprites, which have more than one animation, such as the main character
        // which might run, jump, climp, etc.
        // Other sprites, like a bonus, might have just only one animation.
        String spriteName = "/" + getSpriteName();
        int dashIndex = filename.lastIndexOf("-");
        int animIndex = filename.lastIndexOf( spriteName ) + spriteName.length() + 1;
        if (animIndex <= dashIndex)
            return filename.substring(animIndex,dashIndex);
        else
            return "";
    }
    String getSpriteName()
    {
        return filename.substring(filename.lastIndexOf("/")+1,filename.indexOf("-",filename.lastIndexOf("/")));
    }        
    public SpriteFrame(int x, int y, int w, int h, String spriteName, String animationName, int frameNumber, boolean isMask)
    {
        this.isMask = isMask;
        filename = spriteName + "/" + spriteName + "-" + animationName + "-" + frameNumber;
        frame = new SizeCoordinate(x, y, w, h);
        spriteSourceSize = new SizeCoordinate(0, 0, w, h);
        sourceSize = new SizeCoordinate (0, 0, w, h);
        pivot = new Pivot(0.5f, 0.5f);
    }        
    class Pivot
    {
        float x;
        float y;
        public Pivot (float xp, float yp)
        {
            x = xp;
            y = yp;
        }

    }
}

