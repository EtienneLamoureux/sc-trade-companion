package tools.sctrade.companion.swing;

import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import tools.sctrade.companion.utils.LocalizationUtil;

public class LogsTab extends JPanel {
  private static final long serialVersionUID = 5664549029232335333L;

  private DefaultTableModel model;

  public LogsTab() {
    super(new GridLayout());

    buildModel();
    var table = buildTable();
    add(new JScrollPane(table));
  }

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

    return table;
  }
}
