/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.namedselection.panes;

import javax.swing.ImageIcon;

/**
 * Class that generates and manages an individual <code>NamedSelection</code>'s
 * UI representation.
 *
 * An instance of this class is used for each <code>NamedSelection</code> being
 * represented in in the Named Selection Browser UI.
 *
 * @author betelgeuse
 */
public class NamedSelectionListElement extends javax.swing.JPanel {

    /**
     * Creates new form NamedSelectionListElement.
     */
    public NamedSelectionListElement() {
        initComponents();
    }

    /**
     * Sets the padlock icon in the event of representing a locked named
     * selection.
     *
     * @param isLocked <code>true</code> to set the padlock icon.
     */
    protected void setLockedStatus(final boolean isLocked) {
        if (isLocked) {
            lblLocked.setIcon(new ImageIcon(getClass().getResource(
                    "/au/gov/asd/tac/constellation/views/namedselection/resources/namedselection_lockedselection.png")));
        } else {
            lblLocked.setIcon(null);
        }
    }

    /**
     * Sets the contrast (white) padlock icon in the event of representing a
     * locked named selection for a currently selected item.
     *
     * @param isLocked <code>true</code> to set the white padlock icon.
     */
    protected void setLockedStatusSelected(final boolean isLocked) {
        if (isLocked) {
            lblLocked.setIcon(new ImageIcon(getClass().getResource(
                    "/au/gov/asd/tac/constellation/views/namedselection/resources/namedselection_lockedselectionselected.png")));
        } else {
            lblLocked.setIcon(null);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        lblShortcutKey = new javax.swing.JLabel();
        lblShortcutText = new javax.swing.JLabel();
        lblNamedSelection = new javax.swing.JLabel();
        lblLocked = new javax.swing.JLabel();

        jTextPane1.setBorder(null);
        jScrollPane1.setViewportView(jTextPane1);

        setBackground(java.awt.SystemColor.text);
        setPreferredSize(new java.awt.Dimension(277, 16));

        lblShortcutKey.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        lblShortcutKey.setForeground(java.awt.SystemColor.textHighlight);
        lblShortcutKey.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(lblShortcutKey, org.openide.util.NbBundle.getMessage(NamedSelectionListElement.class, "NamedSelectionListElement.lblShortcutKey.text")); // NOI18N

        lblShortcutText.setForeground(java.awt.SystemColor.controlDkShadow);
        org.openide.awt.Mnemonics.setLocalizedText(lblShortcutText, org.openide.util.NbBundle.getMessage(NamedSelectionListElement.class, "NamedSelectionListElement.lblShortcutText.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblNamedSelection, org.openide.util.NbBundle.getMessage(NamedSelectionListElement.class, "NamedSelectionListElement.lblNamedSelection.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblLocked, org.openide.util.NbBundle.getMessage(NamedSelectionListElement.class, "NamedSelectionListElement.lblLocked.text")); // NOI18N
        lblLocked.setMaximumSize(new java.awt.Dimension(16, 16));
        lblLocked.setMinimumSize(new java.awt.Dimension(16, 16));
        lblLocked.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblNamedSelection, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblShortcutText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblShortcutKey, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblLocked, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblShortcutText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblLocked, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblShortcutKey, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblNamedSelection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        lblShortcutText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NamedSelectionListElement.class, "NamedSelectionListElement.lblShortcutText.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JLabel lblLocked;
    protected javax.swing.JLabel lblNamedSelection;
    protected javax.swing.JLabel lblShortcutKey;
    protected javax.swing.JLabel lblShortcutText;
    // End of variables declaration//GEN-END:variables
}
