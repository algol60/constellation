/*
 * Copyright 2010-2021 Australian Signals Directorate
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

/**
 * JPanel that allows the user to input <code>Boolean</code> content that they
 * wish to search.
 *
 * @author betelgeuse
 */
public class BooleanCriteriaPanel extends javax.swing.JPanel {

    private final FindCriteriaPanel parentPanel;

    /**
     * Creates new form BooleanCriteriaPanel
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     */
    public BooleanCriteriaPanel(final FindCriteriaPanel parent) {
        initComponents();

        this.parentPanel = parent;
    }

    /**
     * Creates a <code>BooleanCriteriaPanel</code> with predefined content.
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     * @param content The predefined state for this panel.
     */
    public BooleanCriteriaPanel(final FindCriteriaPanel parent, final boolean content) {
        this(parent);

        rdBoolTrue.setSelected(content);
        rdBoolFalse.setSelected(!content);
    }

    /**
     * Returns the current state of this panel.
     *
     * @return The currently selected <code>Boolean</code> value.
     */
    public boolean getBooleanContent() {
        return rdBoolTrue.isSelected();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpBoolean = new javax.swing.ButtonGroup();
        rdBoolTrue = new javax.swing.JRadioButton();
        rdBoolFalse = new javax.swing.JRadioButton();

        setOpaque(false);

        grpBoolean.add(rdBoolTrue);
        rdBoolTrue.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rdBoolTrue, org.openide.util.NbBundle.getMessage(BooleanCriteriaPanel.class, "BooleanCriteriaPanel.rdBoolTrue.text")); // NOI18N
        rdBoolTrue.setName("rdBoolTrue"); // NOI18N
        rdBoolTrue.setOpaque(false);
        rdBoolTrue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdBoolTrueActionPerformed(evt);
            }
        });

        grpBoolean.add(rdBoolFalse);
        org.openide.awt.Mnemonics.setLocalizedText(rdBoolFalse, org.openide.util.NbBundle.getMessage(BooleanCriteriaPanel.class, "BooleanCriteriaPanel.rdBoolFalse.text")); // NOI18N
        rdBoolFalse.setName("rdBoolFalse"); // NOI18N
        rdBoolFalse.setOpaque(false);
        rdBoolFalse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdBoolFalseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdBoolTrue)
                .addGap(5, 5, 5)
                .addComponent(rdBoolFalse)
                .addContainerGap(14, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdBoolTrue)
                    .addComponent(rdBoolFalse))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event handler for <code>rdBoolTrue</code> button presses.
     *
     * @param evt The registered event.
     */
    private void rdBoolTrueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdBoolTrueActionPerformed

        parentPanel.saveStateToGraph();
    }//GEN-LAST:event_rdBoolTrueActionPerformed

    /**
     * Event handler for <code>rdBoolFalse</code> button presses.
     *
     * @param evt The registered event.
     */
    private void rdBoolFalseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdBoolFalseActionPerformed

        parentPanel.saveStateToGraph();
    }//GEN-LAST:event_rdBoolFalseActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup grpBoolean;
    private javax.swing.JRadioButton rdBoolFalse;
    private javax.swing.JRadioButton rdBoolTrue;
    // End of variables declaration//GEN-END:variables
}
