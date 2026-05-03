package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Author {
    private final DbConnector dbConnector;
    private int id;
    private String name;

    public Author(DbConnector dbConnector) {
        this.dbConnector = dbConnector;
    }

    public Author(DbConnector dbConnector, int id, String name) {
        this.dbConnector = dbConnector;
        this.id = id;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void insert() throws SQLException {
        String sql = "INSERT INTO authors (name) VALUES (?)";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    this.id = keys.getInt(1);
                    return;
                }
            }
        }
        throw new SQLException("Failed to obtain generated author id");
    }

    public void update() throws SQLException {
        if (id <= 0) {
            throw new SQLException("Cannot update: author has no id (insert first)");
        }
        String sql = "UPDATE authors SET name = ? WHERE id = ?";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, id);
            int n = ps.executeUpdate();
            if (n == 0) {
                throw new SQLException("No author found with id " + id);
            }
        }
    }

    public void delete() throws SQLException {
        if (id <= 0) {
            throw new SQLException("Cannot delete: author has no id");
        }
        String sql = "DELETE FROM authors WHERE id = ?";
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int n = ps.executeUpdate();
            if (n == 0) {
                throw new SQLException("No author found with id " + id);
            }
        }
        id = 0;
    }

    @Override
    public String toString() {
        return id + " — " + name;
    }
}
