# Book Library Backend

This is a backend app built with Java 21 and Spring Boot 4.0.6 that manages a library. It lets you save books, update books, and retrieve books from the database.

To protect certain APIs, especially the delete request, I added a simple API key that gets attached to the request. Role-based access control is also in place, so only the admin role can delete content.

Spring Security filters incoming requests. It checks the request path, decides whether the `X-API-KEY` header is needed, and then passes the request through the API filter to validate the key. After that, Spring Security continues based on the result.

To use `DELETE`, attach this header with the admin key value:

```http
X-API-KEY: admin
```

This app is intentionally simple and uses an H2 in-memory database. I did not use file mode, so the data is not persistent. That means the data gets reset when the app stops.

## How to Run Locally

### What you need to install

You need these tools:

- Java 21
- Maven 3.9+
- Git (if needed)

1. Install Java 21
2. Install Maven, or just use the Maven Wrapper that comes with the project.
3. Set `JAVA_HOME` to your Java 21 install folder.
4. Add `%JAVA_HOME%\bin` to your `PATH`.
5. Check that `java -version` shows Java 21.
6. Check that `mvn -version` works.
7. Either clone the app from the Git Repo, download the ZIP or use the files attached in the email.

### running locally

I have attached 2 zip files in the email:

- One zip contains the jar file itself, which you can run directly.
- The other zip contains the source code, which you can build, compile, and run.

### If you use the jar zip

1. Make sure Java 21 is installed.
2. Open a terminal in the folder where the jar is located.
3. Run the app with:

```bash
java -jar book-0.0.1-SNAPSHOT.jar
```

### If you use the source-code zip

1. Make sure Java 21 and Maven are installed.
2. Open a terminal in the project folder.
3. Build the project with:

```bash
mvn clean package
```

4. Run the app with:

```bash
mvn spring-boot:run
```

### Some errors that could happen

- If `java` is not found, check `JAVA_HOME` and `PATH`.
- If Maven says the Java version is wrong, make sure `JAVA_HOME` points to Java 21.
- If the Maven Wrapper fails, run `./mvnw -v` first to check which Java it is using.
- If the app does not start because of the port, check whether another process is already using port `8080`.

### Deployed version

This app is currently deployed on AWS ECS on an EC2 instance and served through Nginx. It is accessible at:

```text
http://47.129.152.96/
```



## Database

The application uses 4 main tables:

- `api_keys` stores the API key credentials and links them to roles.
- `roles` stores the available roles, like `ROLE_ADMIN`.
- `author` stores author data.
- `books` stores book-related data.

The API key and role tables handle access control. The author and book tables store the library data.

## Exception Handling

Exception handling is there so raw JSON errors and internal details do not leak to the user. Instead of returning low-level framework errors, the app returns a clean response with a `success` flag and a readable message.

## APIs

This app has 4 main APIs:

1. Add a new book
2. Update an existing book
3. Find books
4. Delete a book

All success responses follow this format:

```json
{
  "success": true,
  "message": "some message",
  "data": {}
}
```

All error responses follow this format:

```json
{
  "success": false,
  "message": "some error message"
}
```

## 1. Add a New Book

`POST /api/books`

### Request body

```json
{
  "isbn": "1",
  "title": "Book 1",
  "authors": [
    {
      "name": "Amanda",
      "birthday": "31-03-2025"
    }
  ],
  "year": 2018,
  "price": 45.99,
  "genre": "Programming"
}
```

### Expected result

Returns `201 Created` with a success response containing the saved book.

### Expected errors

- `Book already exists` means a book with the same ISBN already exists.
- `At least one author is required` means the authors list is empty.
- `Each author must have a name` means an author name was missing.
- `Birthday must be in dd-MM-yyyy format` means the birthday format is invalid.
- `Birthday cannot be in the future` means the birthday is not a valid past date.
- `Price must have up to 2 decimal places` means the price has too many decimal places.

## 2. Update an Existing Book

`PUT /api/books/{isbn}`

### Request body

```json
{
  "title": "Book 1 Updated",
  "authors": [
    {
      "name": "Amanda"
    }
  ],
  "year": 2019,
  "price": 46.50,
  "genre": "Programming"
}
```

### Expected result

Returns `200 OK` with a success response containing the updated book.

### Expected errors

- `Book not found` means the ISBN in the path does not exist.
- `At least one author is required` means the authors list is empty.
- `Each author must have a name` means an author name was missing.
- `Price must have up to 2 decimal places` means the price has too many decimal places.

## 3. Find Books

`GET /api/books`

### Query parameters

- `title` optional, exact match
- `authorNames` optional, can be repeated

### Examples

```http
GET /api/books?title=Book%201
GET /api/books?authorNames=Amanda
GET /api/books?authorNames=Amanda&authorNames=Bob
GET /api/books?title=Book%201&authorNames=Amanda&authorNames=Bob
```

### Search rules

- If only `title` is given, the API returns books with that exact title.
- If only one author name is given, the API returns any book with that author.
- If more than one author name is given, the book must contain all provided authors.
- If both `title` and `authorNames` are given, the title must match and the book must contain all provided authors.

### Expected result

Returns `200 OK` with a success response containing the matching books.

If no books match, the response message is:

```json
{
  "success": true,
  "message": "No books found",
  "data": []
}
```

### Expected errors

- `Please provide either book title, author names or both.` means no search filter was given.

## 4. Delete a Book

`DELETE /api/books/{isbn}`

### Expected result

Returns `200 OK` with a success response saying the book was deleted.

### Expected errors

- `Book not found` means the ISBN does not exist.
- `You do not have permission to perform this action` means the API key does not have admin access.

### Permission note

Delete is restricted to `ADMIN` role only.

## Sample `curl` Commands

### Add a new book

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "1",
    "title": "Book 1",
    "authors": [
      {
        "name": "Amanda",
        "birthday": "31-03-2025"
      }
    ],
    "year": 2018,
    "price": 45.99,
    "genre": "Programming"
  }'
```

### Update a book

```bash
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Book 1 Updated",
    "authors": [
      {
        "name": "Amanda"
      }
    ],
    "year": 2019,
    "price": 46.50,
    "genre": "Programming"
  }'
```

### Find books by title

```bash
curl "http://localhost:8080/api/books?title=Book%201"
```

### Find books by one author

```bash
curl "http://localhost:8080/api/books?authorNames=Amanda"
```

### Find books by multiple authors

```bash
curl "http://localhost:8080/api/books?authorNames=Amanda&authorNames=Bob"
```

### Find books by title and multiple authors

```bash
curl "http://localhost:8080/api/books?title=Book%201&authorNames=Amanda&authorNames=Bob"
```

### Delete a book

```bash
curl -X DELETE http://localhost:8080/api/books/1 \
  -H "X-API-KEY: admin"
```
