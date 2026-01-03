# --- !Ups

CREATE TABLE "jmigrate_test_user"
(
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL,
    hashed_password TEXT NOT NULL,
    password_expired_at TIMESTAMP
);

# --- !Downs

DROP TABLE "jmigrate_test_user";
