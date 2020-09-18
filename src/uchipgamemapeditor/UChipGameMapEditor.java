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
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.Painter;


public class UChipGameMapEditor
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
       // TODO code application logic here
       try 
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {   
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    javax.swing.UIManager.getLookAndFeelDefaults().put("DesktopPane[Enabled].backgroundPainter", new DesktopPainter());   
                    
                    break;
                }
            }
        } 
        catch (Exception e) 
        {
           // handle exception
        }
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new MapEditorMainFrame().setVisible(true);
            }
        });
         
    }
    static class DesktopPainter implements Painter<JComponent> 
    {
        @Override
        public void paint(Graphics2D g, JComponent object, int width, int height)
        {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
        }
    }
}
