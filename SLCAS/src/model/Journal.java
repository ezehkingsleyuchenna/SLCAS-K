package model;

/**
 * Represents an academic journal in the library collection.
 */
public class Journal extends LibraryItem {

    private int    volume;
    private int    issue;
    private String doi;

    public Journal(String id, String title, String author, int year,
                   String category, int volume, int issue, String doi) {
        super(id, title, author, year, category);
        this.volume = volume;
        this.issue  = issue;
        this.doi    = doi;
    }

    @Override
    public String getType() { return "Journal"; }

    @Override
    public String getDescription() {
        return String.format("Journal: %s | Vol: %d | Issue: %d | DOI: %s",
            getTitle(), volume, issue, doi);
    }

    // Getters
    public int    getVolume() { return volume; }
    public int    getIssue()  { return issue; }
    public String getDoi()    { return doi; }

    // Setters
    public void setVolume(int volume) { this.volume = volume; }
    public void setIssue(int issue)   { this.issue = issue; }
    public void setDoi(String doi)    { this.doi = doi; }
}
