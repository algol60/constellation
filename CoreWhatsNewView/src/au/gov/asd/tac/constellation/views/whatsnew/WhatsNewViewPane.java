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
package au.gov.asd.tac.constellation.views.whatsnew;

import au.gov.asd.tac.constellation.functionality.CorePluginRegistry;
import au.gov.asd.tac.constellation.functionality.browser.OpenInBrowserPlugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * Whats New View Pane.
 *
 * @author aquila Refactor by:
 * @author aldebaran30701
 */
public class WhatsNewViewPane extends BorderPane {

    private final BorderPane whatsNewViewPane;

    public static final String MOUSE_IMAGE = "resources/mouse3.png";
    public static final String MENU_IMAGE = "resources/sidebar.png";
    public static final String ERROR_BUTTON_MESSAGE = String.format("%s Information", BrandingUtilities.APPLICATION_NAME);
    public static final String WELCOME_TEXT = "Welcome to Constellation";
    public static final double SPLIT_POS = 0.2;

    /**
     * The number of days an entry is considered recent
     */
    public static final int RECENT_DAYS = -30; // 1 month

    /**
     * The number of days before an entry is archived and no longer shown
     */
    public static final int ARCHIVE_DAYS = -180; // 6 months

    /**
     * Required date format
     */
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public WhatsNewViewPane() {
        whatsNewViewPane = new BorderPane();
        ConstellationSecurityManager.startSecurityLaterFX(() -> {
            Platform.setImplicitExit(false);

            final SplitPane splitPane = new SplitPane();
            splitPane.setOrientation(Orientation.HORIZONTAL);
            whatsNewViewPane.setCenter(splitPane);

            //Create left VBox to handle Browser and controls,
            //or error messages
            final VBox leftVBox = new VBox();
            splitPane.getItems().add(leftVBox);

            splitPane.setBackground(new Background(new BackgroundFill(Color.valueOf("#333333"), CornerRadii.EMPTY, Insets.EMPTY)));
            leftVBox.paddingProperty().set(new Insets(5, 5, 5, 5));

            final WebView whatsNewView = new WebView();
            VBox.setVgrow(whatsNewView, Priority.ALWAYS);
            whatsNewView.getEngine().getLoadWorker().stateProperty().addListener((final ObservableValue<? extends Worker.State> observable, final Worker.State oldValue, final Worker.State newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    // Add a click listener for <a> tags.
                    // If there's a valid href attribute, open the default browser at that URL.
                    // If there's a valid helpId attribute, open NetBeans help using the helpId.
                    // An <a> without an href doesn't get underlined, so use href="" in addition to helpId="...".
                    final EventListener listener = (final Event event) -> {
                        final String eventType = event.getType();
                        if (eventType.equals("click")) {
                            event.preventDefault();

                            final String href = ((Element) event.getTarget()).getAttribute("href");
                            if (StringUtils.isNotBlank(href)) {
                                PluginExecution.withPlugin(CorePluginRegistry.OPEN_IN_BROWSER)
                                        .withParameter(OpenInBrowserPlugin.APPLICATION_PARAMETER_ID, "Open Tutorial Link")
                                        .withParameter(OpenInBrowserPlugin.URL_PARAMETER_ID, href)
                                        .executeLater(null);
                            } else {
                                final String helpId = ((Element) event.getTarget()).getAttribute("helpId");
                                if (StringUtils.isNotBlank(helpId)) {
                                    new HelpCtx(helpId).display();
                                }
                            }
                        }
                    };

                    final Document doc = whatsNewView.getEngine().getDocument();
                    final NodeList nodeList = doc.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener("click", listener, true);
                    }
                }
            });
            whatsNewView.getEngine().setUserStyleSheetLocation(WhatsNewTopComponent.class.getResource("resources/whatsnew.css").toExternalForm());
            whatsNewView.getStyleClass().add("web-view");
            try {
                whatsNewView.getEngine().loadContent(getWhatsNew());
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            leftVBox.getChildren().add(whatsNewView);

            //Create left VBox to handle "help" images of menu and mouse
            final VBox rightVBox = new VBox(10);
            splitPane.getItems().add(rightVBox);
            rightVBox.setPadding(new Insets(10, 10, 10, 10));
            rightVBox.setAlignment(Pos.TOP_CENTER);
            rightVBox.setMaxWidth(600);
            rightVBox.setMinWidth(400);

            // Create a checkbox to change users preference regarding showing the Tutorial Page on startup
            final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
            final CheckBox showOnStartUpCheckBox = new CheckBox("Show on Startup");
            rightVBox.getChildren().add(showOnStartUpCheckBox);
            rightVBox.setAlignment(Pos.TOP_RIGHT);
            rightVBox.setBackground(new Background(new BackgroundFill(Color.valueOf("#333333"), CornerRadii.EMPTY, Insets.EMPTY)));
            rightVBox.paddingProperty().set(new Insets(5, 5, 5, 5));
            showOnStartUpCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean oldVal, Boolean newVal) {
                    prefs.putBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, newVal);
                }
            });
            showOnStartUpCheckBox.setSelected(prefs.getBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP_DEFAULT));

            // Create a preferenceListener in order to identify when user preference is changed
            // Keeps tutorial page and options tutorial selections in-sync when both are open
            prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    showOnStartUpCheckBox.setSelected(prefs.getBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, showOnStartUpCheckBox.isSelected()));
                }
            });

            //Create images for Left VBox
            final ImageView menuImage = new ImageView(new Image(WhatsNewTopComponent.class.getResourceAsStream(MENU_IMAGE)));
            menuImage.setFitWidth(300);
            menuImage.setPreserveRatio(true);
            rightVBox.getChildren().add(menuImage);
            final ImageView mouseImage = new ImageView(new Image(WhatsNewTopComponent.class.getResourceAsStream(MOUSE_IMAGE)));
            mouseImage.setFitWidth(300);
            mouseImage.setPreserveRatio(true);
            rightVBox.getChildren().add(mouseImage);

            splitPane.getDividers().get(0).setPosition(SPLIT_POS);
            //Finally, insert the tutorialViewPane object into the BorderPane
            this.setCenter(whatsNewViewPane);
        });
    }

    private String getWhatsNew() throws ParseException {
        Collection<? extends WhatsNewProvider> whatsNew = Lookup.getDefault().lookupAll(WhatsNewProvider.class);
        final ArrayList<WhatsNewProvider.WhatsNewEntry> wnList = new ArrayList<>();
        whatsNew.stream().forEach(wnp -> {
            wnList.addAll(WhatsNewProvider.getWhatsNew(wnp.getClass(), wnp.getResource(), wnp.getSection()));
        });

        Collections.sort(wnList);

        final Date now = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DATE, RECENT_DAYS);
        final Date twoWeeksOld = cal.getTime();

        cal.setTime(now);
        cal.add(Calendar.DATE, ARCHIVE_DAYS);
        final Date archiveLimit = cal.getTime();

        boolean headerDone = false;
        final StringBuilder buf = new StringBuilder();
        buf.append("<!DOCTYPE html><html><body>\n");
        buf.append(String.format("<style>body{font-size:%spx;}</style>", FontUtilities.getApplicationFontSize() * 1.5));
        buf.append(String.format("<style>body{font-family:%s;}</style>", FontUtilities.getApplicationFontFamily()));

        for (final WhatsNewProvider.WhatsNewEntry wne : wnList) {
            // Use a far-future date to indicate an undated fixed position at the top.
            final String dt;
            if (wne.date.compareTo("3000") == -1) {
                if (!headerDone) {
                    buf.append("<hr>\n<h2>What's New</h2>\n");
                    headerDone = true;
                }

                final Date whatsNewDate = dateFormatter.parse(wne.date);
                if (whatsNewDate.after(twoWeeksOld) && whatsNewDate.before(now)) {
                    // display date, header, recent badge and section.
                    dt = String.format("%s <strong>%s</strong> <span class=\"badge badge-recent\">Recent</span> <span class=\"section badge badge-section\">%s</span>", wne.date, wne.header, wne.section);
                } else if (whatsNewDate.before(archiveLimit)) {
                    dt = null;
                } else {
                    // The "normal" case: display date, header and section.
                    dt = String.format("%s <strong>%s</strong> <span class=\"section badge badge-section\">%s</span>", wne.date, wne.header, wne.section);
                }

            } else {
                // Display a pegged entry without the date.
                dt = String.format("<strong>%s</strong> <span class=\"section badge badge-section\">%s</span>", wne.header, wne.section);
            }

            if (dt != null) {
                buf.append(String.format("<dl><dt>%s</dt>%n<dd>", dt));
                buf.append(wne.text);
                buf.append("</dd></dl>\n");
            }
        }

        buf.append("</body></html>");
        return buf.toString();
    }
}
