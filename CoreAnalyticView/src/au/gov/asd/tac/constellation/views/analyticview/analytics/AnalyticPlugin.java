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
package au.gov.asd.tac.constellation.views.analyticview.analytics;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.help.utilities.Generator;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * A plugin designed to be supported by the Analytic View.
 *
 * @param <R> The {@link AnalyticResult} class created by this plugin.
 *
 * @author cygnus_x-1
 */
public abstract class AnalyticPlugin<R extends AnalyticResult<?>> extends SimpleEditPlugin {

    /**
     * Get a URL for documentation describing this plugin.
     * <p>
     * This can either be a standard web URL or a Netbeans Help URL (using the
     * nbdocs protocol).
     *
     * @return a documentation string.
     */
    public String getDocumentationUrl() {
        return null;
    }

    /**
     * Get the set of graph attributes this plugin relies on.
     *
     * @return the set of attributes this plugin relies on.
     */
    public Set<SchemaAttribute> getPrerequisiteAttributes() {
        return Collections.emptySet();
    }

    /**
     * Respond to changes to any of the specified prerequisite attributes.
     *
     * @param graph the graph this plugin is operating on.
     * @param parameters the current plugin parameters.
     */
    public void onPrerequisiteAttributeChange(final Graph graph, final PluginParameters parameters) {
        // Do nothing
    }

    /**
     * Get the graph attribute this plugin will use to store its results.
     *
     * @param parameters
     * @return
     */
    public abstract Set<SchemaAttribute> getAnalyticAttributes(final PluginParameters parameters);

    public void prepareGraph(final GraphWriteMethods graph, final PluginParameters parameters) throws InterruptedException, PluginException {
        // Do nothing
    }

    @Override
    public PluginParameters createParameters() {
        return new PluginParameters();
    }

    public void updateParameters(final PluginParameters parameters) {
        // Do nothing
    }

    public String getHelpPath() {
        final String codebaseName = "constellation";
        final String sep = File.separator;
        return Generator.getBaseDirectory() + sep + "ext" + sep + "docs" + sep + "CoreAnalyticView" + sep + "src" + sep + "au" + sep + "gov" + sep
                + "asd" + sep + "tac" + sep + codebaseName + sep + "views" + sep + "analyticview" +  sep;
    }

    /**
     * Get the results generated by this plugin.
     *
     * @return the results generated by this plugin.
     */
    public abstract R getResults();

    /**
     * Get the type of result generated by this class.
     *
     * @return
     */
    public abstract Class<? extends AnalyticResult<?>> getResultType();

    /**
     * Determines whether this analytic appear in the Categories section of the
     * Analytic View.
     *
     * @return true is this analytic should be visible, false otherwise.
     */
    public boolean isVisible() {
        return true;
    }
}
