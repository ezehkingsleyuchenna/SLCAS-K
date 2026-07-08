package model;

import java.util.Queue;

/**
 * Interface defining borrowable behaviour for library items.
 */
public interface Borrowable {
    boolean borrow(UserAccount user);
    boolean returnItem(UserAccount user);
    Queue<UserAccount> getReservationQueue();
    void addToReservationQueue(UserAccount user);
}
