JMigrate: database migration management for Java/Android apps
==============================================================

[![Sonatype Central](https://maven-badges.sml.io/sonatype-central/io.github.tanin47/jmigrate/badge.png)](https://central.sonatype.com/artifact/io.github.tanin47/jmigrate)
[![Github Actions](https://github.com/tanin47/jmigrate/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/tanin47/jmigrate/actions/workflows/ci.yml?query=branch%3Amain)
[![codecov](https://codecov.io/gh/tanin47/jmigrate/graph/badge.svg?token=SHP3UDYI7Z)](https://codecov.io/gh/tanin47/jmigrate)

JMigrate is a simple and reliable pure Java library for managing database migrations.

It is excellent for desktop and android applications given the below characteristics:
- 📦 Zero external dependencies.
- 🔄 Support loading the database migration scripts from Java's resources with in a JAR file.
- 📝 Simple database migration scripts with numbering from 1.sql, 2.sql, and so on.
- 🔒 Support multi-instances with robust and reliable lock mechanism.

We purposefully don't include JDBC drivers because you probably already include it in your project.
JMigrate loads an appropriate JDBC driver at runtime. It's simpler and avoid dependency conflict.

JMigrate was initially built for [Backdoor](https://github.com/tanin47/backdoor), a modern database tool that focuses on querying and editing,
and has grown to be something much more. 

Try it out for your apps today! Please don't hesitate to open an issue if you have questions.

### Supported Databases

| Database      | Status        |
|---------------|---------------|
| PostgreSQL    | ✅ Supported   |
| SQLite        | 🔜 Coming Soon |
| MySQL         | 🔜 Coming Soon |
| MariaDB       | 🔜 Coming Soon |
| Oracle        | 🔜 Coming Soon |
| MS SQL Server | 🔜 Coming Soon |
| IBM DB2       | 🔜 Coming Soon |


How to use it
---------------

First of all, please include JMigrate as your dependency:

```
<dependency>
    <groupId>io.github.tanin47</groupId>
    <artifactId>jmigrate</artifactId>
    <version>LATEST_VERSION_CHECK_MAVEN_CENTRAL</version>
</dependency>
```

You can check the latest version here: https://central.sonatype.com/artifact/io.github.tanin47/jmigrate

Then, you put your migration scripts under `./src/resources/migrations`. 
Your scripts should be numbered as follows: `1.sql`, `2.sql`, `3.sql`, and so on.

Each migration script consists of 2 sections: up and down. The structure should look like below:

```
# --- !Ups

CREATE TABLE "user"
(
    id TEXT PRIMARY KEY DEFAULT ('user-' || gen_random_uuid()),
    username TEXT NOT NULL UNIQUE,
    hashed_password TEXT NOT NULL,
    password_expired_at TIMESTAMP
);

# --- !Downs

DROP TABLE "user";
```

Then, when you app starts, you must call JMigrate to migrate your database:

```
import tanin.jmigrate.JMigrate;

JMigrate.migrate(
  "postgres://jmigrate_test_user:test@127.0.0.1:5432/jmigrate_test", // The target database JDBC URL.
  YourApp.class, // The class that can access the resources. It should be a class that is in the same jar as the rsources that contain the migration scripts.
  "/migrations // The dir of the migration scripts within the resources
);
```

JMigrate will automatically migrate the database to the final state.


How it works
-------------

JMigrate creates 2 database tables to manage the state of the database: `jmigrate_already_migrated_script` 
and `jmigrate_lock`.

Then, it checks the current set of scripts against the state in the database tables.

If the state is considered "dirty", then it will rollback to point where the state is clean and 
reapply the subsequent migration scripts. Then, the final state will be considered clean again.

For example, if you modify `5.sql`, it'll see that `5.sql` doesn't match what was previously 
applied as `5.sql`. It'll rollback until `4.sql` (exclusive) and apply the migration scripts from `5.sql`.


How to develop
---------------

1. `./gradlew test` in order to run all tests.


How to publish the JAR file
----------------------------

This flow has been set up as the Github Actions workflow: `publish-jar`.

1. Set up `~/.jreleaser/config.toml` with `JRELEASER_MAVENCENTRAL_USERNAME` and `JRELEASER_MAVENCENTRAL_PASSWORD`
2. Run `./gradlew clean publish jreleaseDeploy`. It is *IMPORTANT* to include *clean*.


How to release a new version
-----------------------------

1. Set a new version in `build.gradle.kts` and merge.
2. Create a new release and a new tag. The tag format should be `X.Y.Z` e.g. `1.1.4`.
