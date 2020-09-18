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
*
*/
package uchipgamemapeditor;

import java.awt.Color;

public class ColorChooserDialog extends javax.swing.JDialog
{
    final String labelColorIndexText = "Choose color at  index: ";
    boolean confirmed = false;
    /**
     * Creates new form PaletteSelectorDialog
     */
    public ColorChooserDialog(java.awt.Frame parent, boolean modal, int initialRed, int initialGreen, int initialBlue, int colorNumber)
    {
        super(parent, modal);
        initComponents();
        jLabelIndex.setText(labelColorIndexText + colorNumber);
        jSpinnerRed.setValue(initialRed);
        jSpinnerGreen.setValue(initialGreen);
        jSpinnerBlue.setValue(initialBlue);
        jSliderRed.setValue(initialRed);
        jSliderGreen.setValue(initialGreen);
        jSliderBlue.setValue(initialBlue);
    }
    public int getRed()
    {
        return (Integer) jSpinnerRed.getValue();
    }
    public int getGreen()
    {
        return (Integer) jSpinnerGreen.getValue();
    }
    public int getBlue()
    {
        return (Integer) jSpinnerBlue.getValue();
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

        jSliderRed = new javax.swing.JSlider();
        jLabelIndex = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSliderGreen = new javax.swing.JSlider();
        jSliderBlue = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSpinnerRed = new javax.swing.JSpinner();
        jSpinnerGreen = new javax.swing.JSpinner();
        jSpinnerBlue = new javax.swing.JSpinner();
        jPanelColor = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Select color");
        setResizable(false);

        jSliderRed.setMajorTickSpacing(1);
        jSliderRed.setMaximum(7);
        jSliderRed.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSliderRedStateChanged(evt);
            }
        });

        jLabelIndex.setText("Choose color at  index:");

        jLabel2.setText("Red");

        jSliderGreen.setMajorTickSpacing(1);
        jSliderGreen.setMaximum(7);
        jSliderGreen.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSliderGreenStateChanged(evt);
            }
        });

        jSliderBlue.setMajorTickSpacing(1);
        jSliderBlue.setMaximum(3);
        jSliderBlue.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSliderBlueStateChanged(evt);
            }
        });

        jLabel3.setText("Green");

        jLabel4.setText("Blue");

        jSpinnerRed.setModel(new javax.swing.SpinnerNumberModel(0, 0, 7, 1));
        jSpinnerRed.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerRedStateChanged(evt);
            }
        });

        jSpinnerGreen.setModel(new javax.swing.SpinnerNumberModel(0, 0, 7, 1));
        jSpinnerGreen.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerGreenStateChanged(evt);
            }
        });

        jSpinnerBlue.setModel(new javax.swing.SpinnerNumberModel(0, 0, 3, 1));
        jSpinnerBlue.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                jSpinnerBlueStateChanged(evt);
            }
        });

        jPanelColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanelColorLayout = new javax.swing.GroupLayout(jPanelColor);
        jPanelColor.setLayout(jPanelColorLayout);
        jPanelColorLayout.setHorizontalGroup(
            jPanelColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 86, Short.MAX_VALUE)
        );
        jPanelColorLayout.setVerticalGroup(
            jPanelColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 108, Short.MAX_VALUE)
        );

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Ok");
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelIndex)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jSliderGreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jSpinnerGreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jSliderRed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jSpinnerRed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jSliderBlue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jSpinnerBlue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(jPanelColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(94, 94, 94)
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(22, 22, 22))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabelIndex)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSliderRed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jSpinnerRed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSliderGreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(jSpinnerGreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSliderBlue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jSpinnerBlue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanelColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        confirmed = false;
        setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
    {//GEN-HEADEREND:event_jButton2ActionPerformed
        confirmed = true;
        setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed
    void updateColor()
    {
        int r, g, b;
        r = jSliderRed.getValue();
        g = jSliderGreen.getValue();
        b = jSliderBlue.getValue();
        int c = USVCMapEditorUtilities.USVCRGBtoRGB24(r, g, b);
        jPanelColor.setBackground(new Color(c));
    }
    private void jSliderRedStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSliderRedStateChanged
    {//GEN-HEADEREND:event_jSliderRedStateChanged
        jSpinnerRed.setValue(jSliderRed.getValue());
        updateColor();
    }//GEN-LAST:event_jSliderRedStateChanged

    private void jSliderGreenStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSliderGreenStateChanged
    {//GEN-HEADEREND:event_jSliderGreenStateChanged
        jSpinnerGreen.setValue(jSliderGreen.getValue());
        updateColor();
    }//GEN-LAST:event_jSliderGreenStateChanged

    private void jSliderBlueStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSliderBlueStateChanged
    {//GEN-HEADEREND:event_jSliderBlueStateChanged
        jSpinnerBlue.setValue(jSliderBlue.getValue());
        updateColor();        
    }//GEN-LAST:event_jSliderBlueStateChanged

    private void jSpinnerRedStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerRedStateChanged
    {//GEN-HEADEREND:event_jSpinnerRedStateChanged
        jSliderRed.setValue((Integer) jSpinnerRed.getValue());
    }//GEN-LAST:event_jSpinnerRedStateChanged

    private void jSpinnerGreenStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerGreenStateChanged
    {//GEN-HEADEREND:event_jSpinnerGreenStateChanged
        jSliderGreen.setValue((Integer) jSpinnerGreen.getValue());
    }//GEN-LAST:event_jSpinnerGreenStateChanged

    private void jSpinnerBlueStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jSpinnerBlueStateChanged
    {//GEN-HEADEREND:event_jSpinnerBlueStateChanged
        jSliderBlue.setValue((Integer) jSpinnerBlue.getValue());
    }//GEN-LAST:event_jSpinnerBlueStateChanged

  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabelIndex;
    private javax.swing.JPanel jPanelColor;
    private javax.swing.JSlider jSliderBlue;
    private javax.swing.JSlider jSliderGreen;
    private javax.swing.JSlider jSliderRed;
    private javax.swing.JSpinner jSpinnerBlue;
    private javax.swing.JSpinner jSpinnerGreen;
    private javax.swing.JSpinner jSpinnerRed;
    // End of variables declaration//GEN-END:variables
}
