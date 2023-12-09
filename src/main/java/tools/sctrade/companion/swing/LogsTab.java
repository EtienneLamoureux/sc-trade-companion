package tools.sctrade.companion.swing;

import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import tools.sctrade.companion.utils.LocalizationUtil;

public class LogsTab extends JPanel {
  private static final long serialVersionUID = 5664549029232335333L;

  public LogsTab() {
    super(new GridLayout());

    var model = buildModel();
    var table = buildTable(model);

    add(new JScrollPane(table));

    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "Error", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-02", "Error", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-03", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-01", "INFO", "Bonjour"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "Error", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-09", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-10", "Error", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-08", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-07", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-06", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-05", "INFO", "Screenshot processed"});
    model.addRow(new Object[] {"2023-12-04", "INFO", "Screenshot processed"});
  }

  private DefaultTableModel buildModel() {
    var model = new DefaultTableModel() {
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

  private JTable buildTable(DefaultTableModel model) {
    var table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    table.getColumnModel().getColumn(0).setMaxWidth(150);
    table.getColumnModel().getColumn(1).setMaxWidth(50);

    return table;
  }
}
