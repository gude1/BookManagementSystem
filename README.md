# Book Management System

A small console app in Java for managing **authors** and **books** with MySQL. It uses the JDBC API for persistence and a `Scanner`-driven menu.

## Requirements

- **JDK** 17 or newer (recommended; matches modern Gradle and language features used in the project)
- **MySQL** 8.x (or compatible) running locally
- **Gradle** (use the included `./gradlew` wrapper)

## Configuration

Edit `src/main/java/org/example/DbConnector.java`:

- `SERVER_URL` / `URL` — host, port, and query parameters if your server differs from `localhost:3306`
- `USER` / `PASSWORD` — MySQL credentials
- `DATABASE_NAME` — defaults to `book_management` (the app creates this database if it does not exist)

The MySQL user must be allowed to **create databases** (for first-time setup) and use the `book_management` schema.

## Schema

On startup, `DbConnector.initializeSchema()`:

1. Creates the database `book_management` if needed
2. Creates `authors` and `books` if they do not exist

`books.author_id` references `authors(id)` with **`ON DELETE SET NULL`**: deleting an author keeps the book rows and clears their author link.

## Run

1. Start MySQL.
2. From the project root:

   ```bash
   ./gradlew build
   ```

3. Run **`org.example.Main`** from your IDE (Gradle project import / sync so the MySQL driver is on the classpath).

## Menu

| Option | Action                     |
| ------ | -------------------------- |
| 1      | Add author                 |
| 2      | Add book (existing author) |
| 3      | List authors               |
| 4      | List books                 |
| 5      | Update book                |
| 6      | Delete book                |
| 7      | Delete author              |
| 0      | Exit                       |

## Project layout

- `DbConnector` — connection URL, database/table creation, listing authors and books
- `Author` / `Book` — model classes holding a `DbConnector` reference; `insert`, `update`, `delete` use JDBC on that connector

## Dependencies

Declared in `build.gradle.kts` (notably `com.mysql:mysql-connector-j`).
