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

public class Waypoint
{
    public int x;
    public int y;
    public int rx;
    public int ry;
    public int nextWaypoint1;
    public int nextWaypoint2;
    public Waypoint (int x, int y, int rx, int ry, int nextWaypoint1, int nextWaypoint2)
    {
        this.x = x;
        this.y = y;
        this.rx = rx;
        this.ry = ry;
        this.nextWaypoint1 = nextWaypoint1;
        this.nextWaypoint2 = nextWaypoint2;
    }
    public Waypoint getCopy()
    {
        return new Waypoint(x, y, rx, ry, nextWaypoint1, nextWaypoint2);
    }
}

