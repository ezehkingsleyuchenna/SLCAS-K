package controller;

import model.LibraryItem;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Provides linear, binary, and recursive search algorithms,
 * four sorting algorithms, and recursive utility computations.
 */
public class SearchEngine {

    // ══════════════════════════════════════════════════════════════════════════
    //  SEARCH ALGORITHMS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Linear search – O(n). Works on unsorted data.
     * Searches the chosen field for a case-insensitive substring match.
     */
    public ArrayList<LibraryItem> linearSearch(ArrayList<LibraryItem> items,
                                               String query, String field) {
        ArrayList<LibraryItem> results = new ArrayList<>();
        String q = query.toLowerCase().trim();
        for (LibraryItem item : items) {
            if (matches(item, q, field)) results.add(item);
        }
        return results;
    }

    /**
     * Binary search – O(log n). Requires list sorted by title ascending.
     * Returns the first exact (case-insensitive) title match, or null.
     */
    public LibraryItem binarySearch(ArrayList<LibraryItem> sortedItems, String title) {
        int low = 0, high = sortedItems.size() - 1;
        String target = title.toLowerCase().trim();
        while (low <= high) {
            int mid = (low + high) / 2;
            int cmp = sortedItems.get(mid).getTitle().toLowerCase().compareTo(target);
            if (cmp == 0)      return sortedItems.get(mid);
            else if (cmp < 0)  low  = mid + 1;
            else               high = mid - 1;
        }
        return null;
    }

    /**
     * Recursive search – walks the list recursively from {@code index}.
     * Field and query behave identically to linearSearch.
     */
    public ArrayList<LibraryItem> recursiveSearch(ArrayList<LibraryItem> items,
                                                   String query, String field,
                                                   int index) {
        ArrayList<LibraryItem> results = new ArrayList<>();
        if (index >= items.size()) return results;
        if (matches(items.get(index), query.toLowerCase().trim(), field))
            results.add(items.get(index));
        results.addAll(recursiveSearch(items, query, field, index + 1));
        return results;
    }

    // ── Shared matching predicate ──────────────────────────────────────────────
    private boolean matches(LibraryItem item, String q, String field) {
        switch (field.toLowerCase()) {
            case "title":    return item.getTitle().toLowerCase().contains(q);
            case "author":   return item.getAuthor().toLowerCase().contains(q);
            case "type":     return item.getType().toLowerCase().contains(q);
            case "category": return item.getCategory().toLowerCase().contains(q);
            default:
                return item.getTitle().toLowerCase().contains(q)
                    || item.getAuthor().toLowerCase().contains(q)
                    || item.getType().toLowerCase().contains(q);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SORTING ALGORITHMS
    // ══════════════════════════════════════════════════════════════════════════

    /** Selection Sort – O(n²). In-place. */
    public void selectionSort(ArrayList<LibraryItem> items, Comparator<LibraryItem> cmp) {
        int n = items.size();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++)
                if (cmp.compare(items.get(j), items.get(minIdx)) < 0) minIdx = j;
            if (minIdx != i) swap(items, i, minIdx);
        }
    }

    /** Insertion Sort – O(n²) worst, O(n) best. In-place. */
    public void insertionSort(ArrayList<LibraryItem> items, Comparator<LibraryItem> cmp) {
        int n = items.size();
        for (int i = 1; i < n; i++) {
            LibraryItem key = items.get(i);
            int j = i - 1;
            while (j >= 0 && cmp.compare(items.get(j), key) > 0) {
                items.set(j + 1, items.get(j));
                j--;
            }
            items.set(j + 1, key);
        }
    }

    /** Merge Sort – O(n log n). Returns a new sorted list (recursive). */
    public ArrayList<LibraryItem> mergeSort(ArrayList<LibraryItem> items,
                                             Comparator<LibraryItem> cmp) {
        if (items.size() <= 1) return new ArrayList<>(items);
        int mid = items.size() / 2;
        ArrayList<LibraryItem> left  = mergeSort(new ArrayList<>(items.subList(0, mid)), cmp);
        ArrayList<LibraryItem> right = mergeSort(new ArrayList<>(items.subList(mid, items.size())), cmp);
        return merge(left, right, cmp);
    }

    private ArrayList<LibraryItem> merge(ArrayList<LibraryItem> left,
                                          ArrayList<LibraryItem> right,
                                          Comparator<LibraryItem> cmp) {
        ArrayList<LibraryItem> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (cmp.compare(left.get(i), right.get(j)) <= 0) result.add(left.get(i++));
            else                                               result.add(right.get(j++));
        }
        while (i < left.size())  result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }

    /** Quick Sort – O(n log n) average. In-place. */
    public void quickSort(ArrayList<LibraryItem> items, Comparator<LibraryItem> cmp,
                          int low, int high) {
        if (low < high) {
            int pivot = partition(items, cmp, low, high);
            quickSort(items, cmp, low, pivot - 1);
            quickSort(items, cmp, pivot + 1, high);
        }
    }

    private int partition(ArrayList<LibraryItem> items, Comparator<LibraryItem> cmp,
                          int low, int high) {
        LibraryItem pivot = items.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (cmp.compare(items.get(j), pivot) <= 0) swap(items, ++i, j);
        }
        swap(items, i + 1, high);
        return i + 1;
    }

    private void swap(ArrayList<LibraryItem> list, int a, int b) {
        LibraryItem tmp = list.get(a);
        list.set(a, list.get(b));
        list.set(b, tmp);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RECURSIVE UTILITY COMPUTATIONS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Recursively computes the overdue charge at $0.50 per day.
     * Base case: 0 days → $0.00.
     */
    public double computeOverdueCharge(int daysOverdue) {
        if (daysOverdue <= 0) return 0.0;
        return 0.50 + computeOverdueCharge(daysOverdue - 1);
    }

    /**
     * Recursively counts items that belong to the given category.
     */
    public int countByCategory(ArrayList<LibraryItem> items,
                                String category, int index) {
        if (index >= items.size()) return 0;
        int hit = items.get(index).getCategory().equalsIgnoreCase(category) ? 1 : 0;
        return hit + countByCategory(items, category, index + 1);
    }

    /**
     * Recursively computes the total number of items in the catalogue.
     */
    public int totalCount(ArrayList<LibraryItem> items, int index) {
        if (index >= items.size()) return 0;
        return 1 + totalCount(items, index + 1);
    }
}
