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
import com.google.gson.GsonBuilder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import static uchipgamemapeditor.USVCMapEditorUtilities.infoBox;
/**
 *
 * @author PETN
 */
public class SpriteSetBuilder extends javax.swing.JFrame
{
    static final int DEFAULT_ROW_HEIGHT = 34;
    String spriteImageFileName = "";
    String spriteSetFileName = "";
    TileMapPainter painter;
    JPanel jPanelSpriteSetArea;
    int currentSelectedPixelX1 = 0;
    int currentSelectedPixelX2 = 0;
    int currentSelectedPixelY1 = 0;
    int currentSelectedPixelY2 = 0;    
    /**
     * Creates new form SpriteSetBuilder
     */
    public SpriteSetBuilder()
    {
        initComponents();
        jTableSpriteSet.setRowHeight(DEFAULT_ROW_HEIGHT);
        jTableSpriteSet.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent event) 
            {
                updateSpriteSetSelection();
            }
        });
        painter = new TileMapPainter();
        painter.bufferedImage = new BufferedImage(1, 1,BufferedImage.TYPE_INT_ARGB);
        jPanelSpriteSetArea = new JPanel();
        jPanelSpriteSetArea.setLayout(new BorderLayout());
        painter.setPreferredSize( painter.bufferedImage.getWidth(),painter.bufferedImage.getHeight());
        jPanelSpriteSetArea.setPreferredSize(new Dimension( painter.bufferedImage.getWidth(),painter.bufferedImage.getHeight()));
        jPanelSpriteSetArea.add(painter);
        jScrollPaneSpriteImage.setViewportView(jPanelSpriteSetArea);
        painter.setTileSize(1,1);
        painter.repaint();
        //
        TilesMouseListener tml = new TilesMouseListener();
        jPanelSpriteSetArea.addMouseMotionListener(tml);
        jPanelSpriteSetArea.addMouseListener(tml);        
        //
        jTableSpriteSet.getColumnModel().getColumn(7).setCellRenderer(new IconTableCellRenderer());
        jTableSpriteSet.getTableHeader().setDefaultRenderer(new HeaderRenderer(jTableSpriteSet));
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);       
        for (int columnIndex = 0; columnIndex < jTableSpriteSet.getColumnCount() - 1; columnIndex++)
        {
            jTableSpriteSet.getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);
        }
        updateTableLimits(painter.bufferedImage.getWidth(), painter.bufferedImage.getHeight());
        updateRowHeights(jTableSpriteSet);
        setSpriteZoom();
    }
    void updateSpriteSetSelection()
    {
        int [] rowIndexes = jTableSpriteSet.getSelectedRows();
        int n = rowIndexes.length;
        if (n > 0)
        {
            int [][] selection = new int[n][4];
            for (int i = 0; i < n; i++)
            {
                selection[i][0] = (int) jTableSpriteSet.getValueAt(rowIndexes[i], 0);
                selection[i][1] = (int) jTableSpriteSet.getValueAt(rowIndexes[i], 1);
                selection[i][2] = (int) jTableSpriteSet.getValueAt(rowIndexes[i], 2);
                selection[i][3] = (int) jTableSpriteSet.getValueAt(rowIndexes[i], 3);                    
            }
            painter.multipleSelection = selection;
        }
        else
        {
            if (painter != null)    
                painter.multipleSelection = null;
        }
        if (painter != null)    
            painter.repaint();            
    }
    void setSpriteZoom()
    {
        double zoom = jSliderZoomSpriteImage.getValue() / 10.0;
        int imageW = painter.bufferedImage.getWidth();
        int imageH = painter.bufferedImage.getHeight();
        // the jpanel cannot be smaller than the jScrollPane width or height
        int minW = jScrollPaneSpriteImage.getWidth();
        int minH = jScrollPaneSpriteImage.getHeight();
        //
        double minZoomX = ((double)minW)/imageW;
        double minZoomY = ((double)minH)/imageH;
        zoom = Double.max(zoom,Double.max(minZoomX,minZoomY));
        int w,h;
        w = (int)(imageW * zoom + 0.5);
        h = (int) (imageH * zoom + 0.5);
        jPanelSpriteSetArea.setPreferredSize(new Dimension(w,h));
        painter.setPreferredSize(w,h);
        jScrollPaneSpriteImage.setViewportView(jPanelSpriteSetArea);
        painter.repaint();        
    }
     
    private void updateRowHeights(JTable table)
    {
        for (int row = 0; row < table.getRowCount(); row++)
        {
            int rowHeight = table.getRowHeight();

            for (int column = 0; column < table.getColumnCount(); column++)
            {
                Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }

            table.setRowHeight(row, rowHeight);
        }
    }    
    class HeaderRenderer implements TableCellRenderer
    {

        DefaultTableCellRenderer renderer;
        public HeaderRenderer(JTable table)
        {
            renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
            renderer.setHorizontalAlignment(JLabel.CENTER);
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col)
        {
            return renderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);
        }
    }     
    class IconTableCellRenderer extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column)
        {
            JLabel label = (JLabel)super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column
            );
            if (value instanceof Icon)
            {
                label.setText(null);
                label.setIcon((Icon)value);
            }
            label.setHorizontalAlignment(SwingConstants.CENTER);
            return label;
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

        jSliderZoomSpriteImage = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButtonChangeSpriteImage = new javax.swing.JButton();
        jButtonInsert = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonLoad = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jToggleButtonGetFromImage = new javax.swing.JToggleButton();
        jButtonAddFrames = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPaneSpriteImage = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableSpriteSet = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Sprite Set Editor");

        jSliderZoomSpriteImage.setMinimum(1);
        jSliderZoomSpriteImage.setValue(10);
        jSliderZoomSpriteImage.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSliderZoomSpriteImageStateChanged(evt);
            }
        });

        jLabel1.setText("Zoom:");

        jLabel2.setText("Sprite Set Image");

        jButtonChangeSpriteImage.setText("Change Sprite Set Image");
        jButtonChangeSpriteImage.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonChangeSpriteImageActionPerformed(evt);
            }
        });

        jButtonInsert.setText("Add");
        jButtonInsert.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonInsertActionPerformed(evt);
            }
        });

        jButton1.setText("Remove");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Move Up");
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Move Down");
        jButton3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton3ActionPerformed(evt);
            }
        });

        jButtonSave.setText("Save Set");
        jButtonSave.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonSaveActionPerformed(evt);
            }
        });

        jButtonLoad.setText("Load Set");
        jButtonLoad.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonLoadActionPerformed(evt);
            }
        });

        jLabel3.setText("Sprite set");

        jToggleButtonGetFromImage.setText("Get From Image");

        jButtonAddFrames.setText("Add Frames");
        jButtonAddFrames.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonAddFramesActionPerformed(evt);
            }
        });

        jScrollPaneSpriteImage.setMinimumSize(new java.awt.Dimension(2, 2));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneSpriteImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneSpriteImage, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTableSpriteSet.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "x", "y", "w", "h", "Entity Name", "Anim Name", "Anim Frame", "Preview"
            }
        )
        {
            Class[] types = new Class []
            {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean []
            {
                true, true, true, true, true, true, true, false
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
        jTableSpriteSet.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTableSpriteSet.getTableHeader().setReorderingAllowed(false);
        jTableSpriteSet.addPropertyChangeListener(new java.beans.PropertyChangeListener()
        {
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                jTableSpriteSetPropertyChange(evt);
            }
        });
        jScrollPane2.setViewportView(jTableSpriteSet);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButtonInsert)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                                .addComponent(jButtonAddFrames)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                                .addComponent(jButton1)
                                .addGap(18, 23, Short.MAX_VALUE)
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                                .addComponent(jButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 15, Short.MAX_VALUE)
                                .addComponent(jToggleButtonGetFromImage)
                                .addGap(24, 24, 24))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSliderZoomSpriteImage, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButtonChangeSpriteImage))
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonSave, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addComponent(jButtonLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSliderZoomSpriteImage, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jButtonInsert)
                        .addComponent(jButton1)
                        .addComponent(jButton2)
                        .addComponent(jButton3)
                        .addComponent(jToggleButtonGetFromImage)
                        .addComponent(jButtonAddFrames))
                    .addComponent(jButtonChangeSpriteImage))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonSave)
                    .addComponent(jButtonLoad))
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jSliderZoomSpriteImageStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSliderZoomSpriteImageStateChanged
    {//GEN-HEADEREND:event_jSliderZoomSpriteImageStateChanged
        setSpriteZoom();
    }//GEN-LAST:event_jSliderZoomSpriteImageStateChanged

    private void jButtonChangeSpriteImageActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonChangeSpriteImageActionPerformed
    {//GEN-HEADEREND:event_jButtonChangeSpriteImageActionPerformed
        
        CustomFileChooser fc = USVCMapEditorUtilities.createFileChooser("Open sprite set image", new File(spriteImageFileName), new FileNameExtensionFilter("PNG file", "png"));
        if (fc.showOpenDialog(this) == CustomFileChooser.APPROVE_OPTION)
        {
            spriteImageFileName = fc.getSelectedFile().getAbsolutePath();
            BufferedImage bi = USVCMapEditorUtilities.loadImage(spriteImageFileName);
            if (bi.getWidth() < painter.bufferedImage.getWidth() || bi.getHeight() < painter.bufferedImage.getHeight())
            {
                if (USVCMapEditorUtilities.questionYesNo("Notice! New sprite set image size is smaller! Ok to clip violating coordinates?", "WARNING") == JOptionPane.NO_OPTION)
                {
                    return;
                }
            }
            painter.bufferedImage = bi;
            setSpriteZoom(); 
            updateTable();
            updateTableLimits(painter.bufferedImage.getWidth(), painter.bufferedImage.getHeight());
        }
    }//GEN-LAST:event_jButtonChangeSpriteImageActionPerformed
    void updateTable()
    {
        int n = jTableSpriteSet.getRowCount();
        for (int i = 0; i < n ; i++)
        {
            int x = (int) jTableSpriteSet.getValueAt(i, 0);
            int y = (int) jTableSpriteSet.getValueAt(i, 1);
            int w = (int) jTableSpriteSet.getValueAt(i, 2);
            int h = (int) jTableSpriteSet.getValueAt(i, 3);
            if (painter != null && painter.bufferedImage != null)
            {
                if (x >= painter.bufferedImage.getWidth())
                {
                    x = painter.bufferedImage.getWidth() - 1;
                }
                if (y >= painter.bufferedImage.getHeight())
                {
                    y = painter.bufferedImage.getHeight() - 1;
                }
                w = Math.min(w, painter.bufferedImage.getWidth() - x);
                h = Math.min(h, painter.bufferedImage.getHeight() - y);
                setTableRowEntry(i, x, y, w, h);                
            }
        }
    }
    BufferedImage getSubImage (int x, int y, int w, int h)
    {
        return painter.bufferedImage.getSubimage(x, y, w, h);
    }
    private void jButtonInsertActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonInsertActionPerformed
    {//GEN-HEADEREND:event_jButtonInsertActionPerformed
        int [] rows = jTableSpriteSet.getSelectedRows();
        Object [] obj = new Object [] {0, 0, 1, 1, "UNNAMED", "UNNAMED", 1, new ImageIcon(getSubImage(0, 0, 1, 1))};
        if (rows.length > 0)
        {
            ((DefaultTableModel)(jTableSpriteSet.getModel())).insertRow(rows[0], obj );
        }        
        else
            ((DefaultTableModel)(jTableSpriteSet.getModel())).addRow(obj); 
        updateTable();
    }//GEN-LAST:event_jButtonInsertActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        int [] rows = jTableSpriteSet.getSelectedRows();
        if (rows.length > 0)
        {
            DefaultTableModel model = (DefaultTableModel)jTableSpriteSet.getModel();
            for(int i=0;i<rows.length;i++)
            {
              model.removeRow(rows[i] - i);
            }            
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
    {//GEN-HEADEREND:event_jButton2ActionPerformed
        USVCMapEditorUtilities.moveTableRowsBy(jTableSpriteSet, -1);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton3ActionPerformed
    {//GEN-HEADEREND:event_jButton3ActionPerformed
        USVCMapEditorUtilities.moveTableRowsBy(jTableSpriteSet, 1);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTableSpriteSetPropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_jTableSpriteSetPropertyChange
    {//GEN-HEADEREND:event_jTableSpriteSetPropertyChange
        updateTable();
        updateSpriteSetSelection();
    }//GEN-LAST:event_jTableSpriteSetPropertyChange

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonSaveActionPerformed
    {//GEN-HEADEREND:event_jButtonSaveActionPerformed
        CustomFileChooser fc = USVCMapEditorUtilities.createFileChooser("Save sprite set data", new File(spriteSetFileName), new FileNameExtensionFilter("json file", "json"));
        if (fc.showSaveDialog(this) == CustomFileChooser.APPROVE_OPTION)
        {
            spriteSetFileName = fc.getSelectedFile().getAbsolutePath();
            int numberOfFrames = jTableSpriteSet.getRowCount();
            ArrayList <SpriteFrame> frameList = new ArrayList<>();
            for (int i = 0; i < numberOfFrames; i++)
            {
                SpriteFrame frame = new SpriteFrame((int)jTableSpriteSet.getValueAt(i, 0), 
                                        (int)jTableSpriteSet.getValueAt(i, 1), 
                                        (int)jTableSpriteSet.getValueAt(i, 2), 
                                        (int)jTableSpriteSet.getValueAt(i, 3), 
                                        (String)jTableSpriteSet.getValueAt(i, 4), 
                                        (String)jTableSpriteSet.getValueAt(i, 5), 
                                        (int)jTableSpriteSet.getValueAt(i, 6), false);
                frameList.add(frame);
            }
            SpriteFrame [] frames = new SpriteFrame[frameList.size()];
            SpriteAtlas sa = new SpriteAtlas();
            sa.frames = frameList.toArray(frames);
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(spriteSetFileName), "utf-8")))
            {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String s = gson.toJson(sa);
                writer.write(s);
                writer.close();
            } 
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonLoadActionPerformed
    {//GEN-HEADEREND:event_jButtonLoadActionPerformed
        try
        {
            final CustomFileChooser loadSpriteSetFC = USVCMapEditorUtilities.createFileChooser("Open sprite set data", new File(spriteSetFileName), new FileNameExtensionFilter("JSON file", "json"));
            loadSpriteSetFC.setSelectedFile(new File (spriteSetFileName));
            if (loadSpriteSetFC.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                ((DefaultTableModel)jTableSpriteSet.getModel()).setRowCount(0);   // remove all entries.
                //
                spriteSetFileName = loadSpriteSetFC.getSelectedFile().getAbsolutePath();
                String gsonString = new String (Files.readAllBytes(Paths.get(spriteSetFileName)));
                Gson gson = new Gson();
                SpriteAtlas spriteAtlas =  gson.fromJson(gsonString ,SpriteAtlas.class); 
                // now, we have both the image, and the data about the sprite positions.
                // 
                for (int i = 0; i< spriteAtlas.frames.length; i++)
                {
                    // Note: all the frames are expected to be in order starting from 1!!!
                    int frameNumber = spriteAtlas.frames[i].getFrameNumber();
                    String animName = spriteAtlas.frames[i].getAnimationName();
                    if (animName.length() == 0)
                        animName = "defaultanim";                 
                    Object [] obj = new Object [] {spriteAtlas.frames[i].frame.x, spriteAtlas.frames[i].frame.y, spriteAtlas.frames[i].frame.w, spriteAtlas.frames[i].frame.h, 
                        spriteAtlas.frames[i].getSpriteName(), animName, frameNumber, new ImageIcon(getSubImage(0, 0, 1, 1))};  // the image will be adjusted later
                    ((DefaultTableModel)(jTableSpriteSet.getModel())).addRow(obj); 
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            infoBox("Cannot load sprite data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        updateTable();
    }//GEN-LAST:event_jButtonLoadActionPerformed

    private void jButtonAddFramesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonAddFramesActionPerformed
    {//GEN-HEADEREND:event_jButtonAddFramesActionPerformed
        int [] rows = jTableSpriteSet.getSelectedRows();
        int x = 0; 
        int y = 0;
        int w = 1; 
        int h = 1;
        int numberOfFrames = 1;
        String animName = "UNNAMED";
        String entityName = "UNNAMED";
        int r = 0;
        if (rows.length > 0)
        {
            r = rows[0];
        }        
        else 
            r = jTableSpriteSet.getRowCount() - 1; 
        if (r != -1)
        {
            x = 0;
            w = (int) jTableSpriteSet.getValueAt(r, 2);
            h = (int) jTableSpriteSet.getValueAt(r, 3);
            y = (int) jTableSpriteSet.getValueAt(r, 1) + h;
            entityName = (String)jTableSpriteSet.getValueAt(r, 4);
            animName = (String) jTableSpriteSet.getValueAt(r, 5);
            numberOfFrames = (int) jTableSpriteSet.getValueAt(r, 6);
        }
        AddFramesDialog afd = new AddFramesDialog(this, true, x, y, w, h, entityName, animName, numberOfFrames);
        afd.setVisible(true);
        if (afd.confirmed)
        {
            numberOfFrames = afd.getNumberOfFrames();
            x = afd.getStartX();
            y = afd.getStartY();
            w = afd.getFrameW();
            h = afd.getFrameH();
            entityName = afd.getEntityName();
            animName = afd.getEntityAnim();
            for (int i = 0; i < numberOfFrames; i++)
            {
                Object [] obj = new Object[]  {(x + i * w), y, w , h , entityName, animName, i + 1, null};
                ((DefaultTableModel)(jTableSpriteSet.getModel())).insertRow(i + r + 1, obj);   
            }
        }
        updateTable();

    }//GEN-LAST:event_jButtonAddFramesActionPerformed
    void setTableRowEntry(int rowIndex, int x, int y, int w, int h)
    {
        jTableSpriteSet.setValueAt(x, rowIndex, 0);
        jTableSpriteSet.setValueAt(y, rowIndex, 1);
        jTableSpriteSet.setValueAt(w, rowIndex, 2);
        jTableSpriteSet.setValueAt(h, rowIndex, 3);
        // let's resize the sprite image, so that the largest dimension is 32.
        double largest = w > h ? w : h;
        jTableSpriteSet.setValueAt(new ImageIcon(getSubImage(x, y, w, h).getScaledInstance( Math.max(1,(int) (32 * w/largest)), Math.max(1, (int) (32 * h / largest)), Image.SCALE_DEFAULT)), rowIndex, 7);        
    }
    private class TilesMouseListener extends MouseAdapter
    {
        boolean isSelecting = false;
        public void mouseExited(MouseEvent e)
        {
            updatPainterSelection(); 
        }
        private void updatPainterSelection()
        {
            boolean changed = painter.selectedX1 != currentSelectedPixelX1 || painter.selectedX2 != currentSelectedPixelX2 || painter.selectedY1 != currentSelectedPixelY1 || painter.selectedY2 != currentSelectedPixelY2 || painter.showSelection != isSelecting;
            if (changed)
            {
                painter.selectedX1 = currentSelectedPixelX1;
                painter.selectedX2 = currentSelectedPixelX2;
                painter.selectedY1 = currentSelectedPixelY1;
                painter.selectedY2 = currentSelectedPixelY2;
                painter.showSelection = isSelecting;
                painter.repaint();
            }
        }
        public void mousePressed (MouseEvent e)
        {
            Point p =  e.getPoint();
            int mx = p.x;
            int my = p.y;
            // now get jPanelTileArea size
            Dimension d = jPanelSpriteSetArea.getSize();
            // get current zoom factor            
            double zoom =  d.width / ((double) painter.bufferedImage.getWidth());
            int xPixel = (int) (p.x / zoom); 
            int yPixel = (int) (p.y / zoom);
            if (xPixel < 0)
                xPixel = 0;
            if (yPixel < 0)
                yPixel = 0;
            if (xPixel >= painter.bufferedImage.getWidth())
                xPixel = painter.bufferedImage.getWidth() - 1;
            if (yPixel >= painter.bufferedImage.getHeight())
                yPixel = painter.bufferedImage.getHeight() - 1;
            isSelecting = true;
            // get mouse coordinates relative to jPanelTileArea
            currentSelectedPixelX1 = xPixel;
            currentSelectedPixelY1 = yPixel;
            currentSelectedPixelX2 = xPixel;
            currentSelectedPixelY2 = yPixel;
            // 
            //        
            updatPainterSelection();
        }    
        public void mouseReleased (MouseEvent e)
        {
            isSelecting = false;
            getSelection(e, true);
            // Now we have the selection, so we can update the selected row (if any and if the getFromImage toggle button is selected)
            int rowIndex = jTableSpriteSet.getSelectedRow();
            if (rowIndex != -1 && jToggleButtonGetFromImage.isSelected())
            {
                int x1, x2, y1, y2;
                x1 = currentSelectedPixelX1 > currentSelectedPixelX2 ? currentSelectedPixelX2 : currentSelectedPixelX1;
                x2 = currentSelectedPixelX1 < currentSelectedPixelX2 ? currentSelectedPixelX2 : currentSelectedPixelX1;
                y1 = currentSelectedPixelY1 > currentSelectedPixelY2 ? currentSelectedPixelY2 : currentSelectedPixelY1;
                y2 = currentSelectedPixelY1 < currentSelectedPixelY2 ? currentSelectedPixelY2 : currentSelectedPixelY1;
                // boundary check: already performed before.
                // calculate w and h
                int w = x2 - x1 + 1;
                int h = y2 - y1 + 1;
                setTableRowEntry(rowIndex, x1, y1, w, h);
                updateSpriteSetSelection();
            }
        }   
        void getSelection(MouseEvent e, boolean forceRedraw)
        {
            Point p =  e.getPoint();
            int mx = p.x;
            int my = p.y;
            // now get jPanelTileArea size
            Dimension d = jPanelSpriteSetArea.getSize();
            int dw = d.width;
            int dh = d.height;
            // get current zoom factor            
            double zoom =  d.width / ((double) painter.bufferedImage.getWidth());
            int xPixel = (int) (p.x / zoom); 
            int yPixel = (int) (p.y / zoom);
            if (xPixel < 0)
                xPixel = 0;
            if (yPixel < 0)
                yPixel = 0;
            if (xPixel >= painter.bufferedImage.getWidth())
                xPixel = painter.bufferedImage.getWidth() - 1;
            if (yPixel >= painter.bufferedImage.getHeight())
                yPixel = painter.bufferedImage.getHeight() - 1;
            // Now swap currentSelectedPixelX-Y 1 and 2, so that 1 is always <= 2
            boolean changed = currentSelectedPixelX2 != xPixel || currentSelectedPixelY2 != yPixel;
            if (changed || forceRedraw)
            {
                currentSelectedPixelX2 = xPixel;
                currentSelectedPixelY2 = yPixel;
                updatPainterSelection();   
            }            
        }
        public void mouseDragged(MouseEvent e)
        {
            if (isSelecting)
            {
                getSelection(e, false);
            }
            else
            {
                mousePressed(e);
            }
        }
    }
    void updateTableLimits(int w, int h)
    {
        jTableSpriteSet.getColumnModel().getColumn(0).setCellEditor(new CustomIntegerCellEditor(new JTextField(), 0, w - 1));
        jTableSpriteSet.getColumnModel().getColumn(1).setCellEditor(new CustomIntegerCellEditor(new JTextField(), 0, h - 1));
        jTableSpriteSet.getColumnModel().getColumn(2).setCellEditor(new CustomIntegerCellEditor(new JTextField(), 1, w));
        jTableSpriteSet.getColumnModel().getColumn(3).setCellEditor(new CustomIntegerCellEditor(new JTextField(), 1, h));
        jTableSpriteSet.getColumnModel().getColumn(6).setCellEditor(new CustomIntegerCellEditor(new JTextField(), 1, 65536));     
    }
    private class CustomIntegerCellEditor extends DefaultCellEditor 
    {
        int min = 0;
        int max = 0;
        private final Border red = new LineBorder(Color.red);
        private final Border black = new LineBorder(Color.black);
        private JTextField textField;
        public CustomIntegerCellEditor(JTextField textField, int min, int max) 
        {
            super(textField);
            this.textField = textField;
            this.textField.setHorizontalAlignment(JTextField.CENTER);
            this.min = min;
            this.max = max;
        }
        @Override
        public boolean stopCellEditing() 
        {
            try 
            {
                int v = Integer.valueOf(textField.getText());
                if (v < min || v > max) 
                {
                    throw new NumberFormatException();
                }
            } 
            catch (NumberFormatException e) 
            {
                textField.setBorder(red);
                return false;
            }
            return super.stopCellEditing();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
        {
            textField.setBorder(black);
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
        @Override
        public Object getCellEditorValue() 
        {
            return Integer.valueOf(textField.getText());
        }        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButtonAddFrames;
    private javax.swing.JButton jButtonChangeSpriteImage;
    private javax.swing.JButton jButtonInsert;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPaneSpriteImage;
    private javax.swing.JSlider jSliderZoomSpriteImage;
    private javax.swing.JTable jTableSpriteSet;
    private javax.swing.JToggleButton jToggleButtonGetFromImage;
    // End of variables declaration//GEN-END:variables
}
