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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginSynchronizer;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPaneListener;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.templates.DataAccessPreQueryValidation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Lookup;

/**
 * A panel for selecting plug-ins to run during a query phase. These panes are
 * embedded in the tabs on the Data Access view.
 *
 * @author cygnus_x-1
 * @author ruby_crucis
 */
public class QueryPhasePane extends VBox {
    private static final Logger LOGGER = Logger.getLogger(QueryPhasePane.class.getName());
    
    private static final List<DataAccessPreQueryValidation> PRE_QUERY_VALIDATION = 
            new ArrayList<>(
                    Lookup.getDefault().lookupAll(DataAccessPreQueryValidation.class)
            );
    
    private final GlobalParametersPane globalParametersPane;
    private final VBox dataSourceList = new VBox();
    private final List<DataSourceTitledPane> dataSources = new ArrayList<>();

    private final Set<MenuItem> graphDependentMenuItems = new HashSet<>();
    private final Set<MenuItem> pluginDependentMenuItems = new HashSet<>();

    /**
     * Creates a new query phase pane.
     *
     * @param plugins all discovered data access plugins grouped by type
     * @param top 
     * @param presetGlobalParms the current global plugin parameters
     */
    public QueryPhasePane(final Map<String, List<DataAccessPlugin>> plugins,
                          final PluginParametersPaneListener top,
                          final PluginParameters presetGlobalParms) {
        globalParametersPane = new GlobalParametersPane(presetGlobalParms);

        plugins.entrySet().stream()
                .filter(pluginsOfType -> !pluginsOfType.getValue().isEmpty())
                .forEach(pluginsOfType -> {
                    final HeadingPane heading = new HeadingPane(
                            pluginsOfType.getKey(),
                            pluginsOfType.getValue(),
                            top,
                            globalParametersPane.getParamLabels()
                    );
                    
                    dataSourceList.getChildren().add(heading);
                    dataSources.addAll(heading.getDataSources());
                });

        setFillWidth(true);
        getChildren().addAll(globalParametersPane, dataSourceList);
    }

    
    /**
     * Iterate through all the menu items classed as graph dependent and
     * enable or disable them based on the passed flag.
     *
     * @param enabled true if all graph dependent menu item should be enabled, false otherwise
     */
    public void enableGraphDependentMenuItems(final boolean enabled) {
        graphDependentMenuItems.forEach(menuItem -> 
            menuItem.setDisable(!enabled)
        );
    }

    /**
     * Adds one or more menu items to graph dependent menu items group.
     *
     * @param menuItems the menu items to add
     */
    public void addGraphDependentMenuItems(final MenuItem... menuItems) {
        graphDependentMenuItems.addAll(Arrays.asList(menuItems));
    }

    /**
     * Iterate through all the menu items classed as plugin dependent and
     * enable or disable them based on the passed flag.
     *
     * @param enabled true if all plugin dependent menu item should be enabled, false otherwise
     */
    public void enablePluginDependentMenuItems(final boolean enabled) {
        pluginDependentMenuItems.forEach(menuItem -> 
            menuItem.setDisable(!enabled)
        );
    }

    /**
     * Adds one or more menu items to graph dependent menu items group.
     *
     * @param menuItems the menu items to add
     */
    public void addPluginDependentMenuItems(final MenuItem... menuItems) {
        pluginDependentMenuItems.addAll(Arrays.asList(menuItems));
    }

    

    /**
     * Expand or contract the heading panes and their children based on the passed
     * flags.
     *
     * @param expand true if the heading sections should be expanded, false otherwise
     * @param expandchildren true if the child sections should also be expanded, false otherwise
     */
    public void setHeadingsExpanded(final boolean expand, final boolean expandchildren) {
        dataSourceList.getChildren().stream().forEach(child -> {
            final HeadingPane headingPage = (HeadingPane) child;
            headingPage.setExpanded(expand);
            if (expandchildren) {
                headingPage.getDataSources().forEach(titledPane -> titledPane.setExpanded(expand));
            }
        });
    }

    /**
     * Expand the named plugin (and the section it's in) and scroll it to a
     * visible position.
     * <p/>
     * All other plugins and sections will be unexpanded.
     *
     * @param pluginName the name of the plugin to expand.
     */
    public void expandPlugin(final String pluginName) {
        setHeadingsExpanded(false, true);
        
        for (final Node node : dataSourceList.getChildrenUnmodifiable()) {
            final HeadingPane headingPane = (HeadingPane) node;
            
            for (final DataSourceTitledPane titledPane : headingPane.getDataSources()) {
                if (pluginName.equals(titledPane.getPlugin().getName())) {
                    headingPane.setExpanded(true);
                    titledPane.setExpanded(true);
                    
                    final ScrollPane sp = (ScrollPane) getParent().getParent().getParent();
                    
                    Platform.runLater(() -> {
                        sp.setHvalue(0);
                        final double v = Math.min(
                                headingPane.getLayoutY() + titledPane.getLayoutY()
                                        + titledPane.getHeight() / 2,
                                getHeight()
                        );
                        sp.setVvalue(v);
                    });
                    
                    break;
                }
            }
        }
    }

    /**
     * Expand the section matching the plugin text.
     *
     * @param text the text to search for across plugin names
     */
    public void showMatchingPlugins(final String text) {
        if (text != null) {
            final String textLowerCased = text.toLowerCase();
            
            for (final Node node : dataSourceList.getChildrenUnmodifiable()) {
                final HeadingPane headingPane = (HeadingPane) node;
                boolean shouldExpand = false;
                
                for (final DataSourceTitledPane titledPane : headingPane.getDataSources()) {
                    titledPane.getStyleClass().remove(DataSourceTitledPane.MATCHED_STYLE);
                    if (StringUtils.isNotBlank(text) 
                            && titledPane.getPlugin().getName().toLowerCase().contains(textLowerCased)) {
                        titledPane.getStyleClass().add(DataSourceTitledPane.MATCHED_STYLE);
                        shouldExpand = true;
                    }
                }
                
                if (shouldExpand != headingPane.isExpanded()) {
                    headingPane.setExpanded(shouldExpand);
                }
            }
        }
    }
    
    /**
     * Run the plugin for each data access pane in this query pane, optionally
     * waiting first on a list of passed futures. This method will start the
     * plugin execution and return a list of futures representing each plugins
     * execution.
     *
     * @param async if not null, the plugins to be executed will wait till all the
     *     futures in the list have been completed before running
     * @return a list of futures representing the plugins that were executed
     */
    public List<Future<?>> runPlugins(final List<Future<?>> async) {
        // Get the global plugin parameters for this query pane
        final Map<String, PluginParameter<?>> globalParams = getGlobalParametersPane()
                .getParams().getParameters();

        // Pre-query validation checking
        for (final DataAccessPreQueryValidation check : PRE_QUERY_VALIDATION) {
            if (!check.execute(this)) {
                return Collections.emptyList();
            }
        }

        // Determine the number of plugins that will be executed
        final int pluginsToRun = Long.valueOf(getDataAccessPanes().stream()
                .filter(DataSourceTitledPane::isQueryEnabled)
                .count()).intValue();
        
        LOGGER.log(Level.INFO, "\tRunning {0} plugins", pluginsToRun);
        
        final PluginSynchronizer synchroniser = new PluginSynchronizer(pluginsToRun);
        final List<Future<?>> newAsync = new ArrayList<>(pluginsToRun);
        
        getDataAccessPanes().stream()
                .filter(DataSourceTitledPane::isQueryEnabled)
                .forEach(pane -> {
                    // Copy global parameters into plugin parameters where there
                    // is overlap
                    PluginParameters parameters = pane.getParameters();
                    if (parameters != null) {
                        parameters = parameters.copy();
                        parameters.getParameters().entrySet().stream()
                                .filter(entry -> globalParams.containsKey(entry.getKey()))
                                .forEach(entry -> entry.getValue().setObjectValue(
                                        globalParams.get(entry.getKey()).getObjectValue()
                                ));
                    }
                    
                    // Get the plugin for this pane and run it
                    final Plugin plugin = PluginRegistry.get(pane.getPlugin().getClass().getName());
                    
                    LOGGER.log(Level.INFO, "\t\tRunning {0}", plugin.getName());
                    
                    final Future<?> pluginResult = PluginExecution.withPlugin(plugin)
                        .withParameters(parameters)
                        .waitingFor(async)
                        .synchronizingOn(synchroniser)
                        .executeLater(GraphManager.getDefault().getActiveGraph());
                    
                    newAsync.add(pluginResult);
                
                    DataAccessPaneState.addRunningPlugin(pluginResult, plugin.getName());
                });
        
        return newAsync;
    }
    
    /**
     * Get the actual data access panes on this query phase pane. These can then
     * be accessed to see which ones are enabled and to get the plugin parameters.
     *
     * @return a list of the data access panes
     */
    public List<DataSourceTitledPane> getDataAccessPanes() {
        return dataSources;
    }

    /**
     * Get the global parameters pane on this query phase pane. From this the
     * global plugin parameters can be accessed.
     *
     * @return the global parameters pane
     */
    public GlobalParametersPane getGlobalParametersPane() {
        return globalParametersPane;
    }
    
    protected VBox getDataSourceList() {
        return dataSourceList;
    }

    protected Set<MenuItem> getGraphDependentMenuItems() {
        return graphDependentMenuItems;
    }

    protected Set<MenuItem> getPluginDependentMenuItems() {
        return pluginDependentMenuItems;
    }
}
