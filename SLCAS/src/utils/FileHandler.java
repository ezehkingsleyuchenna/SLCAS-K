package utils;

import model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads library items and users as JSON files.
 * Uses a hand-rolled minimal JSON serialiser/deserialiser
 * so that no external libraries are required.
 */
public class FileHandler {

    // ══════════════════════════════════════════════════════════════════════════
    //  ITEMS
    // ══════════════════════════════════════════════════════════════════════════

    public void saveItems(ArrayList<LibraryItem> items, String path) {
        ensureParentDirs(path);
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("[");
            for (int i = 0; i < items.size(); i++) {
                pw.print(itemToJson(items.get(i)));
                pw.println(i < items.size() - 1 ? "," : "");
            }
            pw.println("]");
        } catch (IOException e) {
            System.err.println("FileHandler – save items error: " + e.getMessage());
        }
    }

    public ArrayList<LibraryItem> loadItems(String path) {
        ArrayList<LibraryItem> result = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return result;
        try {
            String json = readAll(file);
            for (String obj : splitObjects(json)) {
                LibraryItem item = parseItem(obj);
                if (item != null) result.add(item);
            }
        } catch (IOException e) {
            System.err.println("FileHandler – load items error: " + e.getMessage());
        }
        return result;
    }

    private String itemToJson(LibraryItem item) {
        StringBuilder sb = new StringBuilder("  {");
        appendField(sb, "id",          item.getId(),                  true);
        appendField(sb, "type",        item.getType(),                false);
        appendField(sb, "title",       item.getTitle(),               false);
        appendField(sb, "author",      item.getAuthor(),              false);
        appendIntField(sb, "year",     item.getYear());
        appendField(sb, "category",    item.getCategory(),            false);
        appendBoolField(sb, "available", item.isAvailable());
        appendIntField(sb, "borrowCount", item.getBorrowCount());

        if (item instanceof Book) {
            Book b = (Book) item;
            appendField(sb, "isbn",  b.getIsbn(),  false);
            appendField(sb, "genre", b.getGenre(), false);
            appendIntField(sb, "pages", b.getPages());
        } else if (item instanceof Magazine) {
            Magazine m = (Magazine) item;
            appendIntField(sb, "issueNumber", m.getIssueNumber());
            appendField(sb, "publisher", m.getPublisher(), false);
        } else if (item instanceof Journal) {
            Journal j = (Journal) item;
            appendIntField(sb, "volume", j.getVolume());
            appendIntField(sb, "issue",  j.getIssue());
            appendField(sb, "doi", j.getDoi(), false);
        }
        // Remove trailing comma and close object
        if (sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    private LibraryItem parseItem(String json) {
        try {
            String type        = str(json, "type");
            String id          = str(json, "id");
            String title       = str(json, "title");
            String author      = str(json, "author");
            int    year        = num(json, "year");
            String category    = str(json, "category");
            boolean available  = bool(json, "available");
            int    borrowCount = num(json, "borrowCount");

            LibraryItem item;
            switch (type) {
                case "Book":
                    item = new Book(id, title, author, year, category,
                                    str(json, "isbn"), str(json, "genre"), num(json, "pages"));
                    break;
                case "Magazine":
                    item = new Magazine(id, title, author, year, category,
                                        num(json, "issueNumber"), str(json, "publisher"));
                    break;
                case "Journal":
                    item = new Journal(id, title, author, year, category,
                                       num(json, "volume"), num(json, "issue"), str(json, "doi"));
                    break;
                default:
                    return null;
            }
            item.setAvailable(available);
            item.setBorrowCount(borrowCount);
            return item;
        } catch (Exception e) {
            System.err.println("FileHandler – parse item error: " + e.getMessage());
            return null;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  USERS
    // ══════════════════════════════════════════════════════════════════════════

    public void saveUsers(ArrayList<UserAccount> users, String path) {
        ensureParentDirs(path);
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("[");
            for (int i = 0; i < users.size(); i++) {
                pw.print(userToJson(users.get(i)));
                pw.println(i < users.size() - 1 ? "," : "");
            }
            pw.println("]");
        } catch (IOException e) {
            System.err.println("FileHandler – save users error: " + e.getMessage());
        }
    }

    public ArrayList<UserAccount> loadUsers(String path) {
        ArrayList<UserAccount> result = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) return result;
        try {
            String json = readAll(file);
            for (String obj : splitObjects(json)) {
                UserAccount u = parseUser(obj);
                if (u != null) result.add(u);
            }
        } catch (IOException e) {
            System.err.println("FileHandler – load users error: " + e.getMessage());
        }
        return result;
    }

    private String userToJson(UserAccount u) {
        StringBuilder sb = new StringBuilder("  {");
        appendField(sb, "userId", u.getUserId(), true);
        appendField(sb, "name",   u.getName(),   false);
        appendField(sb, "email",  u.getEmail(),  false);
        appendField(sb, "phone",  u.getPhone(),  false);
        sb.append("\"borrowedItems\":").append(listToJson(u.getBorrowedItems())).append(",");
        sb.append("\"overdueItems\":").append(listToJson(u.getOverdueItems()));
        sb.append("}");
        return sb.toString();
    }

    private UserAccount parseUser(String json) {
        try {
            UserAccount u = new UserAccount(
                str(json, "userId"), str(json, "name"),
                str(json, "email"),  str(json, "phone"));
            for (String id : parseStringArray(arraySection(json, "borrowedItems")))
                u.addBorrowedItem(id);
            for (String id : parseStringArray(arraySection(json, "overdueItems")))
                u.addOverdueItem(id);
            return u;
        } catch (Exception e) {
            System.err.println("FileHandler – parse user error: " + e.getMessage());
            return null;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  JSON helpers
    // ══════════════════════════════════════════════════════════════════════════

    private void appendField(StringBuilder sb, String key, String value, boolean first) {
        if (!first) { /* comma already in place */ }
        sb.append("\"").append(key).append("\":\"").append(esc(value)).append("\",");
    }

    private void appendIntField(StringBuilder sb, String key, int value) {
        sb.append("\"").append(key).append("\":").append(value).append(",");
    }

    private void appendBoolField(StringBuilder sb, String key, boolean value) {
        sb.append("\"").append(key).append("\":").append(value).append(",");
    }

    /** Extract a string value from a flat JSON object string. */
    private String str(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return "";
        int start = idx + pattern.length();
        // walk forward handling escaped quotes
        StringBuilder buf = new StringBuilder();
        int i = start;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) { buf.append(json.charAt(i + 1)); i += 2; continue; }
            if (c == '"') break;
            buf.append(c);
            i++;
        }
        return buf.toString();
    }

    /** Extract an integer value. */
    private int num(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx < 0) return 0;
        int start = idx + pattern.length();
        // skip whitespace
        while (start < json.length() && json.charAt(start) == ' ') start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try { return Integer.parseInt(json.substring(start, end).trim()); }
        catch (NumberFormatException ex) { return 0; }
    }

    /** Extract a boolean value. */
    private boolean bool(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx < 0) return true;
        int start = idx + pattern.length();
        return json.startsWith("true", start);
    }

    /** Extract the raw text of an array property. */
    private String arraySection(String json, String key) {
        String pattern = "\"" + key + "\":[";
        int idx = json.indexOf(pattern);
        if (idx < 0) return "[]";
        int start = idx + pattern.length() - 1;
        int depth = 0, end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '[') depth++;
            else if (c == ']') { depth--; if (depth == 0) break; }
            end++;
        }
        return json.substring(start, Math.min(end + 1, json.length()));
    }

    /** Split a JSON array string of strings into a String[]. */
    private String[] parseStringArray(String arrayJson) {
        if (arrayJson == null || arrayJson.equals("[]")) return new String[0];
        String inner = arrayJson.substring(1, arrayJson.length() - 1).trim();
        if (inner.isEmpty()) return new String[0];
        String[] raw = inner.split(",");
        for (int i = 0; i < raw.length; i++)
            raw[i] = raw[i].trim().replaceAll("^\"|\"$", "");
        return raw;
    }

    private String listToJson(List<String> list) {
        if (list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(esc(list.get(i))).append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Split a JSON array body into individual object strings.
     * Handles nested depth via brace counting.
     */
    private List<String> splitObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }

    private String readAll(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private void ensureParentDirs(String path) {
        File parent = new File(path).getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
    }
}
