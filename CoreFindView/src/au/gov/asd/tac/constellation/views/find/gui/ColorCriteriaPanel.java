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

import au.gov.asd.tac.constellation.graph.utilities.widgets.GraphColorChooser;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JColorChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * JPanel that allows the user to input <code>Color</code> content that the wish
 * to perform search operations on.
 *
 * @author betelgeuse
 */
public class ColorCriteriaPanel extends javax.swing.JPanel implements ActionListener {

    private final JColorChooser chooser;
    private Color currentColor = Color.BLUE;
    private final FindCriteriaPanel parentPanel;

    /**
     * Creates new form ColorCriteriaPanel
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     */
    public ColorCriteriaPanel(final FindCriteriaPanel parent) {
        initComponents();

        chooser = new GraphColorChooser();
        this.parentPanel = parent;

        chooser.setColor(currentColor);
//        updateColor(currentColor);
    }

    /**
     * Creates a <code>ColorCriteriaPanel</code> with predefined content.
     *
     * @param parent The <code>FindCriteriaPanel</code> that owns this panel.
     * @param color The predefined state for this panel.
     * @see Color
     */
    public ColorCriteriaPanel(final FindCriteriaPanel parent, final Color color) {
        initComponents();

        chooser = new GraphColorChooser();
        this.parentPanel = parent;

        currentColor = color;
        chooser.setColor(currentColor);
//        updateColor(currentColor);
    }

    /**
     * Returns the current state of this panel.
     *
     * @return The currently selected <code>Color</code> value.
     * @see Color
     */
    public Color getColorContent() {
        final ConstellationColor cv = ConstellationColor.fromJavaColor(currentColor);

        return cv.getJavaColor();
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if ("OK".equals(e.getActionCommand())) {
            currentColor = chooser.getColor();

            updateColor(currentColor);
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

        panelColorChoice = new javax.swing.JPanel();
        btnChooseColor = new javax.swing.JButton();
        lblColorText = new javax.swing.JLabel();
        lblColor = new javax.swing.JLabel();

        setOpaque(false);

        panelColorChoice.setBackground(null);
        panelColorChoice.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.SystemColor.controlShadow));

        btnChooseColor.setText("...");
        btnChooseColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseColorActionPerformed(evt);
            }
        });

        lblColorText.setLabelFor(btnChooseColor);
        lblColorText.setText("White");
        lblColorText.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblColorTextMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout panelColorChoiceLayout = new javax.swing.GroupLayout(panelColorChoice);
        panelColorChoice.setLayout(panelColorChoiceLayout);
        panelColorChoiceLayout.setHorizontalGroup(
            panelColorChoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelColorChoiceLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblColorText, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnChooseColor, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelColorChoiceLayout.setVerticalGroup(
            panelColorChoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelColorChoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnChooseColor, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblColorText))
        );

        lblColor.setBackground(null);
        lblColor.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.SystemColor.controlShadow));
        lblColor.setMaximumSize(new java.awt.Dimension(18, 18));
        lblColor.setMinimumSize(new java.awt.Dimension(18, 18));
        lblColor.setOpaque(true);
        lblColor.setPreferredSize(new java.awt.Dimension(18, 18));
        lblColor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblColorMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelColorChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelColorChoice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event handler for <code>btnChooseColor</code> button presses.
     *
     * @param evt The registered event.
     */
    private void btnChooseColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseColorActionPerformed

        showDialog();
    }//GEN-LAST:event_btnChooseColorActionPerformed

    /**
     * Event handler for <code>lblColor</code> mouse clicks.
     *
     * @param evt The registered event.
     */
    private void lblColorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblColorMouseClicked

        showDialog();
    }//GEN-LAST:event_lblColorMouseClicked

    /**
     * Event handler for <code>lblColorText</code> mouse clicks.
     *
     * @param evt The registered event.
     */
    private void lblColorTextMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblColorTextMouseClicked

        showDialog();
    }//GEN-LAST:event_lblColorTextMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChooseColor;
    private javax.swing.JLabel lblColor;
    private javax.swing.JLabel lblColorText;
    private javax.swing.JPanel panelColorChoice;
    // End of variables declaration//GEN-END:variables

    /**
     * Shows a color selection dialog box to the user.
     */
    private void showDialog() {
        final Dialog dialog = getColorFromUser();
        dialog.setVisible(true);
    }

    /**
     * Handles any change to the current color.
     * <p>
     * This method handles GUI updates to ensure that the GUI correctly
     * represents the current selected color.
     *
     * @param color The new color to handle.
     * @see Color
     */
    private void updateColor(final Color color) {
        // Get most current selected color from the chooser:
        final ConstellationColor cv = ConstellationColor.fromJavaColor(color);

        // Get the color's text value:
        final String text = cv.getName() != null ? cv.getName()
                : String.format("r=%s g=%s b=%s a=%s", formatFloat(cv.getRed()),
                        formatFloat(cv.getGreen()), formatFloat(cv.getBlue()), formatFloat(cv.getAlpha()));

        // Update the background color of the color display:
        lblColor.setBackground(color);

        // Set the text of the color label:
        lblColorText.setText(text);

        // Things have changed, so save the state to the graph:
        parentPanel.saveStateToGraph();
    }

    /**
     * Creates a dialog box based on the <code>ColorPropertyEditor</code> class.
     *
     * @return A color selection dialog box.
     * @see ColorPropertyEditor
     * @see Dialog
     */
    private Dialog getColorFromUser() {
        final DialogDescriptor dialog = new DialogDescriptor(chooser,
                NbBundle.getMessage(ColorPropertyEditor.class, "MSG_SelectColor"), true, this);

        return DialogDisplayer.getDefault().createDialog(dialog);
    }

    /**
     * Formats a given float as a string.
     *
     * @param f The float to format as a string.
     * @return The string representation of the given float.
     */
    private String formatFloat(final float f) {
        String s = String.format("%5.3f", f);
        while (s.endsWith("00")) {
            s = s.substring(0, s.length() - 1);
        }

        return s;
    }
}
