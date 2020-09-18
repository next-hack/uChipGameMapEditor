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

import static uchipgamemapeditor.PaletteRemapEditor.NUMBER_OF_COLORS;

class Palette
{
    USVCColor [] colors;
    boolean definedAtStartOfFrame;  // true if during the vertcal blank, the palette is restored to its initial value
    Palette()
    {
        colors = new USVCColor[NUMBER_OF_COLORS];
        for (int i = 0 ; i < NUMBER_OF_COLORS; i++)
            colors[i] = new USVCColor();
    }
    Palette(Palette p )
    {
        this();
        // get a copy
        for (int i = 0; i < p.colors.length && i < NUMBER_OF_COLORS; i++)
        {
            colors[i].red = p.colors[i].red;
            colors[i].green = p.colors[i].green;
            colors[i].blue = p.colors[i].blue;
        }
        definedAtStartOfFrame = p.definedAtStartOfFrame;
    }
}

