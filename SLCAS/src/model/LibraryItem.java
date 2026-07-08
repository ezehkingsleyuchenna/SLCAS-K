package model;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Abstract base class for all library items.
 * Implements the Borrowable interface and provides common fields/behaviour.
 */
public abstract class LibraryItem implements Borrowable {

    private String id;
    private String title;
    private String author;
    private int year;
    private String category;
    private boolean available;
    private int borrowCount;
    private Queue<UserAccount> reservationQueue;

    public LibraryItem(String id, String title, String author, int year, String category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.category = category;
        this.available = true;
        this.borrowCount = 0;
        this.reservationQueue = new LinkedList<>();
    }

    // ── Abstract template methods ──────────────────────────────────────────────
    public abstract String getType();
    public abstract String getDescription();

    // ── Borrowable implementation ──────────────────────────────────────────────
    @Override
    public boolean borrow(UserAccount user) {
        if (available) {
            available = false;
            borrowCount++;
            user.addBorrowedItem(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean returnItem(UserAccount user) {
        if (!available) {
            available = true;
            user.removeBorrowedItem(id);
            user.addToHistory(id);
            return true;
        }
        return false;
    }

    @Override
    public Queue<UserAccount> getReservationQueue() { return reservationQueue; }

    @Override
    public void addToReservationQueue(UserAccount user) { reservationQueue.add(user); }

    // ── Getters ────────────────────────────────────────────────────────────────
    public String getId()           { return id; }
    public String getTitle()        { return title; }
    public String getAuthor()       { return author; }
    public int    getYear()         { return year; }
    public String getCategory()     { return category; }
    public boolean isAvailable()    { return available; }
    public int    getBorrowCount()  { return borrowCount; }

    // ── Setters ────────────────────────────────────────────────────────────────
    public void setTitle(String title)          { this.title = title; }
    public void setAuthor(String author)        { this.author = author; }
    public void setYear(int year)               { this.year = year; }
    public void setCategory(String category)    { this.category = category; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setBorrowCount(int count)       { this.borrowCount = count; }
    public void incrementBorrowCount()          { this.borrowCount++; }

    @Override
    public String toString() {
        return String.format("[%s] %s by %s (%d) - %s",
            getType(), title, author, year, available ? "Available" : "Borrowed");
    }
}
