package org.example;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        DbConnector db = new DbConnector();
        try {
            db.initializeSchema();
        } catch (SQLException e) {
            System.err.println(
                    "Could not connect or create tables. Check MySQL is running, database exists, and credentials in DbConnector.");
            e.printStackTrace();
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            runMenu(scanner, db);
        }
    }

    private static void runMenu(Scanner scanner, DbConnector db) {
        while (true) {
            System.out.println("""
                    \n--- Book Management ---
                    1) Add author
                    2) Add book (for an existing author)
                    3) List authors
                    4) List books
                    5) Update book
                    6) Delete book
                    7) Delete author (books keep their row; author link cleared)
                    0) Exit
                    Choice:\s""");
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addAuthorFlow(scanner, db);
                    case "2" -> addBookFlow(scanner, db);
                    case "3" -> listAuthorsFlow(db);
                    case "4" -> listBooksFlow(db);
                    case "5" -> updateBookFlow(scanner, db);
                    case "6" -> deleteBookFlow(scanner, db);
                    case "7" -> deleteAuthorFlow(scanner, db);
                    case "0" -> {
                        System.out.println("Goodbye.");
                        return;
                    }
                    default -> System.out.println("Unknown option.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
            }
        }
    }

    private static void addAuthorFlow(Scanner scanner, DbConnector db) throws SQLException {
        System.out.print("Author name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        Author author = new Author(db);
        author.setName(name);
        author.insert();
        System.out.println("Author saved with id " + author.getId() + ".");
    }

    private static void addBookFlow(Scanner scanner, DbConnector db) throws SQLException {
        List<Author> authors = db.listAuthors();
        if (authors.isEmpty()) {
            System.out.println("Add at least one author first.");
            return;
        }
        System.out.println("Authors:");
        for (Author a : authors) {
            System.out.println("  " + a);
        }
        System.out.print("Author id: ");
        String idLine = scanner.nextLine().trim();
        int authorId;
        try {
            authorId = Integer.parseInt(idLine);
        } catch (NumberFormatException e) {
            System.out.println("Invalid id.");
            return;
        }
        boolean found = authors.stream().anyMatch(a -> a.getId() == authorId);
        if (!found) {
            System.out.println("No author with that id.");
            return;
        }
        System.out.print("Book title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }
        Book book = new Book(db);
        book.setTitle(title);
        book.setAuthorId(Integer.valueOf(authorId));
        book.insert();
        System.out.println("Book saved with id " + book.getId() + ".");
    }

    private static void listAuthorsFlow(DbConnector db) throws SQLException {
        List<Author> authors = db.listAuthors();
        if (authors.isEmpty()) {
            System.out.println("(no authors yet)");
            return;
        }
        for (Author a : authors) {
            System.out.println(a);
        }
    }

    private static void listBooksFlow(DbConnector db) throws SQLException {
        List<Book> books = db.listBooks();
        if (books.isEmpty()) {
            System.out.println("(no books yet)");
            return;
        }
        for (Book b : books) {
            System.out.println(b);
        }
    }

    private static void updateBookFlow(Scanner scanner, DbConnector db) throws SQLException {
        List<Book> books = db.listBooks();
        if (books.isEmpty()) {
            System.out.println("(no books to update)");
            return;
        }
        System.out.println("Books:");
        for (Book b : books) {
            System.out.println("  " + b);
        }
        System.out.print("Book id: ");
        int bookId;
        try {
            bookId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid id.");
            return;
        }
        Book target = books.stream().filter(b -> b.getId() == bookId).findFirst().orElse(null);
        if (target == null) {
            System.out.println("No book with that id.");
            return;
        }
        System.out.print("New title: ");
        String newTitle = scanner.nextLine().trim();
        if (newTitle.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }
        System.out.print("New author id (blank = no author): ");
        String authorLine = scanner.nextLine().trim();
        Integer newAuthorId = null;
        if (!authorLine.isEmpty()) {
            try {
                newAuthorId = Integer.parseInt(authorLine);
            } catch (NumberFormatException e) {
                System.out.println("Invalid author id.");
                return;
            }
            List<Author> authors = db.listAuthors();
            int aid = newAuthorId;
            if (authors.stream().noneMatch(a -> a.getId() == aid)) {
                System.out.println("No author with that id.");
                return;
            }
        }
        target.setTitle(newTitle);
        target.setAuthorId(newAuthorId);
        target.update();
        System.out.println("Book updated.");
    }

    private static void deleteBookFlow(Scanner scanner, DbConnector db) throws SQLException {
        List<Book> books = db.listBooks();
        if (books.isEmpty()) {
            System.out.println("(no books to delete)");
            return;
        }
        System.out.println("Books:");
        for (Book b : books) {
            System.out.println("  " + b);
        }
        System.out.print("Book id to delete: ");
        int bookId;
        try {
            bookId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid id.");
            return;
        }
        Book target = books.stream().filter(b -> b.getId() == bookId).findFirst().orElse(null);
        if (target == null) {
            System.out.println("No book with that id.");
            return;
        }
        target.delete();
        System.out.println("Book deleted.");
    }

    private static void deleteAuthorFlow(Scanner scanner, DbConnector db) throws SQLException {
        List<Author> authors = db.listAuthors();
        if (authors.isEmpty()) {
            System.out.println("(no authors to delete)");
            return;
        }
        System.out.println("Authors:");
        for (Author a : authors) {
            System.out.println("  " + a);
        }
        System.out.print("Author id to delete: ");
        int authorId;
        try {
            authorId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid id.");
            return;
        }
        Author target = authors.stream().filter(a -> a.getId() == authorId).findFirst().orElse(null);
        if (target == null) {
            System.out.println("No author with that id.");
            return;
        }
        target.delete();
        System.out.println(
                "Author deleted. Any books they wrote now have no author (author_id set to NULL).");
    }
}
