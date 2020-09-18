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
import java.nio.file.Files;
import java.nio.file.Paths;

public class SpritePosition
{
    int x;
    int y;
    int entityNumber;
    public SpritePosition(int xx, int yy, int n)
    {
        x = xx;
        y = yy;
        entityNumber = n;
    }
    public SpritePosition getCopy()
    {
        SpritePosition newSpritePosition = new SpritePosition(x, y, entityNumber);
        return newSpritePosition;
    }
}
