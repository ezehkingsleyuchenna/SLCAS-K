package model;

/**
 * Represents a physical book in the library collection.
 */
public class Book extends LibraryItem {

    private String isbn;
    private String genre;
    private int    pages;

    public Book(String id, String title, String author, int year,
                String category, String isbn, String genre, int pages) {
        super(id, title, author, year, category);
        this.isbn  = isbn;
        this.genre = genre;
        this.pages = pages;
    }

    @Override
    public String getType() { return "Book"; }

    @Override
    public String getDescription() {
        return String.format("Book: %s | ISBN: %s | Genre: %s | Pages: %d",
            getTitle(), isbn, genre, pages);
    }

    // Getters
    public String getIsbn()  { return isbn; }
    public String getGenre() { return genre; }
    public int    getPages() { return pages; }

    // Setters
    public void setIsbn(String isbn)   { this.isbn = isbn; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setPages(int pages)    { this.pages = pages; }
}
