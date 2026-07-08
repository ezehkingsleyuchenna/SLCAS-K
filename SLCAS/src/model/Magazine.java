package model;

/**
 * Represents a periodical magazine in the library collection.
 */
public class Magazine extends LibraryItem {

    private int    issueNumber;
    private String publisher;

    public Magazine(String id, String title, String author, int year,
                    String category, int issueNumber, String publisher) {
        super(id, title, author, year, category);
        this.issueNumber = issueNumber;
        this.publisher   = publisher;
    }

    @Override
    public String getType() { return "Magazine"; }

    @Override
    public String getDescription() {
        return String.format("Magazine: %s | Issue: %d | Publisher: %s",
            getTitle(), issueNumber, publisher);
    }

    // Getters
    public int    getIssueNumber() { return issueNumber; }
    public String getPublisher()   { return publisher; }

    // Setters
    public void setIssueNumber(int issueNumber) { this.issueNumber = issueNumber; }
    public void setPublisher(String publisher)  { this.publisher = publisher; }
}
