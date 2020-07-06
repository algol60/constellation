/*
 * Copyright 2010-2020 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.views.find.gui;

import java.awt.Color;

/**
 * GUI Class that is used to handle input of float based queries in conjunction
 * with the <code>FindTopComponent</code> / <code>FindCriteriaPanel</code>
 * classes.
 *
 * @author betelgeuse
 * @see FindTopComponent
 * @see FindCriteriaPanel
 */
public class IntegerCriteriaPanel extends javax.swing.JPanel {

    private final FindCriteriaPanel parentPanel;
    private int validInt1 = 0;
    private int validInt2 = 0;

    /**
     * Creates a new form <code>FloatCriteriaPanel</code> with no prefilled
     * content.
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     */
    public IntegerCriteriaPanel(final FindCriteriaPanel parent) {
        initComponents();

        this.parentPanel = parent;
    }

    /**
     * Constructs a new <code>IntCriteriaPanel</code> with prefilled content.
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     * @param intA The value to place in the form's first input box.
     * @param intB The value to place in the form's second input box.
     * @param isBetween Whether or not the second input box should be shown.
     */
    public IntegerCriteriaPanel(final FindCriteriaPanel parent, final int intA,
            final int intB, final boolean isBetween) {
        this(parent);

        txtInt1.setText(Integer.toString(intA));
        txtInt2.setText(Integer.toString(intB));

        setUIState(isBetween);

        this.validate();
        this.repaint();
    }

    /**
     * Returns the current state of this panel's first input box.
     *
     * @return The parsed value of the first input box.
     */
    public int getFirstInt() {
        if (validateInt(txtInt1.getText())) {
            return Integer.parseInt(txtInt1.getText());
        } else {
            return validInt1;
        }
    }

    /**
     * Returns the current state of this panel's second input box.
     *
     * @return The parsed value of the second input box.
     */
    public int getSecondInt() {
        if (validateInt(txtInt2.getText())) {
            return Integer.parseInt(txtInt2.getText());
        } else {
            return validInt2;
        }
    }

    /**
     * Method that adjusts the form to handle multiple float input.
     *
     * @param isBetween <code>true</code> if form should show the extended input
     * controls, <code>false</code> if it should not.
     */
    public void setUIState(final boolean isBetween) {
        lblAnd.setVisible(isBetween);
        txtInt2.setVisible(isBetween);
        lblFloat2.setVisible(isBetween);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtInt1 = new javax.swing.JFormattedTextField();
        lblFloat1 = new javax.swing.JLabel();
        txtInt2 = new javax.swing.JFormattedTextField();
        lblFloat2 = new javax.swing.JLabel();
        lblAnd = new javax.swing.JLabel();

        setOpaque(false);

        txtInt1.setText(org.openide.util.NbBundle.getMessage(IntegerCriteriaPanel.class, "IntegerCriteriaPanel.txtInt1.text")); // NOI18N
        txtInt1.setName("txtInt1"); // NOI18N
        txtInt1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtInt1FocusLost(evt);
            }
        });
        txtInt1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtInt1KeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblFloat1, org.openide.util.NbBundle.getMessage(IntegerCriteriaPanel.class, "IntegerCriteriaPanel.lblFloat1.text")); // NOI18N
        lblFloat1.setMaximumSize(new java.awt.Dimension(0, 100));

        txtInt2.setText(org.openide.util.NbBundle.getMessage(IntegerCriteriaPanel.class, "IntegerCriteriaPanel.txtInt2.text")); // NOI18N
        txtInt2.setName("txtInt2"); // NOI18N
        txtInt2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtInt2FocusLost(evt);
            }
        });
        txtInt2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtInt2KeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblFloat2, org.openide.util.NbBundle.getMessage(IntegerCriteriaPanel.class, "IntegerCriteriaPanel.lblFloat2.text")); // NOI18N
        lblFloat2.setMaximumSize(new java.awt.Dimension(0, 100));

        org.openide.awt.Mnemonics.setLocalizedText(lblAnd, org.openide.util.NbBundle.getMessage(IntegerCriteriaPanel.class, "IntegerCriteriaPanel.lblAnd.text")); // NOI18N
        lblAnd.setMaximumSize(new java.awt.Dimension(0, 20));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtInt1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFloat1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblAnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtInt2, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFloat2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblAnd, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtInt2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblFloat2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFloat1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtInt1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event handler for <code>txtFloat2</code> key releases.
     * <p>
     * This triggers float validation checks.
     *
     * @param evt The registered event.
     */
    private void txtInt2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtInt2KeyReleased

        if (!validateInt(txtInt2.getText())) {
            txtInt2.setForeground(Color.RED);
        } else {
            txtInt2.setForeground(Color.BLACK);
        }
    }//GEN-LAST:event_txtInt2KeyReleased

    /**
     * Event handler for <code>txtFloat1</code> key releases.
     * <p>
     * This triggers float validation checks.
     *
     * @param evt The registered event.
     */
    private void txtInt1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtInt1KeyReleased

        if (!validateInt(txtInt1.getText())) {
            txtInt1.setForeground(Color.RED);
        } else {
            txtInt1.setForeground(Color.BLACK);
        }
    }//GEN-LAST:event_txtInt1KeyReleased

    /**
     * Event handler for <code>txtFloat1</code> loss of focus.
     * <p>
     * This triggers float validation checks.
     *
     * @param evt The registered event.
     */
    private void txtInt1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtInt1FocusLost

        if (validateInt(txtInt1.getText())) {
            validInt1 = Integer.parseInt(txtInt1.getText());
        } else {
            txtInt1.setText(Integer.toString(validInt1));
            txtInt1.setForeground(Color.BLACK);
        }

        // Something may have changed, so save the state:
        parentPanel.saveStateToGraph();
    }//GEN-LAST:event_txtInt1FocusLost

    /**
     * Event handler for <code>txtFloat2</code> loss of focus.
     * <p>
     * This triggers float validation checks.
     *
     * @param evt The registered event.
     */
    private void txtInt2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtInt2FocusLost

        if (validateInt(txtInt2.getText())) {
            validInt2 = Integer.parseInt(txtInt2.getText());
        } else {
            txtInt2.setText(Integer.toString(validInt2));
            txtInt2.setForeground(Color.BLACK);
        }

        // Something may have changed, so save the state:
        parentPanel.saveStateToGraph();
    }//GEN-LAST:event_txtInt2FocusLost
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblAnd;
    private javax.swing.JLabel lblFloat1;
    private javax.swing.JLabel lblFloat2;
    private javax.swing.JFormattedTextField txtInt1;
    private javax.swing.JFormattedTextField txtInt2;
    // End of variables declaration//GEN-END:variables

    /**
     * Helper method that determines whether a given string can be parsed into a
     * float.
     *
     * @param potential String to test.
     * @return <code>true</code> if given string is able to be * parsed,
     * <code>false</code> if it is not.
     */
    private boolean validateInt(final String potential) {
        try {
            Integer.parseInt(potential);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
