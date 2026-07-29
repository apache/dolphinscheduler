ALTER TABLE `t_ds_schedules`
    ADD COLUMN `misfire_policy` tinyint NOT NULL DEFAULT '2' COMMENT 'misfire policy: 0 do nothing, 1 fire and proceed, 2 ignore misfires' AFTER `crontab`,
    ADD COLUMN `trigger_type` tinyint NOT NULL DEFAULT '0' COMMENT 'schedule trigger type: 0 cron, 1 interval' AFTER `misfire_policy`;
