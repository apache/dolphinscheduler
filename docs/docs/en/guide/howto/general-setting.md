# General Setting

## Language

DolphinScheduler supports two types of built-in language which include `English` and `Chinese`. You could click the button
on the top control bar named `English` and `Chinese` and change it to another one when you want to switch the language.
The entire DolphinScheduler page language will shift when you switch the language selection.

## Theme

DolphinScheduler supports two types of built-in theme which include `Dark` and `Light`. When you want to change the theme
of DolphinScheduler, all you have to do is click the button named `Dark`(or `Light`) on the top control bar and on the left
of to [language](#language) control button.

## Time Zone

DolphinScheduler support time zone setting. 

Server Time Zone

Use `bin/dolphinshceduler_daemon.sh` to start server, the default time zone is UTC, you could update `SPRING_JACKSON_TIME_ZONE` in `bin/env/dolphinscheduler_env.sh`, such as `export SPRING_JACKSON_TIME_ZONE=${SPRING_JACKSON_TIME_ZONE:-Asia/Shanghai}`.<br>
If you start server in idea, the default time zone is your local time zone, you could add the JVM parameter to update server time zone, such as `-Duser.timezone=UTC`.

User Time zone

The user's default time zone is based on the time zone in which you run the DolphinScheduler service.You could
click the button on the right of the [language](#language) button and then click `Choose timeZone` to choose the time zone
you want to switch. All time related components will adjust their time zone according to the time zone setting you select.


