package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Book {
    private final DbConnector dbConnector;
    private int id;
    private String title;
    /** Null when the author was deleted ({@code ON DELETE SET NULL}) or unset. */
    private Integer authorId;
    /** Set when loaded via {@link DbConnector#listBooks()} (join with authors). */
    private String authorName;

    public Book(DbConnector dbConnector) {
        this.dbConnector = dbConnector;
    }

    public Book(DbConnector dbConnector, int id, String title, int authorId) {
        this(dbConnector, id, title, Integer.valueOf(authorId), null);
    }

    public Book(DbConnector dbConnector, int id, String title, Integer authorId, String authorName) {
        this.dbConnector = dbConnector;
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.authorName = authorName;
    }

    public DbConnector getDbConnector() {
        return dbConnector;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void insert() throws SQLException {
        String sql = "INSERT INTO books (title, author_id) VALUES (?, ?)";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setObject(2, authorId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    this.id = keys.getInt(1);
                    return;
                }
            }
        }
        throw new SQLException("Failed to obtain generated book id");
    }

    public void update() throws SQLException {
        if (id <= 0) {
            throw new SQLException("Cannot update: book has no id (insert first)");
        }
        String sql = "UPDATE books SET title = ?, author_id = ? WHERE id = ?";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setObject(2, authorId);
            ps.setInt(3, id);
            int n = ps.executeUpdate();
            if (n == 0) {
                throw new SQLException("No book found with id " + id);
            }
        }
    }

    public void delete() throws SQLException {
        if (id <= 0) {
            throw new SQLException("Cannot delete: book has no id");
        }
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int n = ps.executeUpdate();
            if (n == 0) {
                throw new SQLException("No book found with id " + id);
            }
        }
        id = 0;
    }

    @Override
    public String toString() {
        if (authorName != null && !authorName.isEmpty()) {
            return id + " — " + title + " — " + authorName;
        }
        if (authorId != null) {
            return id + " — " + title + " (author id " + authorId + ")";
        }
        return id + " — " + title + " (no author)";
    }
}
