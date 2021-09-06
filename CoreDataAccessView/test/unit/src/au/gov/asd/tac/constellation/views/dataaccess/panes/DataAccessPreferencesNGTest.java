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

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessPreferencesNGTest {

    private static final String TITLE = "a test title";

    public DataAccessPreferencesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void setExpanded() {
        assertEquals(DataAccessPreferences.isExpanded(TITLE, true), true);
        assertEquals(DataAccessPreferences.isExpanded(TITLE, false), false);

        DataAccessPreferences.setExpanded(TITLE, true);
        assertEquals(DataAccessPreferences.isExpanded(TITLE, false), true);
    }

    @Test
    public void setFavourite() {
        assertEquals(DataAccessPreferences.isfavourite(TITLE, true), true);
        assertEquals(DataAccessPreferences.isfavourite(TITLE, false), false);

        DataAccessPreferences.setFavourite(TITLE, true);
        assertEquals(DataAccessPreferences.isfavourite(TITLE, false), true);
    }
}