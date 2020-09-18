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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import static uchipgamemapeditor.PaletteRemapEditor.COLUMN_BLUE;
import static uchipgamemapeditor.PaletteRemapEditor.COLUMN_CHGIDX;
import static uchipgamemapeditor.PaletteRemapEditor.COLUMN_CHGPAL;
import static uchipgamemapeditor.PaletteRemapEditor.COLUMN_COLOR;
import static uchipgamemapeditor.PaletteRemapEditor.COLUMN_CURRPAL;
import static uchipgamemapeditor.PaletteRemapEditor.COLUMN_GREEN;
import static uchipgamemapeditor.PaletteRemapEditor.COLUMN_RED;

public class ShadeCreatorDialog extends javax.swing.JDialog
{
    private final static int PANEL_TYPE_START = 0;
    private final static int PANEL_TYPE_END = 1;
    boolean confirmed = false;
    JPanel [] jPanelStartColorArray;
    JPanel [] jPanelEndColorArray;
    JCheckBox [] jCheckBoxEnableArray;
    final int NUMBER_OF_COLORS = PaletteRemapEditor.NUMBER_OF_COLORS;
    final int MAX_NUMBER_OF_PALETTES = PaletteRemapEditor.MAX_NUMBER_OF_PALETTES;
    USVCColor[] startColors;
    USVCColor[] endColors;
    Color [][] shadePreviewColors;
    JTable table;
    PaletteRemapEditor paletteRemapEditor;    
    int tableRows[][];
    Color checkBoxBackgroundColor;
    PreviewPainter previewPainter;
    /**
     * Creates new form ShadeCreatorDialog
     */
    public ShadeCreatorDialog(java.awt.Frame parent, boolean modal, JTable table, int[] rowIndexes, PaletteRemapEditor remapEditor)
    {
        super(parent, modal);
        this.table = table;
        paletteRemapEditor = remapEditor;

        int rows = table.getRowCount();
        int cols = table.getColumnCount();
        // copy table data.
        tableRows = new int [rows][cols];
        shadePreviewColors = new Color[rows][NUMBER_OF_COLORS];
        for (int r = 0; r < rows; r++)
        {
            for (int c = 0; c < NUMBER_OF_COLORS; c++)
            {
                shadePreviewColors[r][c] = Color.black;
            }
        }
        getCopyOfTableData();
        initComponents();
        checkBoxBackgroundColor = jCheckBoxEnable0.getBackground();
        previewPainter = new PreviewPainter ();        
        jPanelPreview.add(previewPainter);
        jPanelPreview.setPreferredSize( new Dimension(jPanelPreview.getSize().width, table.getRowCount()));
        jScrollPanePreview.invalidate();
        jScrollPanePreview.revalidate();
        jPanelPreview.invalidate();
        jPanelPreview.revalidate();
        jPanelPreview.repaint();
        
        jPanelStartColorArray = new JPanel[NUMBER_OF_COLORS];
        jPanelStartColorArray[0] = jPanelStartColor0;
        jPanelStartColorArray[1] = jPanelStartColor1;
        jPanelStartColorArray[2] = jPanelStartColor2;
        jPanelStartColorArray[3] = jPanelStartColor3;
        jPanelStartColorArray[4] = jPanelStartColor4;
        jPanelStartColorArray[5] = jPanelStartColor5;
        jPanelStartColorArray[6] = jPanelStartColor6;
        jPanelStartColorArray[7] = jPanelStartColor7;
        jPanelStartColorArray[8] = jPanelStartColor8;
        jPanelStartColorArray[9] = jPanelStartColor9;
        jPanelStartColorArray[10] = jPanelStartColor10;
        jPanelStartColorArray[11] = jPanelStartColor11;
        jPanelStartColorArray[12] = jPanelStartColor12;
        jPanelStartColorArray[13] = jPanelStartColor13;
        jPanelStartColorArray[14] = jPanelStartColor14;
        jPanelStartColorArray[15] = jPanelStartColor15;
        jPanelEndColorArray = new JPanel[NUMBER_OF_COLORS];
        jPanelEndColorArray[0] = jPanelEndColor0;
        jPanelEndColorArray[1] = jPanelEndColor1;
        jPanelEndColorArray[2] = jPanelEndColor2;
        jPanelEndColorArray[3] = jPanelEndColor3;
        jPanelEndColorArray[4] = jPanelEndColor4;
        jPanelEndColorArray[5] = jPanelEndColor5;
        jPanelEndColorArray[6] = jPanelEndColor6;
        jPanelEndColorArray[7] = jPanelEndColor7;
        jPanelEndColorArray[8] = jPanelEndColor8;
        jPanelEndColorArray[9] = jPanelEndColor9;
        jPanelEndColorArray[10] = jPanelEndColor10;
        jPanelEndColorArray[11] = jPanelEndColor11;
        jPanelEndColorArray[12] = jPanelEndColor12;
        jPanelEndColorArray[13] = jPanelEndColor13;
        jPanelEndColorArray[14] = jPanelEndColor14;
        jPanelEndColorArray[15] = jPanelEndColor15;
        jCheckBoxEnableArray = new JCheckBox[NUMBER_OF_COLORS];
        jCheckBoxEnableArray[0] = jCheckBoxEnable0;
        jCheckBoxEnableArray[1] = jCheckBoxEnable1;
        jCheckBoxEnableArray[2] = jCheckBoxEnable2;
        jCheckBoxEnableArray[3] = jCheckBoxEnable3;
        jCheckBoxEnableArray[4] = jCheckBoxEnable4;
        jCheckBoxEnableArray[5] = jCheckBoxEnable5;
        jCheckBoxEnableArray[6] = jCheckBoxEnable6;
        jCheckBoxEnableArray[7] = jCheckBoxEnable7;
        jCheckBoxEnableArray[8] = jCheckBoxEnable8;
        jCheckBoxEnableArray[9] = jCheckBoxEnable9;
        jCheckBoxEnableArray[10] = jCheckBoxEnable10;
        jCheckBoxEnableArray[11] = jCheckBoxEnable11;
        jCheckBoxEnableArray[12] = jCheckBoxEnable12;
        jCheckBoxEnableArray[13] = jCheckBoxEnable13;
        jCheckBoxEnableArray[14] = jCheckBoxEnable14;
        jCheckBoxEnableArray[15] = jCheckBoxEnable15;
        startColors = new USVCColor[NUMBER_OF_COLORS];
        endColors = new USVCColor[NUMBER_OF_COLORS];
        for (int i = 0; i < NUMBER_OF_COLORS; i++)
        {
            startColors[i] = new USVCColor();
            endColors[i] = new USVCColor();
            jPanelStartColorArray[i].addMouseListener(new ColorPanelMouseAdapter(i, jPanelStartColorArray[i], startColors[i]));
            jPanelEndColorArray[i].addMouseListener(new ColorPanelMouseAdapter(i, jPanelEndColorArray[i], endColors[i]));
            jCheckBoxEnableArray[i].addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent evt)
                {
                    recalculateShades();
                }
            });
        }
        int startRow,endRow;
        if (rowIndexes.length == 0)
        {
            startRow = 0;
            endRow = 0;
        }
        else if (rowIndexes.length == 1)
        {
            startRow = rowIndexes[0];
            endRow = startRow;
        }
        else
        {
            startRow = rowIndexes[0];
            endRow =  rowIndexes[rowIndexes.length - 1];

        }
        ((SpinnerNumberModel) jSpinnerStartRow.getModel()).setMaximum(rows - 1);
        ((SpinnerNumberModel) jSpinnerEndRow.getModel()).setMaximum(rows - 1); 
        jSpinnerStartRow.setValue(startRow);
        jSpinnerEndRow.setValue(endRow);
        getDataFromTable(startRow, endRow);     
        recalculateShades();
    }
    class PreviewPainter extends JComponent 
    {

        public PreviewPainter() 
        {
           
        }
        public void paint(Graphics gr) 
        {
            Graphics2D g2d = (Graphics2D) gr;
            int rows = table.getRowCount();
            int w = jPanelPreview.getWidth();
            for (int r = 0; r < rows; r++)
            {
                for (int c = 0; c < NUMBER_OF_COLORS; c++)
                {
                    g2d.setPaint(shadePreviewColors[r][c]);
                    g2d.drawLine((w * c) / NUMBER_OF_COLORS, r, (w * (c + 1)) / NUMBER_OF_COLORS - 1, r);
                }
            }  
            g2d.dispose();
        }  
    }
    
    void  getCopyOfTableData()
    {
        int rows = table.getRowCount();
        int cols = table.getColumnCount();        
        for (int r = 0; r < rows; r++)
            {
                for (int c = 0; c < cols; c++)
                    tableRows[r][c] = (Integer) table.getValueAt(r, c);
            }    
    }
    void calculatePreviewShades()
    {  
        int rows = table.getRowCount();
        // create temporary palettes.
        USVCColor [][] colors = new USVCColor [MAX_NUMBER_OF_PALETTES][NUMBER_OF_COLORS];
        for (int i = 0; i < NUMBER_OF_COLORS; i++)
            for (int j= 0; j < MAX_NUMBER_OF_PALETTES; j++)
                colors[j][i] = new USVCColor();
        for (int cycle = 0; cycle < 2; cycle ++)
        {
            for (int i = 0; i < rows; i++)
            {
                int showPal = tableRows[i][COLUMN_CURRPAL];
                int workPal = tableRows[i][COLUMN_CHGPAL];
                int idx = tableRows[i][COLUMN_CHGIDX];
                if (idx < NUMBER_OF_COLORS)
                {                      
                        colors[workPal][idx].red = tableRows[i][COLUMN_RED];
                        colors[workPal][idx].green = tableRows[i][COLUMN_GREEN];
                        colors[workPal][idx].blue =tableRows[i][COLUMN_BLUE];
                }
                if (cycle == 1)
                {
                    for (int c = 0; c < NUMBER_OF_COLORS; c++)
                    {
                        shadePreviewColors[i][c] = new Color (USVCMapEditorUtilities.USVCRGBtoRGB24(colors[showPal][c].red, colors[showPal][c].green, colors[showPal][c].blue));
                    }
                }
            }
            // we need to perform this twice, because, if the palette is not defined at each start of frame, then after the first frame, its value might be determined by the last scanline
            //
            if (cycle ==0 )
            {
                for (int p = 0; p < MAX_NUMBER_OF_PALETTES; p++)
                {
                    for (int i = 0; i < NUMBER_OF_COLORS; i++)
                    {
                        if (paletteRemapEditor.initialPalettes[p].definedAtStartOfFrame)
                        {
                            colors[p][i] = paletteRemapEditor.initialPalettes[p].colors[i].getCopy();
                        }
                    }
                }
            }
        }            
    }
    int interpolate(double startColor, double endColor, double f, boolean squareMode)
    {
        if (squareMode)
        {
            return (int) (Math.sqrt(0.25+ Math.pow(startColor, 2) * (1 - f) + Math.pow(endColor, 2) * f) );            
        }
        else 
            return (int) (0.5 + startColor + (endColor - startColor ) * f);
    }
    void recalculateShades()
    {
        // first: check the values
        if (!checkValues(false))
            return;
        getCopyOfTableData();        
        boolean squareMode = jCheckBoxSquareLaw.isSelected();
        int startRow = (Integer) jSpinnerStartRow.getValue();
        int endRow = (Integer) jSpinnerEndRow.getValue();
        int numberOfShades = (Integer) jSpinnerNumberOfShades.getValue();
        int rowsPerShade = (endRow - startRow + 1) / numberOfShades;
        int workingPalette = 1, shownPalette = 0;
        for (int shade = 0; shade < numberOfShades; shade++)
        {
            int enabledColors = 0;
            if (0 == (shade & 1))
            {
                shownPalette = (Integer) jSpinnerStartShownPalette.getValue();
                workingPalette = (Integer) jSpinnerStartWorkingPalette.getValue();
            }
            else
            {
                workingPalette = (Integer) jSpinnerStartShownPalette.getValue();
                shownPalette = (Integer) jSpinnerStartWorkingPalette.getValue();                
            }
            
            for (int c = 0; c < NUMBER_OF_COLORS; c++)
            {
                if (jCheckBoxEnableArray[c].isSelected())
                {                    
                    int r, g, b;
                    // get start color, end color
                    if (shade < numberOfShades -1 )
                    {
                        //r = (int) (0.5 + startColors[c].red + ( (shade + 1) *  ((double) (endColors[c].red - startColors[c].red))  / (numberOfShades - 1)));
                        //g = (int) (0.5 + startColors[c].green + ( (shade + 1) *  ((double) (endColors[c].green - startColors[c].green))  / (numberOfShades - 1)  ));
                        //b = (int) (0.5 + startColors[c].blue + ( (shade + 1) *  ((double) (endColors[c].blue - startColors[c].blue))  / (numberOfShades - 1)));
                        r = interpolate(startColors[c].red, endColors[c].red, (shade + 1.0)/(numberOfShades - 1.0), squareMode);
                        g = interpolate(startColors[c].green, endColors[c].green, (shade + 1.0)/(numberOfShades - 1.0), squareMode);
                        b = interpolate(startColors[c].blue, endColors[c].blue, (shade + 1.0)/(numberOfShades - 1.0), squareMode);
                        tableRows[shade * rowsPerShade + enabledColors + startRow][COLUMN_RED] = r;
                        tableRows[shade * rowsPerShade + enabledColors + startRow][COLUMN_GREEN] = g;
                        tableRows[shade * rowsPerShade + enabledColors + startRow][COLUMN_BLUE] = b;
                    }
                    tableRows[shade * rowsPerShade + enabledColors + startRow][COLUMN_CURRPAL] = shownPalette;
                    tableRows[shade * rowsPerShade + enabledColors + startRow][COLUMN_CHGPAL] = workingPalette;
                    tableRows[shade * rowsPerShade + enabledColors + startRow][COLUMN_CHGIDX] = c; 
                    enabledColors++;
                }
            }
            for (; enabledColors < rowsPerShade; enabledColors ++)
            {
                tableRows[shade * rowsPerShade + enabledColors + startRow][COLUMN_RED] = tableRows[shade * rowsPerShade + enabledColors + startRow -1][COLUMN_RED];
                tableRows[shade * rowsPerShade + enabledColors + startRow][COLUMN_GREEN] = tableRows[shade * rowsPerShade + enabledColors + startRow -1][COLUMN_GREEN];
                tableRows[shade * rowsPerShade + enabledColors + startRow][COLUMN_BLUE] = tableRows[shade * rowsPerShade + enabledColors + startRow -1][COLUMN_BLUE];
                tableRows[shade * rowsPerShade + enabledColors + startRow][COLUMN_CURRPAL] = tableRows[shade * rowsPerShade + enabledColors + startRow -1][COLUMN_CURRPAL];
                tableRows[shade * rowsPerShade + enabledColors + startRow][COLUMN_CHGPAL] = tableRows[shade * rowsPerShade + enabledColors + startRow -1][COLUMN_CHGPAL];
                tableRows[shade * rowsPerShade + enabledColors + startRow][COLUMN_CHGIDX] = tableRows[shade * rowsPerShade + enabledColors + startRow -1][COLUMN_CHGIDX];            
                // here we have more rows than needed. Just copy last row so it won't have any effect...            
            }
        }
        // then recalculate the shades
        calculatePreviewShades();
        // third repaint
        jPanelPreview.repaint();
    }
    void getDataFromTable(int startRow, int endRow)
    {
        USVCColor [] startColors =  paletteRemapEditor.calculateCurrentPaletteAtRow(startRow);
        USVCColor [] endColors =  paletteRemapEditor.calculateCurrentPaletteAtRow(endRow);
        for (int i = 0; i < NUMBER_OF_COLORS; i++)
        {     
            jPanelStartColorArray[i].setBackground(new Color(USVCMapEditorUtilities.USVCRGBtoRGB24(startColors[i].red, startColors[i].green, startColors[i].blue)));
            jPanelEndColorArray[i].setBackground(new Color(USVCMapEditorUtilities.USVCRGBtoRGB24(endColors[i].red, endColors[i].green, endColors[i].blue)));
            // hard copy
            this.startColors[i].red = startColors[i].red;
            this.startColors[i].green = startColors[i].green;
            this.startColors[i].blue = startColors[i].blue;
            this.endColors[i].red = endColors[i].red;
            this.endColors[i].green = endColors[i].green;
            this.endColors[i].blue = endColors[i].blue;

        }
        jSpinnerStartShownPalette.setValue((Integer) table.getValueAt(startRow, COLUMN_CURRPAL));
        jSpinnerStartWorkingPalette.setValue((Integer) table.getValueAt(startRow, COLUMN_CHGPAL));
    }
    class ColorPanelMouseAdapter extends java.awt.event.MouseAdapter
    {
        JPanel jPanel;
        USVCColor color;
        int colorIndex;
        public void mouseClicked(java.awt.event.MouseEvent evt)
        {
            changeColor(colorIndex, jPanel, color);
            recalculateShades();
        }
        public ColorPanelMouseAdapter(int colorIndex, JPanel jPanel, USVCColor color)
        {
            super();
            this.jPanel = jPanel;
            this.color = color;
            this.colorIndex = colorIndex;
        }
    }
    void changeColor(int colorIndex, JPanel panel, USVCColor color)
    {
        final ColorChooserDialog ccd = new ColorChooserDialog(null, true, color.red, color.green, color.blue, colorIndex);
        ccd.setVisible(true);        
        if (ccd.confirmed)
        {
            color.red = ccd.getRed();
            color.green = ccd.getGreen();
            color.blue = ccd.getBlue();
            int c = USVCMapEditorUtilities.USVCRGBtoRGB24(color.red, color.green, color.blue);
            panel.setBackground(new Color(c));
        }
        // TODO REPAINT EVERYTHING
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jSpinnerStartRow = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jSpinnerEndRow = new javax.swing.JSpinner();
        jScrollPanePreview = new javax.swing.JScrollPane();
        jPanelPreview = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanelStartColor12 = new javax.swing.JPanel();
        jPanelEndColor3 = new javax.swing.JPanel();
        jPanelStartColor8 = new javax.swing.JPanel();
        jPanelEndColor7 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanelEndColor4 = new javax.swing.JPanel();
        jPanelEndColor10 = new javax.swing.JPanel();
        jCheckBoxEnable6 = new javax.swing.JCheckBox();
        jCheckBoxEnable2 = new javax.swing.JCheckBox();
        jCheckBoxEnable1 = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jCheckBoxEnable7 = new javax.swing.JCheckBox();
        jPanelStartColor13 = new javax.swing.JPanel();
        jPanelEndColor13 = new javax.swing.JPanel();
        jPanelStartColor3 = new javax.swing.JPanel();
        jPanelEndColor11 = new javax.swing.JPanel();
        jPanelStartColor7 = new javax.swing.JPanel();
        jPanelStartColor0 = new javax.swing.JPanel();
        jCheckBoxEnable5 = new javax.swing.JCheckBox();
        jPanelEndColor8 = new javax.swing.JPanel();
        jPanelStartColor4 = new javax.swing.JPanel();
        jPanelEndColor2 = new javax.swing.JPanel();
        jCheckBoxEnable8 = new javax.swing.JCheckBox();
        jPanelEndColor6 = new javax.swing.JPanel();
        jPanelStartColor6 = new javax.swing.JPanel();
        jPanelEndColor0 = new javax.swing.JPanel();
        jCheckBoxEnable10 = new javax.swing.JCheckBox();
        jPanelStartColor15 = new javax.swing.JPanel();
        jCheckBoxEnable12 = new javax.swing.JCheckBox();
        jPanelEndColor5 = new javax.swing.JPanel();
        jPanelStartColor14 = new javax.swing.JPanel();
        jCheckBoxEnable9 = new javax.swing.JCheckBox();
        jPanelEndColor1 = new javax.swing.JPanel();
        jPanelStartColor9 = new javax.swing.JPanel();
        jCheckBoxEnable4 = new javax.swing.JCheckBox();
        jCheckBoxEnable15 = new javax.swing.JCheckBox();
        jCheckBoxEnable14 = new javax.swing.JCheckBox();
        jPanelStartColor2 = new javax.swing.JPanel();
        jCheckBoxEnable11 = new javax.swing.JCheckBox();
        jPanelStartColor10 = new javax.swing.JPanel();
        jPanelEndColor15 = new javax.swing.JPanel();
        jCheckBoxEnable13 = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jPanelEndColor12 = new javax.swing.JPanel();
        jPanelStartColor5 = new javax.swing.JPanel();
        jPanelStartColor1 = new javax.swing.JPanel();
        jPanelEndColor9 = new javax.swing.JPanel();
        jPanelEndColor14 = new javax.swing.JPanel();
        jCheckBoxEnable3 = new javax.swing.JCheckBox();
        jCheckBoxEnable0 = new javax.swing.JCheckBox();
        jPanelStartColor11 = new javax.swing.JPanel();
        jCheckBoxSquareLaw = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jSpinnerNumberOfShades = new javax.swing.JSpinner();
        jButtonOk = new javax.swing.JButton();
        jButtonDiscard = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jSpinnerStartShownPalette = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jSpinnerStartWorkingPalette = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jButtonGetDataFromTable = new javax.swing.JButton();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(621, 603));

        jLabel1.setText("Start Row:");

        jSpinnerStartRow.setModel(new javax.swing.SpinnerNumberModel(0, 0, 200, 1));
        jSpinnerStartRow.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerStartRowStateChanged(evt);
            }
        });

        jLabel2.setText("End Row:");

        jSpinnerEndRow.setModel(new javax.swing.SpinnerNumberModel(0, 0, 200, 1));
        jSpinnerEndRow.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerEndRowStateChanged(evt);
            }
        });

        jScrollPanePreview.setToolTipText("");
        jScrollPanePreview.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jPanelPreview.setLayout(new java.awt.BorderLayout());
        jScrollPanePreview.setViewportView(jPanelPreview);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Colors to change"));

        jPanelStartColor12.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor12.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor12.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor12Layout = new javax.swing.GroupLayout(jPanelStartColor12);
        jPanelStartColor12.setLayout(jPanelStartColor12Layout);
        jPanelStartColor12Layout.setHorizontalGroup(
            jPanelStartColor12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor12Layout.setVerticalGroup(
            jPanelStartColor12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor3.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor3.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor3Layout = new javax.swing.GroupLayout(jPanelEndColor3);
        jPanelEndColor3.setLayout(jPanelEndColor3Layout);
        jPanelEndColor3Layout.setHorizontalGroup(
            jPanelEndColor3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor3Layout.setVerticalGroup(
            jPanelEndColor3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor8.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor8.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor8.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor8Layout = new javax.swing.GroupLayout(jPanelStartColor8);
        jPanelStartColor8.setLayout(jPanelStartColor8Layout);
        jPanelStartColor8Layout.setHorizontalGroup(
            jPanelStartColor8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor8Layout.setVerticalGroup(
            jPanelStartColor8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor7.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor7.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor7Layout = new javax.swing.GroupLayout(jPanelEndColor7);
        jPanelEndColor7.setLayout(jPanelEndColor7Layout);
        jPanelEndColor7Layout.setHorizontalGroup(
            jPanelEndColor7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor7Layout.setVerticalGroup(
            jPanelEndColor7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jLabel3.setText("Start colors:");

        jPanelEndColor4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor4.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor4.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor4Layout = new javax.swing.GroupLayout(jPanelEndColor4);
        jPanelEndColor4.setLayout(jPanelEndColor4Layout);
        jPanelEndColor4Layout.setHorizontalGroup(
            jPanelEndColor4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor4Layout.setVerticalGroup(
            jPanelEndColor4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor10.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor10.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor10Layout = new javax.swing.GroupLayout(jPanelEndColor10);
        jPanelEndColor10.setLayout(jPanelEndColor10Layout);
        jPanelEndColor10Layout.setHorizontalGroup(
            jPanelEndColor10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor10Layout.setVerticalGroup(
            jPanelEndColor10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jLabel5.setText("Enable:");

        jPanelStartColor13.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor13.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor13.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor13Layout = new javax.swing.GroupLayout(jPanelStartColor13);
        jPanelStartColor13.setLayout(jPanelStartColor13Layout);
        jPanelStartColor13Layout.setHorizontalGroup(
            jPanelStartColor13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor13Layout.setVerticalGroup(
            jPanelStartColor13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor13.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor13.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor13.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor13Layout = new javax.swing.GroupLayout(jPanelEndColor13);
        jPanelEndColor13.setLayout(jPanelEndColor13Layout);
        jPanelEndColor13Layout.setHorizontalGroup(
            jPanelEndColor13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor13Layout.setVerticalGroup(
            jPanelEndColor13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor3.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor3.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor3Layout = new javax.swing.GroupLayout(jPanelStartColor3);
        jPanelStartColor3.setLayout(jPanelStartColor3Layout);
        jPanelStartColor3Layout.setHorizontalGroup(
            jPanelStartColor3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor3Layout.setVerticalGroup(
            jPanelStartColor3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor11.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor11.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor11.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor11Layout = new javax.swing.GroupLayout(jPanelEndColor11);
        jPanelEndColor11.setLayout(jPanelEndColor11Layout);
        jPanelEndColor11Layout.setHorizontalGroup(
            jPanelEndColor11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor11Layout.setVerticalGroup(
            jPanelEndColor11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor7.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor7.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor7Layout = new javax.swing.GroupLayout(jPanelStartColor7);
        jPanelStartColor7.setLayout(jPanelStartColor7Layout);
        jPanelStartColor7Layout.setHorizontalGroup(
            jPanelStartColor7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor7Layout.setVerticalGroup(
            jPanelStartColor7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor0.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor0.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor0.setMinimumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor0.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor0Layout = new javax.swing.GroupLayout(jPanelStartColor0);
        jPanelStartColor0.setLayout(jPanelStartColor0Layout);
        jPanelStartColor0Layout.setHorizontalGroup(
            jPanelStartColor0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor0Layout.setVerticalGroup(
            jPanelStartColor0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor8.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor8.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor8.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor8Layout = new javax.swing.GroupLayout(jPanelEndColor8);
        jPanelEndColor8.setLayout(jPanelEndColor8Layout);
        jPanelEndColor8Layout.setHorizontalGroup(
            jPanelEndColor8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor8Layout.setVerticalGroup(
            jPanelEndColor8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor4.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor4.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor4Layout = new javax.swing.GroupLayout(jPanelStartColor4);
        jPanelStartColor4.setLayout(jPanelStartColor4Layout);
        jPanelStartColor4Layout.setHorizontalGroup(
            jPanelStartColor4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor4Layout.setVerticalGroup(
            jPanelStartColor4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor2.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor2.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor2Layout = new javax.swing.GroupLayout(jPanelEndColor2);
        jPanelEndColor2.setLayout(jPanelEndColor2Layout);
        jPanelEndColor2Layout.setHorizontalGroup(
            jPanelEndColor2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor2Layout.setVerticalGroup(
            jPanelEndColor2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor6.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor6.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor6Layout = new javax.swing.GroupLayout(jPanelEndColor6);
        jPanelEndColor6.setLayout(jPanelEndColor6Layout);
        jPanelEndColor6Layout.setHorizontalGroup(
            jPanelEndColor6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor6Layout.setVerticalGroup(
            jPanelEndColor6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor6.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor6.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor6Layout = new javax.swing.GroupLayout(jPanelStartColor6);
        jPanelStartColor6.setLayout(jPanelStartColor6Layout);
        jPanelStartColor6Layout.setHorizontalGroup(
            jPanelStartColor6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor6Layout.setVerticalGroup(
            jPanelStartColor6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor0.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor0.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor0.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor0Layout = new javax.swing.GroupLayout(jPanelEndColor0);
        jPanelEndColor0.setLayout(jPanelEndColor0Layout);
        jPanelEndColor0Layout.setHorizontalGroup(
            jPanelEndColor0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor0Layout.setVerticalGroup(
            jPanelEndColor0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor15.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor15.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor15.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor15Layout = new javax.swing.GroupLayout(jPanelStartColor15);
        jPanelStartColor15.setLayout(jPanelStartColor15Layout);
        jPanelStartColor15Layout.setHorizontalGroup(
            jPanelStartColor15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor15Layout.setVerticalGroup(
            jPanelStartColor15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor5.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor5.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor5Layout = new javax.swing.GroupLayout(jPanelEndColor5);
        jPanelEndColor5.setLayout(jPanelEndColor5Layout);
        jPanelEndColor5Layout.setHorizontalGroup(
            jPanelEndColor5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor5Layout.setVerticalGroup(
            jPanelEndColor5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor14.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor14.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor14.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor14Layout = new javax.swing.GroupLayout(jPanelStartColor14);
        jPanelStartColor14.setLayout(jPanelStartColor14Layout);
        jPanelStartColor14Layout.setHorizontalGroup(
            jPanelStartColor14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor14Layout.setVerticalGroup(
            jPanelStartColor14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor1.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor1.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor1Layout = new javax.swing.GroupLayout(jPanelEndColor1);
        jPanelEndColor1.setLayout(jPanelEndColor1Layout);
        jPanelEndColor1Layout.setHorizontalGroup(
            jPanelEndColor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor1Layout.setVerticalGroup(
            jPanelEndColor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor9.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor9.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor9Layout = new javax.swing.GroupLayout(jPanelStartColor9);
        jPanelStartColor9.setLayout(jPanelStartColor9Layout);
        jPanelStartColor9Layout.setHorizontalGroup(
            jPanelStartColor9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor9Layout.setVerticalGroup(
            jPanelStartColor9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor2.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor2.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor2Layout = new javax.swing.GroupLayout(jPanelStartColor2);
        jPanelStartColor2.setLayout(jPanelStartColor2Layout);
        jPanelStartColor2Layout.setHorizontalGroup(
            jPanelStartColor2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor2Layout.setVerticalGroup(
            jPanelStartColor2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor10.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor10.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor10Layout = new javax.swing.GroupLayout(jPanelStartColor10);
        jPanelStartColor10.setLayout(jPanelStartColor10Layout);
        jPanelStartColor10Layout.setHorizontalGroup(
            jPanelStartColor10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor10Layout.setVerticalGroup(
            jPanelStartColor10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor15.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor15.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor15.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor15Layout = new javax.swing.GroupLayout(jPanelEndColor15);
        jPanelEndColor15.setLayout(jPanelEndColor15Layout);
        jPanelEndColor15Layout.setHorizontalGroup(
            jPanelEndColor15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor15Layout.setVerticalGroup(
            jPanelEndColor15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jLabel4.setText("End colors:");

        jPanelEndColor12.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor12.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor12.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor12Layout = new javax.swing.GroupLayout(jPanelEndColor12);
        jPanelEndColor12.setLayout(jPanelEndColor12Layout);
        jPanelEndColor12Layout.setHorizontalGroup(
            jPanelEndColor12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor12Layout.setVerticalGroup(
            jPanelEndColor12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor5.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor5.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor5Layout = new javax.swing.GroupLayout(jPanelStartColor5);
        jPanelStartColor5.setLayout(jPanelStartColor5Layout);
        jPanelStartColor5Layout.setHorizontalGroup(
            jPanelStartColor5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor5Layout.setVerticalGroup(
            jPanelStartColor5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor1.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor1.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor1Layout = new javax.swing.GroupLayout(jPanelStartColor1);
        jPanelStartColor1.setLayout(jPanelStartColor1Layout);
        jPanelStartColor1Layout.setHorizontalGroup(
            jPanelStartColor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor1Layout.setVerticalGroup(
            jPanelStartColor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor9.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor9.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor9Layout = new javax.swing.GroupLayout(jPanelEndColor9);
        jPanelEndColor9.setLayout(jPanelEndColor9Layout);
        jPanelEndColor9Layout.setHorizontalGroup(
            jPanelEndColor9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor9Layout.setVerticalGroup(
            jPanelEndColor9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelEndColor14.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelEndColor14.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelEndColor14.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelEndColor14Layout = new javax.swing.GroupLayout(jPanelEndColor14);
        jPanelEndColor14.setLayout(jPanelEndColor14Layout);
        jPanelEndColor14Layout.setHorizontalGroup(
            jPanelEndColor14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelEndColor14Layout.setVerticalGroup(
            jPanelEndColor14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelStartColor11.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelStartColor11.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelStartColor11.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelStartColor11Layout = new javax.swing.GroupLayout(jPanelStartColor11);
        jPanelStartColor11.setLayout(jPanelStartColor11Layout);
        jPanelStartColor11Layout.setHorizontalGroup(
            jPanelStartColor11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelStartColor11Layout.setVerticalGroup(
            jPanelStartColor11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel4)
                            .addGap(16, 16, 16))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable0))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelEndColor14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBoxEnable14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxEnable15)
                            .addComponent(jPanelEndColor15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanelStartColor0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelStartColor15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jCheckBoxEnable8)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanelStartColor8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelStartColor15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanelEndColor8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelEndColor15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jCheckBoxEnable0)
                            .addComponent(jCheckBoxEnable1)
                            .addComponent(jCheckBoxEnable2)
                            .addComponent(jCheckBoxEnable3)
                            .addComponent(jCheckBoxEnable4)
                            .addComponent(jCheckBoxEnable5)
                            .addComponent(jCheckBoxEnable6)
                            .addComponent(jCheckBoxEnable7)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)))
                    .addComponent(jCheckBoxEnable9)
                    .addComponent(jCheckBoxEnable10)
                    .addComponent(jCheckBoxEnable11)
                    .addComponent(jCheckBoxEnable12)
                    .addComponent(jCheckBoxEnable13)
                    .addComponent(jCheckBoxEnable14)
                    .addComponent(jCheckBoxEnable15))
                .addContainerGap())
        );

        jCheckBoxSquareLaw.setText("Square Law");
        jCheckBoxSquareLaw.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxSquareLawActionPerformed(evt);
            }
        });

        jLabel7.setText("Number of shades:");

        jSpinnerNumberOfShades.setModel(new javax.swing.SpinnerNumberModel(8, 2, 8, 1));
        jSpinnerNumberOfShades.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerNumberOfShadesStateChanged(evt);
            }
        });

        jButtonOk.setText("Accept and Copy to table");
        jButtonOk.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonOkActionPerformed(evt);
            }
        });

        jButtonDiscard.setText("Discard");
        jButtonDiscard.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonDiscardActionPerformed(evt);
            }
        });

        jLabel6.setText("Start Shown Palette:");

        jSpinnerStartShownPalette.setModel(new javax.swing.SpinnerNumberModel(0, 0, 15, 1));
        jSpinnerStartShownPalette.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerStartShownPaletteStateChanged(evt);
            }
        });

        jSpinnerStartWorkingPalette.setModel(new javax.swing.SpinnerNumberModel(1, 0, 15, 1));
        jSpinnerStartWorkingPalette.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerStartWorkingPaletteStateChanged(evt);
            }
        });

        jLabel9.setText("Start Workng Palette");

        jButtonGetDataFromTable.setText("Get Start Data From Table");
        jButtonGetDataFromTable.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonGetDataFromTableActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPanePreview)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonOk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonDiscard))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinnerStartShownPalette, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinnerStartWorkingPalette, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonGetDataFromTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(17, 17, 17)
                        .addComponent(jSpinnerStartRow, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSpinnerEndRow, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBoxSquareLaw)
                        .addGap(27, 27, 27)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSpinnerNumberOfShades, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jSpinnerStartRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jSpinnerEndRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxSquareLaw)
                    .addComponent(jLabel7)
                    .addComponent(jSpinnerNumberOfShades, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9)
                        .addComponent(jSpinnerStartWorkingPalette, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonGetDataFromTable))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(jSpinnerStartShownPalette, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8)))
                .addGap(9, 9, 9)
                .addComponent(jScrollPanePreview, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonOk)
                    .addComponent(jButtonDiscard))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonOkActionPerformed
    {//GEN-HEADEREND:event_jButtonOkActionPerformed
        if (checkValues(true))
        {
            if (USVCMapEditorUtilities.questionYesNo("Overwrite data in table ?", "Confirm data overwrite") == JOptionPane.YES_OPTION)
            {
                confirmed = true;
                // copy data.
                int rows = table.getRowCount();
                int startRow = (Integer) jSpinnerStartRow.getValue();
                int endRow = (Integer) jSpinnerEndRow.getValue();
                for (int r = startRow; r <= endRow; r++)
                {
                    table.setValueAt( tableRows[r][COLUMN_RED], r, COLUMN_RED);
                    table.setValueAt( tableRows[r][COLUMN_GREEN], r, COLUMN_GREEN);
                    table.setValueAt( tableRows[r][COLUMN_BLUE], r, COLUMN_BLUE);
                    table.setValueAt( tableRows[r][COLUMN_CURRPAL], r, COLUMN_CURRPAL);
                    table.setValueAt( tableRows[r][COLUMN_CHGPAL], r, COLUMN_CHGPAL);
                    table.setValueAt(  tableRows[r][COLUMN_CHGIDX], r, COLUMN_CHGIDX); 
                    table.setValueAt(USVCMapEditorUtilities.USVCRGBtoRGB24(tableRows[r][COLUMN_RED], tableRows[r][COLUMN_GREEN], tableRows[r][COLUMN_BLUE]), r, COLUMN_COLOR);
                }
                setVisible(false);
                dispose();
            }
        }
    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jButtonDiscardActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonDiscardActionPerformed
    {//GEN-HEADEREND:event_jButtonDiscardActionPerformed
         if (USVCMapEditorUtilities.questionYesNo("Confirm discard?", "Confirm discard") == JOptionPane.YES_OPTION)
        {
            confirmed = true;
            setVisible(false);
            dispose();
        }
    }//GEN-LAST:event_jButtonDiscardActionPerformed
    void warnAboutWrongValues(String text, boolean show)
    {
        if (show)
            USVCMapEditorUtilities.infoBox(text, "Wrong value warning", JOptionPane.WARNING_MESSAGE);
    }
    boolean checkValues(boolean showInfoBox)
    {
        int numberOfShades = (Integer) jSpinnerNumberOfShades.getValue();
        int enabledColors = 0;
        for (int i = 0; i < NUMBER_OF_COLORS; i++)
            if (jCheckBoxEnableArray[i].isSelected())
                enabledColors++;
        int startRow = (Integer) jSpinnerStartRow.getValue();
        int endRow = (Integer) jSpinnerEndRow.getValue();
        int initialVisiblePalette = (Integer) jSpinnerStartShownPalette.getValue();
        int initialWorkingPalette = (Integer) jSpinnerStartWorkingPalette.getValue();
        // reset colors to original
        ((JSpinner.DefaultEditor) jSpinnerStartShownPalette.getEditor()).getTextField().setForeground(Color.black);
        ((JSpinner.DefaultEditor) jSpinnerStartWorkingPalette.getEditor()).getTextField().setForeground(Color.black);
        ((JSpinner.DefaultEditor) jSpinnerStartRow.getEditor()).getTextField().setForeground(Color.black);
        ((JSpinner.DefaultEditor) jSpinnerEndRow.getEditor()).getTextField().setForeground(Color.black);
        ((JSpinner.DefaultEditor) jSpinnerNumberOfShades.getEditor()).getTextField().setForeground(Color.black);
        for (int i = 0; i < NUMBER_OF_COLORS; i++)
            jCheckBoxEnableArray[i].setBackground(checkBoxBackgroundColor);
        
        // first check that the initial working palette is different form the initial shown palette 
        if (initialVisiblePalette == initialWorkingPalette)
        {
            warnAboutWrongValues("Initial working and visible palette should be dfferent", showInfoBox);
            ((JSpinner.DefaultEditor) jSpinnerStartShownPalette.getEditor()).getTextField().setForeground(Color.red);
            ((JSpinner.DefaultEditor) jSpinnerStartWorkingPalette.getEditor()).getTextField().setForeground(Color.red);
            return false;
        }
        if (enabledColors == 0)
        {
            for (int i = 0; i < NUMBER_OF_COLORS; i++)
                jCheckBoxEnableArray[i].setBackground(Color.red);
            warnAboutWrongValues("Enable at least one color!", showInfoBox);
            return false;
        }
        // then check if startRow and End row are ok
        if (startRow >= endRow)
        {
            ((JSpinner.DefaultEditor) jSpinnerStartRow.getEditor()).getTextField().setForeground(Color.red);
            ((JSpinner.DefaultEditor) jSpinnerEndRow.getEditor()).getTextField().setForeground(Color.red);
            warnAboutWrongValues("End row should be strictly larger than start row", showInfoBox);
            return false;            
        }
        // finally, the total number of rows (endRow - startRow + 1) should be divisible by the number of shades, and the quotient should be equal or larger than the number of selected colors 
        int remainder = (endRow - startRow + 1) % numberOfShades;
        int quotient = (endRow - startRow + 1) / numberOfShades;       
        if (remainder != 0 || quotient < enabledColors)
        {
            ((JSpinner.DefaultEditor) jSpinnerNumberOfShades.getEditor()).getTextField().setForeground(Color.red);
            warnAboutWrongValues("The number of rows '(end row - start row + 1)' should be divisible by the number of shades, and '(end row - start row + 1) / number of shades' should be at least equal to the number of enabled colors", showInfoBox);
            return false;
        }
        return true;
    }
    void createShades ()
    {
        // 
    }
    private void jButtonGetDataFromTableActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonGetDataFromTableActionPerformed
    {//GEN-HEADEREND:event_jButtonGetDataFromTableActionPerformed
        getDataFromTable((Integer) jSpinnerStartRow.getValue(), (Integer) jSpinnerEndRow.getValue());   
        recalculateShades();
    }//GEN-LAST:event_jButtonGetDataFromTableActionPerformed

    private void jSpinnerStartRowStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerStartRowStateChanged
    {//GEN-HEADEREND:event_jSpinnerStartRowStateChanged
        recalculateShades();
    }//GEN-LAST:event_jSpinnerStartRowStateChanged

    private void jSpinnerEndRowStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerEndRowStateChanged
    {//GEN-HEADEREND:event_jSpinnerEndRowStateChanged
        recalculateShades();
    }//GEN-LAST:event_jSpinnerEndRowStateChanged

    private void jCheckBoxSquareLawActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxSquareLawActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxSquareLawActionPerformed
        recalculateShades();
    }//GEN-LAST:event_jCheckBoxSquareLawActionPerformed

    private void jSpinnerNumberOfShadesStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerNumberOfShadesStateChanged
    {//GEN-HEADEREND:event_jSpinnerNumberOfShadesStateChanged
        recalculateShades();
    }//GEN-LAST:event_jSpinnerNumberOfShadesStateChanged

    private void jSpinnerStartShownPaletteStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerStartShownPaletteStateChanged
    {//GEN-HEADEREND:event_jSpinnerStartShownPaletteStateChanged
        recalculateShades();
    }//GEN-LAST:event_jSpinnerStartShownPaletteStateChanged

    private void jSpinnerStartWorkingPaletteStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerStartWorkingPaletteStateChanged
    {//GEN-HEADEREND:event_jSpinnerStartWorkingPaletteStateChanged
        recalculateShades();
    }//GEN-LAST:event_jSpinnerStartWorkingPaletteStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDiscard;
    private javax.swing.JButton jButtonGetDataFromTable;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JCheckBox jCheckBoxEnable0;
    private javax.swing.JCheckBox jCheckBoxEnable1;
    private javax.swing.JCheckBox jCheckBoxEnable10;
    private javax.swing.JCheckBox jCheckBoxEnable11;
    private javax.swing.JCheckBox jCheckBoxEnable12;
    private javax.swing.JCheckBox jCheckBoxEnable13;
    private javax.swing.JCheckBox jCheckBoxEnable14;
    private javax.swing.JCheckBox jCheckBoxEnable15;
    private javax.swing.JCheckBox jCheckBoxEnable2;
    private javax.swing.JCheckBox jCheckBoxEnable3;
    private javax.swing.JCheckBox jCheckBoxEnable4;
    private javax.swing.JCheckBox jCheckBoxEnable5;
    private javax.swing.JCheckBox jCheckBoxEnable6;
    private javax.swing.JCheckBox jCheckBoxEnable7;
    private javax.swing.JCheckBox jCheckBoxEnable8;
    private javax.swing.JCheckBox jCheckBoxEnable9;
    private javax.swing.JCheckBox jCheckBoxSquareLaw;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelEndColor0;
    private javax.swing.JPanel jPanelEndColor1;
    private javax.swing.JPanel jPanelEndColor10;
    private javax.swing.JPanel jPanelEndColor11;
    private javax.swing.JPanel jPanelEndColor12;
    private javax.swing.JPanel jPanelEndColor13;
    private javax.swing.JPanel jPanelEndColor14;
    private javax.swing.JPanel jPanelEndColor15;
    private javax.swing.JPanel jPanelEndColor2;
    private javax.swing.JPanel jPanelEndColor3;
    private javax.swing.JPanel jPanelEndColor4;
    private javax.swing.JPanel jPanelEndColor5;
    private javax.swing.JPanel jPanelEndColor6;
    private javax.swing.JPanel jPanelEndColor7;
    private javax.swing.JPanel jPanelEndColor8;
    private javax.swing.JPanel jPanelEndColor9;
    private javax.swing.JPanel jPanelPreview;
    private javax.swing.JPanel jPanelStartColor0;
    private javax.swing.JPanel jPanelStartColor1;
    private javax.swing.JPanel jPanelStartColor10;
    private javax.swing.JPanel jPanelStartColor11;
    private javax.swing.JPanel jPanelStartColor12;
    private javax.swing.JPanel jPanelStartColor13;
    private javax.swing.JPanel jPanelStartColor14;
    private javax.swing.JPanel jPanelStartColor15;
    private javax.swing.JPanel jPanelStartColor2;
    private javax.swing.JPanel jPanelStartColor3;
    private javax.swing.JPanel jPanelStartColor4;
    private javax.swing.JPanel jPanelStartColor5;
    private javax.swing.JPanel jPanelStartColor6;
    private javax.swing.JPanel jPanelStartColor7;
    private javax.swing.JPanel jPanelStartColor8;
    private javax.swing.JPanel jPanelStartColor9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPanePreview;
    private javax.swing.JSpinner jSpinnerEndRow;
    private javax.swing.JSpinner jSpinnerNumberOfShades;
    private javax.swing.JSpinner jSpinnerStartRow;
    private javax.swing.JSpinner jSpinnerStartShownPalette;
    private javax.swing.JSpinner jSpinnerStartWorkingPalette;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
