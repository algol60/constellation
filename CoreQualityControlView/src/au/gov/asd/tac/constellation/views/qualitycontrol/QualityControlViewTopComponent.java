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
package au.gov.asd.tac.constellation.views.qualitycontrol;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlAutoVetter;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlListener;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlState;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component for the Quality Control View.
 *
 * @author cygnus_x-1
 */
@TopComponent.Description(
        preferredID = "QualityControlViewTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/qualitycontrol/resources/quality-control-view.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlViewTopComponent")
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 1000),
    @ActionReference(path = "Shortcuts", name = "CS-Q")})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_QualityControlViewAction",
        preferredID = "QualityControlViewTopComponent")
@Messages({
    "CTL_QualityControlViewAction=Quality Control View",
    "CTL_QualityControlViewTopComponent=Quality Control View",
    "HINT_QualityControlViewTopComponent=Quality Control View"})
public final class QualityControlViewTopComponent extends JavaFxTopComponent<QualityControlViewPane> implements QualityControlListener {

    private final QualityControlViewPane qualityControlViewPane;

    public QualityControlViewTopComponent() {
        setName(Bundle.CTL_QualityControlViewTopComponent());
        setToolTipText(Bundle.HINT_QualityControlViewTopComponent());

        initComponents();

        qualityControlViewPane = new QualityControlViewPane();
        initContent();
    }

    @Override
    protected QualityControlViewPane createContent() {
        return qualityControlViewPane;
    }

    @Override
    protected String createStyle() {
        return "resources/quality-control-view.css";
    }

    @Override
    public void handleComponentOpened() {
        super.handleComponentOpened();
        QualityControlAutoVetter.getInstance().addListener(this);
        QualityControlAutoVetter.getInstance().invokeListener(this);
        QualityControlAutoVetter.getInstance().init();
    }

    @Override
    public void handleComponentClosed() {
        super.handleComponentClosed();
        QualityControlAutoVetter.getInstance().removeListener(this);
    }

    @Override
    public void qualityControlChanged(final QualityControlState state) {
        if (needsUpdate()) {
            qualityControlViewPane.refreshQualityControlView(state);
        }
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        QualityControlAutoVetter.getInstance().initWithRefresh(true);
    }

    @Override
    protected void handleGraphClosed(final Graph graph) {
        qualityControlViewPane.refreshQualityControlView(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
