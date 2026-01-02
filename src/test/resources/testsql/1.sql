# --- !Ups

CREATE TABLE "jmigrate_test_user"
(
    id TEXT PRIMARY KEY DEFAULT ('user-' || gen_random_uuid()),
    username TEXT NOT NULL,
    hashed_password TEXT NOT NULL,
    password_expired_at TIMESTAMP
);

# --- !Downs

DROP TABLE "jmigrate_test_user";
