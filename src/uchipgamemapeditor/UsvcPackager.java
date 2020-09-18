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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public class UsvcPackager
{
    static final String USVCHEADER = "USVC";
    static final String version = "0.0";
    static final int PREVIEW_WIDTH = 96;
    static final int PREVIEW_HEIGHT = 72;
    static final int AUTHOR_LENGTH = 14;
    static final int SHORT_TITLE_LENGTH = 20;
    static final int LONG_TITLE_LENGTH = 14;
    static final int DESCRIPTION_LENGTH = 14;
    static final int DATE_LENGTH = 8;
    static final int VERSION_LENGTH = 5;

    public static byte[] convertToUSVCPreview (BufferedImage image, int width, int height)
    {
        // why should we create a new buffered image ? Because we want the image to be ARGB, while the original could be of any type.
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);       
        int [] dataBuffer =  ((DataBufferInt)newImage.getRaster().getDataBuffer()).getData();
        byte [] buffer = new byte[dataBuffer.length];
        for (int pixel = 0; pixel < buffer.length; pixel++)
        {
            // the databuffer is linear. We need to arrange it in tiles.
            int x = pixel % PREVIEW_WIDTH;
            int y = pixel / PREVIEW_WIDTH;
            int tile  = x / 8 + (y / 8) * PREVIEW_WIDTH/8 ;
            int tileX = x % 8;
            int tileY = y % 8;
            int red = MapEditorMainFrame.uChipVGAred[(dataBuffer[pixel] >> 16)  & 0xFF];
            int green = MapEditorMainFrame.uChipVGAgreen[(dataBuffer[pixel] >> 8 ) & 0xFF];
            int blue = MapEditorMainFrame.uChipVGAblue[(dataBuffer[pixel] >> 0 ) & 0xFF];
            // now that we have the uChipVGAred-uChipVGAgreen-b (3-3-2) components, we can create the uChip-palette
            buffer[tile * 64 + tileX + 8 * tileY] = (byte) ((red & 1) | ((red & 4) >> 1) | ((red & 2) << 1)  | ((blue & 1) << 3) | ((green & 1) << 4) | ((blue & 2) << 4) | ((green & 2) << 5) | ((green & 4) << 5) );
        }
        g2d.dispose();
        return buffer;        
    }       
    static boolean createPackage(String binFilePath, String metaFilePath, String imageFilePath, String outputFilePath)
    {
        long checkSum = 0;
        int error = 0;
        String shortTitle = "", date = "", version = "";
        String [] longTitle, description, authors;
        longTitle = new String[4];
        description = new String [4];
        authors = new String [2];
        byte [] binData = new byte [0];
        // everything was inserted. Let's load the files.
        try (BufferedReader br = new BufferedReader( new FileReader((metaFilePath))))
        {
            shortTitle = br.readLine();
            System.out.println("Short Title: \"" + shortTitle + "\"");
            for (int i = 0; i < longTitle.length; i++)
            {
                longTitle[i] = br.readLine();
                System.out.println("Long Title Row "+ (1 + i) + ": \"" + longTitle[i] + "\"");
            }
            for (int i = 0; i < description.length; i++)
            {
                description[i] = br.readLine();
                System.out.println("Description Row "+ (1 + i) + ": \"" + description[i] + "\"");
            }
            for (int i = 0; i < authors.length; i++)
            {
                authors[i] = br.readLine();
                System.out.println("Authors Row "+ (1 + i) + ": \"" + authors[i] + "\"");
            }                
            date = br.readLine();
            System.out.println("Date: \""+date + "\"");
            version = br.readLine();
            System.out.println("Version: \""+version + "\""); 
        }
        catch (Exception e)
        {
            System.out.println("Cannot open the specified meta file \"" + metaFilePath + "\"");
            error = 1;
        }
        try 
        {
            binData = Files.readAllBytes(Paths.get(binFilePath));
            // for simplicity copy binData to a 4-byte aligned array
            byte [] tmpBin = new byte [4 * ((binData.length +  3) / 4)];
            for (int i = 0; i < binData.length; i += 1)
            {
                tmpBin[i] = binData[i];
            }
            binData = tmpBin;
            // calculate checksum
            for (int i = 0; i < binData.length; i +=4)
            {
                int b0 = 0xFF & binData[i];
                int b1 = 0xFF & binData[i + 1];
                int b2 = 0xFF & binData[i + 2];
                int b3 = 0xFF & binData[i + 3];
                checkSum += b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
            }
        }
        catch(Exception e)
        {
            System.out.println("Cannot open the specified bin file \"" + binFilePath + "\"");
            error = 1;
        }
        byte [] imgData = null;
        try
        {
            imgData = convertToUSVCPreview(ImageIO.read(new File(imageFilePath)), 96, 72);
        } 
        catch (Exception e)
        {
            System.out.println("Cannot open the specified image \"" + imageFilePath + "\"");
            error = 1;                
        }
        if (error == 0)
        {                
            // Now everything is ready. Let's build and save the file
            final int uscLength = 512 + 512 * ((PREVIEW_WIDTH * PREVIEW_HEIGHT + 511) / 512) + binData.length;
            System.out.println("Final USC file size: "+ uscLength);
            byte [] finalUSC = new byte[uscLength];
            /*
            * "USVC" 4 bytes header
            * Checksum (4 byte, binary little endian)
            * Bin Length (4 byte, binary little endian)
            * Paddings (20 bytes) 
            * Short Title (32 chars)
            * Long Titles (4 x 32 chars)
            * Description (4 x 32 chars)
            * Authonrs (2 x 32 chars)
            * date (32 chars)
            * version (32 chars)
            * paddings (64 bytes)
            * 13 sectors for the preview image
            * 0.5 sector for the preview image
            * 0.5 sector Paddings
            * binary file 
            */
            // copy header @0
            for (int i = 0; i < USVCHEADER.length(); i++)
            {
                finalUSC[i] = USVCHEADER.getBytes()[i];
            }
            // copy checksum @4
            for (int i = 0; i < 4; i++)
            {
                finalUSC[4 + i] = (byte) ((checkSum >> i * 8) & 0xFF);
            }
            // copy length @8
            for (int i = 0; i < 4; i++)
            {
                finalUSC[8 + i] = (byte) ((binData.length >> i * 8) & 0xFF);                    
                System.out.println("Bin Length byte: " + (finalUSC[8 + i] & 0xFF));
            }
            System.out.println("Bin Length: " + binData.length);
            // copy short title @32
            for (int i = 0; i < shortTitle.length() && i < 32; i++)
            {
                finalUSC[i + 32] = shortTitle.getBytes()[i];
            }       
            // copy long titles
            for (int n = 0; n < longTitle.length; n++)
            {
                for (int i = 0; i < longTitle[n].length() && i < 32; i++)
                {
                    finalUSC[i + 64 + n * 32] = longTitle[n].getBytes()[i];
                }                      
            }
            //  copy description
            for (int n = 0; n < description.length; n++)
            {
                for (int i = 0; i < description[n].length() && i < 32; i++)
                {
                    finalUSC[i + 64 + longTitle.length * 32 + n * 32] = description[n].getBytes()[i];
                }                      
            }   
            // copy authors
            for (int n = 0; n < authors.length; n++)
            {
                for (int i = 0; i < authors[n].length() && i < 32; i++)
                {
                    finalUSC[i + 64 + (longTitle.length + description.length) * 32 + n * 32] = authors[n].getBytes()[i];
                }                      
            }     
            // copy date 
            for (int i = 0; i < date.length() && i < 32; i++)
            {
                finalUSC[i + 64 + (longTitle.length + description.length + authors.length) * 32] = date.getBytes()[i];
            }   
            // copy version 
            for (int i = 0; i < version.length() && i < 32; i++)
            {
                finalUSC[i + 64 + 32 + (longTitle.length + description.length + authors.length) * 32] = version.getBytes()[i];
            }   
            // copy @512 the image data
            for (int i = 0; i < imgData.length; i++)
            {
                finalUSC[512 + i] = imgData[i];
            }   
            // copy @512 + 512 *((PREVIEW_WIDTH * PREVIEW_HEIGHT + 511) / 512) +  the bin data
            for (int i = 0; i < binData.length; i++)
            {
                finalUSC[512 + 512 * ((PREVIEW_WIDTH * PREVIEW_HEIGHT + 511) / 512) + i] = binData[i];
            }                   
            try ( FileOutputStream fos = new FileOutputStream(outputFilePath))
            {
                fos.write(finalUSC);
                System.out.println("Done! Bye bye!");
            }
            catch (Exception e)
            {
                error = 1;
                System.out.println("Cannot write to the specified output file \"" + outputFilePath + "\"");                
            }
        }
        if (error == 0)
            return true;
        else
            return false;
    }

}
