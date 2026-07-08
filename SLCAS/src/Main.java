import controller.LibraryManager;
import gui.MainWindow;
import model.*;
import utils.IDGenerator;

import javax.swing.*;

/**
 * Entry point for the Smart Library Circulation & Automation System.
 *
 * Bootstraps the LibraryManager, loads persisted data (if any),
 * seeds sample data on first run, then launches the Swing GUI on the EDT.
 */
public class Main {

    public static void main(String[] args) {
        // Set system look-and-feel for native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { /* fall back to Metal */ }

        LibraryManager manager = new LibraryManager();

        // Attempt to load persisted data; seed sample data if catalogue is empty
        manager.loadData("data");
        if (manager.getDatabase().getItems().isEmpty()) {
            seedSampleData(manager);
        }

        // Launch GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new MainWindow(manager));
    }

    // ── Sample data for demonstration ──────────────────────────────────────────
    private static void seedSampleData(LibraryManager manager) {

        // ── Books ─────────────────────────────────────────────────────────────
        manager.addItem(new Book(manager.generateNewItemId(),
            "Introduction to Algorithms", "Cormen, Leiserson & Rivest", 2022,
            "Computer Science", "978-0262046305", "Textbook", 1312));
        manager.addItem(new Book(manager.generateNewItemId(),
            "Clean Code", "Robert C. Martin", 2008,
            "Software Engineering", "978-0132350884", "Professional", 431));
        manager.addItem(new Book(manager.generateNewItemId(),
            "The Pragmatic Programmer", "Hunt & Thomas", 2019,
            "Software Engineering", "978-0135957059", "Professional", 352));
        manager.addItem(new Book(manager.generateNewItemId(),
            "Thinking, Fast and Slow", "Daniel Kahneman", 2011,
            "Psychology", "978-0374533557", "Non-Fiction", 499));
        manager.addItem(new Book(manager.generateNewItemId(),
            "Sapiens: A Brief History of Humankind", "Yuval Noah Harari", 2015,
            "History", "978-0062316097", "Non-Fiction", 443));
        manager.addItem(new Book(manager.generateNewItemId(),
            "Design Patterns", "Gang of Four", 1994,
            "Computer Science", "978-0201633610", "Reference", 395));
        manager.addItem(new Book(manager.generateNewItemId(),
            "Calculus Early Transcendentals", "James Stewart", 2020,
            "Mathematics", "978-1337613927", "Textbook", 1376));
        manager.addItem(new Book(manager.generateNewItemId(),
            "University Physics", "Young & Freedman", 2019,
            "Physics", "978-0135159552", "Textbook", 1600));

        // ── Magazines ─────────────────────────────────────────────────────────
        manager.addItem(new Magazine(manager.generateNewItemId(),
            "Nature", "Nature Publishing Group", 2026,
            "Science", 629, "Nature Publishing Group"));
        manager.addItem(new Magazine(manager.generateNewItemId(),
            "Scientific American", "Scientific American Editors", 2026,
            "Science", 334, "Springer Nature"));
        manager.addItem(new Magazine(manager.generateNewItemId(),
            "IEEE Spectrum", "IEEE Editorial", 2026,
            "Engineering", 63, "IEEE"));
        manager.addItem(new Magazine(manager.generateNewItemId(),
            "National Geographic", "NG Editorial", 2025,
            "Geography", 248, "National Geographic Society"));

        // ── Journals ──────────────────────────────────────────────────────────
        manager.addItem(new Journal(manager.generateNewItemId(),
            "Journal of Artificial Intelligence Research", "JAIR Editorial Board", 2026,
            "Computer Science", 78, 1, "10.1613/jair.1.18000"));
        manager.addItem(new Journal(manager.generateNewItemId(),
            "ACM Computing Surveys", "ACM Editorial", 2026,
            "Computer Science", 57, 2, "10.1145/3700000"));
        manager.addItem(new Journal(manager.generateNewItemId(),
            "The Lancet", "Lancet Editorial Board", 2026,
            "Medicine", 407, 10, "10.1016/S0140-6736(26)00001-5"));
        manager.addItem(new Journal(manager.generateNewItemId(),
            "Physical Review Letters", "APS Editorial", 2026,
            "Physics", 136, 5, "10.1103/PhysRevLett.136.050001"));

        // ── Users ─────────────────────────────────────────────────────────────
        manager.addUser(new UserAccount(manager.generateNewUserId(),
            "Alice Johnson", "alice.johnson@university.edu", "08012345678"));
        manager.addUser(new UserAccount(manager.generateNewUserId(),
            "Bob Okafor",    "bob.okafor@university.edu",    "08023456789"));
        manager.addUser(new UserAccount(manager.generateNewUserId(),
            "Chioma Eze",    "chioma.eze@university.edu",    "08034567890"));
        manager.addUser(new UserAccount(manager.generateNewUserId(),
            "David Smith",   "david.smith@university.edu",   "08045678901"));
        manager.addUser(new UserAccount(manager.generateNewUserId(),
            "Emeka Nwosu",   "emeka.nwosu@university.edu",   "08056789012"));

        // Simulate a few borrows to give borrow-count data to reports
        String[] itemIds = new String[4];
        for (int i = 0; i < 4; i++)
            itemIds[i] = manager.getDatabase().getItems().get(i).getId();
        String userId0 = manager.getDatabase().getUsers().get(0).getUserId();
        String userId1 = manager.getDatabase().getUsers().get(1).getUserId();

        manager.borrowItem(itemIds[0], userId0);
        manager.borrowItem(itemIds[1], userId1);
        manager.borrowItem(itemIds[2], userId0);

        // Auto-save the seeded data
        manager.saveData("data");
    }
}
