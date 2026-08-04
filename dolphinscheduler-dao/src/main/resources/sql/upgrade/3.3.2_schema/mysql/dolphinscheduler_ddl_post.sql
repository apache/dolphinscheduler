ALTER TABLE `t_ds_schedules`
    ADD COLUMN `missed_fire_policy` tinyint NOT NULL DEFAULT '1' COMMENT 'missed fire policy: 0 skip missed, 1 fire once now, 2 fire all missed' AFTER `crontab`;
