package tools.sctrade.companion.gui;

import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import tools.sctrade.companion.domain.notification.NotificationRepository;
import tools.sctrade.companion.utils.LocalizationUtil;

/**
 * The logs tab for the companion GUI. This is where users can see the notifications.
 *
 * @see NotificationRepository
 */
public class LogsTab extends JPanel {
  private static final long serialVersionUID = 5664549029232335333L;

  private DefaultTableModel model;

  /**
   * Creates a new instance of the logs tab.
   */
  public LogsTab() {
    super(new GridLayout());

    buildModel();
    var table = buildTable();
    add(new JScrollPane(table));
  }

  /**
   * Adds a log to the table.
   *
   * @param row The row to add.
   */
  public void addLog(Object[] row) {
    model.addRow(row);
  }

  private DefaultTableModel buildModel() {
    model = new DefaultTableModel() {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    model.addColumn(LocalizationUtil.get("tableColumnTime"));
    model.addColumn(LocalizationUtil.get("tableColumnType"));
    model.addColumn(LocalizationUtil.get("tableColumnMessage"));

    return model;
  }

  private JTable buildTable() {
    var table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    table.getColumnModel().getColumn(0).setMaxWidth(150);
    table.getColumnModel().getColumn(1).setMaxWidth(50);

    TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
    table.setRowSorter(sorter);
    sorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(0, SortOrder.DESCENDING)));

    return table;
  }
}
