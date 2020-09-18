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
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class USVCMapEditorUtilities
{
    // hex chars for fast bytes2hex.
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    // pixel signals are arranged in this way bgxxxxxxggxxbrrr
    static final int pixelAndMask = 0b1100000011001111;              
    static final int pixelOrMask = 1 << 9;  // disable SDCS
    static final int pixelMulFactor = 1024 + 1; // convert a USVC encoded color byte to its signals
    public static String bytesToHex(byte[] bytes) 
    {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) 
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }   
    public static int USVCpixelToSignals(int pixel)
    {  // pixel USVC encoded!
        return (pixelOrMask | ( (pixelMulFactor * pixel) & pixelAndMask )); 
    }
    public static int USVCbiPixelToSignals(int bipixel)
    {
        return USVCpixelToSignals (bipixel & 0xFF ) | ( USVCpixelToSignals((bipixel >> 8)) << 16 );
    }
    public static String createCFileArrayString(String arrayName, int [] array, int elementSize, int maxElementsPerLine, boolean unsigned, boolean constant)   
    {
        int mask;
        String arrayType;
        switch (elementSize)
        {
            case 1:
                mask = 0xFF;
                arrayType = "int8_t";                
                break;
            case 2: 
                mask = 0xFFFF;
                arrayType = "int16_t";
                break;
            default:
            case 4:
                elementSize = 4;    // remove garbages...
                mask = 0xFFFFFFFF;
                arrayType = "int32_t";
        }
        String formatString = "0x%0" + ( 2 * elementSize) + "X";
        StringBuilder sb = new StringBuilder((constant ? "const " : "") + (unsigned ? "u" : "") + arrayType+ " "+ arrayName + "[" + array.length + "] = \r\n{\n\t");
        for (int i = 0; i < array.length; i++)
        {
            sb.append(String.format(Locale.ROOT, formatString, array[i] & mask));
            if (i < array.length - 1)
            {
                sb.append(", ");
                if (i % maxElementsPerLine == (maxElementsPerLine - 1 ))
                {
                    sb.append("\r\n\t");
                }
            }
            else
            {
                sb.append("\r\n};\r\n");
            }
        }
        return sb.toString();
    }
    public static CustomFileChooser createFileChooser(String title, File selectedFile, javax.swing.filechooser.FileFilter filter)
    {
        final CustomFileChooser fc = new CustomFileChooser();
        if(title != null)
            fc.setDialogTitle(title);
        if (filter != null)
            fc.setFileFilter(filter);
        if (selectedFile != null)
            fc.setSelectedFile(selectedFile);
        return fc;
    }    
    public static void infoBox(String infoMessage, String titleBar, int icon)
    {
        final JDialog dialog = new JDialog();
        dialog.setAlwaysOnTop(true);  
        JOptionPane.showMessageDialog(dialog, infoMessage, titleBar, icon);
        dialog.dispose();
    }       
    public static int questionBox(String infoMessage, String titleBar, int icon)
    {
        final JDialog dialog = new JDialog();
        dialog.setAlwaysOnTop(true);  
        return JOptionPane.showConfirmDialog(dialog, infoMessage, titleBar, icon);
    }   
    public static int USVCRGBto8bit(int red, int green, int blue)
    {
        return ((red & 1) | ((red & 4) >> 1) | ((red & 2) << 1)  | ((blue & 1) << 3) | ((green & 1) << 4) | ((blue & 2) << 4) | ((green & 2) << 5) | ((green & 4) << 5) ) ;
    }
    public static int USVCRGBtoRGB24(int r, int g, int b)
    {
        // due to resistor values, these are the approximate levels for a 3-bit component (red, green): 0, 32, 71, 103, 151, 184, 222, 255
        // and these are for a 2-bit component (blue): 0, 86, 180, 255
        final int rgValues [] = {0, 32, 71, 103, 151, 184, 222, 255};
        final int bValues [] = {0, 86, 180, 255};
        return (rgValues[r & 7] << 16) | (rgValues[g & 7] << 8) | bValues[b & 3];
    }
    public static int questionYesNo(String question, String titleBar)
    {
        final JDialog dialog = new JDialog();
        dialog.setAlwaysOnTop(true);  
        
        return JOptionPane.showConfirmDialog(dialog, question, titleBar, JOptionPane.YES_NO_OPTION);
        
    }     
    public static BufferedImage loadImage(String name)
    {
        BufferedImage img = null;
        try
        {
            img = ImageIO.read(new File(name));
        } 
        catch (Exception e)
        {
        }
        return img;
    }
    public static boolean arePicturePaletteEqual(BufferedImage bi1, BufferedImage bi2)
    {
        ColorModel cm1, cm2;
        cm1 = bi1.getColorModel();
        cm2 = bi2.getColorModel();
        if (cm1.getPixelSize() > 8 || cm2.getPixelSize() > 8)
            return  false;
        else if (cm1.getPixelSize() != cm2.getPixelSize())
            return false;
        IndexColorModel icm1, icm2;
        icm1 = (IndexColorModel) cm1;
        icm2 = (IndexColorModel) cm2;
        //
        int ms1 = icm1.getMapSize();
        int ms2 = icm2.getMapSize();
        int [] rgb1 = new int [ms1]; 
        int [] rgb2 = new int [ms2];
        icm1.getRGBs(rgb1);
        icm2.getRGBs(rgb2);
        if (ms1 == ms2)
        {
            for (int i = 0; i < ms1; i++)
            {
                if (rgb1[i] != rgb2[i])
                    return false;  
            }    
        }
        return true;
    }
    // table utils
    public static void moveTableRowsBy(JTable table, int movement)
    {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int[] rows = table.getSelectedRows();
        if (rows.length == 0)
            return;
        int destination = rows[0] + movement;
        int rowCount = model.getRowCount();
        if (destination < 0 || destination >= rowCount)
        {
            return;
        }
        model.moveRow(rows[0], rows[rows.length - 1], destination);
        table.setRowSelectionInterval(rows[0] + movement, rows[rows.length - 1] + movement);
    }    
}
