package gui;

import controller.LibraryManager;
import model.LibraryItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * Tab 4 – Search & Sort.
 * Provides all four search algorithms and all four sort algorithms
 * through GUI controls. Results are shown in a live JTable.
 */
public class SearchSortPanel extends JPanel {

    private final LibraryManager manager;

    // ── Search widgets ─────────────────────────────────────────────────────────
    private JTextField        searchField;
    private JComboBox<String> fieldCombo;
    private JComboBox<String> searchAlgoCombo;
    private JButton           searchBtn;
    private JLabel            searchResultCount;

    // ── Sort widgets ───────────────────────────────────────────────────────────
    private JComboBox<String> sortFieldCombo;
    private JComboBox<String> sortAlgoCombo;
    private JButton           sortBtn;
    private JLabel            sortTimeLabel;

    // ── Results table ──────────────────────────────────────────────────────────
    private DefaultTableModel resultsModel;
    private JTable            resultsTable;

    private static final String[] COLS =
        {"ID", "Type", "Title", "Author", "Year", "Category", "Available", "Borrows"};

    public SearchSortPanel(LibraryManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buildUI();
    }

    private void buildUI() {
        // ── Search panel ───────────────────────────────────────────────────────
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        searchField     = new JTextField(22);
        fieldCombo      = new JComboBox<>(new String[]{"all", "title", "author", "type", "category"});
        searchAlgoCombo = new JComboBox<>(new String[]{"linear", "binary", "recursive"});
        searchBtn       = new JButton("Search");
        searchResultCount = new JLabel("Results: 0");

        searchField.setToolTipText("Enter search text");
        fieldCombo.setToolTipText("Field to search in");
        searchAlgoCombo.setToolTipText(
            "linear – scans all items; binary – sorted title exact match; recursive – recursive traversal");
        searchBtn.setMnemonic('S');
        searchBtn.setToolTipText("Run the search (Alt+S)");
        searchBtn.setBackground(new Color(50, 100, 200));
        searchBtn.setForeground(Color.WHITE);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0; searchPanel.add(new JLabel("Query:"),     gc);
        gc.gridx = 1;               gc.weightx = 1; searchPanel.add(searchField,               gc);
        gc.gridx = 2;               gc.weightx = 0; searchPanel.add(new JLabel("Field:"),      gc);
        gc.gridx = 3;                               searchPanel.add(fieldCombo,                gc);
        gc.gridx = 4;                               searchPanel.add(new JLabel("Algorithm:"),  gc);
        gc.gridx = 5;                               searchPanel.add(searchAlgoCombo,           gc);
        gc.gridx = 6;                               searchPanel.add(searchBtn,                 gc);
        gc.gridx = 7;                               searchPanel.add(searchResultCount,         gc);

        // Disable field combo when binary is selected (always searches title)
        searchAlgoCombo.addActionListener(e -> {
            boolean isBinary = "binary".equals(searchAlgoCombo.getSelectedItem());
            fieldCombo.setEnabled(!isBinary);
            if (isBinary) fieldCombo.setSelectedItem("title");
        });

        // ── Sort panel ─────────────────────────────────────────────────────────
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        sortPanel.setBorder(BorderFactory.createTitledBorder("Sort"));

        sortFieldCombo = new JComboBox<>(new String[]{"title", "author", "year"});
        sortAlgoCombo  = new JComboBox<>(new String[]{
            "Merge Sort", "Quick Sort", "Selection Sort", "Insertion Sort"});
        sortBtn        = new JButton("Sort All Items");
        sortTimeLabel  = new JLabel("Sort time: –");

        sortFieldCombo.setToolTipText("Sort by this field");
        sortAlgoCombo.setToolTipText("Sorting algorithm to use");
        sortBtn.setMnemonic('O');
        sortBtn.setToolTipText("Sort the entire catalogue and display below (Alt+O)");
        sortBtn.setBackground(new Color(120, 60, 160));
        sortBtn.setForeground(Color.WHITE);

        sortPanel.add(new JLabel("Sort by:"));
        sortPanel.add(sortFieldCombo);
        sortPanel.add(new JLabel("Algorithm:"));
        sortPanel.add(sortAlgoCombo);
        sortPanel.add(sortBtn);
        sortPanel.add(sortTimeLabel);

        // ── Algorithm info panel ───────────────────────────────────────────────
        JTextArea algoInfo = new JTextArea(3, 40);
        algoInfo.setEditable(false);
        algoInfo.setFont(new Font("SansSerif", Font.ITALIC, 11));
        algoInfo.setBackground(new Color(255, 255, 220));
        algoInfo.setBorder(BorderFactory.createTitledBorder("Algorithm Info"));
        algoInfo.setText(
            "Search algorithms: Linear O(n) – any order; " +
            "Binary O(log n) – sorted list, exact title; " +
            "Recursive O(n) – recursive traversal.\n" +
            "Sort algorithms: Merge Sort O(n log n); Quick Sort O(n log n) avg; " +
            "Selection Sort O(n²); Insertion Sort O(n²) worst / O(n) best.");

        // ── Results table ──────────────────────────────────────────────────────
        resultsModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        resultsTable = new JTable(resultsModel);
        resultsTable.setRowHeight(22);
        resultsTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resultsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        int[] widths = {120, 70, 240, 150, 50, 120, 70, 60};
        for (int i = 0; i < widths.length; i++)
            resultsTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Custom renderer: highlight availability
        resultsTable.setDefaultRenderer(Object.class, (table, value, isSelected, hasFocus, row, col) -> {
            JLabel lbl = new JLabel(value != null ? value.toString() : "");
            lbl.setOpaque(true);
            if (isSelected) {
                lbl.setBackground(new Color(173, 216, 230));
            } else {
                String avail = (String) resultsModel.getValueAt(row, 6);
                lbl.setBackground("No".equals(avail) ? new Color(255, 210, 210) :
                    (row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245)));
            }
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            return lbl;
        });

        JScrollPane scroll = new JScrollPane(resultsTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Results"));

        // ── Events ────────────────────────────────────────────────────────────
        searchBtn.addActionListener(e -> handleSearch());
        sortBtn.addActionListener(e -> handleSort());
        searchField.addActionListener(e -> handleSearch());

        // ── Layout ────────────────────────────────────────────────────────────
        JPanel topPanel = new JPanel(new BorderLayout(4, 4));
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(sortPanel,   BorderLayout.CENTER);
        topPanel.add(algoInfo,    BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scroll,   BorderLayout.CENTER);

        showAll();
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) { showAll(); return; }

        String field     = (String) fieldCombo.getSelectedItem();
        String algorithm = (String) searchAlgoCombo.getSelectedItem();

        long start = System.nanoTime();
        ArrayList<LibraryItem> results = manager.search(query, field, algorithm);
        long elapsed = System.nanoTime() - start;

        populateTable(results);
        searchResultCount.setText(String.format("Results: %d  (%.2f ms)",
            results.size(), elapsed / 1_000_000.0));
    }

    private void handleSort() {
        String field     = (String) sortFieldCombo.getSelectedItem();
        String algorithm = (String) sortAlgoCombo.getSelectedItem();

        long start = System.nanoTime();
        ArrayList<LibraryItem> sorted = manager.sort(field, algorithm);
        long elapsed = System.nanoTime() - start;

        populateTable(sorted);
        sortTimeLabel.setText(String.format("Sort time: %.2f ms", elapsed / 1_000_000.0));
        searchResultCount.setText("Results: " + sorted.size());
    }

    private void showAll() {
        populateTable(manager.getDatabase().getItems());
        searchResultCount.setText("Results: " + manager.getDatabase().getItems().size());
    }

    private void populateTable(ArrayList<LibraryItem> items) {
        resultsModel.setRowCount(0);
        for (LibraryItem item : items) {
            resultsModel.addRow(new Object[]{
                item.getId(), item.getType(), item.getTitle(),
                item.getAuthor(), item.getYear(), item.getCategory(),
                item.isAvailable() ? "Yes" : "No",
                item.getBorrowCount()
            });
        }
    }

    public void refresh() { showAll(); }
}
