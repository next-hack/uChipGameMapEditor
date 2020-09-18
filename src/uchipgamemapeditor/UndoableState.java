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

import java.util.ArrayList;

public class UndoableState
{
    
    public static final int PLACE_TILE = 0;
    public static final int PLACE_SPRITE = 1;
    public static final int PLACE_WAYPOINT = 2;    
    int index = 0;
    ArrayList <Command> commandList;
    // current state. This is public
    public GameMap map;
    public ArrayList <SpritePosition> spritePositionList;
    public ArrayList <Waypoint> waypointList;
    private class Command
    {
        Object state;
        int type;
        public Command(Object s, int t)
        {
            state = s;
            type = t;
        }
    }
    
    public UndoableState()
    {
        commandList = new ArrayList();
    }
    void saveState(int commandNumber)
    {
        // remove everything after current index.
        for (int i = commandList.size() -  1; i >= index;  i--)
            commandList.remove(commandList.size() -  1);
        addState(commandNumber);
        //System.out.println("Saving State on index: "+ index+". Next Index is "+(index+1));
        index++;
    }
    private void addState(int commandNumber)
    {
        System.out.println("ADD STATE");
        Object state = null;
        switch (commandNumber)
        {
            case PLACE_TILE:
                state = map.getCopy();
                break;
            case PLACE_SPRITE:
                ArrayList<SpritePosition> spriteListCopy = new ArrayList<SpritePosition>();
                for (int i = 0; i < spritePositionList.size(); i++)
                {
                    spriteListCopy.add(spritePositionList.get(i).getCopy());
                }
                state = spriteListCopy;
                break; 
            case PLACE_WAYPOINT:
                ArrayList<Waypoint> waypointListCopy = new ArrayList<Waypoint>();
                for (int i = 0; i < waypointList.size(); i++)
                {
                    waypointListCopy.add(waypointList.get(i).getCopy());
                }
                state = waypointListCopy;
                break;
            default:
                state = null;
        }
        Command command = new Command(state, commandNumber);
        commandList.add(command);        
    }
    boolean canRedo()
    {
        return index < commandList.size() - 1;
    }
    boolean canUndo()
    {
        return index > 0;
    }
    void redo()
    {
        if (!canRedo())
            return;
        index++;
        Command cmd = commandList.get(index);
        setState(cmd);
    }
    void undo()
    {
        if (!canUndo())
            return;
        boolean hasNext = canRedo();
        //System.out.println("State is "+index+" Decrementing to and undoing to state: " +(index - 1));
        Command cmd  = commandList.get(index - 1);   
        if (!hasNext)
        {
            //System.out.println("Adding state because there was no redo opportunity");        
            addState(cmd.type);
        }
        index--;
        setState(cmd);
    }        
    void clear()
    {
        commandList.clear();
        index = 0;
    }
    private void setState(Command cmd)
    {
        Object state = cmd.state;
        switch (cmd.type)
        {
            case PLACE_SPRITE:
                spritePositionList = (ArrayList <SpritePosition>) state;
                break;
            case PLACE_TILE:
                map = (GameMap) state;
                break;
            case PLACE_WAYPOINT:
                waypointList = (ArrayList <Waypoint>) state;
        }        
    }
}
