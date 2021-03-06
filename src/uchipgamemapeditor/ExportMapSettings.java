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

import java.awt.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class ExportMapSettings extends javax.swing.JDialog
{
    final int bytesPerMapTile = 2;
    final int bytesPerMapMetaTile = 2;
    final int bytesPerTileEntryInMeta = 2;
    boolean confirmed = false;
    GameMap map;
    GameMap reducedMap;
    LinkedHashMap<ArrayList<Integer>,Integer> metaHashMap;
    GameMap metaMap;
    /**
     * Creates new form ExportMapSettings
     */
    public ExportMapSettings(java.awt.Frame parent, boolean modal, GameMap map)
    {
        super(parent, modal);
        initComponents();
        this.map = map;
        reducedMap = ((MapEditorMainFrame) parent).createReducedMap(map);
        calculateSizes();
        // now let's calculate the number of metatiles
    }
    public int calculateNumberOfMetatiles(GameMap map, int metaSizeX, int metaSizeY)
    {
        if (map.getSizeX() % metaSizeX != 0 || map.getSizeY() %metaSizeY != 0)
            return -1;      // return an invalid number if the map size is not divisible by the meta tile sizes.
        if ( metaSizeX > 0 && ((metaSizeX & (metaSizeX - 1)) != 0))
            return -2;      //
        if ( metaSizeY > 0 && ((metaSizeY & (metaSizeY - 1)) != 0))
            return -2;          
        int nMetasX = map.getSizeX() / metaSizeX;
        int nMetasY = map.getSizeY() / metaSizeY;
        metaMap = new GameMap(nMetasX, nMetasY);
        metaHashMap = new LinkedHashMap<>(); 
        int metaIndex = 0;
        for (int y = 0; y < nMetasY; y++)
        {
            for (int x = 0; x < nMetasX; x++)
            {
                ArrayList<Integer> meta = new ArrayList<> ();
                for (int my = 0; my < metaSizeY; my++)
                {
                    for (int mx = 0; mx < metaSizeX; mx++)
                    {
                        meta.add(map.getTile(mx + x * metaSizeX, my + y * metaSizeY));
                    }
                }
                if (!metaHashMap.containsKey(meta))
                {
                    metaHashMap.put(meta, metaIndex);
                    metaMap.setTile(x, y, (short) metaIndex);
                    metaIndex++;
                }                
                else
                {
                    metaMap.setTile(x, y, (short) ((int) metaHashMap.get(meta)));
                }
            }
        }
        return metaHashMap.size();
    }
    void calculateSizes()
    {
        int metaSizeX = (int) jSpinnerMetaTileX.getValue();
        int metaSizeY = (int) jSpinnerMetaTileY.getValue();
        int n;
        if (jCheckBoxRemoveDuplicateTiles.isSelected())
            n = calculateNumberOfMetatiles(reducedMap,  metaSizeX,  metaSizeY);
        else
            n = calculateNumberOfMetatiles(map,  metaSizeX,  metaSizeY);
        jLabelSizeWithoutMetatiles.setText("" + bytesPerMapTile * map.getSizeX() * map.getSizeY());
        if (n == -1)
        {
            jLabelNumberofMetatiles.setText("Map sizes must be multiple of meta tile sizes!");
            jLabelSizeWithMetatiles.setText("NA");
        }
        else if (n == - 2)
        {
            jLabelNumberofMetatiles.setText("Only power of two are accepted as meta tile sizes!");
            jLabelSizeWithMetatiles.setText("NA");            
        }
        else
        {
            int nMetaTiles = map.getSizeX() * map.getSizeY() / metaSizeX / metaSizeY;
            jLabelNumberofMetatiles.setText("" + n);
            jLabelSizeWithMetatiles.setText("" + (nMetaTiles * bytesPerMapMetaTile + n * bytesPerTileEntryInMeta * metaSizeX * metaSizeY ));
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jCheckBoxGenerateMetaTiles = new javax.swing.JCheckBox();
        jSpinnerMetaTileX = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jSpinnerMetaTileY = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldMapVariableName = new javax.swing.JTextField();
        jTextFieldMetaTileVariableName = new javax.swing.JTextField();
        jButtonOk = new javax.swing.JButton();
        jCheckBoxRemoveDuplicateTiles = new javax.swing.JCheckBox();
        jCheckBoxAppendLevelVariableName = new javax.swing.JCheckBox();
        jTextFieldLevelName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabelSizeWithoutMetatiles = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabelNumberofMetatiles = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabelSizeWithMetatiles = new javax.swing.JLabel();
        jButtonComputeNumberOfMetaTiles = new javax.swing.JButton();
        jCheckBoxXbyYArray = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Export map settings");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });

        jCheckBoxGenerateMetaTiles.setText("Generate Meta Tiles");
        jCheckBoxGenerateMetaTiles.setToolTipText("To minimize map size, the map is divided in blocks. The editor will look for repeated instances of the blocks, and will generate a new metatile. Note that this will increase the complexity of the map drawing routine.");

        jSpinnerMetaTileX.setModel(new javax.swing.SpinnerNumberModel(2, 1, 128, 1));
        jSpinnerMetaTileX.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerMetaTileXStateChanged(evt);
            }
        });

        jLabel1.setText("Meta tile X");

        jSpinnerMetaTileY.setModel(new javax.swing.SpinnerNumberModel(2, 1, 128, 1));
        jSpinnerMetaTileY.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerMetaTileYStateChanged(evt);
            }
        });

        jLabel2.setText("Meta tileY");

        jLabel3.setText("Map Variable Name:");

        jLabel4.setText("Meta tile variable Name:");

        jTextFieldMapVariableName.setText("gameMap");

        jTextFieldMetaTileVariableName.setText("metaTiles");

        jButtonOk.setText("Ok");
        jButtonOk.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonOkActionPerformed(evt);
            }
        });

        jCheckBoxRemoveDuplicateTiles.setSelected(true);
        jCheckBoxRemoveDuplicateTiles.setText("Remove duplicate tiles");
        jCheckBoxRemoveDuplicateTiles.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxRemoveDuplicateTilesActionPerformed(evt);
            }
        });

        jCheckBoxAppendLevelVariableName.setSelected(true);
        jCheckBoxAppendLevelVariableName.setText("Append suffix to variable name and defines");

        jTextFieldLevelName.setText("_level1");

        jLabel5.setText("Size without metatiles:");

        jLabelSizeWithoutMetatiles.setText("NA");

        jLabel6.setText("Computed numbers of metatiles:");

        jLabelNumberofMetatiles.setText("NA");

        jLabel8.setText("Size with metatiles:");

        jLabelSizeWithMetatiles.setText("NA");

        jButtonComputeNumberOfMetaTiles.setText("Calculate Optimal values");
        jButtonComputeNumberOfMetaTiles.setMaximumSize(null);
        jButtonComputeNumberOfMetaTiles.setPreferredSize(null);
        jButtonComputeNumberOfMetaTiles.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonComputeNumberOfMetaTilesActionPerformed(evt);
            }
        });

        jCheckBoxXbyYArray.setText("x-by-y array");
        jCheckBoxXbyYArray.setToolTipText("Creates a bidimensional array (map[HEIGHT][WIDTH] instead of map[HEIGHT*WIDTH)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBoxGenerateMetaTiles)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBoxRemoveDuplicateTiles)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBoxXbyYArray)
                        .addGap(0, 82, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonOk)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldMapVariableName)
                            .addComponent(jTextFieldMetaTileVariableName)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBoxAppendLevelVariableName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldLevelName))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(jLabelSizeWithoutMetatiles)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelNumberofMetatiles)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelSizeWithMetatiles)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSpinnerMetaTileX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)
                        .addGap(12, 12, 12)
                        .addComponent(jSpinnerMetaTileY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonComputeNumberOfMetaTiles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxGenerateMetaTiles)
                    .addComponent(jCheckBoxRemoveDuplicateTiles)
                    .addComponent(jCheckBoxXbyYArray))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinnerMetaTileX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jSpinnerMetaTileY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jButtonComputeNumberOfMetaTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabelSizeWithoutMetatiles))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabelNumberofMetatiles))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabelSizeWithMetatiles))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldMapVariableName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldMetaTileVariableName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxAppendLevelVariableName)
                    .addComponent(jTextFieldLevelName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButtonOk)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public String getLevelName()
    {
        if (jCheckBoxAppendLevelVariableName.isSelected())
            return jTextFieldLevelName.getText();
        else
            return "";
    }
    public String getMapName()
    {
        return jTextFieldMapVariableName.getText();
    }
    public String getMetaTileName()
    {
        return jTextFieldMetaTileVariableName.getText();
    }
    public GameMap getGameMap()
    {  
        if (jCheckBoxGenerateMetaTiles.isSelected())
            return metaMap;
        else
        {
            if (jCheckBoxRemoveDuplicateTiles.isSelected())
            {
                return reducedMap;
            }
            else return map;
        }
    }
    public int getNumberOfMetaTiles()
    {
        return metaHashMap.size();
    }
    public boolean usesMetaTiles()
    {
        return jCheckBoxGenerateMetaTiles.isSelected();
    }
    public LinkedHashMap<ArrayList<Integer>, Integer> getMetaHashMap()
    {
        return metaHashMap;
    }
    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
        confirmed = false;
    }//GEN-LAST:event_formWindowClosing
    public boolean getUseMetaTiles()
    {
        return jCheckBoxGenerateMetaTiles.isSelected();
    }
    public boolean isXbyYarraySelected()
    {
        return jCheckBoxXbyYArray.isSelected();
    }    
    public boolean getAppendLevelName()
    {
        return jCheckBoxAppendLevelVariableName.isSelected();
    }
    public int getMetaSizeX()
    {
        return (Integer) jSpinnerMetaTileX.getValue();
    }
    public int getMetaSizeY()
    {
        return (Integer) jSpinnerMetaTileY.getValue();
    }

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOkActionPerformed
    {//GEN-HEADEREND:event_jButtonOkActionPerformed
        confirmed = true;
        setVisible(false);
    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jSpinnerMetaTileXStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerMetaTileXStateChanged
    {//GEN-HEADEREND:event_jSpinnerMetaTileXStateChanged
        calculateSizes();
    }//GEN-LAST:event_jSpinnerMetaTileXStateChanged

    private void jSpinnerMetaTileYStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerMetaTileYStateChanged
    {//GEN-HEADEREND:event_jSpinnerMetaTileYStateChanged
        calculateSizes();
    }//GEN-LAST:event_jSpinnerMetaTileYStateChanged

    private void jCheckBoxRemoveDuplicateTilesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxRemoveDuplicateTilesActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxRemoveDuplicateTilesActionPerformed
        calculateSizes();        
    }//GEN-LAST:event_jCheckBoxRemoveDuplicateTilesActionPerformed

    private void jButtonComputeNumberOfMetaTilesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonComputeNumberOfMetaTilesActionPerformed
    {//GEN-HEADEREND:event_jButtonComputeNumberOfMetaTilesActionPerformed
        int bestX = 1;
        int bestY = 1;
        int bestSize = 0x7FFFFFFF;
        int size;
        GameMap m;
        if (jCheckBoxRemoveDuplicateTiles.isSelected())
            m = reducedMap;
        else
            m = map;        
        for (int x = 1; x <= 128; x++)
        {
            for (int y = 1; y <= 128; y++)
            {
                int nMetaTiles = map.getSizeX() * map.getSizeY() / x / y;
                int n = calculateNumberOfMetatiles(m, x, y);
                size = nMetaTiles * bytesPerMapMetaTile + n * bytesPerTileEntryInMeta * x * y;
                if (size < bestSize && n > 0)
                {
                    bestSize = size;
                    bestX = x;
                    bestY = y;
                }
            }
        }
        jSpinnerMetaTileX.setValue(bestX);
        jSpinnerMetaTileY.setValue(bestY);

    }//GEN-LAST:event_jButtonComputeNumberOfMetaTilesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonComputeNumberOfMetaTiles;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JCheckBox jCheckBoxAppendLevelVariableName;
    private javax.swing.JCheckBox jCheckBoxGenerateMetaTiles;
    private javax.swing.JCheckBox jCheckBoxRemoveDuplicateTiles;
    private javax.swing.JCheckBox jCheckBoxXbyYArray;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelNumberofMetatiles;
    private javax.swing.JLabel jLabelSizeWithMetatiles;
    private javax.swing.JLabel jLabelSizeWithoutMetatiles;
    private javax.swing.JSpinner jSpinnerMetaTileX;
    private javax.swing.JSpinner jSpinnerMetaTileY;
    private javax.swing.JTextField jTextFieldLevelName;
    private javax.swing.JTextField jTextFieldMapVariableName;
    private javax.swing.JTextField jTextFieldMetaTileVariableName;
    // End of variables declaration//GEN-END:variables
}
