# --- !Ups

ALTER TABLE "jmigrate_test_user" ADD COLUMN "age" INT;


# --- !Downs

ALTER TABLE "jmigrate_test_user" DROP COLUMN "age";
