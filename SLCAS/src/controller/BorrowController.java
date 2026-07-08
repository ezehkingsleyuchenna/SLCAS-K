package controller;

import model.LibraryDatabase;
import model.LibraryItem;
import model.UserAccount;

import java.util.ArrayList;
import java.util.Queue;

/**
 * Handles all borrow and return transactions.
 * Delegates recursive charge computation to SearchEngine.
 */
public class BorrowController {

    private final LibraryDatabase database;
    private final SearchEngine    searchEngine;

    public BorrowController(LibraryDatabase database) {
        this.database     = database;
        this.searchEngine = new SearchEngine();
    }

    /**
     * Borrow an item for a user.
     * If the item is unavailable the user is added to its reservation queue.
     *
     * @return a human-readable status string prefixed with SUCCESS / QUEUED / FAILED
     */
    public String borrowItem(String itemId, String userId) {
        LibraryItem  item = database.findItemById(itemId);
        UserAccount  user = database.findUserById(userId);

        if (item == null) return "FAILED: Item not found – " + itemId;
        if (user == null) return "FAILED: User not found – " + userId;

        if (item.isAvailable()) {
            item.borrow(user);
            return "SUCCESS: \"" + item.getTitle() + "\" borrowed by " + user.getName();
        } else {
            item.addToReservationQueue(user);
            return "QUEUED: Item unavailable. " + user.getName()
                 + " added to reservation queue for \"" + item.getTitle() + "\"";
        }
    }

    /**
     * Return an item from a user.
     * If there is a pending reservation, automatically re-borrows to that user.
     */
    public String returnItem(String itemId, String userId) {
        LibraryItem item = database.findItemById(itemId);
        UserAccount user = database.findUserById(userId);

        if (item == null) return "FAILED: Item not found – " + itemId;
        if (user == null) return "FAILED: User not found – " + userId;

        if (!user.getBorrowedItems().contains(itemId))
            return "FAILED: This item is not currently borrowed by " + user.getName();

        item.returnItem(user);
        user.removeOverdueItem(itemId); // clear any overdue flag on return

        String msg = "SUCCESS: \"" + item.getTitle() + "\" returned by " + user.getName();
        String next = processReservationQueue(item);
        if (next != null) msg += " | AUTO-BORROWED → " + next;
        return msg;
    }

    /** Poll the reservation queue and auto-borrow to the next waiting user. */
    private String processReservationQueue(LibraryItem item) {
        Queue<UserAccount> queue = item.getReservationQueue();
        if (!queue.isEmpty()) {
            UserAccount nextUser = queue.poll();
            item.borrow(nextUser);
            return nextUser.getName();
        }
        return null;
    }

    /**
     * Recursive overdue charge: $0.50 per day.
     */
    public double calculateOverdueCharge(int daysOverdue) {
        return searchEngine.computeOverdueCharge(daysOverdue);
    }

    /** Returns a list of all users who have at least one overdue item. */
    public ArrayList<UserAccount> getUsersWithOverdue() {
        ArrayList<UserAccount> result = new ArrayList<>();
        for (UserAccount u : database.getUsers()) {
            if (!u.getOverdueItems().isEmpty()) result.add(u);
        }
        return result;
    }
}
