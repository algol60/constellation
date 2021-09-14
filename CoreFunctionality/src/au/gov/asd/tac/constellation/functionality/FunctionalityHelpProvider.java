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
package au.gov.asd.tac.constellation.functionality;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the functionality module
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class)
@NbBundle.Messages("FunctionalityHelpProvider=Functionality Help Provider")
public class FunctionalityHelpProvider extends HelpPageProvider {

    @Override
    public Map<String, String> getHelpMap() {
        Map<String, String> map = new HashMap<>();
        final String sep = File.separator;
        final String functionalityModulePath = ".." + sep + "constellation" + sep + "CoreFunctionality" + sep + "src" + sep + "au" + sep + "gov"
                + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "functionality" + sep + "docs" + sep;

        map.put("au.gov.asd.tac.constellation.functionality.about", functionalityModulePath + "about-constellation.md");
        map.put("au.gov.asd.tac.constellation.functionality.gettingstarted", functionalityModulePath + "getting-started.md");
        map.put("au.gov.asd.tac.constellation.functionality.graphwindow", functionalityModulePath + "the-graph-window.md");
        map.put("au.gov.asd.tac.constellation.functionality.compare", functionalityModulePath + "compare-graph.md");
        return map;
    }

    @Override
    public String getHelpTOC() {
        final String sep = File.separator;
        final String functionalityPath;
        functionalityPath = "constellation" + sep + "CoreFunctionality" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac"
                + sep + "constellation" + sep + "functionality" + sep + "docs" + sep + "core-toc.xml";
        return functionalityPath;
    }
}
