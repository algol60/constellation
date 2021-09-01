# Add and Selection Modes

## Add Mode

Select the ![Add Mode Icon](resources/add.png) icon to switch to add
mode. This mode allows the user to manually create nodes and
transactions.

<table data-border="1">
<caption>Creating the Graph in Add Mode using the Mouse</caption>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th scope="col">Graph Action</th>
<th scope="col">Mouse Actions</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Create a Node</td>
<td>Left-click on the background</td>
</tr>
<tr class="even">
<td>Create a Transaction</td>
<td><ol>
<li>Left-click on a node</li>
<li>Left-click on another node (or same node for a loop)</li>
</ol></td>
</tr>
<tr class="odd">
<td>Create a Transaction Sequence</td>
<td><ol>
<li>Left-click on a node</li>
<li>Ctrl-left-click on another node (or same node for a loop). A second transaction will automatically be created starting from that node.</li>
<li>Repeat step 2 until final transaction.</li>
<li>Left-click on another node (or same node for a loop)</li>
</ol></td>
</tr>
<tr class="even">
<td>Create a Series of Transactions Starting From the Same Node</td>
<td><ol>
<li>Left-click on a node</li>
<li>Shift-left-click on another node (or same node for a loop). A second transaction will automatically be created starting from the original node.</li>
<li>Repeat step 2 until final transaction.</li>
<li>Left-click on another node (or same node for a loop)</li>
</ol></td>
</tr>
</tbody>
</table>

Creating the Graph in Add Mode using the Mouse

NOTE: While creating a transaction, you can left-click on the background
or press ESC to abort creating it.

When creating transactions, you can choose whether to generate directed
or undirected transactions by selecting either the ![Directed
Transactions Icon](resources/directed.png) icon (for directed) or the
![Undirected Transactions Icon](resources/undirected.png) icon (for
undirected). By default, directed will be selected.

## Selection Mode

Select the ![Selection Mode Icon](resources/select.png) icon to switch
to selection mode. This mode allows the user to select elements in the
graph and perform graphical operations such as zoom, pan, and rotate.

Navigating the graph in selection mode using the mouse is done as
follows (In general the left button is for selecting, the middle button
for rotating, and the right button for dragging and panning):

<table data-border="1">
<caption>Navigating the Graph in Selection Mode using the Mouse</caption>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th scope="col">Mouse Action</th>
<th scope="col">Graph Action</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Left-click on an element</td>
<td>Select a node or transaction.</td>
</tr>
<tr class="even">
<td>Left-click on the background</td>
<td>Set focus on the graph window.</td>
</tr>
<tr class="odd">
<td>Double-left-click on the background</td>
<td>Deselect all elements</td>
</tr>
<tr class="even">
<td>Shift-left-click on an element</td>
<td>Select additional nodes and/or transactions.</td>
</tr>
<tr class="odd">
<td>Ctrl-left-click on an element</td>
<td>Toggle selection of nodes and/or transactions.</td>
</tr>
<tr class="even">
<td>Left-drag</td>
<td>Select multiple nodes and/or transactions.</td>
</tr>
<tr class="odd">
<td>Shift-left-drag</td>
<td>Select additional multiple nodes and/or transactions.</td>
</tr>
<tr class="even">
<td>Ctrl-left-drag</td>
<td>Toggle selection of multiple nodes and/or transactions.</td>
</tr>
<tr class="odd">
<td>Middle-drag</td>
<td>Rotate the graph.
<ul>
<li>Dragging the mouse up and down the middle of the window will rotate around the X axis.</li>
<li>Dragging the mouse across the middle of the window will rotate around the Y axis.</li>
<li>Dragging the mouse along the edges of the window will rotate around the Z axis.</li>
</ul></td>
</tr>
<tr class="even">
<td>Right-drag on background</td>
<td>Pan the graph.</td>
</tr>
<tr class="odd">
<td>Right-drag on node</td>
<td>Drag the node and other selected nodes.</td>
</tr>
<tr class="even">
<td>Right-drag on transaction</td>
<td>Pan the graph.</td>
</tr>
</tbody>
</table>

Navigating the Graph in Selection Mode using the Mouse