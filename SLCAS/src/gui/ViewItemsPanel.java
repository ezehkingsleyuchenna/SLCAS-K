package gui;

import controller.LibraryManager;
import model.LibraryItem;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Tab 1 – View Items.
 * Displays all library items in a JTable with a custom cell renderer that
 * colour-codes rows by availability and item type.
 */
public class ViewItemsPanel extends JPanel {

    private final LibraryManager manager;

    private JTable            itemsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterTypeCombo;
    private JLabel            countLabel;

    private static final String[] COLUMNS =
        {"ID", "Type", "Title", "Author", "Year", "Category", "Available", "Borrows"};

    // Colour scheme
    private static final Color ROW_BOOK      = new Color(230, 245, 255);
    private static final Color ROW_MAGAZINE  = new Color(230, 255, 230);
    private static final Color ROW_JOURNAL   = new Color(255, 250, 230);
    private static final Color ROW_AVAILABLE = Color.WHITE;
    private static final Color ROW_BORROWED  = new Color(255, 220, 220);
    private static final Color ROW_ALT       = new Color(245, 245, 245);
    private static final Color SEL_COLOR     = new Color(173, 216, 230);

    public ViewItemsPanel(LibraryManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buildUI();
    }

    private void buildUI() {
        // ── Toolbar ───────────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        toolbar.setBorder(BorderFactory.createEtchedBorder());

        toolbar.add(new JLabel("Filter by type:"));
        filterTypeCombo = new JComboBox<>(new String[]{"All", "Book", "Magazine", "Journal"});
        filterTypeCombo.setToolTipText("Show only items of the selected type");
        filterTypeCombo.addActionListener(e -> refresh());
        toolbar.add(filterTypeCombo);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setMnemonic('R');
        refreshBtn.setToolTipText("Reload the item list (Alt+R)");
        refreshBtn.addActionListener(e -> refresh());
        toolbar.add(refreshBtn);

        countLabel = new JLabel("Items: 0");
        countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD));
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(countLabel);

        // ── Table ─────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(tableModel);
        itemsTable.setRowHeight(22);
        itemsTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        itemsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        itemsTable.setSelectionBackground(SEL_COLOR);
        itemsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsTable.setToolTipText("Select a row to see item details");

        // Column widths
        int[] widths = {120, 70, 240, 150, 50, 120, 70, 60};
        for (int i = 0; i < widths.length; i++)
            itemsTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Custom renderer
        ItemCellRenderer renderer = new ItemCellRenderer();
        for (int i = 0; i < COLUMNS.length; i++)
            itemsTable.getColumnModel().getColumn(i).setCellRenderer(renderer);

        JScrollPane scroll = new JScrollPane(itemsTable);

        // ── Detail panel (shown on row selection) ─────────────────────────────
        JTextArea detailArea = new JTextArea(3, 60);
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailArea.setBorder(BorderFactory.createTitledBorder("Item Details"));
        detailArea.setBackground(new Color(250, 250, 250));

        itemsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && itemsTable.getSelectedRow() >= 0) {
                String id = (String) tableModel.getValueAt(itemsTable.getSelectedRow(), 0);
                LibraryItem item = manager.getDatabase().findItemById(id);
                if (item != null)
                    detailArea.setText(item.getDescription()
                        + "\nReservation queue size: "
                        + item.getReservationQueue().size());
            }
        });

        // ── Legend ────────────────────────────────────────────────────────────
        JPanel legend = buildLegend();

        // ── Layout ────────────────────────────────────────────────────────────
        add(toolbar,            BorderLayout.NORTH);
        add(scroll,             BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(4, 4));
        south.add(legend,      BorderLayout.WEST);
        south.add(new JScrollPane(detailArea), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        p.setBorder(BorderFactory.createTitledBorder("Legend"));
        addSwatch(p, ROW_BOOK,     "Book");
        addSwatch(p, ROW_MAGAZINE, "Magazine");
        addSwatch(p, ROW_JOURNAL,  "Journal");
        addSwatch(p, ROW_BORROWED, "Borrowed");
        return p;
    }

    private void addSwatch(JPanel p, Color c, String label) {
        JLabel swatch = new JLabel("  " + label);
        swatch.setOpaque(true);
        swatch.setBackground(c);
        swatch.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        swatch.setFont(swatch.getFont().deriveFont(11f));
        p.add(swatch);
    }

    /** Reload table data from the library database, respecting the type filter. */
    public void refresh() {
        tableModel.setRowCount(0);
        String filter = (String) filterTypeCombo.getSelectedItem();
        ArrayList<LibraryItem> items = manager.getDatabase().getItems();
        int count = 0;
        for (LibraryItem item : items) {
            if (!"All".equals(filter) && !item.getType().equals(filter)) continue;
            tableModel.addRow(new Object[]{
                item.getId(), item.getType(), item.getTitle(),
                item.getAuthor(), item.getYear(), item.getCategory(),
                item.isAvailable() ? "Yes" : "No",
                item.getBorrowCount()
            });
            count++;
        }
        countLabel.setText("Items: " + count);
    }

    // ── Custom cell renderer ───────────────────────────────────────────────────
    private class ItemCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String type       = (String) tableModel.getValueAt(row, 1);
                String available  = (String) tableModel.getValueAt(row, 6);
                Color rowColor;
                if ("No".equals(available))       rowColor = ROW_BORROWED;
                else if ("Book".equals(type))     rowColor = ROW_BOOK;
                else if ("Magazine".equals(type)) rowColor = ROW_MAGAZINE;
                else if ("Journal".equals(type))  rowColor = ROW_JOURNAL;
                else                              rowColor = (row % 2 == 0) ? ROW_ALT : Color.WHITE;
                c.setBackground(rowColor);
                c.setForeground(Color.BLACK);
            } else {
                c.setBackground(SEL_COLOR);
                c.setForeground(Color.BLACK);
            }
            return c;
        }
    }
}
