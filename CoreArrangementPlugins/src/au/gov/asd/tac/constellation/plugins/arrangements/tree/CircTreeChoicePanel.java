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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.utilities.widgets.AttributeSelectionPanel;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JPanel;

/**
 * UI panel provided the user input parameters for the circular tree arrangement
 *
 * @author algol
 */
public class CircTreeChoicePanel extends JPanel {

    public CircTreeChoicePanel() {
        initComponents();

        final CircTreeChoiceParameters params = CircTreeChoiceParameters.getDefaultParameters();
        scaleSpinner.setValue(params.scale);
        circularLayoutCheck.setSelected(params.strictCircularLayout);
        rootValueText.setText(params.rootValue);
    }

    public void setGraph(final Graph graph) {
        Collection<GraphElementType> elementTypes = Collections.singleton(GraphElementType.VERTEX);
        attributeSelectionPanel.setGraph(graph, elementTypes, null, null);
    }

    public CircTreeChoiceParameters getParameters() {
        final float scale = (Float) scaleSpinner.getValue();
        final boolean scl = circularLayoutCheck.isSelected();
        final String value = rootValueText.getText();
        final int attrId = attributeSelectionPanel.getAttributeId();

        return new CircTreeChoiceParameters(scale, scl, attrId, value);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        scaleSpinner = new javax.swing.JSpinner();
        circularLayoutCheck = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        rootValueText = new javax.swing.JTextField();
        attributeSelectionPanel = new AttributeSelectionPanel("Select the root attribute");

        jLabel1.setText(org.openide.util.NbBundle.getMessage(CircTreeChoicePanel.class, "CircTreeChoicePanel.jLabel1.text")); // NOI18N

        scaleSpinner.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), null, Float.valueOf(0.1f)));
        scaleSpinner.setPreferredSize(new java.awt.Dimension(31, 20));

        circularLayoutCheck.setText(org.openide.util.NbBundle.getMessage(CircTreeChoicePanel.class, "CircTreeChoicePanel.circularLayoutCheck.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(CircTreeChoicePanel.class, "CircTreeChoicePanel.jLabel2.text")); // NOI18N

        rootValueText.setText(org.openide.util.NbBundle.getMessage(CircTreeChoicePanel.class, "CircTreeChoicePanel.rootValueText.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(attributeSelectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rootValueText))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(circularLayoutCheck)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scaleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(scaleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(circularLayoutCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(rootValueText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(attributeSelectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private au.gov.asd.tac.constellation.graph.utilities.widgets.AttributeSelectionPanel attributeSelectionPanel;
    private javax.swing.JCheckBox circularLayoutCheck;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField rootValueText;
    private javax.swing.JSpinner scaleSpinner;
    // End of variables declaration//GEN-END:variables
}
