ALTER TABLE t_ds_schedules
    ADD COLUMN misfire_policy smallint NOT NULL DEFAULT 2,
    ADD COLUMN trigger_type smallint NOT NULL DEFAULT 0;
