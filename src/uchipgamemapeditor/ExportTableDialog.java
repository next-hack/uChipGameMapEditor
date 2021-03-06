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

public class ExportTableDialog extends javax.swing.JDialog
{
    public static final int SINE = 0;
    public static final int COSINE = 1;
    public static final int SQRT = 2;
    public static final int ATAN = 3;
    
    boolean confirmed = false;
    /**
     * Creates new form ExportTable
     */
    public ExportTableDialog(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
    }
    public int getNumberOfEntries()
    {
        return (Integer)jSpinnerNumEntries.getModel().getValue();
    }
    public String getTableName()
    {
        return jTextFieldFunctionName.getText();
    }
    public int getFunctionType()
    {
        if (jRadioButtonFullSine.isSelected())
            return SINE;
        else if (jRadioButtonFullCosine.isSelected())
            return COSINE;
        else if (jRadioButtonSqrt.isSelected())
            return SQRT;
        else 
            return ATAN;
    }
    /**
     * 
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        buttonGroupFunction = new javax.swing.ButtonGroup();
        buttonGroupFormat = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jRadioButtonFullSine = new javax.swing.JRadioButton();
        jRadioButtonFullCosine = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jSpinnerNumEntries = new javax.swing.JSpinner();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldFunctionName = new javax.swing.JTextField();
        jRadioButtonSqrt = new javax.swing.JRadioButton();
        jRadioButtonAtan = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Export Math Table");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });

        jLabel1.setText("Function:");

        buttonGroupFunction.add(jRadioButtonFullSine);
        jRadioButtonFullSine.setSelected(true);
        jRadioButtonFullSine.setText("full sine");

        buttonGroupFunction.add(jRadioButtonFullCosine);
        jRadioButtonFullCosine.setText("full cosine");

        jLabel2.setText("Number of entries (recommended power of 2)");

        jSpinnerNumEntries.setModel(new javax.swing.SpinnerNumberModel(1024, 2, 65536, 1));

        jButton1.setText("Ok");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel4.setText("Variable Name:");

        jTextFieldFunctionName.setText("functionTable");

        buttonGroupFunction.add(jRadioButtonSqrt);
        jRadioButtonSqrt.setText("Sqrt");

        buttonGroupFunction.add(jRadioButtonAtan);
        jRadioButtonAtan.setText("ArcTan(x) (- PI/4, PI/4]");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldFunctionName))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSpinnerNumEntries, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jRadioButtonFullSine)
                                .addGap(18, 18, 18)
                                .addComponent(jRadioButtonFullCosine)
                                .addGap(18, 18, 18)
                                .addComponent(jRadioButtonSqrt)
                                .addGap(31, 31, 31)
                                .addComponent(jRadioButtonAtan)))
                        .addGap(0, 56, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonFullSine)
                    .addComponent(jRadioButtonFullCosine)
                    .addComponent(jRadioButtonSqrt)
                    .addComponent(jRadioButtonAtan))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jSpinnerNumEntries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextFieldFunctionName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
        confirmed = false;
    }//GEN-LAST:event_formWindowClosing

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        confirmed = true;
        setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupFormat;
    private javax.swing.ButtonGroup buttonGroupFunction;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JRadioButton jRadioButtonAtan;
    private javax.swing.JRadioButton jRadioButtonFullCosine;
    private javax.swing.JRadioButton jRadioButtonFullSine;
    private javax.swing.JRadioButton jRadioButtonSqrt;
    private javax.swing.JSpinner jSpinnerNumEntries;
    private javax.swing.JTextField jTextFieldFunctionName;
    // End of variables declaration//GEN-END:variables
}
