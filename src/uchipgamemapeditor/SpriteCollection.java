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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import static uchipgamemapeditor.MapEditorMainFrame.convertToRGB565;
import static uchipgamemapeditor.MapEditorMainFrame.convertTouChipVGA4bpp;
import static uchipgamemapeditor.MapEditorMainFrame.convertTouChipVGA8bpp;

public class SpriteCollection
{ 
    int numberOfEntities;
    SpriteAtlas spriteAtlas;
    int maxWidth = 1;
    int maxHeight = 1;   
    LinkedHashMap <String,Entity> entityMap = new LinkedHashMap <String,Entity>();
    // the following is used to quickly determine the offsets and obtain WYSIWYG
    ArrayList <Entity> entityArray = new ArrayList <Entity>();
    BufferedImage painterImage;
    private BufferedImage imageToBufferedImage(Image image) 
    {

        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }

    public BufferedImage makeColorTransparent(BufferedImage im, final Color color) 
    {
        ImageFilter filter = new RGBImageFilter() 
        {

            // the color we are looking for... Alpha bits are set to opaque
            public int markerRGB = color.getRGB() | 0xFF000000;

            public final int filterRGB(int x, int y, int rgb) 
            {
                if ((rgb | 0xFF000000) == markerRGB) 
                {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } 
                else 
                {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return imageToBufferedImage(Toolkit.getDefaultToolkit().createImage(ip));
    } 
    boolean readAtlasFile (String spriteSheetFileName, String jsonFileName) throws Exception
    {
        // this function reads both the spriteSheet
        String gsonString = new String (Files.readAllBytes(Paths.get(jsonFileName)));
        Gson gson = new Gson();
        spriteAtlas =  gson.fromJson(gsonString ,SpriteAtlas.class); 
        BufferedImage spriteSetImage = null;
        spriteSetImage = ImageIO.read(new File(spriteSheetFileName));
        // create empty hashmap and array list
        entityMap = new LinkedHashMap <String,Entity>();
        entityArray = new ArrayList <Entity>();
        
        // now, we have both the image, and the data about the sprite positions.
        // 
        for (int i = 0; i< spriteAtlas.frames.length; i++)
        {
            // Note: all the frames are expected to be in order starting from 1!!!
            int frameNumber = spriteAtlas.frames[i].getFrameNumber() - 1;
            String animName = spriteAtlas.frames[i].getAnimationName();
            if (animName.length() == 0)
                animName = "defaultanim";
            String spriteName = spriteAtlas.frames[i].getSpriteName();
            Entity entity;  
            if (!entityMap.containsKey(spriteName))
            {
                entity = new Entity();
                entity.name = spriteName;
                entityMap.put(spriteName, entity);
            }
            else
                entity = entityMap.get(spriteName);
            // now we have to check if the animation already exists
            ArrayList <SpriteAnimationFrame> a;
            if (!entity.animationMap.containsKey(animName))
            {
                a = new ArrayList <SpriteAnimationFrame>();
                entity.animationMap.put(animName,a);
            }
            else
                a = entity.animationMap.get(animName);
            // now we must add the frame
            SpriteAnimationFrame saf = new SpriteAnimationFrame(spriteSetImage, spriteAtlas.frames[i].frame.x,
            spriteAtlas.frames[i].frame.y,
            spriteAtlas.frames[i].frame.w,
            spriteAtlas.frames[i].frame.h, true, spriteAtlas.frames[i].isMask);
            a.add(frameNumber, saf );
            if (saf.h > maxHeight)
                maxHeight = saf.h;
            if (saf.w > maxWidth)
                maxWidth = saf.w;
            System.out.println("Sprite " + i + spriteAtlas.frames[i].filename 
                                             + " Sprite name: " + spriteName
                                             + " Anim name: " + animName
                                             + " frame-Number: " + frameNumber
                                             + " x: "+spriteAtlas.frames[i].frame.x
                                             + " y: "+spriteAtlas.frames[i].frame.y
                                             + " w: "+spriteAtlas.frames[i].frame.w
                                             + " h: "+spriteAtlas.frames[i].frame.h);
        }
        numberOfEntities = entityMap.size();
        painterImage =  new BufferedImage( numberOfEntities * maxWidth, maxHeight ,BufferedImage.TYPE_4BYTE_ABGR);
        // now let's paint the sprites in the buffered image which wil be used for the painter
        Iterator it = entityMap.entrySet().iterator();
        int numberOfDifferentSprites = 0;
        Graphics2D g2d = (Graphics2D) painterImage.getGraphics();
        while (it.hasNext())
        {   
            // 
            Entry <String,Entity> entry =  (Entry <String,Entity>) it.next();
            Entity itEntity = entry.getValue();
            entityArray.add(itEntity);
            // get one animation frame. Does not matter which one at the moment
            SpriteAnimationFrame saf = itEntity.animationMap.values().iterator().next().get(0);
            BufferedImage img = saf.frameImage;
            if (img.getColorModel().getPixelSize() == 8) // indexed
            {
                // make color 0 transparent.
                img = makeColorTransparent(img, Color.BLACK);
            }            
            itEntity.entityDisplayImage = img;
            itEntity.entityDisplayVerticalOffset = saf.verticalOffset;
            itEntity.entityDisplayHorizontalOffset = saf.horizontalOffset;
            // let's plot in the center the image
            int width = img.getWidth();
            int height = img.getHeight();
            
            g2d.drawImage(img, maxWidth * numberOfDifferentSprites + (maxWidth-width) / 2, (maxHeight-height) / 2,  width, height, null);  
            numberOfDifferentSprites++;

        }
        g2d.dispose();
        return true;
    }

    void exportToCFile(Writer writer, int exportMode) throws Exception
    {
        final int MAX_ENTRIES_PER_LINE = 16;
        StringBuilder sbSpriteData = new StringBuilder("");        
        StringBuilder sbFrameData = new StringBuilder("");        
        //
        StringBuilder sbAnimationData = new StringBuilder("");        
        //
        StringBuilder sbEntityData = new StringBuilder("");        
        StringBuilder sbEntityAnimStartIndex = new StringBuilder("");        
        StringBuilder sbEntityDefines = new StringBuilder ("");
        StringBuilder sbAnimDefines = new StringBuilder ("");
        StringBuilder sbAnimFrameDefines = new StringBuilder("");
        StringBuilder sbAnimNumFrameDefines = new StringBuilder("");
     
        int spriteDataSize = exportMode == ExportSpriteToCFileDialog.MODE_16BPP ?  16 : 8;
        //
        // First, we need to create an array with all the sprite data.
        int numPixels = 0;
        int numFrames = 0;
        int numAnimations = 0;
        int entityAnimation;
        for (int en = 0; en < entityArray.size(); en++)
        {
            sbEntityDefines.append("#define "+entityArray.get(en).name.toUpperCase()+" "+en+"\r\n");
            Iterator itAnim = entityArray.get(en).animationMap.entrySet().iterator();
            sbEntityData.append( "&animData[" + String.format(Locale.ROOT,"0x%02X],\r\n\t",numAnimations));    
            sbEntityAnimStartIndex.append(String.format(Locale.ROOT,"0x%02X,\r\n\t",numAnimations));
            entityAnimation = 0;
            while (itAnim.hasNext())
            {          
                Entry <String, ArrayList <SpriteAnimationFrame>> animEntry = (Entry <String, ArrayList <SpriteAnimationFrame>>) itAnim.next();
                ArrayList <SpriteAnimationFrame> animFrameArrayList = animEntry.getValue();
                //
                sbAnimationData.append( "{ .frameIndex = " + String.format(Locale.ROOT,"0x%04X, ",((exportMode != ExportSpriteToCFileDialog.MODE_USVC_4BPPC) ? numFrames : numFrames >> 1) ) +
                                            " .numFrames = " + String.format(Locale.ROOT,"0x%02X },\r\n\t", animFrameArrayList.size()));   
                sbAnimDefines.append("#define "+(entityArray.get(en).name+"_"+animEntry.getKey()).toUpperCase()+" "+entityAnimation+"\r\n");
                sbAnimFrameDefines.append("#define "+(entityArray.get(en).name+"_"+animEntry.getKey()).toUpperCase()+"_FRAMEINDEX "+String.format(Locale.ROOT,"0x%04X ",((exportMode != ExportSpriteToCFileDialog.MODE_USVC_4BPPC) ? numFrames : numFrames >> 1) )+"\r\n");
                sbAnimNumFrameDefines.append("#define "+(entityArray.get(en).name+"_"+animEntry.getKey()).toUpperCase()+"_NUMFRAMES "+String.format(Locale.ROOT,"0x%04X ",animFrameArrayList.size() )+"\r\n");
                entityAnimation++;
                numAnimations++;
                for (int frame = 0; frame < animFrameArrayList.size(); frame++)
                {
                    SpriteAnimationFrame saf = animFrameArrayList.get(frame);
                    BufferedImage image = saf.frameImage;
                    //   
                    if (image.getColorModel().getPixelSize() == 8)
                    {
                        if (exportMode != ExportSpriteToCFileDialog.MODE_USVC_8BPP)
                            System.out.println("Warning, the pixel of frame " + frame + "is "+ image.getColorModel().getPixelSize());
                        byte [] data = convertTouChipVGA8bpp(image); 
                        sbFrameData.append( "{ .w = " + String.format(Locale.ROOT,"0x%02X, ",saf.w ) +
                                            " .h = " + String.format(Locale.ROOT,"0x%02X, ",saf.h ) + 
                                            " .ox = " + String.format(Locale.ROOT,"0x%02X, ",saf.horizontalOffset ) + 
                                            " .oy = " + String.format(Locale.ROOT,"0x%02X, ",saf.verticalOffset) +
                                            " .pData = &spriteData[" + String.format(Locale.ROOT,"0x%05X] },\r\n\t",numPixels));
                        numFrames++;
                        for (int i = 0 ; i < data.length; i++ )
                        {
                            numPixels++;
                            short pixel = (short) (data[i] & 0xFF); 
                            if (i % MAX_ENTRIES_PER_LINE == (MAX_ENTRIES_PER_LINE - 1))
                                sbSpriteData.append(String.format(Locale.ROOT,"0x%02X, \r\n\t", pixel));
                            else
                                sbSpriteData.append(String.format(Locale.ROOT,"0x%02X, ",pixel));       
                        }       
                        if ((data.length % MAX_ENTRIES_PER_LINE) != 0)
                        {
                            sbSpriteData.append("\r\n\t");
                        }
                        //                          
                    }
                    else if (image.getColorModel().getPixelSize() == 4)
                    {
                        if (exportMode != ExportSpriteToCFileDialog.MODE_USVC_4BPPC)
                            System.out.println("Warning, the pixel of frame " + frame + "is "+ image.getColorModel().getPixelSize());
                        // we have to create for each frame its shifted version.
                        // first: the non shifted version
                        byte [] data = convertTouChipVGA4bpp(image); 
                        sbFrameData.append( "{ .w = " + String.format(Locale.ROOT,"0x%02X, ",saf.w  ) +
                                            " .h = " + String.format(Locale.ROOT,"0x%02X, ",saf.h ) + 
                                            " .ox = " + String.format(Locale.ROOT,"0x%02X, ",saf.horizontalOffset ) + 
                                            " .oy = " + String.format(Locale.ROOT,"0x%02X, ",saf.verticalOffset) +
                                            " .pData = &spriteData[" + String.format(Locale.ROOT,"0x%05X] },\r\n\t",numPixels));
                        numFrames++;    
                        int i = 0;
                        for (int y = 0 ; y < saf.h; y++ )
                        {
                            for (int x = 0; x < saf.w; x += 2)
                            {                                    
                                numPixels++;
                                int value, valueLo, valueHi;
                                valueLo = 0x0F & data [x + y * saf.w ];
                                // note: the width could be odd. We need to check for this and create a dummy pixel.
                                if ( (x + 1) == saf.w )
                                {
                                    valueHi = saf.isMask ? 0xF: 0;
                                }
                                else
                                    valueHi = 0x0F & data [x + 1 + y * saf.w ];                                    
                                value = valueLo | (valueHi << 4);
                                if (i % MAX_ENTRIES_PER_LINE == (MAX_ENTRIES_PER_LINE - 1))
                                    sbSpriteData.append(String.format(Locale.ROOT,"0x%02X, \r\n\t", value));
                                else
                                    sbSpriteData.append(String.format(Locale.ROOT,"0x%02X, ",value));       
                                i++;
                            }                        
                        }    
                        if ((i % MAX_ENTRIES_PER_LINE) != 0)
                        {
                            sbSpriteData.append("\r\n\t");
                        }                        
                        // now the 1-pixel shifted version
                        data = convertTouChipVGA4bpp(image); 
                        sbFrameData.append( "{ .w = " + String.format(Locale.ROOT,"0x%02X, ",saf.w + 1) +
                                            " .h = " + String.format(Locale.ROOT,"0x%02X, ",saf.h ) + 
                                            " .ox = " + String.format(Locale.ROOT,"0x%02X, ",saf.horizontalOffset ) + 
                                            " .oy = " + String.format(Locale.ROOT,"0x%02X, ",saf.verticalOffset) +
                                            " .pData = &spriteData[" + String.format(Locale.ROOT,"0x%05X] },\r\n\t",numPixels));
                        numFrames++;  
                        i = 0;
                        for (int y = 0 ; y < saf.h; y++ )
                        {
                            for (int xx = -1; xx < saf.w ; xx += 2)
                            {    
                                numPixels++;
                                int x = xx + 1;
                                int value, valueLo, valueHi;
                                if (xx == -1)       // the first row is 0
                                {
                               
                                    valueLo = saf.isMask ? 0xF: 0;;
                                    valueHi = 0x0F & data [x + y * saf.w ];                                                                        
                                }
                                else
                                {
                                    valueLo = 0x0F & data [xx + y * saf.w ];
                                    // note: the width could be odd. We need to check for this and create a dummy pixel.
                                    if (x == saf.w )
                                    {
                                        valueHi = saf.isMask ? 0xF: 0;;
                                    }
                                    else
                                        valueHi = 0x0F & data [x + y * saf.w ];                                                                        
                                }
                                value = valueLo | (valueHi << 4);
                                if (i % MAX_ENTRIES_PER_LINE == (MAX_ENTRIES_PER_LINE - 1))
                                    sbSpriteData.append(String.format(Locale.ROOT,"0x%02X, \r\n\t", value));
                                else
                                    sbSpriteData.append(String.format(Locale.ROOT,"0x%02X, ",value));       
                                i++;
                            }                        
                        } 
                        if ((i % MAX_ENTRIES_PER_LINE) != 0)
                        {
                            sbSpriteData.append("\r\n\t");
                        }                         
                    }
                    else
                    {
                        if (exportMode != ExportSpriteToCFileDialog.MODE_16BPP)
                            System.out.println("Warning, the pixel of frame " + frame + "is "+ image.getColorModel().getPixelSize());
                        short [] data = convertToRGB565(image); 
                        sbFrameData.append( "{ .w = " + String.format(Locale.ROOT,"0x%02X, ",saf.w ) +
                                            " .h = " + String.format(Locale.ROOT,"0x%02X, ",saf.h ) + 
                                            " .ox = " + String.format(Locale.ROOT,"0x%02X, ",saf.horizontalOffset ) + 
                                            " .oy = " + String.format(Locale.ROOT,"0x%02X, ",saf.verticalOffset) +
                                            " .pData = &spriteData[" + String.format(Locale.ROOT,"0x%05X] },\r\n\t",numPixels));
                        numFrames++;
                        for (int i = 0 ; i < data.length; i++ )
                        {
                            numPixels++;
                            short pixel = (short) (((data[i] >> 8) & 0xFF) | (data[i] << 8)); // endianess conversion...
                            if (i % MAX_ENTRIES_PER_LINE == (MAX_ENTRIES_PER_LINE - 1))
                                sbSpriteData.append(String.format(Locale.ROOT,"0x%04X, \r\n\t", pixel));
                            else
                                sbSpriteData.append(String.format(Locale.ROOT,"0x%04X, ",pixel));       
                        }         
                        //       
                    }
                }
            }
        }
        StringBuilder sbDefines = new StringBuilder();
        sbDefines.append("#define SPRITEDATA_NUMPIXELS " + numPixels + "\r\n");
        sbDefines.append("#define FRAMEDATA_NUMFRAMES " + numFrames + "\r\n");
        sbDefines.append("#define ANIMDATA_NUMANIM " + numAnimations + "\r\n");
        sbDefines.append("#define ENTITYDATA_NUMENTITIES " + entityArray.size() + "\r\n");
        sbDefines.append(sbEntityDefines);
        sbDefines.append(sbAnimDefines);
        sbDefines.append(sbAnimFrameDefines);
        sbDefines.append(sbAnimNumFrameDefines);
        //
        // TODO remove this on USVC as it is defined in VGA.h
        sbDefines.append("typedef struct\r\n{\r\n");
        sbDefines.append("\tuint8_t w;  //width\n");
        sbDefines.append("\tuint8_t h;  //height\n");
        sbDefines.append("\tint8_t ox;  //offset x - for handle\r\n");
        sbDefines.append("\tint8_t oy;  //offset y - for handle\r\n");
        sbDefines.append("\tint"+spriteDataSize+"_t* pData;  //pointer to the topleft pixel\r\n");
        sbDefines.append("} frame_t;\r\n");
        //    
        sbDefines.append("typedef struct\r\n{\r\n");
        //sbDefines.append("\tframe_t *pFrameData;  //pointer to the first frame data\r\n");
        sbDefines.append("\tuint16_t frameIndex;  //index to the first frame data\r\n"); 
        sbDefines.append("\tuint8_t numFrames;  //number of frames\r\n");
        sbDefines.append("} anim_t;\r\n");        
        sbDefines.append("extern const uint"+spriteDataSize+"_t spriteData[SPRITEDATA_NUMPIXELS];\r\n");
        sbDefines.append("extern const frame_t frameData[FRAMEDATA_NUMFRAMES];\r\n");
        sbDefines.append("extern const anim_t animData[ANIMDATA_NUMANIM];\r\n");
        sbDefines.append("extern const anim_t* entityData[ENTITYDATA_NUMENTITIES];\r\n");
        sbDefines.append("extern const uint16_t entityAnimStartIndex[ENTITYDATA_NUMENTITIES]; // this is redundant but by using this, the compiler will save a lot of RAM\r\n");

        //
        writer.write("//BEGIN: Copy the following lines to a header file!\r\n");
        writer.write("#include <stdint.h>\r\n");
        writer.write(sbDefines.toString());
        writer.write("//\r\n");

        writer.write("//Copy the following lines to the C file!\r\n");
        writer.write("#include <stdint.h>\r\n");
        
        writer.write("const uint"+spriteDataSize+"_t spriteData[SPRITEDATA_NUMPIXELS] = \r\n{\r\n\t");
        // remove last ","
        String s = sbSpriteData.toString();
        writer.write(s.substring(0, s.lastIndexOf(",")));  //remove last ","
        writer.write("\r\n};\r\n");
        //
        writer.write("const frame_t frameData[FRAMEDATA_NUMFRAMES] = \r\n{\r\n\t");
        s = sbFrameData.toString();
        writer.write(s.substring(0, s.lastIndexOf(","))); 
        writer.write("\r\n};\r\n");
        //
        writer.write("const anim_t animData[ANIMDATA_NUMANIM] = \r\n{\r\n\t");
        s = sbAnimationData.toString();
        writer.write(s.substring(0, s.lastIndexOf(","))); 
        writer.write("\r\n};\r\n");
        //
        writer.write("const anim_t* entityData[ENTITYDATA_NUMENTITIES] = \r\n{\r\n\t");
        s = sbEntityData.toString();
        writer.write(s.substring(0, s.lastIndexOf(","))); 
        writer.write("\r\n};\r\n");
        writer.write("const uint16_t entityAnimStartIndex[ENTITYDATA_NUMENTITIES] = \r\n{\r\n\t");
        s = sbEntityAnimStartIndex.toString();
        writer.write(s.substring(0, s.lastIndexOf(","))); 
        writer.write("\r\n};\r\n");        
        USVCMapEditorUtilities.infoBox ("Sprites Exported to C file", "Notice", JOptionPane.INFORMATION_MESSAGE);
        
        

    }
  }
