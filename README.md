JMigrate: database migration management for Java apps
==============================================================

[![Sonatype Central](https://maven-badges.sml.io/sonatype-central/io.github.tanin47/jmigrate/badge.png)](https://central.sonatype.com/artifact/io.github.tanin47/jmigrate)
[![Github Actions](https://github.com/tanin47/jmigrate/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/tanin47/jmigrate/actions/workflows/ci.yml?query=branch%3Amain)
[![codecov](https://codecov.io/gh/tanin47/jmigrate/graph/badge.svg?token=SHP3UDYI7Z)](https://codecov.io/gh/tanin47/jmigrate)

JMigrate is a simple and reliable pure Java library for managing database migrations.

It is excellent for desktop applications given the below characteristics:
- 📦 Zero external dependencies.
- 🔄 Support loading the database migration scripts from Java's resources with in a JAR file.
- 📝 Simple database migration scripts with numbering from 1.sql, 2.sql, and so on.
- 🔒 Support multi-instances with robust and reliable lock mechanism.
- ⚡ No separate CLI execution. You can run JMigrate when your application starts. It's simple to use.

We purposefully don't include JDBC drivers because you probably already include it in your project.
JMigrate loads an appropriate JDBC driver at runtime. It's simpler and avoid dependency conflict.

JMigrate was initially built for [Backdoor](https://github.com/tanin47/backdoor), a modern database tool that focuses on querying and editing,
and has grown to be something much more. 

Try it out for your apps today! Please don't hesitate to open an issue if you have questions.

### Supported Databases

| Database      | Status         |
|---------------|----------------|
| PostgreSQL    | ✅ Supported    |
| SQLite        | ✅ Supported    |
| MySQL         | 🔜 Coming Soon |
| MariaDB       | 🔜 Coming Soon |
| Oracle        | 🔜 Coming Soon |
| MS SQL Server | 🔜 Coming Soon |
| IBM DB2       | 🔜 Coming Soon |


Recommended Development Practice
---------------------------------

JMigrate is a painless and low-headache way of managing database schemas. It enforces that your schema changes are tracked by git.

When an engineer gets a new version of the source code and runs the app, their database schemas are automatically updated.
In dev, we recommend setting `allowExecutingDownScripts` to false, so engineers aren't blocked when the down scripts (or revert scripts) need to be executed.

When the code is deployed to production, the database schemas are updated automatically. In prod, we strongly recommend
setting `allowExecutingDownScripts` to false in order to ensure there's no data loss. Once a migration script is deployed,
you should never modify it. If you need to make a schema change or revert the previous schema change, then you should
make a new migration script for that.


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
  "/migrations // The dir of the migration scripts within the resources,
  false // `allowExecutingDownScripts` indicates whether to allow the down script execution. Recommendation: false for production and true for development.
);
```

JMigrate will automatically migrate the database to the final state.

If `allowExecutingDownScripts` is false (recommended for production), and the down scripts need to be executed, an exception of `ExecutingDownScriptForbiddenException` will be thrown.


How it works
-------------

JMigrate creates 2 database tables to manage the state of the database: `jmigrate_already_migrated_script` 
and `jmigrate_lock`.

Then, it checks the current set of scripts against the state in the database tables.

If the state is considered "dirty", then it will rollback to point where the state is clean and 
re-apply the later migration scripts. Then, the final state will be considered clean again.

For example, if you modify `5.sql`, it'll see that `5.sql` doesn't match what was previously 
applied as `5.sql`. It'll rollback until `4.sql` (exclusive) and apply the migration scripts from `5.sql`.

Generally, you should never modify prior migration scripts if they have been deployed to production where 
`allowExecutingDownScripts` is false. We emphasize again that `allowExecutingDownScripts` should be false in production.

If you accidentally modify a prior migration script, we recommend reverting the modification and re-deploy.

If you really want the modification, then you will need to modify the record in `jmigrate_already_migrated_script` to
match your modified script.

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
