package controller;

import model.*;
import utils.FileHandler;
import utils.IDGenerator;

import java.util.*;

/**
 * Facade / coordinator for the entire library system.
 * All GUI panels interact only with this class.
 */
public class LibraryManager {

    private final LibraryDatabase  database;
    private final SearchEngine     searchEngine;
    private final BorrowController borrowController;
    private final FileHandler      fileHandler;

    public LibraryManager() {
        database         = new LibraryDatabase();
        searchEngine     = new SearchEngine();
        borrowController = new BorrowController(database);
        fileHandler      = new FileHandler();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ITEM MANAGEMENT
    // ══════════════════════════════════════════════════════════════════════════

    public void addItem(LibraryItem item) { database.addItem(item); }

    /**
     * Remove item by ID. The removed item is pushed onto the undo stack.
     * @return true if found and removed.
     */
    public boolean removeItem(String itemId) { return database.removeItem(itemId); }

    /** Restore the last admin-deleted item. Returns null if stack is empty. */
    public LibraryItem undoLastRemoval() { return database.undoLastRemoval(); }

    public boolean canUndo() { return database.canUndo(); }

    // ══════════════════════════════════════════════════════════════════════════
    //  USER MANAGEMENT
    // ══════════════════════════════════════════════════════════════════════════

    public void addUser(UserAccount user) { database.addUser(user); }

    public boolean removeUser(String userId) { return database.removeUser(userId); }

    // ══════════════════════════════════════════════════════════════════════════
    //  SEARCH (delegates to SearchEngine)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Search using the chosen algorithm.
     * @param query     search text
     * @param field     title | author | type | category | all
     * @param algorithm linear | binary | recursive
     */
    public ArrayList<LibraryItem> search(String query, String field, String algorithm) {
        ArrayList<LibraryItem> items = database.getItems();
        switch (algorithm.toLowerCase()) {
            case "binary": {
                // Binary search requires ascending-title sort first
                ArrayList<LibraryItem> sorted = new ArrayList<>(items);
                searchEngine.insertionSort(sorted,
                    Comparator.comparing(i -> i.getTitle().toLowerCase()));
                LibraryItem found = searchEngine.binarySearch(sorted, query);
                ArrayList<LibraryItem> res = new ArrayList<>();
                if (found != null) res.add(found);
                return res;
            }
            case "recursive":
                return searchEngine.recursiveSearch(items, query, field, 0);
            default: // linear
                return searchEngine.linearSearch(items, query, field);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SORTING (delegates to SearchEngine)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Sort and return a new list using the chosen field + algorithm.
     * @param field     title | author | year
     * @param algorithm Selection Sort | Insertion Sort | Merge Sort | Quick Sort
     */
    public ArrayList<LibraryItem> sort(String field, String algorithm) {
        ArrayList<LibraryItem> items = new ArrayList<>(database.getItems());
        Comparator<LibraryItem> cmp;
        switch (field.toLowerCase()) {
            case "author": cmp = Comparator.comparing(i -> i.getAuthor().toLowerCase()); break;
            case "year":   cmp = Comparator.comparingInt(LibraryItem::getYear); break;
            default:       cmp = Comparator.comparing(i -> i.getTitle().toLowerCase());
        }
        switch (algorithm) {
            case "Selection Sort": searchEngine.selectionSort(items, cmp); break;
            case "Insertion Sort": searchEngine.insertionSort(items, cmp); break;
            case "Quick Sort":     searchEngine.quickSort(items, cmp, 0, items.size() - 1); break;
            default:               items = searchEngine.mergeSort(items, cmp); // Merge Sort
        }
        return items;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BORROW / RETURN
    // ══════════════════════════════════════════════════════════════════════════

    public String borrowItem(String itemId, String userId) {
        return borrowController.borrowItem(itemId, userId);
    }

    public String returnItem(String itemId, String userId) {
        return borrowController.returnItem(itemId, userId);
    }

    public double calculateOverdueCharge(int days) {
        return borrowController.calculateOverdueCharge(days);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REPORTS
    // ══════════════════════════════════════════════════════════════════════════

    public String generateMostBorrowedReport() {
        ArrayList<LibraryItem> items = new ArrayList<>(database.getItems());
        searchEngine.selectionSort(items, (a, b) -> b.getBorrowCount() - a.getBorrowCount());
        StringBuilder sb = new StringBuilder("=== MOST BORROWED ITEMS ===\n\n");
        int count = Math.min(10, items.size());
        for (int i = 0; i < count; i++) {
            LibraryItem it = items.get(i);
            sb.append(String.format("%2d. %-40s [%s]  borrows: %d%n",
                i + 1, it.getTitle(), it.getType(), it.getBorrowCount()));
        }
        return sb.toString();
    }

    public String generateOverdueReport() {
        ArrayList<UserAccount> overdueUsers = borrowController.getUsersWithOverdue();
        StringBuilder sb = new StringBuilder("=== USERS WITH OVERDUE ITEMS ===\n\n");
        if (overdueUsers.isEmpty()) {
            sb.append("No overdue items found.\n");
        } else {
            for (UserAccount u : overdueUsers) {
                sb.append(String.format("%-20s (%s)  overdue: %s%n",
                    u.getName(), u.getUserId(), u.getOverdueItems()));
            }
        }
        return sb.toString();
    }

    public String generateCategoryReport() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (LibraryItem item : database.getItems())
            map.merge(item.getCategory(), 1, Integer::sum);

        // Also count via recursive helper for demonstration
        StringBuilder sb = new StringBuilder("=== CATEGORY DISTRIBUTION ===\n\n");
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            int recursive = searchEngine.countByCategory(database.getItems(), e.getKey(), 0);
            sb.append(String.format("%-25s : %d items (recursive count: %d)%n",
                e.getKey(), e.getValue(), recursive));
        }
        sb.append(String.format("%nTotal items (recursive): %d%n",
            searchEngine.totalCount(database.getItems(), 0)));
        return sb.toString();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PERSISTENCE
    // ══════════════════════════════════════════════════════════════════════════

    public void saveData(String directory) {
        fileHandler.saveItems(database.getItems(), directory + "/items.json");
        fileHandler.saveUsers(database.getUsers(), directory + "/users.json");
    }

    public void loadData(String directory) {
        // Clear current data before loading
        database.getItems().clear();
        database.getUsers().clear();
        fileHandler.loadItems(directory + "/items.json").forEach(database::addItem);
        fileHandler.loadUsers(directory + "/users.json").forEach(database::addUser);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ID GENERATION
    // ══════════════════════════════════════════════════════════════════════════

    public String generateNewItemId() { return IDGenerator.generateId("ITEM"); }
    public String generateNewUserId() { return IDGenerator.generateId("USR"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  ACCESSORS
    // ══════════════════════════════════════════════════════════════════════════

    public LibraryDatabase  getDatabase()         { return database; }
    public SearchEngine     getSearchEngine()      { return searchEngine; }
    public BorrowController getBorrowController()  { return borrowController; }
}
