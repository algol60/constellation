/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.interaction;

import java.util.Map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class InteractiveGraphHelpProviderNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getHelpMap method, of class InteractiveGraphHelpProvider.
     */
    @Test
    public void testGetHelpMap() {
        System.out.println("getHelpMap");
        
        final InteractiveGraphHelpProvider instance = new InteractiveGraphHelpProvider();
        final Map<String, String> helpMap = instance.getHelpMap();

        assertEquals(helpMap.size(), 4);
        //not going to go through and assert the existence of each specific item in the map 
        //but will assert they're all correctly located within the interaction package
        final String packageStart = "au.gov.asd.tac.constellation.graph.interaction.";
        for (final String path : helpMap.keySet()) {
            assertTrue(path.startsWith(packageStart));
        }
    }

    /**
     * Test of getHelpTOC method, of class InteractiveGraphHelpProvider.
     */
    @Test
    public void testGetHelpTOC() {
        System.out.println("getHelpTOC");
        
        final InteractiveGraphHelpProvider instance = new InteractiveGraphHelpProvider();
        final String tocLocation = instance.getHelpTOC();

        //the file separator used will vary from OS to OS so rather than copying the
        //exact string from the actual class and asserting what we got is the same,
        //we'll assert that some of the keyparts of the expected filepath are present
        assertTrue(tocLocation.contains("docs"));
        assertTrue(tocLocation.contains("CoreInteractiveGraph"));
        assertTrue(tocLocation.contains("src"));
        assertTrue(tocLocation.contains("graph"));
        assertTrue(tocLocation.contains("interaction"));
        assertTrue(tocLocation.contains("interactivegraph-toc.xml"));

        assertTrue(tocLocation.indexOf("docs") < tocLocation.indexOf("CoreInteractiveGraph"));
        assertTrue(tocLocation.indexOf("CoreInteractiveGraph") < tocLocation.indexOf("src"));
        assertTrue(tocLocation.indexOf("src") < tocLocation.indexOf("graph"));
        assertTrue(tocLocation.indexOf("graph") < tocLocation.indexOf("interaction"));
        assertTrue(tocLocation.indexOf("interaction") < tocLocation.indexOf("interactivegraph-toc.xml"));
    }    
}
