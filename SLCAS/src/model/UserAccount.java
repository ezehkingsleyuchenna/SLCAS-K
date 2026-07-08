package model;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Represents a library user account.
 * Uses a Stack to maintain borrowing history (most-recent on top).
 */
public class UserAccount {

    private String              userId;
    private String              name;
    private String              email;
    private String              phone;
    private ArrayList<String>   borrowedItems;     // currently held item IDs
    private Stack<String>       borrowingHistory;  // stack of previously borrowed item IDs
    private ArrayList<String>   overdueItems;      // item IDs flagged as overdue

    public UserAccount(String userId, String name, String email, String phone) {
        this.userId          = userId;
        this.name            = name;
        this.email           = email;
        this.phone           = phone;
        this.borrowedItems   = new ArrayList<>();
        this.borrowingHistory = new Stack<>();
        this.overdueItems    = new ArrayList<>();
    }

    // ── Borrow history helpers ─────────────────────────────────────────────────
    public void addBorrowedItem(String itemId) {
        if (!borrowedItems.contains(itemId)) borrowedItems.add(itemId);
    }

    public void removeBorrowedItem(String itemId) {
        borrowedItems.remove(itemId);
    }

    /** Push returned item onto the history stack. */
    public void addToHistory(String itemId) {
        borrowingHistory.push(itemId);
    }

    /** Peek at the most recently returned item without removing it. */
    public String getLastBorrowed() {
        return borrowingHistory.isEmpty() ? null : borrowingHistory.peek();
    }

    // ── Overdue helpers ────────────────────────────────────────────────────────
    public void addOverdueItem(String itemId) {
        if (!overdueItems.contains(itemId)) overdueItems.add(itemId);
    }

    public void removeOverdueItem(String itemId) {
        overdueItems.remove(itemId);
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    public String             getUserId()         { return userId; }
    public String             getName()           { return name; }
    public String             getEmail()          { return email; }
    public String             getPhone()          { return phone; }
    public ArrayList<String>  getBorrowedItems()  { return borrowedItems; }
    public Stack<String>      getBorrowingHistory(){ return borrowingHistory; }
    public ArrayList<String>  getOverdueItems()   { return overdueItems; }

    // ── Setters ────────────────────────────────────────────────────────────────
    public void setName(String name)   { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return String.format("User[%s]: %s <%s> | Borrowed: %d",
            userId, name, email, borrowedItems.size());
    }
}
