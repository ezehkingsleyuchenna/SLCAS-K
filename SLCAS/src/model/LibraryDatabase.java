package model;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Central data store (composition root).
 * Contains the item catalogue, registered users, an undo stack for admin
 * deletions, and a fixed-size cache of most-frequently-accessed items.
 */
public class LibraryDatabase {

    private static final int CACHE_SIZE = 5;

    private ArrayList<LibraryItem> items;
    private ArrayList<UserAccount> users;
    private Stack<LibraryItem>     undoStack;          // undo last deletion
    private LibraryItem[]          frequentAccessCache; // fixed array cache

    public LibraryDatabase() {
        items               = new ArrayList<>();
        users               = new ArrayList<>();
        undoStack           = new Stack<>();
        frequentAccessCache = new LibraryItem[CACHE_SIZE];
    }

    // ── Item CRUD ──────────────────────────────────────────────────────────────
    public void addItem(LibraryItem item) {
        items.add(item);
    }

    /** Remove by ID and push onto undo stack; returns true on success. */
    public boolean removeItem(String itemId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(itemId)) {
                undoStack.push(items.remove(i));
                return true;
            }
        }
        return false;
    }

    /** Pop the undo stack and re-add the last-removed item. */
    public LibraryItem undoLastRemoval() {
        if (!undoStack.isEmpty()) {
            LibraryItem restored = undoStack.pop();
            items.add(restored);
            return restored;
        }
        return null;
    }

    public LibraryItem findItemById(String id) {
        for (LibraryItem item : items) {
            if (item.getId().equals(id)) return item;
        }
        return null;
    }

    // ── User CRUD ──────────────────────────────────────────────────────────────
    public void addUser(UserAccount user) { users.add(user); }

    public UserAccount findUserById(String userId) {
        for (UserAccount u : users) {
            if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }

    public boolean removeUser(String userId) {
        return users.removeIf(u -> u.getUserId().equals(userId));
    }

    // ── Frequent access cache ──────────────────────────────────────────────────
    /** Rebuild the fixed-size cache from current borrow counts. */
    public void updateFrequentCache() {
        ArrayList<LibraryItem> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> b.getBorrowCount() - a.getBorrowCount());
        for (int i = 0; i < CACHE_SIZE; i++) {
            frequentAccessCache[i] = (i < sorted.size()) ? sorted.get(i) : null;
        }
    }

    public LibraryItem[] getFrequentAccessCache() {
        updateFrequentCache();
        return frequentAccessCache;
    }

    // ── Accessors ──────────────────────────────────────────────────────────────
    public ArrayList<LibraryItem> getItems()              { return items; }
    public ArrayList<UserAccount> getUsers()              { return users; }
    public Stack<LibraryItem>     getUndoStack()          { return undoStack; }
    public boolean                canUndo()               { return !undoStack.isEmpty(); }
}
