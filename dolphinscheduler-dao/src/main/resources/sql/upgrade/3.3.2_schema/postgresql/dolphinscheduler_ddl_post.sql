ALTER TABLE t_ds_schedules
    ADD COLUMN missed_fire_policy smallint NOT NULL DEFAULT 1;
