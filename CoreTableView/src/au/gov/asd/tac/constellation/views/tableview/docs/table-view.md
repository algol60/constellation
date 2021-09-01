# Table View

<table data-border="1">
<caption>Table View Actions</caption>
<colgroup>
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
</colgroup>
<thead>
<tr class="header">
<th scope="col">Constellation Action</th>
<th scope="col">Keyboard Shortcut</th>
<th scope="col">User Action</th>
<th scope="col">Menu Icon</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td>Open Table View</td>
<td>Ctrl + Shift + Y</td>
<td>Views -&gt; Table View</td>
<td><div style="text-align: center">
<img src="../resources/table-view.png" width="16" height="16" />
</div></td>
</tr>
</tbody>
</table>

Table View Actions

## Introduction

The Table View presents attribute data from the graph in a tabular
format. Highlighted rows in the table represent selected elements in the
graph. Selection of the table rows (including the use of the shift and
control buttons) will result in the selection of the table rows and the
associated elements in the graph itself. Conversely, changes to the
graph selection are reflected in the table.

<div style="text-align: center">

![Table View](resources/TableView.png)

</div>

## Column Sorting

Left-clicking any column header will sort the table by the values in
that column. A second click will reverse the sort order and a third
click will remove the sort. If you hold shift while you click on column
headers you can sort by multiple columns. This sorting will occur in the
order that you click the columns (as indicated by the dots and numbers).

## Column Filtering

Right-clicking any column header will open a filter dialog allowing you
to select / deselect values manually, or type something to apply a
filter to the data.

## Context Menu

Right-clicking anywhere on the table will open a context menu providing
options to copy data from the clicked cell, row or column.

## Menu Items

-   *Column Visibility* ![Column Visibility
    Button](resources/TableColumnVisibility.png) - Clicking the column
    visibility toolbar button will open a menu allowing you to customise
    which attributes are displayed in the table.
-   *Selected Only* ![Selected Only Button
    Unselected](resources/TableVisibilityAll.png) \<\> ![Selected Only
    Button Selected](resources/TableVisibilitySelectedOnly.png) -
    Clicking the selected only toolbar button will hide any elements
    which are not selected on the graph. Note that while this option is
    enabled, selection in the table will not update selection on the
    graph.
-   *Element Type* ![Transaction Element Type
    Button](resources/TableElementTypeTransactions.png) \<\> ![Node
    Element Type Button](resources/TableElementTypeNodes.png) - Clicking
    the element type toolbar button will switch between tabular views of
    transaction data (which includes the nodes at either end), or node
    data.
-   *Copy Table* ![Copy Table Button](resources/TableCopy.png) -
    Clicking on the copy toolbar button will provide you with options to
    copy the table to the system clipboard. The table will be copied
    exactly as it appears in the Table View.
-   *Export Table* ![Export Table Button](resources/TableExport.png) -
    Clicking on the export toolbar button will provide you with options
    to export the table to CSV or Excel. The table will be copied
    exactly as it appears in the Table View.
-   *Other Settings* ![Other Settings
    Button](resources/TableOtherSettings.png) - Clicking on the other
    settings toolbar button will provide you with options to load and
    save your table preferences (e.g. column ordering, column sorting)
    as well as change the size of each page in the table.