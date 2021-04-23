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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle.Messages;

/**
 * GUI Class that is used to handle input of string based queries in conjunction
 * with the <code>FindTopComponent</code> / <code>FindCriteriaPanel</code>
 * classes.
 *
 * @author betelgeuse
 * @see FindTopComponent
 * @see FindCriteriaPanel
 */
@Messages({
    "String_REGEX=(regular expression)"
})
public class StringCriteriaPanel extends javax.swing.JPanel implements ActionListener {

    private final FindCriteriaPanel parentPanel;
    private StringListPanel listPanel;

    /**
     * Creates a new StringCriteriaPanel with no prefilled content.
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     */
    public StringCriteriaPanel(final FindCriteriaPanel parent) {
        initComponents();

        this.parentPanel = parent;
    }

    /**
     * Constructs a new StringCriteriaPanel with predefined content.
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     * @param content The value to place in the form's input box.
     * @param isCaseSensitive The value of the form's checkbox component.
     * @param isUsingList The value of the corresponding checkbox component for
     * the form.
     * @param isRegex Whether or not the form should be adjusted to handle regex
     * input.
     */
    public StringCriteriaPanel(final FindCriteriaPanel parent, final String content,
            final boolean isCaseSensitive, final boolean isUsingList, final boolean isRegex) {
        this(parent);

        txtString.setText(content);
        chkCaseSensitive.setSelected(isCaseSensitive);
        chkList.setSelected(isUsingList);
        btnMore.setSelected(isUsingList);

        setUIState(isRegex);

        this.validate();
        this.repaint();
    }

    /**
     * Returns the current value from the form's input box.
     *
     * @return string content.
     */
    public String getStringContent() {
        return txtString.getText();
    }

    /**
     * Returns the current value from the form's checkbox.
     *
     * @return isCaseSensitive.
     */
    public boolean isCaseSensitive() {
        return chkCaseSensitive.isSelected();
    }

    /**
     * Returns the current value from the form's checkbox.
     *
     * @return <code>true</code> if 'use list' check box is currently selected,
     * <code>false</code> if not.
     */
    public boolean isUsingList() {
        return chkList.isSelected();
    }

    /**
     * Method that adjusts the form to handle regex input.
     *
     * @param isRegex <code>true</code> if form should be set up to handle regex
     * input, <code>false</code> if it should not.
     */
    public void setUIState(final boolean isRegex) {
        chkCaseSensitive.setVisible(!isRegex);
        chkList.setVisible(!isRegex);
        btnMore.setVisible(!isRegex);

        if (isRegex) {
            lblString.setText(Bundle.String_REGEX());
        } else {
            lblString.setText("");
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("OK")) {
            txtString.setText(listPanel.getContent());

            parentPanel.saveStateToGraph();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chkCaseSensitive = new javax.swing.JCheckBox();
        pnlHolder = new javax.swing.JPanel();
        pnlInput = new javax.swing.JPanel();
        btnMore = new javax.swing.JButton();
        txtString = new javax.swing.JFormattedTextField();
        lblString = new javax.swing.JLabel();
        chkList = new javax.swing.JCheckBox();

        setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(chkCaseSensitive, org.openide.util.NbBundle.getMessage(StringCriteriaPanel.class, "StringCriteriaPanel.chkCaseSensitive.text")); // NOI18N
        chkCaseSensitive.setName("chkCaseSensitive"); // NOI18N
        chkCaseSensitive.setOpaque(false);
        chkCaseSensitive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkCaseSensitiveActionPerformed(evt);
            }
        });

        pnlHolder.setOpaque(false);

        pnlInput.setBackground(new java.awt.Color(255, 255, 255));
        pnlInput.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.SystemColor.controlDkShadow));
        pnlInput.setMinimumSize(new java.awt.Dimension(100, 20));
        pnlInput.setPreferredSize(new java.awt.Dimension(271, 20));

        org.openide.awt.Mnemonics.setLocalizedText(btnMore, org.openide.util.NbBundle.getMessage(StringCriteriaPanel.class, "StringCriteriaPanel.btnMore.text")); // NOI18N
        btnMore.setEnabled(false);
        btnMore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoreActionPerformed(evt);
            }
        });

        txtString.setBorder(null);
        txtString.setText(org.openide.util.NbBundle.getMessage(StringCriteriaPanel.class, "StringCriteriaPanel.txtString.text")); // NOI18N
        txtString.setName("txtString"); // NOI18N
        txtString.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtStringFocusLost(evt);
            }
        });

        javax.swing.GroupLayout pnlInputLayout = new javax.swing.GroupLayout(pnlInput);
        pnlInput.setLayout(pnlInputLayout);
        pnlInputLayout.setHorizontalGroup(
            pnlInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInputLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(txtString, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnMore, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnlInputLayout.setVerticalGroup(
            pnlInputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnMore, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(txtString, javax.swing.GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(lblString, org.openide.util.NbBundle.getMessage(StringCriteriaPanel.class, "StringCriteriaPanel.lblString.text")); // NOI18N
        lblString.setMaximumSize(new java.awt.Dimension(0, 150));

        javax.swing.GroupLayout pnlHolderLayout = new javax.swing.GroupLayout(pnlHolder);
        pnlHolder.setLayout(pnlHolderLayout);
        pnlHolderLayout.setHorizontalGroup(
            pnlHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHolderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlInput, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblString, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlHolderLayout.setVerticalGroup(
            pnlHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHolderLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(pnlHolderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblString, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13))
        );

        org.openide.awt.Mnemonics.setLocalizedText(chkList, org.openide.util.NbBundle.getMessage(StringCriteriaPanel.class, "StringCriteriaPanel.chkList.text")); // NOI18N
        chkList.setToolTipText(org.openide.util.NbBundle.getMessage(StringCriteriaPanel.class, "StringCriteriaPanel.chkList.toolTipText")); // NOI18N
        chkList.setName("chkCaseSensitive"); // NOI18N
        chkList.setOpaque(false);
        chkList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkListActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(pnlHolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chkList)
                .addGap(18, 18, 18)
                .addComponent(chkCaseSensitive, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(188, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkCaseSensitive, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkList, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlHolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event handler for <code>txtString</code> loss of focus.
     *
     * @param evt The registered event.
     */
    private void txtStringFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtStringFocusLost

        // Something may have changed, so save the state:
        parentPanel.saveStateToGraph();
    }//GEN-LAST:event_txtStringFocusLost

    /**
     * Event handler for <code>chkCaseSensitive</code> clicks.
     *
     * @param evt The registered event.
     */
    private void chkCaseSensitiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkCaseSensitiveActionPerformed

        // Something may have changed, so save the state:
        parentPanel.saveStateToGraph();
    }//GEN-LAST:event_chkCaseSensitiveActionPerformed

    /**
     * Event handler for <code>btnMore</code> clicks.
     *
     * @param evt The registered event.
     */
    private void btnMoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoreActionPerformed

        getMoreContent();
    }//GEN-LAST:event_btnMoreActionPerformed

    /**
     * Event handler for <code>chkList</code> clicks.
     *
     * @param evt The registered event.
     */
    private void chkListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkListActionPerformed

        toggleListMode();
    }//GEN-LAST:event_chkListActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnMore;
    private javax.swing.JCheckBox chkCaseSensitive;
    private javax.swing.JCheckBox chkList;
    private javax.swing.JLabel lblString;
    private javax.swing.JPanel pnlHolder;
    private javax.swing.JPanel pnlInput;
    private javax.swing.JFormattedTextField txtString;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates a dialog box to collect the list of terms to be searched.
     */
    private void getMoreContent() {
        listPanel = new StringListPanel(txtString.getText());

        final DialogDescriptor dd = new DialogDescriptor(listPanel,
                "String Editor", true, this);

        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        // Show the dialog:
        dialog.setVisible(true);
    }

    /**
     * Helper method that toggles the list state for the UI, and alerts the
     * parent of a change to the overall find state.
     */
    private void toggleListMode() {
        btnMore.setEnabled(chkList.isSelected());

        parentPanel.saveStateToGraph();
    }
}
