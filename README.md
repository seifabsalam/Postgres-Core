# postgres-core

A lightweight Java library that eliminates the boilerplate of bootstrapping a PostgreSQL database connection at the start of every project. It wraps **HikariCP** for production-grade connection pooling and **jOOQ** for type-safe SQL, exposing a minimal, easy-to-use API.

## Features

- ✅ HikariCP connection pool — pre-configured with sensible defaults
- ✅ jOOQ `DSLContext` — ready to use out of the box
- ✅ Constructor-based credential injection — you decide how to load your config
- ✅ `Closeable` — safely usable in try-with-resources blocks
- ✅ Custom unchecked exceptions with full cause chain preservation

## Requirements

- Java 11+
- Maven 3.6+
- A running PostgreSQL instance (e.g., [Neon](https://neon.tech), local Postgres, etc.)

## Installation

This library is distributed via **[JitPack](https://jitpack.io/#seifabsalam/Postgres-Core)**.

### Step 1 — Add the JitPack repository to your `pom.xml`

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### Step 2 — Add the dependency

```xml
<dependency>
    <groupId>com.github.seifabsalam</groupId>
    <artifactId>Postgres-Core</artifactId>
    <version>v1.0.0</version>
</dependency>
```

> **Note:** JitPack uses `com.github.seifabsalam` as the `groupId` regardless of what is declared in `pom.xml`.
> Replace `v1.0.0` with any [released tag](https://github.com/seifabsalam/Postgres-Core/tags) or use `master-SNAPSHOT` to track the latest commit.

## Usage

Instantiate `Database` by passing your credentials directly. The constructor immediately initializes the connection pool.

```java
import com.telecom.postgrescore.Database;
import org.jooq.DSLContext;

// Use try-with-resources to ensure the pool is closed when done
try (Database db = new Database("jdbc:postgresql://host:5432/mydb", "user", "password")) {

    // Option 1: use jOOQ DSLContext (recommended — jOOQ manages connection lifecycle)
    DSLContext ctx = db.getDSLContext();
    ctx.selectOne().fetch();

    // Option 2: obtain a raw JDBC connection manually
    try (var conn = db.getConnection()) {
        // use connection directly
    }

    // Option 3: wrap an external connection in a jOOQ DSLContext (static utility)
    try (var conn = db.getConnection()) {
        DSLContext ctx2 = Database.getDSLContext(conn);
    }
}
```

### Loading credentials from a properties file

The library does not load any configuration files itself. You are free to load credentials however suits your project:

```java
Properties props = new Properties();
try (InputStream in = getClass().getResourceAsStream("/db.properties")) {
    props.load(in);
}

Database db = new Database(
    props.getProperty("db.url"),
    props.getProperty("db.username"),
    props.getProperty("db.password")
);
```

## Connection Pool Defaults

| Setting             | Value   |
|---------------------|---------|
| `maximumPoolSize`   | 2       |
| `minimumIdle`       | 0       |
| `idleTimeout`       | 10,000 ms |
| `connectionTimeout` | 20,000 ms |

> These defaults are intentionally conservative and well-suited for hosted free-tier databases (e.g., Neon) with limited concurrent connection allowances.

## Exceptions

| Exception                    | When thrown                                              |
|------------------------------|----------------------------------------------------------|
| `DatabaseConnectionException` | Pool initialization fails or pool is closed              |
| `QueryExecutionException`     | Query execution fails (for use in consuming applications) |

Both extend `RuntimeException` and preserve the original cause chain for easier debugging.

## jOOQ Code Generation (Consuming Projects)

To generate type-safe Java classes from your PostgreSQL schema, add the `jooq-codegen-maven` plugin
to your project's `pom.xml`. This is a one-time setup that runs during `mvn generate-sources` and
produces classes that you use alongside this library's `DSLContext`.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.jooq</groupId>
            <artifactId>jooq-codegen-maven</artifactId>
            <version>3.15.11</version>
            <executions>
                <execution>
                    <id>jooq-codegen</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <jdbc>
                    <driver>org.postgresql.Driver</driver>
                    <url>jdbc:postgresql://host:5432/mydb</url>
                    <user>your_username</user>
                    <password>your_password</password>
                </jdbc>
                <generator>
                    <database>
                        <name>org.jooq.meta.postgres.PostgresDatabase</name>
                        <!-- Include all tables, or restrict with a regex e.g. PUBLIC\.my_table -->
                        <includes>.*</includes>
                        <inputSchema>public</inputSchema>
                    </database>
                    <target>
                        <!-- Package where generated classes will be placed -->
                        <packageName>com.example.myapp.db</packageName>
                        <directory>target/generated-sources/jooq</directory>
                    </target>
                </generator>
            </configuration>
        </plugin>
    </plugins>
</build>
```

> **Tip:** To avoid hardcoding credentials in `pom.xml`, use the
> [`properties-maven-plugin`](https://www.mojohaus.org/properties-maven-plugin/) to load them
> from a local `db.properties` file (add it to your `.gitignore`):
>
> ```xml
> <plugin>
>     <groupId>org.codehaus.mojo</groupId>
>     <artifactId>properties-maven-plugin</artifactId>
>     <version>1.2.1</version>
>     <executions>
>         <execution>
>             <phase>initialize</phase>
>             <goals><goal>read-project-properties</goal></goals>
>             <configuration>
>                 <files>
>                     <file>${project.basedir}/db.properties</file>
>                 </files>
>             </configuration>
>         </execution>
>     </executions>
> </plugin>
> ```
>
> Then reference properties in the jOOQ plugin with `${db.url}`, `${db.username}`, `${db.password}`.

Once generated, use the classes directly with `getDSLContext()`:

```java
import com.example.myapp.db.Tables;

try (Database db = new Database(url, user, password)) {
    var results = db.getDSLContext()
        .selectFrom(Tables.MY_TABLE)
        .where(Tables.MY_TABLE.STATUS.eq("active"))
        .fetch();
}
```

## License

MIT
