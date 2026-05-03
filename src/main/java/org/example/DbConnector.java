package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DbConnector {

    private static final String DATABASE_NAME = "book_management";
    private static final String URL_PARAMS = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    /**
     * Connects to the server without selecting a database (used to create the
     * schema).
     */
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/" + URL_PARAMS;
    private static final String URL = "jdbc:mysql://localhost:3306/" + DATABASE_NAME + URL_PARAMS;
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final String BOOKS_AUTHOR_FK = "fk_books_author";

    public void createDatabaseIfNotExists() throws SQLException {
        String sql = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
        try (Connection conn = DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public void createAuthorsTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS authors (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL UNIQUE
                )
                """;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public void createBooksTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS books (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    author_id INT NULL,
                    CONSTRAINT %s FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE SET NULL
                )
                """.formatted(BOOKS_AUTHOR_FK);
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public void initializeSchema() throws SQLException {
        createDatabaseIfNotExists();
        createAuthorsTable();
        createBooksTable();
    }

    public List<Author> listAuthors() throws SQLException {
        List<Author> authors = new ArrayList<>();
        String sql = "SELECT id, name FROM authors ORDER BY name";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                authors.add(new Author(this, rs.getInt("id"), rs.getString("name")));
            }
        }
        return authors;
    }

    public List<Book> listBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = """
                SELECT b.id, b.title, b.author_id, a.name AS author_name
                FROM books b
                LEFT JOIN authors a ON a.id = b.author_id
                ORDER BY b.title
                """;
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int rawAuthorId = rs.getInt("author_id");
                Integer authorId = rs.wasNull() ? null : rawAuthorId;
                books.add(new Book(this, rs.getInt("id"), rs.getString("title"), authorId,
                        rs.getString("author_name")));
            }
        }
        return books;
    }
}
