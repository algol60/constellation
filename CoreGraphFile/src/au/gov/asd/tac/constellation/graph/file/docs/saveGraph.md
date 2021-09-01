## Save Graph

Saving the state of a Constellation graph for later retrieval can be
done at any time. After saving a graph, the user may completely quit the
application and resume where they left off at another time. Saving is
also useful to guard against data loss in the event of a system crash.

The Constellation saves the graph to a proprietary format. A saved
Constellation file retains the graph layout and selections, attribute
definitions and their values, and just about everything else. The menu
option below initiates an operation that records the contents of the
window in a text file:

-   *File → Save*
-   *File → Save As*
-   or press the save button in the tool bar.

The "Save" command will prompt the user for a file name the first time
the file is being saved. Otherwise, it will use the current name.

The "Save As" command can be used with new or existing graphs to
explicitly save the information to a specific file.

When specifying a file name, the user will be asked to confirm the
operation if the file already exists.

The application will not allow the user to specify an existing filename
for another currently open graph.

The file generated by the save operations are assigned the .star suffix
by default.

**Note:** When a file has changes pending, its name (in the upper tab)
appear bolded in blue, otherwise it is shown in simple black text.