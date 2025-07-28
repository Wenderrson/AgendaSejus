/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agenda;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author 01200193296
 */
public class TableColumnAdjuster {
    
    private final JTable table;

    public TableColumnAdjuster(JTable table) {
        this.table = table;
    }

    public void adjustColumns() {
        for (int col = 0; col < table.getColumnCount(); col++) {
            adjustColumn(col);
        }
    }

    private void adjustColumn(int columnIndex) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        Component comp;
        int maxWidth = 0;

        // Cabeçalho
        TableCellRenderer headerRenderer = column.getHeaderRenderer();
        if (headerRenderer == null) {
            headerRenderer = table.getTableHeader().getDefaultRenderer();
        }
        comp = headerRenderer.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, 0, columnIndex);
        maxWidth = comp.getPreferredSize().width;

        // Linhas
        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer cellRenderer = table.getCellRenderer(row, columnIndex);
            comp = cellRenderer.getTableCellRendererComponent(
                    table,
                    table.getValueAt(row, columnIndex),
                    false,
                    false,
                    row,
                    columnIndex
            );
            maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
        }

        column.setPreferredWidth(maxWidth + 15); // margem extra para espaçamento
    }
}

    

