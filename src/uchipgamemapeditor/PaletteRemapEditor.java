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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultFormatter;
import static uchipgamemapeditor.MapEditorMainFrame.uChipVGAred;
import static uchipgamemapeditor.MapEditorMainFrame.uChipVGAgreen;
import static uchipgamemapeditor.MapEditorMainFrame.uChipVGAblue;
import static uchipgamemapeditor.PaletteRemapEditor.NUMBER_OF_COLORS;
import static uchipgamemapeditor.USVCMapEditorUtilities.createFileChooser;
import static uchipgamemapeditor.USVCMapEditorUtilities.infoBox;
import static uchipgamemapeditor.USVCMapEditorUtilities.loadImage;

public class PaletteRemapEditor extends javax.swing.JFrame
{
    public static final int COLUMN_LINE = 0;
    public static final int COLUMN_CURRPAL = 1;
    public static final int COLUMN_CHGPAL = 2;
    public static final int COLUMN_CHGIDX = 3;
    public static final int COLUMN_RED = 4;
    public static final int COLUMN_GREEN = 5;
    public static final int COLUMN_BLUE = 6;
    public static final int COLUMN_COLOR = 7;
    public static final int MAX_NUMBER_OF_PALETTES = 16;
    public static final int NUMBER_OF_COLORS = 16;
    //
    public static final int HIGHRES_ROWS_PER_SCREEN = 400;
    private PreviewPainter  previewPainter; 
    public Palette [] initialPalettes;
    private JPanel [] jPanelInitialPaletteColorArray;
    private JPanel [] jPanelPaletteColorAtRowArray;
    private File selected4bppImageFile = null;
    private File dataJsonFile = null;
    private File exportFile = null;
    /**
     * Creates new form PaletteRemapEditor
     */
    class PreviewPainter extends JComponent 
    {

        public PreviewPainter() 
        {
           
        }
        public void paint(Graphics gr) 
        {
            Graphics2D g2d = (Graphics2D) gr;
            int rows = jTableColorList.getRowCount();
            boolean lowRes = true; //now  always enabled for better visibility. 
            int [] colors = new int[lowRes ? rows * 2 : rows];
            decodeTableForColorIndex((Integer) jSpinnerColorIndex.getValue(), colors, lowRes);
            for (int i = 0; i < colors.length; i++)
            {
                Color c = new Color(colors[i]);
                g2d.setPaint(c);
                g2d.drawLine(0, i, jPanelPreview.getWidth(), i);
            }   
            g2d.dispose();
        }  
    }
    void decodeTableForColorIndex(int index, int [] colors, boolean lowRes)
    {   // given the color index we want to analyze, this function outputs, line per line, the color. 
        int [] palettes = new int [MAX_NUMBER_OF_PALETTES];
        int r, g, b;
        int rows = colors.length;
        for (int cycle = 0; cycle < 2; cycle ++)
        {
            int currentPaletteNumber = 0;
            for (int i = 0; i < rows; i++)
            {
                int tableRow = lowRes ? i / 2 : i;                    
                currentPaletteNumber = (Integer) jTableColorList.getValueAt(tableRow, COLUMN_CURRPAL);
                int paletteNumberToChange = (Integer) jTableColorList.getValueAt(tableRow, COLUMN_CHGPAL);
                int idx = (Integer) jTableColorList.getValueAt(tableRow, COLUMN_CHGIDX);
                if (idx == index && idx < NUMBER_OF_COLORS)
                {
                    if (!lowRes || (i & 1) == 0 )
                    {   
                        r = (Integer) jTableColorList.getValueAt(tableRow, COLUMN_RED);
                        g = (Integer) jTableColorList.getValueAt(tableRow, COLUMN_GREEN);
                        b =(Integer) jTableColorList.getValueAt(tableRow, COLUMN_BLUE);
                        palettes[paletteNumberToChange & (MAX_NUMBER_OF_PALETTES - 1)] =  USVCMapEditorUtilities.USVCRGBtoRGB24(r, g, b);
                    }
                }
                colors[i] = palettes[currentPaletteNumber & (MAX_NUMBER_OF_PALETTES - 1)];
            }
            // we need to perform this twice, because, if the palette is not defined at each start of frame, then after the first frame, its value might be determined by the last scanline
            //
            if (cycle ==0 )
            {
                for (int i = 0; i < MAX_NUMBER_OF_PALETTES; i++)
                {
                    if (initialPalettes[i].definedAtStartOfFrame)
                    {
                        r = initialPalettes[i].colors[index].red;
                        g = initialPalettes[i].colors[index].green;
                        b = initialPalettes[i].colors[index].blue;
                        palettes[i] =  USVCMapEditorUtilities.USVCRGBtoRGB24(r, g, b);
                    }
                }
            }
        }        
    }
    public USVCColor[] calculateCurrentPaletteAtRow (int rowNumber)
    {
        int currentPaletteToCalculate = (Integer) jTableColorList.getValueAt(rowNumber, COLUMN_CURRPAL);
        int rows = jTableColorList.getRowCount();
        USVCColor [] colors = new USVCColor [NUMBER_OF_COLORS];
        for (int i = 0; i < NUMBER_OF_COLORS; i++)
            colors[i] = new USVCColor();
        for (int cycle = 0; cycle < 2; cycle ++)
        {
            for (int i = 0; i < rows; i++)
            {
                if (cycle != 0 && i == rowNumber)
                    break;
                int paletteNumberToChange = (Integer) jTableColorList.getValueAt(i, COLUMN_CHGPAL);
                int idx = (Integer) jTableColorList.getValueAt(i, COLUMN_CHGIDX);
                if (idx < NUMBER_OF_COLORS && currentPaletteToCalculate == paletteNumberToChange)
                {                      
                        colors[idx].red = (Integer) jTableColorList.getValueAt(i, COLUMN_RED);
                        colors[idx].green = (Integer) jTableColorList.getValueAt(i, COLUMN_GREEN);
                        colors[idx].blue =(Integer) jTableColorList.getValueAt(i, COLUMN_BLUE);
                }
            }
            // we need to perform this twice, because, if the palette is not defined at each start of frame, then after the first frame, its value might be determined by the last scanline
            //
            if (cycle ==0 )
            {
                for (int i = 0; i < NUMBER_OF_COLORS; i++)
                {
                    if (initialPalettes[currentPaletteToCalculate].definedAtStartOfFrame)
                    {
                        colors[i] = initialPalettes[currentPaletteToCalculate].colors[i].getCopy();
                    }
                }
            }
        }        
        return colors;
    }
    void paintCurrentPalettePanels(int rowNumber)
    {
        USVCColor[] colors = calculateCurrentPaletteAtRow (rowNumber);
        // let's get which palette is displayed at the nth row.
        for (int i = 0; i < NUMBER_OF_COLORS; i++)
        {
            int color = USVCMapEditorUtilities.USVCRGBtoRGB24(colors[i].red, colors[i].green, colors[i].blue);
            jPanelPaletteColorAtRowArray[i].setBackground(new Color(color));
        }
    }
    public PaletteRemapEditor()
    {
        initComponents();
        previewPainter = new PreviewPainter();
        jPanelPreview.add(previewPainter); 
        // TODO: the array can be filled using a loop and getComponentByName. But this would break if chenges are made to the component name at design time, if the name is not updated in the code.
        jPanelInitialPaletteColorArray = new JPanel[NUMBER_OF_COLORS];
        jPanelInitialPaletteColorArray[0] = jPanelInitColor0;
        jPanelInitialPaletteColorArray[1] = jPanelInitColor1;
        jPanelInitialPaletteColorArray[2] = jPanelInitColor2;
        jPanelInitialPaletteColorArray[3] = jPanelInitColor3;
        jPanelInitialPaletteColorArray[4] = jPanelInitColor4;
        jPanelInitialPaletteColorArray[5] = jPanelInitColor5;
        jPanelInitialPaletteColorArray[6] = jPanelInitColor6;
        jPanelInitialPaletteColorArray[7] = jPanelInitColor7;
        jPanelInitialPaletteColorArray[8] = jPanelInitColor8;
        jPanelInitialPaletteColorArray[9] = jPanelInitColor9;
        jPanelInitialPaletteColorArray[10] = jPanelInitColor10;
        jPanelInitialPaletteColorArray[11] = jPanelInitColor11;
        jPanelInitialPaletteColorArray[12] = jPanelInitColor12;
        jPanelInitialPaletteColorArray[13] = jPanelInitColor13;
        jPanelInitialPaletteColorArray[14] = jPanelInitColor14;
        jPanelInitialPaletteColorArray[15] = jPanelInitColor15;
        //
        for (int i = 0; i < NUMBER_OF_COLORS; i++)
        {
            jPanelInitialPaletteColorArray[i].addMouseListener(new java.awt.event.MouseAdapter()
            {
                public void mouseClicked(java.awt.event.MouseEvent evt)
                {
                    JPanel jp = (JPanel) evt.getSource();
                    for (int i = 0; i < NUMBER_OF_COLORS; i++)
                    {
                        if (jp == jPanelInitialPaletteColorArray[i])
                        {
                            changeInitialColorOfCurrentSelectedPalette(i);
                            break;
                        }
                    }
                }
            });
        }
        jPanelPaletteColorAtRowArray = new JPanel[NUMBER_OF_COLORS];
        jPanelPaletteColorAtRowArray[0] = jPanelCurrentPaletteColor0;
        jPanelPaletteColorAtRowArray[1] = jPanelCurrentPaletteColor1;
        jPanelPaletteColorAtRowArray[2] = jPanelCurrentPaletteColor2;
        jPanelPaletteColorAtRowArray[3] = jPanelCurrentPaletteColor3;
        jPanelPaletteColorAtRowArray[4] = jPanelCurrentPaletteColor4;
        jPanelPaletteColorAtRowArray[5] = jPanelCurrentPaletteColor5;
        jPanelPaletteColorAtRowArray[6] = jPanelCurrentPaletteColor6;
        jPanelPaletteColorAtRowArray[7] = jPanelCurrentPaletteColor7;
        jPanelPaletteColorAtRowArray[8] = jPanelCurrentPaletteColor8;
        jPanelPaletteColorAtRowArray[9] = jPanelCurrentPaletteColor9;
        jPanelPaletteColorAtRowArray[10] = jPanelCurrentPaletteColor10;
        jPanelPaletteColorAtRowArray[11] = jPanelCurrentPaletteColor11;
        jPanelPaletteColorAtRowArray[12] = jPanelCurrentPaletteColor12;
        jPanelPaletteColorAtRowArray[13] = jPanelCurrentPaletteColor13;
        jPanelPaletteColorAtRowArray[14] = jPanelCurrentPaletteColor14;
        jPanelPaletteColorAtRowArray[15] = jPanelCurrentPaletteColor15;        
        initialPalettes = new Palette [MAX_NUMBER_OF_PALETTES];
        for (int i = 0; i < initialPalettes.length; i++)
            initialPalettes[i] = new Palette();
        SpinnerNumberModel spinnerModel1 = new SpinnerNumberModel(0, 0, 15, 1);
        SpinnerNumberModel spinnerModelRG = new SpinnerNumberModel(0, 0, 7, 1);
        SpinnerNumberModel spinnerModelB = new SpinnerNumberModel(0, 0, 3, 1);
        // Current Palette
        TableColumn col = jTableColorList.getColumnModel().getColumn(COLUMN_CURRPAL);
        col.setCellEditor(new SpinnerEditor(spinnerModel1));
        // Palette to change
        col = jTableColorList.getColumnModel().getColumn(COLUMN_CHGPAL);
        col.setCellEditor(new SpinnerEditor(spinnerModel1));
        // Color Index to change
        col = jTableColorList.getColumnModel().getColumn(COLUMN_CHGIDX);
        col.setCellEditor(new SpinnerEditor(spinnerModel1));
        // Red
        col = jTableColorList.getColumnModel().getColumn(COLUMN_RED);
        col.setCellEditor(new SpinnerEditor(spinnerModelRG));
    //    col.getCellEditor().addCellEditorListener(cel);
        // Green
        col = jTableColorList.getColumnModel().getColumn(COLUMN_GREEN);
        col.setCellEditor(new SpinnerEditor(spinnerModelRG));
    //    col.getCellEditor().addCellEditorListener(cel);
        // Blue
        col = jTableColorList.getColumnModel().getColumn(COLUMN_BLUE);
        col.setCellEditor(new SpinnerEditor(spinnerModelB));
    //    col.getCellEditor().addCellEditorListener(cel);
        // color
        col = jTableColorList.getColumnModel().getColumn(COLUMN_COLOR);
        col.setCellRenderer(new ColorColumnCellRenderer());
        DefaultTableModel dtm = (DefaultTableModel) jTableColorList.getModel();
        // make commits on focus lost
        jTableColorList.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        // color         
        for (int i = 0; i < 400; i++)
        {
            dtm.addRow(new Object [] {i, 0, 1, 0, 0, 0, 0, 0});
        }
        for (int i = 0; i < NUMBER_OF_COLORS; i++)
           updateInitialColor(i, 0);        
        adjustAndUpdatePanelPreview();      
        paintCurrentPalettePanels((Integer)jSpinnerRowNumberToCheck.getValue());
        
    }
    void refreshTableColorsAndPreviewPanel()
    {
        DefaultTableModel dtm = (DefaultTableModel) jTableColorList.getModel();
        int rows = dtm.getRowCount();
        for (int i = 0; i < rows; i++)
        {
            int r, g, b;
            r = (Integer) dtm.getValueAt(i, COLUMN_RED);
            g = (Integer) dtm.getValueAt(i, COLUMN_GREEN);
            b =(Integer) dtm.getValueAt(i, COLUMN_BLUE);
            dtm.setValueAt(USVCMapEditorUtilities.USVCRGBtoRGB24(r, g, b), i, COLUMN_COLOR);
        }
        paintCurrentPalettePanels((Integer)jSpinnerRowNumberToCheck.getValue());  
    }
    void refreshSingleTableRowAndPrevewPanel(int row)
    {
        DefaultTableModel dtm = (DefaultTableModel) jTableColorList.getModel();
        int r, g, b;
        r = (Integer) dtm.getValueAt(row, COLUMN_RED);
        g = (Integer) dtm.getValueAt(row, COLUMN_GREEN);
        b =(Integer) dtm.getValueAt(row, COLUMN_BLUE);
        dtm.setValueAt(USVCMapEditorUtilities.USVCRGBtoRGB24(r, g, b), row, COLUMN_COLOR);
        previewPainter.repaint();
        paintCurrentPalettePanels((Integer)jSpinnerRowNumberToCheck.getValue());  
    }    
    class RGBChangeListener implements CellEditorListener
    {
        @Override
        public void editingStopped(ChangeEvent e) 
        {
            refreshTableColorsAndPreviewPanel();
        }        

        @Override
        public void editingCanceled(ChangeEvent e)
        {
            System.out.println("editing calcelled ");
        }
    }
    class ColorColumnCellRenderer extends DefaultTableCellRenderer 
    {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) 
        {
            //Cells are by default rendered as a JLabel.
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            int color;
            if (value == null)
            {
                color = 0xFFFFFF;
                value = color;
            }
            else
            {
                color = (Integer) value & 0xFFFFFF;
                value = color;
            }
            label.setText(String.format(Locale.ROOT,"0x%06X",color ));
            Color c = new Color(color);
            // get some contrast color
            int red = 255, green = 255, blue = 255;
            if (c.getBlue() > 127)
                blue = 0;
            if (c.getRed() > 127)
                red = 0;
            if (c.getGreen() > 127)
                green = 0;
            Color f = new Color(red, green, blue);
            label.setBackground(c);
            label.setForeground(f);
            //Return the JLabel which renders the cell.
            return label;
        } 
    }
    class SpinnerEditor extends AbstractCellEditor implements TableCellEditor
    {

        protected JSpinner spinner;

        public SpinnerEditor(SpinnerNumberModel snm)
        {
            spinner = new JSpinner();
            spinner.setModel(snm);
            JComponent comp = spinner.getEditor();
            JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
            DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
            formatter.setCommitsOnValidEdit(true);
            spinner.addChangeListener(new ChangeListener() 
            {
                @Override
                public void stateChanged(ChangeEvent e) 
                {
                     try
                    {
                        spinner.commitEdit();
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    int r = jTableColorList.getEditingRow();
                    int c = jTableColorList.getEditingColumn();
                    System.out.println("value changed: " + spinner.getValue()+ " Column and row:" + c  +" "+r);                    
                    if (r != -1 && c != -1)
                    {
                        jTableColorList.setValueAt(spinner.getValue(), r, c);
                        refreshSingleTableRowAndPrevewPanel(r);
                    }
                    /*
                    java.awt.EventQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            int r = jTableColorList.getSelectedRow();
                            int c = jTableColorList.getSelectedColumn();
                            System.out.println("value changed: " + spinner.getValue()+ " Column and row:" + c  +" "+r);                    
                            jTableColorList.setValueAt(spinner.getValue(), r, c);
                            refreshSingleTableRowAndPrevewPanel(r);
                        }
                    });      */              
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column)
        {
            if (value != null)
            {
                spinner.setValue(value);
            }
            return spinner;
        }

        public Object getCellEditorValue()
        {
            return  spinner.getValue();
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

        jLabel1 = new javax.swing.JLabel();
        jSpinnerColorIndex = new javax.swing.JSpinner();
        jPanelDefinePaletteAtStartOfFrame = new javax.swing.JPanel();
        jPanelInitColor0 = new javax.swing.JPanel();
        jPanelInitColor1 = new javax.swing.JPanel();
        jPanelInitColor2 = new javax.swing.JPanel();
        jPanelInitColor4 = new javax.swing.JPanel();
        jPanelInitColor3 = new javax.swing.JPanel();
        jPanelInitColor5 = new javax.swing.JPanel();
        jPanelInitColor6 = new javax.swing.JPanel();
        jPanelInitColor8 = new javax.swing.JPanel();
        jPanelInitColor9 = new javax.swing.JPanel();
        jPanelInitColor10 = new javax.swing.JPanel();
        jPanelInitColor11 = new javax.swing.JPanel();
        jPanelInitColor12 = new javax.swing.JPanel();
        jPanelInitColor13 = new javax.swing.JPanel();
        jPanelInitColor14 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jSpinnerPaletteNumberToSet = new javax.swing.JSpinner();
        jCheckBoxDefined = new javax.swing.JCheckBox();
        jPanelInitColor7 = new javax.swing.JPanel();
        jPanelInitColor15 = new javax.swing.JPanel();
        jButtonLoadFromImage = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor0 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor1 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor2 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor4 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor3 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor5 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor6 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor8 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor9 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor10 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor11 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor12 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor13 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor14 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSpinnerRowNumberToCheck = new javax.swing.JSpinner();
        jPanelCurrentPaletteColor7 = new javax.swing.JPanel();
        jPanelCurrentPaletteColor15 = new javax.swing.JPanel();
        jButtonLoad = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonExport = new javax.swing.JButton();
        jScrollPanePreview = new javax.swing.JScrollPane();
        jPanelPreview = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jCheckBoxPackColorChangeEntries = new javax.swing.JCheckBox();
        jButtonMoveRowUp = new javax.swing.JButton();
        jButtonMoveRowDown = new javax.swing.JButton();
        jButtonDeleteRow = new javax.swing.JButton();
        jButtonInsertRow = new javax.swing.JButton();
        jButtonSetRowNumber = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTableColorList = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButtonSelectColorForSelectedRows = new javax.swing.JButton();
        jButtonAlternatePalette = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Background Rainbow Editor");

        jLabel1.setText("Preview Color:");

        jSpinnerColorIndex.setModel(new javax.swing.SpinnerNumberModel(0, 0, 15, 1));
        jSpinnerColorIndex.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerColorIndexStateChanged(evt);
            }
        });

        jPanelDefinePaletteAtStartOfFrame.setBorder(javax.swing.BorderFactory.createTitledBorder("Initial Palette"));

        jPanelInitColor0.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor0.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor0.setMinimumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor0.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor0Layout = new javax.swing.GroupLayout(jPanelInitColor0);
        jPanelInitColor0.setLayout(jPanelInitColor0Layout);
        jPanelInitColor0Layout.setHorizontalGroup(
            jPanelInitColor0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor0Layout.setVerticalGroup(
            jPanelInitColor0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor1.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor1.setMinimumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor1.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor1Layout = new javax.swing.GroupLayout(jPanelInitColor1);
        jPanelInitColor1.setLayout(jPanelInitColor1Layout);
        jPanelInitColor1Layout.setHorizontalGroup(
            jPanelInitColor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor1Layout.setVerticalGroup(
            jPanelInitColor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor2.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor2.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor2Layout = new javax.swing.GroupLayout(jPanelInitColor2);
        jPanelInitColor2.setLayout(jPanelInitColor2Layout);
        jPanelInitColor2Layout.setHorizontalGroup(
            jPanelInitColor2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor2Layout.setVerticalGroup(
            jPanelInitColor2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor4.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor4.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor4Layout = new javax.swing.GroupLayout(jPanelInitColor4);
        jPanelInitColor4.setLayout(jPanelInitColor4Layout);
        jPanelInitColor4Layout.setHorizontalGroup(
            jPanelInitColor4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor4Layout.setVerticalGroup(
            jPanelInitColor4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor3.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor3.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor3Layout = new javax.swing.GroupLayout(jPanelInitColor3);
        jPanelInitColor3.setLayout(jPanelInitColor3Layout);
        jPanelInitColor3Layout.setHorizontalGroup(
            jPanelInitColor3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor3Layout.setVerticalGroup(
            jPanelInitColor3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanelInitColor5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor5.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor5.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor5Layout = new javax.swing.GroupLayout(jPanelInitColor5);
        jPanelInitColor5.setLayout(jPanelInitColor5Layout);
        jPanelInitColor5Layout.setHorizontalGroup(
            jPanelInitColor5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor5Layout.setVerticalGroup(
            jPanelInitColor5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor6.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor6.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor6Layout = new javax.swing.GroupLayout(jPanelInitColor6);
        jPanelInitColor6.setLayout(jPanelInitColor6Layout);
        jPanelInitColor6Layout.setHorizontalGroup(
            jPanelInitColor6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor6Layout.setVerticalGroup(
            jPanelInitColor6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor8.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor8.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor8.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor8Layout = new javax.swing.GroupLayout(jPanelInitColor8);
        jPanelInitColor8.setLayout(jPanelInitColor8Layout);
        jPanelInitColor8Layout.setHorizontalGroup(
            jPanelInitColor8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor8Layout.setVerticalGroup(
            jPanelInitColor8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor9.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor9.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor9Layout = new javax.swing.GroupLayout(jPanelInitColor9);
        jPanelInitColor9.setLayout(jPanelInitColor9Layout);
        jPanelInitColor9Layout.setHorizontalGroup(
            jPanelInitColor9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor9Layout.setVerticalGroup(
            jPanelInitColor9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor10.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor10.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor10Layout = new javax.swing.GroupLayout(jPanelInitColor10);
        jPanelInitColor10.setLayout(jPanelInitColor10Layout);
        jPanelInitColor10Layout.setHorizontalGroup(
            jPanelInitColor10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor10Layout.setVerticalGroup(
            jPanelInitColor10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor11.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor11.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor11.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor11Layout = new javax.swing.GroupLayout(jPanelInitColor11);
        jPanelInitColor11.setLayout(jPanelInitColor11Layout);
        jPanelInitColor11Layout.setHorizontalGroup(
            jPanelInitColor11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor11Layout.setVerticalGroup(
            jPanelInitColor11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor12.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor12.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor12.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor12Layout = new javax.swing.GroupLayout(jPanelInitColor12);
        jPanelInitColor12.setLayout(jPanelInitColor12Layout);
        jPanelInitColor12Layout.setHorizontalGroup(
            jPanelInitColor12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor12Layout.setVerticalGroup(
            jPanelInitColor12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor13.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor13.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor13.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor13Layout = new javax.swing.GroupLayout(jPanelInitColor13);
        jPanelInitColor13.setLayout(jPanelInitColor13Layout);
        jPanelInitColor13Layout.setHorizontalGroup(
            jPanelInitColor13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor13Layout.setVerticalGroup(
            jPanelInitColor13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor14.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor14.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor14.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor14Layout = new javax.swing.GroupLayout(jPanelInitColor14);
        jPanelInitColor14.setLayout(jPanelInitColor14Layout);
        jPanelInitColor14Layout.setHorizontalGroup(
            jPanelInitColor14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor14Layout.setVerticalGroup(
            jPanelInitColor14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jLabel2.setText("Palette n:");

        jSpinnerPaletteNumberToSet.setModel(new javax.swing.SpinnerNumberModel(0, 0, 15, 1));
        jSpinnerPaletteNumberToSet.setToolTipText("For each palette you need 1kB RAM, so use it wisely. Furthermore the engine only support continuous palettes, therefore if you use palette 0 and 2, you will be actually using 3kB!");
        jSpinnerPaletteNumberToSet.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerPaletteNumberToSetStateChanged(evt);
            }
        });

        jCheckBoxDefined.setText("defined at start of frame");
        jCheckBoxDefined.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxDefinedActionPerformed(evt);
            }
        });

        jPanelInitColor7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor7.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor7.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor7Layout = new javax.swing.GroupLayout(jPanelInitColor7);
        jPanelInitColor7.setLayout(jPanelInitColor7Layout);
        jPanelInitColor7Layout.setHorizontalGroup(
            jPanelInitColor7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor7Layout.setVerticalGroup(
            jPanelInitColor7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelInitColor15.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInitColor15.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelInitColor15.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelInitColor15Layout = new javax.swing.GroupLayout(jPanelInitColor15);
        jPanelInitColor15.setLayout(jPanelInitColor15Layout);
        jPanelInitColor15Layout.setHorizontalGroup(
            jPanelInitColor15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelInitColor15Layout.setVerticalGroup(
            jPanelInitColor15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jButtonLoadFromImage.setText("Load from Image");
        jButtonLoadFromImage.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonLoadFromImageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelDefinePaletteAtStartOfFrameLayout = new javax.swing.GroupLayout(jPanelDefinePaletteAtStartOfFrame);
        jPanelDefinePaletteAtStartOfFrame.setLayout(jPanelDefinePaletteAtStartOfFrameLayout);
        jPanelDefinePaletteAtStartOfFrameLayout.setHorizontalGroup(
            jPanelDefinePaletteAtStartOfFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSpinnerPaletteNumberToSet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jCheckBoxDefined, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createSequentialGroup()
                        .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createSequentialGroup()
                                .addComponent(jPanelInitColor8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createSequentialGroup()
                                .addComponent(jPanelInitColor0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanelInitColor7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(jButtonLoadFromImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanelDefinePaletteAtStartOfFrameLayout.setVerticalGroup(
            jPanelDefinePaletteAtStartOfFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createSequentialGroup()
                .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelInitColor6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelInitColor5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelInitColor4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jPanelInitColor1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jPanelInitColor2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jPanelInitColor0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jPanelInitColor3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jPanelInitColor7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelInitColor14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelInitColor13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelInitColor12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jPanelInitColor9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jPanelInitColor10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jPanelInitColor8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jPanelInitColor11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanelInitColor15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDefinePaletteAtStartOfFrameLayout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(jButtonLoadFromImage, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanelDefinePaletteAtStartOfFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jSpinnerPaletteNumberToSet, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBoxDefined))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Check current palette at row"));

        jPanelCurrentPaletteColor0.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor0.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor0.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor0Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor0);
        jPanelCurrentPaletteColor0.setLayout(jPanelCurrentPaletteColor0Layout);
        jPanelCurrentPaletteColor0Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor0Layout.setVerticalGroup(
            jPanelCurrentPaletteColor0Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor1.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor1.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor1Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor1);
        jPanelCurrentPaletteColor1.setLayout(jPanelCurrentPaletteColor1Layout);
        jPanelCurrentPaletteColor1Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor1Layout.setVerticalGroup(
            jPanelCurrentPaletteColor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor2.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor2.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor2Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor2);
        jPanelCurrentPaletteColor2.setLayout(jPanelCurrentPaletteColor2Layout);
        jPanelCurrentPaletteColor2Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor2Layout.setVerticalGroup(
            jPanelCurrentPaletteColor2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor4.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor4.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor4Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor4);
        jPanelCurrentPaletteColor4.setLayout(jPanelCurrentPaletteColor4Layout);
        jPanelCurrentPaletteColor4Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor4Layout.setVerticalGroup(
            jPanelCurrentPaletteColor4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor3.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor3.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor3Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor3);
        jPanelCurrentPaletteColor3.setLayout(jPanelCurrentPaletteColor3Layout);
        jPanelCurrentPaletteColor3Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor3Layout.setVerticalGroup(
            jPanelCurrentPaletteColor3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor5.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor5.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor5Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor5);
        jPanelCurrentPaletteColor5.setLayout(jPanelCurrentPaletteColor5Layout);
        jPanelCurrentPaletteColor5Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor5Layout.setVerticalGroup(
            jPanelCurrentPaletteColor5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor6.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor6.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor6Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor6);
        jPanelCurrentPaletteColor6.setLayout(jPanelCurrentPaletteColor6Layout);
        jPanelCurrentPaletteColor6Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor6Layout.setVerticalGroup(
            jPanelCurrentPaletteColor6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor8.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor8.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor8.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor8Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor8);
        jPanelCurrentPaletteColor8.setLayout(jPanelCurrentPaletteColor8Layout);
        jPanelCurrentPaletteColor8Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor8Layout.setVerticalGroup(
            jPanelCurrentPaletteColor8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor9.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor9.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor9Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor9);
        jPanelCurrentPaletteColor9.setLayout(jPanelCurrentPaletteColor9Layout);
        jPanelCurrentPaletteColor9Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor9Layout.setVerticalGroup(
            jPanelCurrentPaletteColor9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor10.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor10.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor10Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor10);
        jPanelCurrentPaletteColor10.setLayout(jPanelCurrentPaletteColor10Layout);
        jPanelCurrentPaletteColor10Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor10Layout.setVerticalGroup(
            jPanelCurrentPaletteColor10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor11.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor11.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor11.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor11Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor11);
        jPanelCurrentPaletteColor11.setLayout(jPanelCurrentPaletteColor11Layout);
        jPanelCurrentPaletteColor11Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor11Layout.setVerticalGroup(
            jPanelCurrentPaletteColor11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor12.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor12.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor12.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor12Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor12);
        jPanelCurrentPaletteColor12.setLayout(jPanelCurrentPaletteColor12Layout);
        jPanelCurrentPaletteColor12Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor12Layout.setVerticalGroup(
            jPanelCurrentPaletteColor12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor13.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor13.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor13.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor13Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor13);
        jPanelCurrentPaletteColor13.setLayout(jPanelCurrentPaletteColor13Layout);
        jPanelCurrentPaletteColor13Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor13Layout.setVerticalGroup(
            jPanelCurrentPaletteColor13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor14.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor14.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor14.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor14Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor14);
        jPanelCurrentPaletteColor14.setLayout(jPanelCurrentPaletteColor14Layout);
        jPanelCurrentPaletteColor14Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor14Layout.setVerticalGroup(
            jPanelCurrentPaletteColor14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jLabel3.setText("Row number:");

        jSpinnerRowNumberToCheck.setModel(new javax.swing.SpinnerNumberModel(0, 0, 199, 1));
        jSpinnerRowNumberToCheck.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerRowNumberToCheckStateChanged(evt);
            }
        });

        jPanelCurrentPaletteColor7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor7.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor7.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor7Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor7);
        jPanelCurrentPaletteColor7.setLayout(jPanelCurrentPaletteColor7Layout);
        jPanelCurrentPaletteColor7Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor7Layout.setVerticalGroup(
            jPanelCurrentPaletteColor7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanelCurrentPaletteColor15.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelCurrentPaletteColor15.setMaximumSize(new java.awt.Dimension(25, 25));
        jPanelCurrentPaletteColor15.setMinimumSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout jPanelCurrentPaletteColor15Layout = new javax.swing.GroupLayout(jPanelCurrentPaletteColor15);
        jPanelCurrentPaletteColor15.setLayout(jPanelCurrentPaletteColor15Layout);
        jPanelCurrentPaletteColor15Layout.setHorizontalGroup(
            jPanelCurrentPaletteColor15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );
        jPanelCurrentPaletteColor15Layout.setVerticalGroup(
            jPanelCurrentPaletteColor15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanelCurrentPaletteColor0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanelCurrentPaletteColor8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanelCurrentPaletteColor15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(jSpinnerRowNumberToCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelCurrentPaletteColor6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelCurrentPaletteColor5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelCurrentPaletteColor4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanelCurrentPaletteColor1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanelCurrentPaletteColor2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanelCurrentPaletteColor0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanelCurrentPaletteColor3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanelCurrentPaletteColor7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelCurrentPaletteColor14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelCurrentPaletteColor13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelCurrentPaletteColor12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanelCurrentPaletteColor9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanelCurrentPaletteColor10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanelCurrentPaletteColor8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanelCurrentPaletteColor11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanelCurrentPaletteColor15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSpinnerRowNumberToCheck, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jButtonLoad.setText("Load");
        jButtonLoad.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonLoadActionPerformed(evt);
            }
        });

        jButtonSave.setText("Save");
        jButtonSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonExport.setText("Export to C file");
        jButtonExport.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonExportActionPerformed(evt);
            }
        });

        jScrollPanePreview.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPanePreview.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jPanelPreview.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanelPreview.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanelPreview.setMaximumSize(new java.awt.Dimension(132, 400));
        jPanelPreview.setMinimumSize(new java.awt.Dimension(132, 400));
        jPanelPreview.setPreferredSize(new java.awt.Dimension(132, 400));
        jPanelPreview.setLayout(new java.awt.BorderLayout());
        jScrollPanePreview.setViewportView(jPanelPreview);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Color Table"));

        jCheckBoxPackColorChangeEntries.setText("Pack Entries");
        jCheckBoxPackColorChangeEntries.setToolTipText("If selected, 2 color change index entries will be packed to a byte.");

        jButtonMoveRowUp.setText("Move Row Up");
        jButtonMoveRowUp.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonMoveRowUpActionPerformed(evt);
            }
        });

        jButtonMoveRowDown.setText("Move Row Down");
        jButtonMoveRowDown.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonMoveRowDownActionPerformed(evt);
            }
        });

        jButtonDeleteRow.setText("Delete Row");
        jButtonDeleteRow.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonDeleteRowActionPerformed(evt);
            }
        });

        jButtonInsertRow.setText("Add/Insert Row");
        jButtonInsertRow.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonInsertRowActionPerformed(evt);
            }
        });

        jButtonSetRowNumber.setText("Set Row Number");
        jButtonSetRowNumber.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSetRowNumberActionPerformed(evt);
            }
        });

        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTableColorList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Line number", "Palette of current line", "Palette to change", "Color Index", "Red", "Green", "Blue", "Color"
            }
        )
        {
            Class[] types = new Class []
            {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean []
            {
                false, true, true, true, true, true, true, false
            };

            public Class getColumnClass(int columnIndex)
            {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        jTableColorList.setMaximumSize(new java.awt.Dimension(2147483647, 2147483647));
        jTableColorList.setMinimumSize(new java.awt.Dimension(120, 290));
        jTableColorList.setRequestFocusEnabled(false);
        jTableColorList.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(jTableColorList);
        if (jTableColorList.getColumnModel().getColumnCount() > 0)
        {
            jTableColorList.getColumnModel().getColumn(4).setPreferredWidth(30);
            jTableColorList.getColumnModel().getColumn(5).setPreferredWidth(30);
            jTableColorList.getColumnModel().getColumn(6).setPreferredWidth(30);
            jTableColorList.getColumnModel().getColumn(7).setPreferredWidth(30);
        }

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckBoxPackColorChangeEntries)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonMoveRowUp)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonMoveRowDown)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonDeleteRow)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonInsertRow)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonSetRowNumber)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jScrollPane3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxPackColorChangeEntries)
                    .addComponent(jButtonMoveRowUp)
                    .addComponent(jButtonMoveRowDown)
                    .addComponent(jButtonDeleteRow)
                    .addComponent(jButtonInsertRow)
                    .addComponent(jButtonSetRowNumber))
                .addContainerGap())
        );

        jButton1.setText("Create Shades");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        jButtonSelectColorForSelectedRows.setText("Set selected rows color");
        jButtonSelectColorForSelectedRows.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSelectColorForSelectedRowsActionPerformed(evt);
            }
        });

        jButtonAlternatePalette.setText("Alternate Palettes");
        jButtonAlternatePalette.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonAlternatePaletteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelDefinePaletteAtStartOfFrame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jButtonLoad)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonSave)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonExport)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonAlternatePalette)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonSelectColorForSelectedRows)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(36, 36, 36)
                        .addComponent(jSpinnerColorIndex, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPanePreview, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelDefinePaletteAtStartOfFrame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonLoad)
                    .addComponent(jButtonSave)
                    .addComponent(jButtonExport)
                    .addComponent(jButton1)
                    .addComponent(jButtonSelectColorForSelectedRows)
                    .addComponent(jButtonAlternatePalette))
                .addGap(18, 18, 18))
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jSpinnerColorIndex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPanePreview)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxDefinedActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxDefinedActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxDefinedActionPerformed
        int n = (Integer) jSpinnerPaletteNumberToSet.getValue();
        initialPalettes[n].definedAtStartOfFrame = jCheckBoxDefined.isSelected();
        jPanelPreview.repaint();
        paintCurrentPalettePanels((Integer)jSpinnerRowNumberToCheck.getValue());
    }//GEN-LAST:event_jCheckBoxDefinedActionPerformed

    private void jSpinnerPaletteNumberToSetStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerPaletteNumberToSetStateChanged
    {//GEN-HEADEREND:event_jSpinnerPaletteNumberToSetStateChanged
        int n = (Integer) jSpinnerPaletteNumberToSet.getValue();
        jCheckBoxDefined.setSelected(initialPalettes[n].definedAtStartOfFrame);
        for (int i = 0; i < NUMBER_OF_COLORS; i++)
            updateInitialColor(i, n);
    }//GEN-LAST:event_jSpinnerPaletteNumberToSetStateChanged

    private void jSpinnerColorIndexStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerColorIndexStateChanged
    {//GEN-HEADEREND:event_jSpinnerColorIndexStateChanged
        jPanelPreview.repaint();
    }//GEN-LAST:event_jSpinnerColorIndexStateChanged

    private void jButtonLoadFromImageActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonLoadFromImageActionPerformed
    {//GEN-HEADEREND:event_jButtonLoadFromImageActionPerformed
       JFileChooser fc = createFileChooser("Select a 4bpp image", selected4bppImageFile, new FileNameExtensionFilter("PNG file", "png"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            selected4bppImageFile = fc.getSelectedFile();
            BufferedImage image = loadImage(selected4bppImageFile.getAbsolutePath());
            if (image != null)
            {
                if (image.getColorModel().getPixelSize() != 4)
                {
                    infoBox("Tile image should be in 4-bpp format.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // get the palette
                int [] rgbs = new int[16];
                ((IndexColorModel) image.getColorModel()).getRGBs(rgbs);
                int n = (Integer) jSpinnerPaletteNumberToSet.getValue();
                for (int i = 0; i < rgbs.length; i++)
                {
                    int r = uChipVGAred[(rgbs[i] >> 16)  & 0xFF];
                    int g = uChipVGAgreen[(rgbs[i] >> 8 ) & 0xFF];
                    int b = uChipVGAblue[(rgbs[i] >> 0 ) & 0xFF];
                    // now that we have the uChipVGAred-uChipVGAgreen-b (3-3-2) components, we can create the uChip-palette
                    initialPalettes[n].colors[i].red = r;
                    initialPalettes[n].colors[i].green = g;
                    initialPalettes[n].colors[i].blue = b;
                    updateInitialColor(i, n);
                }                
                initialPalettes[n].definedAtStartOfFrame = true;
                jCheckBoxDefined.setSelected(true);
                jPanelPreview.repaint();
                paintCurrentPalettePanels((Integer)jSpinnerRowNumberToCheck.getValue());
            }   
            else
            {
                infoBox("Cannot load image", "Error", JOptionPane.ERROR_MESSAGE);                
            }
        }
    }//GEN-LAST:event_jButtonLoadFromImageActionPerformed

    private void jButtonDeleteRowActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonDeleteRowActionPerformed
    {//GEN-HEADEREND:event_jButtonDeleteRowActionPerformed
        int [] rows = jTableColorList.getSelectedRows();
        if (rows.length > 0)
        {
            DefaultTableModel model = (DefaultTableModel)jTableColorList.getModel();
            for(int i=0;i<rows.length;i++)
            {
              model.removeRow(rows[i] - i);
            }            
            renumberColorTableAndUpdateLimitsAndColors();
        }
    }//GEN-LAST:event_jButtonDeleteRowActionPerformed
    void adjustAndUpdatePanelPreview()
    {
        jPanelPreview.setPreferredSize( new Dimension(jPanelPreview.getSize().width, jTableColorList.getRowCount() * (/*jCheckBoxLowRes.isSelected() ? 2 : 1*/ 2)));
        jScrollPanePreview.invalidate();
        jScrollPanePreview.revalidate();
        jPanelPreview.invalidate();
        jPanelPreview.revalidate();
        jPanelPreview.repaint();
    }
    private void jButtonInsertRowActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonInsertRowActionPerformed
    {//GEN-HEADEREND:event_jButtonInsertRowActionPerformed
        int [] rows = jTableColorList.getSelectedRows();
        if (rows.length > 0)
        {
            ((DefaultTableModel)(jTableColorList.getModel())).insertRow(rows[0], new Object [] {0, 0, 0, 0, 0, 0, 0, 0});
        }        
        else
            ((DefaultTableModel)(jTableColorList.getModel())).addRow(new Object [] {0, 0, 0, 0, 0, 0, 0, 0});            
        renumberColorTableAndUpdateLimitsAndColors();        
    }//GEN-LAST:event_jButtonInsertRowActionPerformed
    
    private void jButtonMoveRowUpActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonMoveRowUpActionPerformed
    {//GEN-HEADEREND:event_jButtonMoveRowUpActionPerformed
        USVCMapEditorUtilities.moveTableRowsBy(jTableColorList, -1);
        renumberColorTableAndUpdateLimitsAndColors();  
    }//GEN-LAST:event_jButtonMoveRowUpActionPerformed

    private void jButtonMoveRowDownActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonMoveRowDownActionPerformed
    {//GEN-HEADEREND:event_jButtonMoveRowDownActionPerformed
        USVCMapEditorUtilities.moveTableRowsBy(jTableColorList, 1);
        renumberColorTableAndUpdateLimitsAndColors();          
    }//GEN-LAST:event_jButtonMoveRowDownActionPerformed

    private void jButtonSetRowNumberActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonSetRowNumberActionPerformed
    {//GEN-HEADEREND:event_jButtonSetRowNumberActionPerformed
        int rc = jTableColorList.getRowCount();
        IntegerSpinnerDialog isd = new IntegerSpinnerDialog(this, true, "Set new number of rows",1, 8192, 1, rc);
        isd.setVisible(true);
        if (isd.confirmed)
        {
            int newRc = isd.getValue();
            if (rc < newRc)
            {
                DefaultTableModel model = (DefaultTableModel)jTableColorList.getModel();
                for (int i = 0; i < newRc - rc; i++)
                {
                    model.addRow(new Object [] {0, 0, 0, 0, 0, 0, 0, 0});
                }        
                renumberColorTableAndUpdateLimitsAndColors();        
            }
            else if (rc > newRc)
            {
                if (USVCMapEditorUtilities.questionYesNo("You have selected a smaller row count (new value: " + newRc  +", old value " + rc + ")\r\nDo you want to permanently remove the last "+ (rc - newRc)+ " entries in your table?\r\n(Warning: this operation cannot be undone!)", "Confirm row removal") == JOptionPane.YES_OPTION)
                {
                    DefaultTableModel model = (DefaultTableModel)jTableColorList.getModel();
                    for (int i = 0; i < rc - newRc; i++)
                    {
                        model.removeRow(rc - 1 - i);
                    }        
                    renumberColorTableAndUpdateLimitsAndColors();                    
                }
            }
        }
        
    }//GEN-LAST:event_jButtonSetRowNumberActionPerformed
    class TableEntryData
    {
        int currentPaletteNumber = 0;
        int paletteNumberToChange = 0;
        int colorIndexToChange = 0;
        int red = 0;
        int green = 0;
        int blue = 0;
        TableEntryData()
        {
            
        }
        TableEntryData (int currentPaletteNumber, int paletteNumberToChange, int colorIndexToChange, int red, int green, int blue)
        {
            this.currentPaletteNumber = currentPaletteNumber;
            this.paletteNumberToChange = paletteNumberToChange;
            this.colorIndexToChange = colorIndexToChange; 
            this.red = red;
            this.green = green;
            this.blue = blue;   
        }
        TableEntryData (TableEntryData t)
        {
            this.currentPaletteNumber = t.currentPaletteNumber;
            this.paletteNumberToChange = t.paletteNumberToChange;
            this.colorIndexToChange = t.colorIndexToChange; 
            this.red = t.red;
            this.green = t.green;
            this.blue = t.blue;             
        }
    }
    class PaletteRemapData
    {
        Palette [] initialPalettes;
        TableEntryData[] data;
        //boolean lowRes = false;
        boolean packColorChangeIndexEntries = false;
        PaletteRemapData(int rowNum)
        {
            initialPalettes = new Palette[MAX_NUMBER_OF_PALETTES];
            for (int i = 0; i < NUMBER_OF_COLORS; i++)
            {
                initialPalettes[i] = new Palette();
            }
            data = new TableEntryData[rowNum];
            for (int i = 0; i < rowNum; i++)
            {
                data[i] = new TableEntryData();
            }
            
        }
        PaletteRemapData (PaletteRemapData p)
        {
            // get a copy
            initialPalettes = new Palette[MAX_NUMBER_OF_PALETTES];
            for (int i = 0; i < p.initialPalettes.length && i < MAX_NUMBER_OF_PALETTES; i++)
            {
                initialPalettes[i] = new Palette(p.initialPalettes[i]);
            }
            data = new TableEntryData[p.data.length];
            for (int i = 0; i < p.data.length; i++)
            {
                data[i] = new TableEntryData(p.data[i]);
            }
            //this.lowRes = p.lowRes;
            this.packColorChangeIndexEntries = p.packColorChangeIndexEntries;
        }
    }
    private void jSpinnerRowNumberToCheckStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerRowNumberToCheckStateChanged
    {//GEN-HEADEREND:event_jSpinnerRowNumberToCheckStateChanged
        paintCurrentPalettePanels((Integer)jSpinnerRowNumberToCheck.getValue());       
    }//GEN-LAST:event_jSpinnerRowNumberToCheckStateChanged

    private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonLoadActionPerformed
    {//GEN-HEADEREND:event_jButtonLoadActionPerformed
        JFileChooser fc = createFileChooser("Choose Json File To Load", dataJsonFile, new FileNameExtensionFilter ("Json file","json"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            dataJsonFile = fc.getSelectedFile();
            try
            {
                String gsonString = new String (Files.readAllBytes(Paths.get(dataJsonFile.getAbsolutePath())));
                Gson gson = new Gson();       
                PaletteRemapData p = new PaletteRemapData(gson.fromJson(gsonString, PaletteRemapData.class)); 
                // Now put everything on table
                int rows = p.data.length;
                DefaultTableModel dtm = (DefaultTableModel) jTableColorList.getModel();
                for (int i = 0; i < rows; i++)
                {
                    if (jTableColorList.getRowCount() == i)
                        dtm.addRow(new Object[] {i , p.data[i].currentPaletteNumber, p.data[i].paletteNumberToChange, p.data[i].colorIndexToChange, p.data[i].red, p.data[i].green, p.data[i].blue});
                    else
                    {
                        jTableColorList.setValueAt(p.data[i].currentPaletteNumber, i, COLUMN_CURRPAL);
                        jTableColorList.setValueAt(p.data[i].paletteNumberToChange, i, COLUMN_CHGPAL);
                        jTableColorList.setValueAt(p.data[i].colorIndexToChange, i, COLUMN_CHGIDX);
                        jTableColorList.setValueAt(p.data[i].red, i, COLUMN_RED);
                        jTableColorList.setValueAt(p.data[i].green, i, COLUMN_GREEN);
                        jTableColorList.setValueAt(p.data[i].blue, i, COLUMN_BLUE);            
                    }
                }                
                jCheckBoxPackColorChangeEntries.setSelected(p.packColorChangeIndexEntries);
                initialPalettes = p.initialPalettes;
                refreshTableColorsAndPreviewPanel();
                renumberColorTableAndUpdateLimitsAndColors();
                for (int i = 0; i < NUMBER_OF_COLORS; i++)
                {
                    updateInitialColor(i, (Integer) jSpinnerPaletteNumberToSet.getValue());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                infoBox("Cannot load file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButtonLoadActionPerformed

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonSaveActionPerformed
    {//GEN-HEADEREND:event_jButtonSaveActionPerformed
        CustomFileChooser fc = createFileChooser("Choose Json File To Save", dataJsonFile, new FileNameExtensionFilter ("Json file","json"));
        if (fc.showSaveDialog(this) == CustomFileChooser.APPROVE_OPTION)
        {
            dataJsonFile = fc.getSelectedFile();
            String fileName = dataJsonFile.getAbsolutePath();
            int rowNum = jTableColorList.getRowCount();
            PaletteRemapData p = new PaletteRemapData(rowNum);
            for (int i = 0; i < rowNum; i++)
            {
                p.data[i].currentPaletteNumber = (Integer) jTableColorList.getValueAt(i, COLUMN_CURRPAL);
                p.data[i].paletteNumberToChange = (Integer) jTableColorList.getValueAt(i, COLUMN_CHGPAL);
                p.data[i].colorIndexToChange = (Integer) jTableColorList.getValueAt(i, COLUMN_CHGIDX);
                p.data[i].red = (Integer) jTableColorList.getValueAt(i, COLUMN_RED);
                p.data[i].green = (Integer) jTableColorList.getValueAt(i, COLUMN_GREEN);
                p.data[i].blue = (Integer) jTableColorList.getValueAt(i, COLUMN_BLUE);            
            }
            for (int i = 0; i < MAX_NUMBER_OF_PALETTES; i++)
            {
                p.initialPalettes[i] = initialPalettes[i];
            }
            //p.lowRes = jCheckBoxLowRes.isSelected();
            p.packColorChangeIndexEntries = jCheckBoxPackColorChangeEntries.isSelected();
            // now get a gson and save
            String jsonString = new Gson().toJson(p);
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8")))
            {
                writer.write(jsonString);
            } 
            catch (Exception e)
            {
                infoBox("Cannot save file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }                
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonExportActionPerformed
    {//GEN-HEADEREND:event_jButtonExportActionPerformed
        final int MAX_ENTRIES_PER_LINE = 16;
        ExportPaletteRemapDataDialog ed = new ExportPaletteRemapDataDialog(this, true);
        ed.setVisible(true);
        if (ed.confirmed)
        {
            CustomFileChooser fc = createFileChooser("Select export file name", exportFile, new FileNameExtensionFilter ("C file","c") );
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                exportFile = fc.getSelectedFile();
                //
                String variableName = ed.getExportVariableName();
                StringBuilder sb = new StringBuilder();
                sb.append("// Put the following lines in a header file!\r\n");
                sb.append("#include <stdint.h>\r\n");
                //
                int rows = jTableColorList.getRowCount();
                // detect the last number of enabled palette
                int maxIndexOfUsedPalette;
                for (maxIndexOfUsedPalette = MAX_NUMBER_OF_PALETTES - 1; maxIndexOfUsedPalette >= 0; maxIndexOfUsedPalette--)
                {
                    if (initialPalettes[maxIndexOfUsedPalette].definedAtStartOfFrame)
                    {
                        break;
                    }
                }                
                int checkPointInterval = ed.getCheckpointInterval();
                sb.append("#define " + variableName.toUpperCase() + "NUMBER_OF_PALETTES " + (maxIndexOfUsedPalette + 1) +"\r\n");
                sb.append("#define " + variableName.toUpperCase() + "COLOR_REMAP_ROWS "+ rows +"\r\n");
                sb.append("#define " + variableName.toUpperCase() + "NUMBER_OF_REMAP_CHECKPOINTS " + ((rows - HIGHRES_ROWS_PER_SCREEN) / checkPointInterval + 1) +"\r\n");
                sb.append("extern const uint16_t " + variableName + "checkpointData["+ variableName.toUpperCase() + "NUMBER_OF_REMAP_CHECKPOINTS * 16 *" + variableName.toUpperCase() + "NUMBER_OF_PALETTES];\r\n");
                sb.append("extern const uint8_t " + variableName + "rowPaletteIndexes["+ variableName.toUpperCase() + "COLOR_REMAP_ROWS];\r\n");
                sb.append("extern const uint8_t " + variableName + "colorToChangeIndex[" + variableName.toUpperCase() + "COLOR_REMAP_ROWS" + ( jCheckBoxPackColorChangeEntries.isSelected() ? " / 2" : "" ) + "];\r\n");
                sb.append("extern const uint8_t " + variableName + "newColorTable["+variableName.toUpperCase()+"COLOR_REMAP_ROWS];\r\n");
                //
                sb.append("// Put the following lines in the C file! Remember to include the header too!\r\n");
                sb.append("#include <stdint.h>\r\n");
                // first we create the array containing the current palette and the palette to be changed. These two 4-bit indexes are packed to one single byte.
                sb.append("const uint8_t " + variableName + "rowPaletteIndexes["+ variableName.toUpperCase() + "COLOR_REMAP_ROWS] = \r\n{");
                for (int i = 0; i < rows; i++)
                {
                    int paletteIndexData = ((0x0F & (Integer) jTableColorList.getValueAt(i, COLUMN_CHGPAL)) << 4) | (0x0F & (Integer) jTableColorList.getValueAt(i, COLUMN_CURRPAL));
                    if ((i % MAX_ENTRIES_PER_LINE) == 0)
                            sb.append("\r\n\t");                        
                    if (i == rows - 1)
                    {
                        sb.append(String.format(Locale.ROOT,"0x%02X\r\n};\r\n", paletteIndexData));                         
                    }
                    else 
                    {
                        sb.append(String.format(Locale.ROOT,"0x%02X, ", paletteIndexData));                         
                    } 
                }
                // now let's get the color indexes
                sb.append("const uint8_t " + variableName + "colorToChangeIndex[" + variableName.toUpperCase() + "COLOR_REMAP_ROWS" + ( jCheckBoxPackColorChangeEntries.isSelected() ? " / 2" : "" ) + "] = \r\n{");
                int nEntries = 0;
                // WARNING: THIS MUST BE CHECKED!
                int increment = jCheckBoxPackColorChangeEntries.isSelected() ? 2 : 1;
                for (int i = 0; i < rows; i = i + increment)
                {
                    int colorIndexData;
                    if (!jCheckBoxPackColorChangeEntries.isSelected())
                    {
                        colorIndexData =  (0x0F & (Integer) jTableColorList.getValueAt(i, COLUMN_CHGIDX));                        
                    }
                    else
                    {
                        if (rows - i > 1)
                            colorIndexData = ((0x0F & (Integer) jTableColorList.getValueAt(i + 1, COLUMN_CHGIDX)) << 4) | (0x0F & (Integer) jTableColorList.getValueAt(i, COLUMN_CHGIDX));
                        else
                            colorIndexData = (0x0F & (Integer) jTableColorList.getValueAt(i , COLUMN_CHGIDX));                            
                    }
                    if ((nEntries % MAX_ENTRIES_PER_LINE) == 0)
                            sb.append("\r\n\t");                        
                    if (i + increment >= rows) // last element?
                    {
                        sb.append(String.format(Locale.ROOT,"0x%02X\r\n};\r\n", colorIndexData));                         
                    }
                    else 
                    {
                        sb.append(String.format(Locale.ROOT,"0x%02X, ", colorIndexData));                         
                    } 
                    nEntries++;
                }
                // then, let's compute the color byte values
                sb.append("const uint8_t " + variableName + "newColorTable["+variableName.toUpperCase()+"COLOR_REMAP_ROWS] = \r\n{");
                for (int i = 0; i < rows; i++)
                {
                    int red, green, blue;
                    red = (Integer) jTableColorList.getValueAt(i, COLUMN_RED); 
                    green = (Integer) jTableColorList.getValueAt(i, COLUMN_GREEN); 
                    blue = (Integer) jTableColorList.getValueAt(i, COLUMN_BLUE); 
                    int color = USVCMapEditorUtilities.USVCRGBto8bit(red, green, blue);;
                    if ((i % MAX_ENTRIES_PER_LINE) == 0)
                            sb.append("\r\n\t");                        
                    if (i == rows - 1)
                    {
                        sb.append(String.format(Locale.ROOT,"0x%02X\r\n};\r\n", color));                         
                    }
                    else 
                    {
                        sb.append(String.format(Locale.ROOT,"0x%02X, ", color));                         
                    } 
                }
                // here maxIndexOfUsedPalette is the highest index of palette.
                // finally, if enabled, (strongly recommended!) we save the checkpoints, i.e. the precomputed palette values at 16- 32 o 64 lines interval.
                Palette [] p = new Palette[MAX_NUMBER_OF_PALETTES]; // to avoid issues we declare 16 palettes, even if then we will save the correct number.
                for (int i = 0; i < MAX_NUMBER_OF_PALETTES; i++)
                {
                    p[i] = new Palette(initialPalettes[i]);
                }
                // Now that we have the initial palette values, we can interpret the table.
                sb.append("const uint16_t " + variableName + "checkpointData["+ variableName.toUpperCase() + "NUMBER_OF_REMAP_CHECKPOINTS * 16 * "+ variableName.toUpperCase()+"NUMBER_OF_PALETTES] = \r\n{");
                nEntries = 0;
                for (int i = 0; i < rows - HIGHRES_ROWS_PER_SCREEN; i++)
                {
                    int r = 0, g = 0, b = 0;
                    int paletteNumberToChange = (Integer) jTableColorList.getValueAt(i, COLUMN_CHGPAL);
                    int idx = (Integer) jTableColorList.getValueAt(i, COLUMN_CHGIDX);
                    if (idx < NUMBER_OF_COLORS)
                    {
                        r = (Integer) jTableColorList.getValueAt(i, COLUMN_RED);
                        g = (Integer) jTableColorList.getValueAt(i, COLUMN_GREEN);
                        b =(Integer) jTableColorList.getValueAt(i, COLUMN_BLUE);
                        p[paletteNumberToChange].colors[idx].red = r;
                        p[paletteNumberToChange].colors[idx].green = g;
                        p[paletteNumberToChange].colors[idx].blue = b;                        
                    }
                    if (i % checkPointInterval == 0)
                    {
                        // save all the palettes
                        for (int n = 0; n <= maxIndexOfUsedPalette; n++)
                        {
                            for (int c = 0; c < NUMBER_OF_COLORS; c++)
                            {
                                int color = USVCMapEditorUtilities.USVCpixelToSignals(USVCMapEditorUtilities.USVCRGBto8bit(p[n].colors[c].red, p[n].colors[c].green, p[n].colors[c].blue));
                                // write color signal
                                if (nEntries == 0)
                                {
                                    sb.append("\r\n\t" + String.format(Locale.ROOT,"0x%04X", color));       
                                }
                                else if (nEntries % MAX_ENTRIES_PER_LINE == 0)
                                {
                                    sb.append(",\r\n\t" + String.format(Locale.ROOT,"0x%04X", color));                                          
                                }
                                else
                                {
                                    sb.append(", " + String.format(Locale.ROOT,"0x%04X", color));                                                                              
                                }
                                nEntries++;
                            }
                        }
                    }
                }
                sb.append("\r\n};\r\n");
                String s = sb.toString();  
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(exportFile.getAbsolutePath()), "utf-8")))
                {
                    writer.write(s);
                } 
                catch (Exception e)
                {
                    e.printStackTrace();
                    infoBox("Cannot export color remap data!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_jButtonExportActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        ShadeCreatorDialog scd = new ShadeCreatorDialog(this, true, jTableColorList,jTableColorList.getSelectedRows(),this);
        scd.setVisible(true);
        jPanelPreview.repaint();
        jTableColorList.repaint();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButtonSelectColorForSelectedRowsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonSelectColorForSelectedRowsActionPerformed
    {//GEN-HEADEREND:event_jButtonSelectColorForSelectedRowsActionPerformed
        int [] rows = jTableColorList.getSelectedRows();
        if (rows.length > 0)
        {
            int idx =  (Integer) jTableColorList.getValueAt(rows[0], COLUMN_CHGIDX);
            int r = (Integer) jTableColorList.getValueAt(rows[0], COLUMN_RED);
            int g = (Integer) jTableColorList.getValueAt(rows[0], COLUMN_GREEN);
            int b = (Integer) jTableColorList.getValueAt(rows[0], COLUMN_BLUE);
            final ColorChooserDialog ccd = new ColorChooserDialog (this, true, r, g, b, idx);
            ccd.setVisible(true);
            if (ccd.confirmed)
            {
                for(int i=0; i<rows.length; i++)
                {
                    jTableColorList.setValueAt(ccd.getRed(), rows[i], COLUMN_RED);
                    jTableColorList.setValueAt(ccd.getGreen(), rows[i], COLUMN_GREEN);
                    jTableColorList.setValueAt(ccd.getBlue(), rows[i], COLUMN_BLUE);
                    jTableColorList.setValueAt(USVCMapEditorUtilities.USVCRGBtoRGB24(ccd.getRed(), ccd.getGreen(), ccd.getBlue()), rows[i], COLUMN_COLOR);
                }            
            }
        }        
        renumberColorTableAndUpdateLimitsAndColors();        
    }//GEN-LAST:event_jButtonSelectColorForSelectedRowsActionPerformed

    private void jButtonAlternatePaletteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAlternatePaletteActionPerformed
    {//GEN-HEADEREND:event_jButtonAlternatePaletteActionPerformed
        int [] rows = jTableColorList.getSelectedRows();
        if (rows.length > 0)
        {
            int currentPalette =  (Integer) jTableColorList.getValueAt(rows[0], COLUMN_CURRPAL);
            int paletteToChange = (Integer) jTableColorList.getValueAt(rows[0], COLUMN_CHGPAL);
            for(int i=0; i<rows.length; i++)
            {
                if ((i & 1) == 0)
                {
                    jTableColorList.setValueAt(currentPalette, rows[i],COLUMN_CURRPAL );
                    jTableColorList.setValueAt(paletteToChange, rows[i], COLUMN_CHGPAL);
                }
                else
                {
                    jTableColorList.setValueAt(paletteToChange, rows[i],COLUMN_CURRPAL );
                    jTableColorList.setValueAt(currentPalette , rows[i], COLUMN_CHGPAL);                        
                }
            }            
        }        
        renumberColorTableAndUpdateLimitsAndColors();      
    }//GEN-LAST:event_jButtonAlternatePaletteActionPerformed
    void renumberColorTableAndUpdateLimitsAndColors()
    {
        int rc = jTableColorList.getRowCount();
        for (int i = 0; i < rc; i++)
            jTableColorList.setValueAt(i, i, COLUMN_LINE);
        adjustAndUpdatePanelPreview();
        int row = (Integer)jSpinnerRowNumberToCheck.getValue();
        if (row >= rc)
            jSpinnerRowNumberToCheck.setValue(rc - 1);
        ((SpinnerNumberModel) jSpinnerRowNumberToCheck.getModel()).setMaximum(rc - 1);
        paintCurrentPalettePanels((Integer)jSpinnerRowNumberToCheck.getValue());        
    }
    void updateInitialColor(int colorIndex, int paletteNumber)
    {
        int r, g, b;
        r =   initialPalettes[paletteNumber].colors[colorIndex].red;
        g =   initialPalettes[paletteNumber].colors[colorIndex].green;
        b =   initialPalettes[paletteNumber].colors[colorIndex].blue;
        int c = USVCMapEditorUtilities.USVCRGBtoRGB24(r, g, b);
        jPanelInitialPaletteColorArray[colorIndex].setBackground(new Color(c));        
    }
    void changeInitialColorOfCurrentSelectedPalette(int colorIndex)
    {
        int paletteNumber = (Integer) jSpinnerPaletteNumberToSet.getValue();
        final ColorChooserDialog ccd = new ColorChooserDialog(this, true, 
                initialPalettes[paletteNumber].colors[colorIndex].red,
                initialPalettes[paletteNumber].colors[colorIndex].green, 
                initialPalettes[paletteNumber].colors[colorIndex].blue, colorIndex);
        ccd.setVisible(true);        
        if (ccd.confirmed)
        {
            int r,g,b;
            r = ccd.getRed();
            g = ccd.getGreen();
            b = ccd.getBlue();
            int c = USVCMapEditorUtilities.USVCRGBtoRGB24(r, g, b);
            initialPalettes[paletteNumber].colors[colorIndex].red = r;
            initialPalettes[paletteNumber].colors[colorIndex].green = g;
            initialPalettes[paletteNumber].colors[colorIndex].blue = b;
            jPanelInitialPaletteColorArray[colorIndex].setBackground(new Color(c));
        }
        jPanelPreview.repaint();
        paintCurrentPalettePanels((Integer)jSpinnerRowNumberToCheck.getValue());
    }
 
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonAlternatePalette;
    private javax.swing.JButton jButtonDeleteRow;
    private javax.swing.JButton jButtonExport;
    private javax.swing.JButton jButtonInsertRow;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonLoadFromImage;
    private javax.swing.JButton jButtonMoveRowDown;
    private javax.swing.JButton jButtonMoveRowUp;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonSelectColorForSelectedRows;
    private javax.swing.JButton jButtonSetRowNumber;
    private javax.swing.JCheckBox jCheckBoxDefined;
    private javax.swing.JCheckBox jCheckBoxPackColorChangeEntries;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelCurrentPaletteColor0;
    private javax.swing.JPanel jPanelCurrentPaletteColor1;
    private javax.swing.JPanel jPanelCurrentPaletteColor10;
    private javax.swing.JPanel jPanelCurrentPaletteColor11;
    private javax.swing.JPanel jPanelCurrentPaletteColor12;
    private javax.swing.JPanel jPanelCurrentPaletteColor13;
    private javax.swing.JPanel jPanelCurrentPaletteColor14;
    private javax.swing.JPanel jPanelCurrentPaletteColor15;
    private javax.swing.JPanel jPanelCurrentPaletteColor2;
    private javax.swing.JPanel jPanelCurrentPaletteColor3;
    private javax.swing.JPanel jPanelCurrentPaletteColor4;
    private javax.swing.JPanel jPanelCurrentPaletteColor5;
    private javax.swing.JPanel jPanelCurrentPaletteColor6;
    private javax.swing.JPanel jPanelCurrentPaletteColor7;
    private javax.swing.JPanel jPanelCurrentPaletteColor8;
    private javax.swing.JPanel jPanelCurrentPaletteColor9;
    private javax.swing.JPanel jPanelDefinePaletteAtStartOfFrame;
    private javax.swing.JPanel jPanelInitColor0;
    private javax.swing.JPanel jPanelInitColor1;
    private javax.swing.JPanel jPanelInitColor10;
    private javax.swing.JPanel jPanelInitColor11;
    private javax.swing.JPanel jPanelInitColor12;
    private javax.swing.JPanel jPanelInitColor13;
    private javax.swing.JPanel jPanelInitColor14;
    private javax.swing.JPanel jPanelInitColor15;
    private javax.swing.JPanel jPanelInitColor2;
    private javax.swing.JPanel jPanelInitColor3;
    private javax.swing.JPanel jPanelInitColor4;
    private javax.swing.JPanel jPanelInitColor5;
    private javax.swing.JPanel jPanelInitColor6;
    private javax.swing.JPanel jPanelInitColor7;
    private javax.swing.JPanel jPanelInitColor8;
    private javax.swing.JPanel jPanelInitColor9;
    private javax.swing.JPanel jPanelPreview;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPanePreview;
    private javax.swing.JSpinner jSpinnerColorIndex;
    private javax.swing.JSpinner jSpinnerPaletteNumberToSet;
    private javax.swing.JSpinner jSpinnerRowNumberToCheck;
    private javax.swing.JTable jTableColorList;
    // End of variables declaration//GEN-END:variables
}
