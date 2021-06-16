/*
 * Copyright 2010-2020 Australian Signals Directorate
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
/**
 * A new HTML help user interface for Constellation.
 * <p>
 * This package provides a browser-based help system that allows existing
 * {@code HelpCtx()} based code to work unchanged.
 * <h2>Help format</h2>
 *
 * <p>
 * Help is provided as a directory tree of HTML and other files that can be
 * displayed by a standard web browser, located in a directory called
 * {@code html}. The only assumption is that there is an {@code index.html} at
 * the root of the tree.</p>
 *
 * <p>
 * To enable the mapping of helpId (provided by {@code HelpCtx()} to HTML page,
 * a mapping file is also required. This file is called {@code help_map.txt} and
 * is located at the root of the tree.</p>
 *
 * <p>
 * For example, a help directory tree might like this, where
 * {@code help_map.txt} and (@code html/index.html} are the only required
 * files.</p>
 *
 * <pre>
 * help_map.txt
 * html
 * |---- index.html
 * |---- contact.html
 * |---- resources
 *       |----------- logo.jpg
 *       |----------- banner.jpg
 * |---- pages
 *       ----- page1.html
 * </pre>
 *
 * The HTML and associated files could be created by hand, or generated by a
 * tool such as <a href="https://www.sphinx-doc.org/en/master/">Sphinx</a>.
 * <h2>help_map.txt format</h2>
 *
 * <p>
 * The {@code help_map.txt} is a simple two column CSV file. The first column is
 * a helpId as used by a {@code HelpCtx()} instance. The second column is the
 * path of the related web page relative to the html root. For example, if the
 * helpId "{@code com.example.one}" refers to (@code page1.html} above, the
 * {@code help_map.txt} will contain the line:</p>
 *
 * <pre>
 * com.example.one,pages/page1.html
 * </pre>
 *
 * <p>
 * There are three options for presenting the help.</p>
 *
 * <h3>Internal zip file resource</h3>
 *
 * <p>
 * This is the default method. The files are packaged in a zip file, so that the
 * {@code help_map.txt} file and {@code html} directory are at the root of the
 * zip. The zip file is named (@code help.zip} and stored as a resource next to
 * {@code ConstellationHelpDisplayer.class}. (This would typically be done at
 * build time, rather than storing {@code help.zip} with the source code.)
 * Constellation's internal web server is used to present the zipped files to
 * the user's browser. (See details below.)</p>
 *
 * <h3>External zip file</h3>
 *
 * <p>
 * This is essentially the same as using an internal zip file resource, except
 * the zip file is stored in the standard filesystem, and can have any name
 * (that doesn't start with "{@code http}").</p>
 *
 * <p>
 * The zip file is the same as the internal zip file. The Java property
 * {@code contellation.help} is defined to be the fully qualified path of the
 * zip file. For example:</p>
 *
 * <pre>
 * run.args.extra="-J-Dconstellation.help=D:/Documents/constellation_help.zip"
 * </pre>
 *
 * <p>
 * Constellation's internal web server is used to present the zipped files to
 * the user's browser. (See details below.)
 *
 * <h3>External web server.</h3>
 *
 * <p>
 * The help files (the {@code help_map.txt} file and {@code html} directory) are
 * served from an external web server. How the files are stored is up to the web
 * server, as long as they are available in the same layout as above.
 * </p>
 *
 * <p>
 * The Java property {@code contellation.help} is defined to be the URL of the
 * root of the help (without a trailing "{@code /}". For example:</p>
 *
 * <pre>
 * run.args.extra="-J-Dconstellation.help=http://www.example.com/constellation"
 * </pre>
 *
 * <p>
 * The {@code help_map.txt} file must then be available at
 * {@code http://www.example.com/constellation/help_map.txt};
 * {@code html/index.html} must be available at
 * {@code http://www.example.com/constellation/html/index.html}, with other
 * files available as expected relative to {@code html/index.html}.</p>
 *
 * <p>
 * The URL can use either {@code http} or {@code https}, the help code just
 * looks for a string starting with "{@code http}" (which is why the zip file
 * above can't start with that.</p>
 *
 * <p>
 * Using this method means that all help is served to the user's browser from
 * the specified web server. After the initial browse to a URL after the user's
 * help request, Constellation is not otherwise involved.</p>
 *
 * <p>
 * This method can be tested by unzipping the zip file into a temporary
 * directory, running {@code python -m http.server} in that directory, and
 * defining {@code constellation.help} to be {@code http://localhost:8000}.</p>
 *
 * <h2>How it works</h2>
 *
 * <p>
 * NetBeans applications request help to be displayed for a helpId either by
 * explicitly calling {@code HelpCtx.display()}, or by associating a
 * {@code HelpCtx()} with a NetBeans help-aware object (such as a TopComponent).
 * A helpId is associated with a help page via a {@code -map.xml} file in a
 * helpset. The rest of the helpset information allows NetBeans to combine all
 * the helpsets into a single JavaHelp display.</p>
 *
 * <p>
 * The {@code ConstellationHelp} class overrides the built-in NetBeans help
 * provider and looks up the highest priority {@code HelpCtx.Displayer} class,
 * which happens to be {@code ConstellationHelpDisplayer}. Depending on the
 * definition (or lack thereof) of {@code constellation.help},
 * {@code ConstellationHelpDisplayer} obtains {@code help_map.txt} on the first
 * help request, uses it to map helpIds to help pages, and builds a URL for the
 * user's browser to go to.</p>
 *
 * <p>
 * If the help is stored in a zip file, the URL uses the internal web server on
 * localhost. A help servlet with endpoint {@code /help} accepts the request and
 * calls {@code ConstellationHelpDisplayer.copy()} to copy the resource from the
 * zip file to the servlet's output stream.</p>
 *
 * <p>
 * If the help is stored on an external webserver, that URL is built and passed
 * to the user's browser, and Constellation plays no further part.</p>
 */
package au.gov.asd.tac.constellation.help;
