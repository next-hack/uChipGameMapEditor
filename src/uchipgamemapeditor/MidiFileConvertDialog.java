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

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author PETN
 */
public class MidiFileConvertDialog extends javax.swing.JDialog
{
    boolean confirmed = false;
    /**
     * Creates new form MidiFileConvertDialog
     */
    public MidiFileConvertDialog(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
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
        jButtonChooseMidiFile = new javax.swing.JButton();
        jTextFieldMidiFile = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jButtonChooseOutputFile = new javax.swing.JButton();
        jTextFieldCFile = new javax.swing.JTextField();
        jCheckBoxNoteOff0 = new javax.swing.JCheckBox();
        jCheckBoxNoteOff1 = new javax.swing.JCheckBox();
        jCheckBoxNoteOff2 = new javax.swing.JCheckBox();
        jCheckBoxNoteOff3 = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jCheckBoxSetLoop = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jSpinnerStartTicks = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jSpinnerEndTicks = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldVariableName = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Midi File Conversion Options");

        jLabel1.setText("Source Midi File:");

        jButtonChooseMidiFile.setText("Choose");
        jButtonChooseMidiFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonChooseMidiFileActionPerformed(evt);
            }
        });

        jTextFieldMidiFile.setText("music.mid");

        jLabel2.setText("Output C file:");

        jButtonChooseOutputFile.setText("Choose");
        jButtonChooseOutputFile.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButtonChooseOutputFileActionPerformed(evt);
            }
        });

        jTextFieldCFile.setText("music.mid");

        jCheckBoxNoteOff0.setText("Include note off Event for Channel 0");

        jCheckBoxNoteOff1.setText("Include note off Event for Channel 1");

        jCheckBoxNoteOff2.setText("Include note off Event for Channel 2");

        jCheckBoxNoteOff3.setText("Include note off Event for Channel 3");

        jLabel3.setText("Options:");

        jCheckBoxSetLoop.setText("Set loop start & end ticks");

        jLabel4.setText("start:");

        jSpinnerStartTicks.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));

        jLabel5.setText("end:");

        jSpinnerEndTicks.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));

        jLabel6.setText("Output variable name:");

        jTextFieldVariableName.setText("midMusic");

        jButton1.setText("OK");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Cancel");
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(jTextFieldMidiFile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonChooseMidiFile))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(30, 30, 30)
                                .addComponent(jTextFieldCFile, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButtonChooseOutputFile))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCheckBoxNoteOff0)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jCheckBoxNoteOff1))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jCheckBoxNoteOff2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jCheckBoxNoteOff3))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(18, 18, 18)
                                .addComponent(jTextFieldVariableName))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jCheckBoxSetLoop)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel4)
                                        .addGap(18, 18, 18)
                                        .addComponent(jSpinnerStartTicks, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel5)
                                        .addGap(18, 18, 18)
                                        .addComponent(jSpinnerEndTicks, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addGap(117, 117, 117)
                        .addComponent(jButton2)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jButtonChooseMidiFile)
                    .addComponent(jTextFieldMidiFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jButtonChooseOutputFile)
                    .addComponent(jTextFieldCFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxNoteOff0)
                    .addComponent(jCheckBoxNoteOff1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxNoteOff2)
                    .addComponent(jCheckBoxNoteOff3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxSetLoop)
                    .addComponent(jLabel4)
                    .addComponent(jSpinnerStartTicks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jSpinnerEndTicks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextFieldVariableName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonChooseMidiFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonChooseMidiFileActionPerformed
    {//GEN-HEADEREND:event_jButtonChooseMidiFileActionPerformed
        final JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File (jTextFieldMidiFile.getText()));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            jTextFieldMidiFile.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonChooseMidiFileActionPerformed

    private void jButtonChooseOutputFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonChooseOutputFileActionPerformed
    {//GEN-HEADEREND:event_jButtonChooseOutputFileActionPerformed
        final JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File (jTextFieldCFile.getText()));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            jTextFieldCFile.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonChooseOutputFileActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        if (getLoopEnd() < getLoopStart() && getSpecifyLoop())
        {
            USVCMapEditorUtilities.infoBox("Please set the loop end value greater or equal than the start value!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
            confirmed = true;
            setVisible(false);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton2ActionPerformed
    {//GEN-HEADEREND:event_jButton2ActionPerformed
        confirmed = false;
        setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed
    String getVariableName()
    {
        return jTextFieldVariableName.getText();
    }
    boolean [] getNoteOff()
    {
        return new boolean[] {jCheckBoxNoteOff0.isSelected(), jCheckBoxNoteOff1.isSelected(), jCheckBoxNoteOff2.isSelected() ,jCheckBoxNoteOff3.isSelected()};
    }
    int getLoopStart()
    {
        return (Integer) jSpinnerStartTicks.getValue();
    }
    int getLoopEnd()
    {
        return (Integer) jSpinnerEndTicks.getValue();
    }
    boolean getSpecifyLoop()
    {
        return jCheckBoxSetLoop.isSelected();
    }
    String getMidiFileName()
    {
        return jTextFieldMidiFile.getText();
    }
    String getCFileName()
    {
        return jTextFieldCFile.getText();
    }  
    //
    void setVariableName(String name)
    {
        jTextFieldVariableName.setText(name);
    }
    void setNoteOff(boolean [] no)
    {
        jCheckBoxNoteOff0.setSelected(no[0]);
        jCheckBoxNoteOff0.setSelected(no[1]);
        jCheckBoxNoteOff0.setSelected(no[2]);
        jCheckBoxNoteOff0.setSelected(no[3]);
    }
    void setLoopStart(int start)
    {
        jSpinnerStartTicks.setValue(start);
    }
    void setLoopEnd(int end)
    {
        jSpinnerEndTicks.setValue(end);
    }
    void setSpecifyLoop(boolean s)
    {
        jCheckBoxSetLoop.setSelected(s);
    }
    void setMidiFileName(String name)
    {
        jTextFieldMidiFile.setText(name);
    }
    void setCFileName(String name)
    {
        jTextFieldCFile.setText(name);
    }     
    void setOptions(MidiConvertOptions options)
    {
        setMidiFileName(options.midiFileName);
        setCFileName(options.cFileName);
        setSpecifyLoop(options.specifyLoop);
        setLoopStart(options.loopStart);
        setLoopEnd(options.loopEnd);
        setNoteOff(options.noteOff);
        setVariableName(options.variableName);
    }
    MidiConvertOptions getOptions()
    {
        MidiConvertOptions options = new MidiConvertOptions();
        options.midiFileName = getMidiFileName();
        options.cFileName = getCFileName();
        options.specifyLoop = getSpecifyLoop();
        options.loopStart = getLoopStart();
        options.loopEnd = getLoopEnd();
        options.noteOff = getNoteOff();
        options.variableName = getVariableName();
        return options;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButtonChooseMidiFile;
    private javax.swing.JButton jButtonChooseOutputFile;
    private javax.swing.JCheckBox jCheckBoxNoteOff0;
    private javax.swing.JCheckBox jCheckBoxNoteOff1;
    private javax.swing.JCheckBox jCheckBoxNoteOff2;
    private javax.swing.JCheckBox jCheckBoxNoteOff3;
    private javax.swing.JCheckBox jCheckBoxSetLoop;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JSpinner jSpinnerEndTicks;
    private javax.swing.JSpinner jSpinnerStartTicks;
    private javax.swing.JTextField jTextFieldCFile;
    private javax.swing.JTextField jTextFieldMidiFile;
    private javax.swing.JTextField jTextFieldVariableName;
    // End of variables declaration//GEN-END:variables
}