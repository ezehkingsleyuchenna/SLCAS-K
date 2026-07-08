package gui;

import controller.LibraryManager;
import model.LibraryItem;
import model.UserAccount;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * Tab 2 – Borrow / Return.
 * Allows a user to borrow or return items. Validates all input
 * and shows the user's currently borrowed items in a live table.
 */
public class BorrowPanel extends JPanel {

    private final LibraryManager manager;
    private final MainWindow     parent;

    // Borrow widgets
    private JTextField itemIdField;
    private JTextField userIdField;
    private JTextField searchField;
    private JButton    borrowBtn;
    private JButton    returnBtn;
    private JTextArea  statusArea;

    // Currently-borrowed table
    private DefaultTableModel borrowedModel;
    private JTable            borrowedTable;

    // Overdue charge calculator
    private JTextField daysField;
    private JLabel     chargeLabel;

    private static final String[] BORROW_COLS =
        {"Item ID", "Title", "Type", "User ID", "User Name"};

    public BorrowPanel(LibraryManager manager, MainWindow parent) {
        this.manager = manager;
        this.parent  = parent;
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buildUI();
    }

    private void buildUI() {
        // ── Top: quick item search ─────────────────────────────────────────────
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Quick Item Search"));
        searchField = new JTextField(20);
        searchField.setToolTipText("Type part of a title or author to find an item ID");
        JButton searchBtn = new JButton("Search");
        searchBtn.setMnemonic('S');
        searchBtn.setToolTipText("Search items by title/author (Alt+S)");
        JTextArea quickResults = new JTextArea(3, 50);
        quickResults.setEditable(false);
        quickResults.setFont(new Font("Monospaced", Font.PLAIN, 12));
        searchPanel.add(new JLabel("Query:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(new JScrollPane(quickResults));

        searchBtn.addActionListener(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) return;
            ArrayList<LibraryItem> results = manager.search(q, "all", "linear");
            if (results.isEmpty()) {
                quickResults.setText("No items found.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (LibraryItem it : results)
                    sb.append(String.format("%-15s %-40s %-10s %s%n",
                        it.getId(), it.getTitle(), it.getType(),
                        it.isAvailable() ? "[Available]" : "[Borrowed]"));
                quickResults.setText(sb.toString());
            }
        });

        // ── Centre: borrow/return form ─────────────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Borrow / Return"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 8, 5, 8);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        itemIdField = new JTextField(18);
        itemIdField.setToolTipText("Enter the exact item ID (e.g. ITEM-26-1001)");
        userIdField = new JTextField(18);
        userIdField.setToolTipText("Enter the exact user ID (e.g. USR-26-1005)");

        addRow(formPanel, gc, 0, "Item ID:", itemIdField);
        addRow(formPanel, gc, 1, "User ID:", userIdField);

        borrowBtn = new JButton("Borrow");
        borrowBtn.setMnemonic('B');
        borrowBtn.setBackground(new Color(70, 160, 70));
        borrowBtn.setForeground(Color.WHITE);
        borrowBtn.setToolTipText("Borrow the specified item for the user (Alt+B)");

        returnBtn = new JButton("Return");
        returnBtn.setMnemonic('T');
        returnBtn.setBackground(new Color(210, 90, 40));
        returnBtn.setForeground(Color.WHITE);
        returnBtn.setToolTipText("Return the specified item from the user (Alt+T)");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel.add(borrowBtn);
        btnPanel.add(returnBtn);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        formPanel.add(btnPanel, gc);

        // ── Status output ──────────────────────────────────────────────────────
        statusArea = new JTextArea(5, 60);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusArea.setBackground(new Color(245, 245, 245));
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setBorder(BorderFactory.createTitledBorder("Transaction Log"));

        // ── Bottom: currently borrowed items table ─────────────────────────────
        borrowedModel = new DefaultTableModel(BORROW_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        borrowedTable = new JTable(borrowedModel);
        borrowedTable.setRowHeight(22);
        borrowedTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        int[] bw = {120, 250, 80, 100, 150};
        for (int i = 0; i < bw.length; i++)
            borrowedTable.getColumnModel().getColumn(i).setPreferredWidth(bw[i]);

        JScrollPane borrowedScroll = new JScrollPane(borrowedTable);
        borrowedScroll.setBorder(BorderFactory.createTitledBorder("Currently Borrowed Items"));
        borrowedScroll.setPreferredSize(new Dimension(600, 160));

        // ── Overdue charge calculator ──────────────────────────────────────────
        JPanel overduePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        overduePanel.setBorder(BorderFactory.createTitledBorder("Overdue Charge Calculator"));
        daysField   = new JTextField(5);
        chargeLabel = new JLabel("$0.00");
        chargeLabel.setFont(chargeLabel.getFont().deriveFont(Font.BOLD, 14f));
        chargeLabel.setForeground(new Color(180, 0, 0));
        JButton calcBtn = new JButton("Calculate");
        calcBtn.setToolTipText("Compute overdue charge recursively at $0.50/day");
        calcBtn.addActionListener(e -> {
            try {
                int days = Integer.parseInt(daysField.getText().trim());
                if (days < 0) throw new NumberFormatException();
                double charge = manager.calculateOverdueCharge(days);
                chargeLabel.setText(String.format("$%.2f", charge));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    "Please enter a valid non-negative number of days.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        overduePanel.add(new JLabel("Days overdue:"));
        overduePanel.add(daysField);
        overduePanel.add(calcBtn);
        overduePanel.add(new JLabel("Charge:"));
        overduePanel.add(chargeLabel);

        // ── Event listeners ────────────────────────────────────────────────────
        borrowBtn.addActionListener(e -> handleBorrow());
        returnBtn.addActionListener(e -> handleReturn());

        // Allow Enter in fields to trigger borrow
        itemIdField.addActionListener(e -> userIdField.requestFocus());
        userIdField.addActionListener(e -> handleBorrow());

        // ── Assemble layout ────────────────────────────────────────────────────
        JPanel topHalf = new JPanel(new BorderLayout(4, 4));
        topHalf.add(searchPanel, BorderLayout.NORTH);
        topHalf.add(formPanel,   BorderLayout.CENTER);
        topHalf.add(statusScroll, BorderLayout.SOUTH);

        JPanel bottomHalf = new JPanel(new BorderLayout(4, 4));
        bottomHalf.add(borrowedScroll, BorderLayout.CENTER);
        bottomHalf.add(overduePanel,   BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topHalf, bottomHalf);
        split.setResizeWeight(0.55);
        split.setDividerSize(6);

        add(split, BorderLayout.CENTER);
        refreshBorrowedTable();
    }

    // ── Helper: add labelled field row ─────────────────────────────────────────
    private void addRow(JPanel p, GridBagConstraints gc,
                        int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
        p.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1;
        p.add(field, gc);
    }

    // ── Borrow action ──────────────────────────────────────────────────────────
    private void handleBorrow() {
        String itemId = itemIdField.getText().trim();
        String userId = userIdField.getText().trim();
        if (!validateInputs(itemId, userId)) return;

        String result = manager.borrowItem(itemId, userId);
        log(result);
        if (result.startsWith("SUCCESS") || result.startsWith("QUEUED")) {
            clearFields();
            parent.refreshAll();
        }
        parent.updateStatus(result);
    }

    // ── Return action ──────────────────────────────────────────────────────────
    private void handleReturn() {
        String itemId = itemIdField.getText().trim();
        String userId = userIdField.getText().trim();
        if (!validateInputs(itemId, userId)) return;

        String result = manager.returnItem(itemId, userId);
        log(result);
        if (result.startsWith("SUCCESS")) {
            clearFields();
            parent.refreshAll();
        }
        parent.updateStatus(result);
    }

    // ── Validation ─────────────────────────────────────────────────────────────
    private boolean validateInputs(String itemId, String userId) {
        if (itemId.isEmpty() || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Both Item ID and User ID are required.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void log(String msg) {
        statusArea.append(msg + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    private void clearFields() {
        itemIdField.setText("");
        userIdField.setText("");
    }

    /** Rebuild the currently-borrowed table from the live database. */
    public void refreshBorrowedTable() {
        borrowedModel.setRowCount(0);
        for (UserAccount user : manager.getDatabase().getUsers()) {
            for (String iid : user.getBorrowedItems()) {
                LibraryItem it = manager.getDatabase().findItemById(iid);
                String title = (it != null) ? it.getTitle() : "(unknown)";
                String type  = (it != null) ? it.getType()  : "?";
                borrowedModel.addRow(new Object[]{iid, title, type,
                    user.getUserId(), user.getName()});
            }
        }
    }

    public void refresh() { refreshBorrowedTable(); }
}
