package gui;

import controller.LibraryManager;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Main application window.
 *
 * Features:
 *   • JMenuBar with keyboard shortcuts / mnemonics
 *   • JTabbedPane with four panels (View Items, Borrow/Return, Admin, Search & Sort)
 *   • Status bar at the bottom
 *   • javax.swing.Timer that checks for overdue items every 30 seconds
 *   • Tooltips throughout
 *   • CardLayout-based splash → main transition (demonstrates CardLayout)
 */
public class MainWindow extends JFrame {

    private final LibraryManager manager;

    // Panels
    private ViewItemsPanel  viewItemsPanel;
    private BorrowPanel     borrowPanel;
    private AdminPanel      adminPanel;
    private SearchSortPanel searchSortPanel;

    // Status bar
    private JLabel statusLabel;
    private JLabel clockLabel;

    // Timer for overdue notifications (fires every 30 s)
    private javax.swing.Timer overdueTimer;

    // CardLayout root (splash + main)
    private CardLayout rootCard;
    private JPanel     rootPanel;

    // ── Constructor ────────────────────────────────────────────────────────────
    public MainWindow(LibraryManager manager) {
        super("Smart Library Circulation & Automation System  (SLCAS)");
        this.manager = manager;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setIconImage(createIcon());

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { handleExit(); }
        });

        buildRoot();
        initMenu();
        initStatusBar();
        initOverdueTimer();
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ROOT CARD LAYOUT  (Splash → Main)
    // ══════════════════════════════════════════════════════════════════════════
    private void buildRoot() {
        rootCard  = new CardLayout();
        rootPanel = new JPanel(rootCard);

        // Splash card
        JPanel splash = buildSplashPanel();

        // Main card (tabbed pane)
        JPanel main = buildMainPanel();

        rootPanel.add(splash, "SPLASH");
        rootPanel.add(main,   "MAIN");
        add(rootPanel, BorderLayout.CENTER);
        rootCard.show(rootPanel, "SPLASH");
    }

    private JPanel buildSplashPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(20, 40, 80));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Smart Library Circulation");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("& Automation System  (SLCAS)");
        sub.setFont(new Font("SansSerif", Font.ITALIC, 18));
        sub.setForeground(new Color(180, 210, 255));

        JLabel univ = new JLabel("University Library Management");
        univ.setFont(new Font("SansSerif", Font.PLAIN, 14));
        univ.setForeground(new Color(140, 180, 220));

        JButton enterBtn = new JButton("  Enter System  ");
        enterBtn.setFont(enterBtn.getFont().deriveFont(Font.BOLD, 16f));
        enterBtn.setBackground(new Color(255, 200, 50));
        enterBtn.setForeground(new Color(20, 40, 80));
        enterBtn.setFocusPainted(false);
        enterBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        enterBtn.setToolTipText("Click to open the library system");
        enterBtn.addActionListener(e -> rootCard.show(rootPanel, "MAIN"));

        gc.gridx = 0; gc.gridy = 0; p.add(title,    gc);
        gc.gridy = 1;               p.add(sub,       gc);
        gc.gridy = 2;               p.add(univ,      gc);
        gc.gridy = 3; gc.insets = new Insets(30, 10, 10, 10);
        p.add(enterBtn, gc);
        return p;
    }

    private JPanel buildMainPanel() {
        viewItemsPanel  = new ViewItemsPanel(manager);
        borrowPanel     = new BorrowPanel(manager, this);
        adminPanel      = new AdminPanel(manager, this);
        searchSortPanel = new SearchSortPanel(manager);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("View Items",     loadIcon("view"), viewItemsPanel,  "Browse all library items");
        tabs.addTab("Borrow / Return",loadIcon("borrow"), borrowPanel,   "Borrow or return items");
        tabs.addTab("Admin",          loadIcon("admin"),  adminPanel,    "Manage items, users and data");
        tabs.addTab("Search & Sort",  loadIcon("search"), searchSortPanel,"Search and sort the catalogue");

        // Keyboard shortcut: Ctrl+1..4 to switch tabs
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, InputEvent.CTRL_DOWN_MASK);
            tabs.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "tab" + i);
            tabs.getActionMap().put("tab" + i, new AbstractAction() {
                public void actionPerformed(ActionEvent e) { tabs.setSelectedIndex(idx); }
            });
        }

        JPanel main = new JPanel(new BorderLayout());
        main.add(tabs, BorderLayout.CENTER);
        return main;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MENU BAR
    // ══════════════════════════════════════════════════════════════════════════
    private void initMenu() {
        JMenuBar mb = new JMenuBar();

        // ── File ──────────────────────────────────────────────────────────────
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        JMenuItem saveItem = item("Save Data",    KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        JMenuItem loadItem = item("Load Data",    KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK);
        JMenuItem exitItem = item("Exit",         KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK);

        saveItem.setToolTipText("Save all data to ./data/ (Ctrl+S)");
        loadItem.setToolTipText("Load data from ./data/ (Ctrl+L)");

        saveItem.addActionListener(e -> {
            manager.saveData("data");
            updateStatus("Data saved to ./data/");
        });
        loadItem.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                "Replace current data with saved data?", "Confirm Load",
                JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) { manager.loadData("data"); refreshAll(); }
        });
        exitItem.addActionListener(e -> handleExit());

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // ── Edit ──────────────────────────────────────────────────────────────
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');

        JMenuItem undoItem = item("Undo Last Delete", KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);
        undoItem.setToolTipText("Restore the last admin-deleted item (Ctrl+Z)");
        undoItem.addActionListener(e -> {
            LibraryItem r = manager.undoLastRemoval();
            if (r != null) { refreshAll(); updateStatus("Restored: " + r.getTitle()); }
            else JOptionPane.showMessageDialog(this,
                "Nothing to undo.", "Undo", JOptionPane.INFORMATION_MESSAGE);
        });
        editMenu.add(undoItem);

        // ── View ──────────────────────────────────────────────────────────────
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        JMenuItem refreshItem = item("Refresh All", KeyEvent.VK_F5, 0);
        refreshItem.addActionListener(e -> refreshAll());
        viewMenu.add(refreshItem);

        // ── Reports ───────────────────────────────────────────────────────────
        JMenu reportMenu = new JMenu("Reports");
        reportMenu.setMnemonic('R');
        JMenuItem rBorrow   = new JMenuItem("Most Borrowed");
        JMenuItem rOverdue  = new JMenuItem("Overdue Users");
        JMenuItem rCategory = new JMenuItem("Category Distribution");
        rBorrow.addActionListener(e ->
            showReport("Most Borrowed", manager.generateMostBorrowedReport()));
        rOverdue.addActionListener(e ->
            showReport("Overdue Users", manager.generateOverdueReport()));
        rCategory.addActionListener(e ->
            showReport("Category Distribution", manager.generateCategoryReport()));
        reportMenu.add(rBorrow);
        reportMenu.add(rOverdue);
        reportMenu.add(rCategory);

        // ── Help ──────────────────────────────────────────────────────────────
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        JMenuItem aboutItem = new JMenuItem("About SLCAS");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        mb.add(fileMenu);
        mb.add(editMenu);
        mb.add(viewMenu);
        mb.add(reportMenu);
        mb.add(helpMenu);
        setJMenuBar(mb);
    }

    private JMenuItem item(String text, int keyCode, int modifiers) {
        JMenuItem mi = new JMenuItem(text);
        if (keyCode != 0)
            mi.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
        return mi;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  STATUS BAR
    // ══════════════════════════════════════════════════════════════════════════
    private void initStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        bar.setBackground(new Color(240, 240, 240));

        statusLabel = new JLabel("Welcome to SLCAS – University Library Automation System");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        clockLabel.setForeground(Color.DARK_GRAY);
        updateClock();

        // Update clock every second
        new javax.swing.Timer(1000, e -> updateClock()).start();

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(clockLabel,  BorderLayout.EAST);
        add(bar, BorderLayout.SOUTH);
    }

    private void updateClock() {
        clockLabel.setText(new java.text.SimpleDateFormat("EEE dd-MMM-yyyy  HH:mm:ss")
            .format(new java.util.Date()) + "  ");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  OVERDUE TIMER  (fires every 30 seconds)
    // ══════════════════════════════════════════════════════════════════════════
    private void initOverdueTimer() {
        overdueTimer = new javax.swing.Timer(30_000, e -> checkOverdue());
        overdueTimer.start();
    }

    private void checkOverdue() {
        ArrayList<model.UserAccount> overdueUsers =
            manager.getBorrowController().getUsersWithOverdue();
        if (!overdueUsers.isEmpty()) {
            StringBuilder msg = new StringBuilder("OVERDUE REMINDER\n\nThe following users have overdue items:\n");
            for (model.UserAccount u : overdueUsers)
                msg.append("  • ").append(u.getName())
                   .append(" (").append(u.getUserId()).append(")\n");
            JOptionPane.showMessageDialog(this, msg.toString(),
                "Overdue Notification", JOptionPane.WARNING_MESSAGE);
            updateStatus("⚠ Overdue items detected for " + overdueUsers.size() + " user(s)");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PUBLIC API USED BY CHILD PANELS
    // ══════════════════════════════════════════════════════════════════════════

    public void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /** Refresh every panel so all tables stay in sync after any data change. */
    public void refreshAll() {
        viewItemsPanel.refresh();
        borrowPanel.refresh();
        adminPanel.refresh();
        searchSortPanel.refresh();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private void handleExit() {
        int r = JOptionPane.showConfirmDialog(this,
            "Save data before exiting?", "Exit",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.CANCEL_OPTION) return;
        if (r == JOptionPane.YES_OPTION)    manager.saveData("data");
        overdueTimer.stop();
        dispose();
        System.exit(0);
    }

    private void showReport(String title, String content) {
        JTextArea area = new JTextArea(content);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        area.setPreferredSize(new Dimension(560, 340));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
            title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        String msg =
            "Smart Library Circulation & Automation System\n" +
            "Version 1.0  –  2026\n\n" +
            "Features:\n" +
            "  • OOP class hierarchy (LibraryItem → Book / Magazine / Journal)\n" +
            "  • ArrayList, Queue (reservations), Stack (undo), Array cache\n" +
            "  • Linear, Binary & Recursive search\n" +
            "  • Selection, Insertion, Merge & Quick sort\n" +
            "  • Recursive overdue charge & category count\n" +
            "  • Java Swing GUI with event-driven programming\n" +
            "  • JSON file persistence\n\n" +
            "Keyboard shortcuts:\n" +
            "  Ctrl+S – Save   Ctrl+L – Load   Ctrl+Z – Undo\n" +
            "  Ctrl+1..4 – Switch tabs   Ctrl+Q – Exit";
        JOptionPane.showMessageDialog(this, msg, "About SLCAS", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Create a simple programmatic icon (blue book silhouette). */
    private Image createIcon() {
        java.awt.image.BufferedImage img =
            new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(30, 60, 140));
        g.fillRoundRect(4, 2, 24, 28, 4, 4);
        g.setColor(Color.WHITE);
        g.fillRect(7, 8, 18, 2);
        g.fillRect(7, 14, 18, 2);
        g.fillRect(7, 20, 12, 2);
        g.dispose();
        return img;
    }

    /** Return a small coloured dot icon for the tab; falls back to null gracefully. */
    private Icon loadIcon(String name) {
        java.awt.image.BufferedImage img =
            new java.awt.image.BufferedImage(12, 12, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color c;
        switch (name) {
            case "view":   c = new Color(50, 130, 200); break;
            case "borrow": c = new Color(50, 160, 70);  break;
            case "admin":  c = new Color(200, 100, 30); break;
            default:       c = new Color(140, 60, 180);
        }
        g.setColor(c);
        g.fillOval(1, 1, 10, 10);
        g.dispose();
        return new ImageIcon(img);
    }
}
