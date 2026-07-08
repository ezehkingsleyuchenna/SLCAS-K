package gui;

import controller.LibraryManager;
import model.*;
import utils.IDGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Tab 3 – Admin Panel.
 * Provides facilities to:
 *   • Add Books, Magazines, and Journals (dynamic fields via CardLayout)
 *   • Delete items and undo the last deletion (Stack)
 *   • Manage user accounts
 *   • Import / export data via a JFileChooser
 *   • View simple system reports
 */
public class AdminPanel extends JPanel {

    private final LibraryManager manager;
    private final MainWindow     parent;

    // ── Add-item form ──────────────────────────────────────────────────────────
    private JComboBox<String> typeCombo;
    private JTextField        titleField, authorField, yearField, categoryField;

    // CardLayout panels for type-specific fields
    private JPanel            cardPanel;
    private CardLayout        cardLayout;
    private JTextField        isbnField, genreField, pagesField;
    private JTextField        issueNumField, publisherField;
    private JTextField        volumeField, journalIssueField, doiField;

    // ── Users table ───────────────────────────────────────────────────────────
    private DefaultTableModel userModel;
    private JTable            usersTable;

    // ── Items admin table ─────────────────────────────────────────────────────
    private DefaultTableModel adminItemModel;
    private JTable            adminItemTable;

    // ── Buttons ───────────────────────────────────────────────────────────────
    private JButton addItemBtn, deleteItemBtn, undoBtn;
    private JButton addUserBtn, deleteUserBtn;
    private JButton saveBtn, loadBtn, exportBtn;
    private JButton reportBorrowedBtn, reportOverdueBtn, reportCategoryBtn;

    // ── User form ──────────────────────────────────────────────────────────────
    private JTextField uNameField, uEmailField, uPhoneField;

    private static final String[] ITEM_ADMIN_COLS = {"ID", "Type", "Title", "Author", "Available"};
    private static final String[] USER_COLS       = {"User ID", "Name", "Email", "Phone", "Borrowed"};

    public AdminPanel(LibraryManager manager, MainWindow parent) {
        this.manager = manager;
        this.parent  = parent;
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buildUI();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Items",   buildItemsAdminTab());
        tabs.addTab("Users",   buildUsersTab());
        tabs.addTab("Reports", buildReportsTab());
        tabs.addTab("Data I/O",buildDataIOTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ITEMS SUB-TAB
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildItemsAdminTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));

        // ── Add form (north) ──────────────────────────────────────────────────
        JPanel addForm = new JPanel(new BorderLayout(4, 4));
        addForm.setBorder(BorderFactory.createTitledBorder("Add New Item"));

        // Common fields
        JPanel commonFields = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 6, 3, 6);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        titleField    = new JTextField(20);
        authorField   = new JTextField(20);
        yearField     = new JTextField(6);
        categoryField = new JTextField(15);
        typeCombo     = new JComboBox<>(new String[]{"Book", "Magazine", "Journal"});

        titleField.setToolTipText("Enter the item title");
        authorField.setToolTipText("Enter the author/editor name");
        yearField.setToolTipText("4-digit publication year");
        categoryField.setToolTipText("e.g. Science, History, Technology");

        addGbRow(commonFields, gc, 0, "Type:",     typeCombo);
        addGbRow(commonFields, gc, 1, "Title:",    titleField);
        addGbRow(commonFields, gc, 2, "Author:",   authorField);
        addGbRow(commonFields, gc, 3, "Year:",     yearField);
        addGbRow(commonFields, gc, 4, "Category:", categoryField);

        // Type-specific card panels
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBorder(BorderFactory.createEtchedBorder());

        isbnField   = new JTextField(15);
        genreField  = new JTextField(12);
        pagesField  = new JTextField(6);
        JPanel bookCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bookCard.add(new JLabel("ISBN:")); bookCard.add(isbnField);
        bookCard.add(new JLabel("Genre:")); bookCard.add(genreField);
        bookCard.add(new JLabel("Pages:")); bookCard.add(pagesField);

        issueNumField  = new JTextField(6);
        publisherField = new JTextField(15);
        JPanel magCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        magCard.add(new JLabel("Issue #:")); magCard.add(issueNumField);
        magCard.add(new JLabel("Publisher:")); magCard.add(publisherField);

        volumeField       = new JTextField(6);
        journalIssueField = new JTextField(6);
        doiField          = new JTextField(20);
        JPanel journalCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        journalCard.add(new JLabel("Volume:")); journalCard.add(volumeField);
        journalCard.add(new JLabel("Issue:"));  journalCard.add(journalIssueField);
        journalCard.add(new JLabel("DOI:"));    journalCard.add(doiField);

        cardPanel.add(bookCard,    "Book");
        cardPanel.add(magCard,     "Magazine");
        cardPanel.add(journalCard, "Journal");

        typeCombo.addActionListener(e -> {
            cardLayout.show(cardPanel, (String) typeCombo.getSelectedItem());
        });

        addItemBtn = new JButton("Add Item");
        addItemBtn.setMnemonic('A');
        addItemBtn.setBackground(new Color(50, 130, 200));
        addItemBtn.setForeground(Color.WHITE);
        addItemBtn.setToolTipText("Add the new item to the catalogue (Alt+A)");
        addItemBtn.addActionListener(e -> handleAddItem());

        JPanel addBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        addBtnRow.add(addItemBtn);

        addForm.add(commonFields, BorderLayout.CENTER);
        addForm.add(cardPanel,    BorderLayout.EAST);
        addForm.add(addBtnRow,    BorderLayout.SOUTH);

        // ── Items table (centre) ──────────────────────────────────────────────
        adminItemModel = new DefaultTableModel(ITEM_ADMIN_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        adminItemTable = new JTable(adminItemModel);
        adminItemTable.setRowHeight(22);
        adminItemTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        int[] w = {120, 75, 260, 150, 70};
        for (int i = 0; i < w.length; i++)
            adminItemTable.getColumnModel().getColumn(i).setPreferredWidth(w[i]);

        JScrollPane itemScroll = new JScrollPane(adminItemTable);
        itemScroll.setBorder(BorderFactory.createTitledBorder("Current Catalogue"));

        // ── Delete / Undo toolbar ─────────────────────────────────────────────
        deleteItemBtn = new JButton("Delete Selected");
        deleteItemBtn.setMnemonic('D');
        deleteItemBtn.setForeground(new Color(160, 0, 0));
        deleteItemBtn.setToolTipText("Delete the selected item (Alt+D)");
        deleteItemBtn.addActionListener(e -> handleDeleteItem());

        undoBtn = new JButton("Undo Last Delete");
        undoBtn.setMnemonic('U');
        undoBtn.setToolTipText("Restore the last deleted item (Alt+U)");
        undoBtn.setEnabled(false);
        undoBtn.addActionListener(e -> handleUndo());

        JPanel deleteBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        deleteBtnRow.add(deleteItemBtn);
        deleteBtnRow.add(undoBtn);

        panel.add(addForm,      BorderLayout.NORTH);
        panel.add(itemScroll,   BorderLayout.CENTER);
        panel.add(deleteBtnRow, BorderLayout.SOUTH);

        refreshAdminItemTable();
        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  USERS SUB-TAB
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));

        // ── Add user form ─────────────────────────────────────────────────────
        JPanel userForm = new JPanel(new GridBagLayout());
        userForm.setBorder(BorderFactory.createTitledBorder("Register New User"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        uNameField  = new JTextField(18);
        uEmailField = new JTextField(20);
        uPhoneField = new JTextField(14);

        addGbRow(userForm, gc, 0, "Name:",  uNameField);
        addGbRow(userForm, gc, 1, "Email:", uEmailField);
        addGbRow(userForm, gc, 2, "Phone:", uPhoneField);

        addUserBtn = new JButton("Register User");
        addUserBtn.setBackground(new Color(50, 130, 200));
        addUserBtn.setForeground(Color.WHITE);
        addUserBtn.setToolTipText("Create a new user account");
        addUserBtn.addActionListener(e -> handleAddUser());

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.EAST;
        userForm.add(addUserBtn, gc);

        // ── Users table ───────────────────────────────────────────────────────
        userModel = new DefaultTableModel(USER_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(userModel);
        usersTable.setRowHeight(22);
        usersTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        int[] uw = {110, 150, 180, 110, 65};
        for (int i = 0; i < uw.length; i++)
            usersTable.getColumnModel().getColumn(i).setPreferredWidth(uw[i]);

        JScrollPane userScroll = new JScrollPane(usersTable);
        userScroll.setBorder(BorderFactory.createTitledBorder("Registered Users"));

        deleteUserBtn = new JButton("Delete Selected User");
        deleteUserBtn.setForeground(new Color(160, 0, 0));
        deleteUserBtn.setToolTipText("Remove the selected user account");
        deleteUserBtn.addActionListener(e -> handleDeleteUser());

        panel.add(userForm,     BorderLayout.NORTH);
        panel.add(userScroll,   BorderLayout.CENTER);
        panel.add(deleteUserBtn, BorderLayout.SOUTH);

        refreshUsersTable();
        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REPORTS SUB-TAB
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);

        reportBorrowedBtn  = new JButton("Most Borrowed");
        reportOverdueBtn   = new JButton("Overdue Users");
        reportCategoryBtn  = new JButton("Category Distribution");

        reportBorrowedBtn.setToolTipText("Show top 10 most-borrowed items");
        reportOverdueBtn.setToolTipText("Show all users with overdue items");
        reportCategoryBtn.setToolTipText("Show item count per category (uses recursive count)");

        reportBorrowedBtn.addActionListener(e ->
            reportArea.setText(manager.generateMostBorrowedReport()));
        reportOverdueBtn.addActionListener(e ->
            reportArea.setText(manager.generateOverdueReport()));
        reportCategoryBtn.addActionListener(e ->
            reportArea.setText(manager.generateCategoryReport()));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnRow.add(reportBorrowedBtn);
        btnRow.add(reportOverdueBtn);
        btnRow.add(reportCategoryBtn);

        // Show cache of most-frequently accessed items
        JPanel cachePanel = new JPanel(new BorderLayout());
        JTextArea cacheArea = new JTextArea(4, 60);
        cacheArea.setEditable(false);
        cacheArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        cacheArea.setBackground(new Color(245, 255, 245));
        JButton refreshCacheBtn = new JButton("Refresh Cache");
        refreshCacheBtn.setToolTipText("Update the top-5 most-accessed items cache");
        refreshCacheBtn.addActionListener(e -> {
            model.LibraryItem[] cache = manager.getDatabase().getFrequentAccessCache();
            StringBuilder sb = new StringBuilder("Top-5 Frequent Access Cache:\n");
            for (int i = 0; i < cache.length; i++) {
                if (cache[i] != null)
                    sb.append(String.format("  %d. %-40s borrows: %d%n",
                        i + 1, cache[i].getTitle(), cache[i].getBorrowCount()));
            }
            cacheArea.setText(sb.toString());
        });
        cachePanel.setBorder(BorderFactory.createTitledBorder("Most Frequently Accessed (Cache)"));
        cachePanel.add(refreshCacheBtn,        BorderLayout.NORTH);
        cachePanel.add(new JScrollPane(cacheArea), BorderLayout.CENTER);

        panel.add(btnRow,                       BorderLayout.NORTH);
        panel.add(new JScrollPane(reportArea),  BorderLayout.CENTER);
        panel.add(cachePanel,                   BorderLayout.SOUTH);
        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DATA I/O SUB-TAB
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel buildDataIOTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        saveBtn   = new JButton("Save Data to Default Location");
        loadBtn   = new JButton("Load Data from Default Location");
        exportBtn = new JButton("Choose Export / Import Folder…");

        saveBtn.setToolTipText("Save items and users to ./data/ directory");
        loadBtn.setToolTipText("Load items and users from ./data/ directory");
        exportBtn.setToolTipText("Choose a custom folder for save / load via file chooser");

        saveBtn.setBackground(new Color(50, 130, 60));
        saveBtn.setForeground(Color.WHITE);
        loadBtn.setBackground(new Color(80, 100, 180));
        loadBtn.setForeground(Color.WHITE);

        saveBtn.addActionListener(e -> {
            manager.saveData("data");
            JOptionPane.showMessageDialog(this,
                "Data saved to ./data/ successfully.",
                "Save", JOptionPane.INFORMATION_MESSAGE);
            parent.updateStatus("Data saved to ./data/");
        });

        loadBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                "Loading will replace all current data. Continue?",
                "Confirm Load", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                manager.loadData("data");
                parent.refreshAll();
                JOptionPane.showMessageDialog(this,
                    "Data loaded from ./data/ successfully.",
                    "Load", JOptionPane.INFORMATION_MESSAGE);
                parent.updateStatus("Data loaded from ./data/");
            }
        });

        exportBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(".");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogTitle("Select Data Folder");
            if (fc.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
                File dir = fc.getSelectedFile();
                String[] options = {"Save to this folder", "Load from this folder", "Cancel"};
                int choice = JOptionPane.showOptionDialog(this,
                    "Folder: " + dir.getAbsolutePath(),
                    "Save or Load?", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (choice == 0) {
                    manager.saveData(dir.getAbsolutePath());
                    parent.updateStatus("Data saved to " + dir.getName());
                } else if (choice == 1) {
                    manager.loadData(dir.getAbsolutePath());
                    parent.refreshAll();
                    parent.updateStatus("Data loaded from " + dir.getName());
                }
            }
        });

        gc.gridx = 0; gc.gridy = 0; panel.add(saveBtn,   gc);
        gc.gridy = 1;               panel.add(loadBtn,   gc);
        gc.gridy = 2;               panel.add(exportBtn, gc);

        JLabel hint = new JLabel("<html><i>Default location: ./data/ relative to the working directory.</i></html>");
        gc.gridy = 3; panel.add(hint, gc);

        return panel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ACTION HANDLERS
    // ══════════════════════════════════════════════════════════════════════════

    private void handleAddItem() {
        // Validate common fields
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String yearStr = yearField.getText().trim();
        String category = categoryField.getText().trim();

        if (title.isEmpty() || author.isEmpty() || yearStr.isEmpty() || category.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Title, Author, Year, and Category are all required.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
            if (year < 1000 || year > 9999) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Year must be a 4-digit number.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String type = (String) typeCombo.getSelectedItem();
        String id   = manager.generateNewItemId();
        LibraryItem item;

        try {
            switch (type) {
                case "Book": {
                    String isbn  = isbnField.getText().trim();
                    String genre = genreField.getText().trim();
                    int pages    = Integer.parseInt(pagesField.getText().trim());
                    item = new Book(id, title, author, year, category, isbn, genre, pages);
                    break;
                }
                case "Magazine": {
                    int issueNum     = Integer.parseInt(issueNumField.getText().trim());
                    String publisher = publisherField.getText().trim();
                    item = new Magazine(id, title, author, year, category, issueNum, publisher);
                    break;
                }
                default: { // Journal
                    int vol      = Integer.parseInt(volumeField.getText().trim());
                    int iss      = Integer.parseInt(journalIssueField.getText().trim());
                    String doi   = doiField.getText().trim();
                    item = new Journal(id, title, author, year, category, vol, iss, doi);
                    break;
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Numeric fields (Pages / Issue # / Volume / Issue) must be valid integers.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        manager.addItem(item);
        clearItemForm();
        parent.refreshAll();
        parent.updateStatus("Added: " + item.getTitle() + " [" + id + "]");
        JOptionPane.showMessageDialog(this,
            "Item added successfully!\nID: " + id,
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDeleteItem() {
        int row = adminItemTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an item from the table first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String itemId = (String) adminItemModel.getValueAt(row, 0);
        String title  = (String) adminItemModel.getValueAt(row, 2);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete \"" + title + "\" (" + itemId + ")?\nThis can be undone immediately.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.removeItem(itemId);
            undoBtn.setEnabled(true);
            parent.refreshAll();
            parent.updateStatus("Deleted item: " + title);
        }
    }

    private void handleUndo() {
        LibraryItem restored = manager.undoLastRemoval();
        if (restored != null) {
            parent.refreshAll();
            parent.updateStatus("Restored: " + restored.getTitle());
            JOptionPane.showMessageDialog(this,
                "Restored: \"" + restored.getTitle() + "\"",
                "Undo Successful", JOptionPane.INFORMATION_MESSAGE);
        }
        undoBtn.setEnabled(manager.canUndo());
    }

    private void handleAddUser() {
        String name  = uNameField.getText().trim();
        String email = uEmailField.getText().trim();
        String phone = uPhoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Name and Email are required.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid email address.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = manager.generateNewUserId();
        UserAccount user = new UserAccount(id, name, email, phone);
        manager.addUser(user);
        uNameField.setText(""); uEmailField.setText(""); uPhoneField.setText("");
        refreshUsersTable();
        parent.updateStatus("Registered user: " + name + " [" + id + "]");
        JOptionPane.showMessageDialog(this,
            "User registered!\nID: " + id, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDeleteUser() {
        int row = usersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a user from the table first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String userId = (String) userModel.getValueAt(row, 0);
        String name   = (String) userModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete user \"" + name + "\" (" + userId + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.removeUser(userId);
            refreshUsersTable();
            parent.updateStatus("Deleted user: " + name);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REFRESH
    // ══════════════════════════════════════════════════════════════════════════

    public void refreshAdminItemTable() {
        adminItemModel.setRowCount(0);
        for (LibraryItem item : manager.getDatabase().getItems()) {
            adminItemModel.addRow(new Object[]{
                item.getId(), item.getType(), item.getTitle(),
                item.getAuthor(), item.isAvailable() ? "Yes" : "No"
            });
        }
        undoBtn.setEnabled(manager.canUndo());
    }

    public void refreshUsersTable() {
        userModel.setRowCount(0);
        for (UserAccount u : manager.getDatabase().getUsers()) {
            userModel.addRow(new Object[]{
                u.getUserId(), u.getName(), u.getEmail(),
                u.getPhone(), u.getBorrowedItems().size()
            });
        }
    }

    public void refresh() {
        refreshAdminItemTable();
        refreshUsersTable();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private void addGbRow(JPanel p, GridBagConstraints gc,
                          int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0;
        p.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1;
        p.add(comp, gc);
    }

    private void clearItemForm() {
        titleField.setText(""); authorField.setText("");
        yearField.setText("");  categoryField.setText("");
        isbnField.setText(""); genreField.setText(""); pagesField.setText("");
        issueNumField.setText(""); publisherField.setText("");
        volumeField.setText(""); journalIssueField.setText(""); doiField.setText("");
    }
}
