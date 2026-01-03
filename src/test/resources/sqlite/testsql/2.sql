# --- !Ups

ALTER TABLE "jmigrate_test_user" DROP COLUMN "password_expired_at";


# --- !Downs

ALTER TABLE "jmigrate_test_user" ADD COLUMN "password_expired_at" TIMESTAMP;
