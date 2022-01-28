package org.apache.dolphinscheduler.plugin.alert.telegram;

import com.google.auto.service.AutoService;
import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.params.PasswordParam;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;

import java.util.Arrays;
import java.util.List;

import static org.apache.dolphinscheduler.spi.utils.Constants.*;

/**
 * Telegram Alert Channel Factory
 *
 * @author pinkhello
 */
@AutoService(AlertChannelFactory.class)
public final class TelegramAlertChannelFactory implements AlertChannelFactory {

    @Override
    public String name() {
        return "Telegram";
    }

    @Override
    public AlertChannel create() {
        return new TelegramAlertChannel();
    }

    @Override
    public List<PluginParams> params() {

        InputParam webHookParam = InputParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_WEB_HOOK, TelegramParamsConstants.TELEGRAM_WEB_HOOK)
                .setPlaceholder("telegram web hook")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam botTokenParam = InputParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_BOT_TOKEN, TelegramParamsConstants.TELEGRAM_BOT_TOKEN)
                .setPlaceholder("telegram bot token")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        InputParam chatIdParam = InputParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_CHAT_ID, TelegramParamsConstants.TELEGRAM_CHAT_ID)
                .setPlaceholder("telegram channel id")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        RadioParam parseMode = RadioParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_PARSE_MODE, TelegramParamsConstants.TELEGRAM_PARSE_MODE)
                .addParamsOptions(new ParamsOptions(TelegramAlertConstants.PARSE_MODE_TXT, TelegramAlertConstants.PARSE_MODE_TXT, false))
                .addParamsOptions(new ParamsOptions(TelegramAlertConstants.PARSE_MODE_MARKDOWN, TelegramAlertConstants.PARSE_MODE_MARKDOWN, false))
                .addParamsOptions(new ParamsOptions(TelegramAlertConstants.PARSE_MODE_MARKDOWN_V2, TelegramAlertConstants.PARSE_MODE_MARKDOWN_V2, false))
                .addParamsOptions(new ParamsOptions(TelegramAlertConstants.PARSE_MODE_HTML, TelegramAlertConstants.PARSE_MODE_HTML, false))
                .setValue(TelegramAlertConstants.PARSE_MODE_TXT)
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();

        RadioParam isEnableProxy = RadioParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_PROXY_ENABLE, TelegramParamsConstants.TELEGRAM_PROXY_ENABLE)
                .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                .setValue(STRING_FALSE)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam proxyParam = InputParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_PROXY, TelegramParamsConstants.TELEGRAM_PROXY)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam portParam = InputParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_PORT, TelegramParamsConstants.TELEGRAM_PORT)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();

        InputParam userParam = InputParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_USER, TelegramParamsConstants.TELEGRAM_USER)
                .addValidate(Validate.newBuilder()
                        .setRequired(false)
                        .build())
                .build();
        PasswordParam passwordParam = PasswordParam
                .newBuilder(TelegramParamsConstants.NAME_TELEGRAM_PASSWORD, TelegramParamsConstants.TELEGRAM_PASSWORD)
                .setPlaceholder("if enable use authentication, you need input password")
                .build();

        return Arrays.asList(webHookParam, botTokenParam, chatIdParam, parseMode, isEnableProxy, proxyParam, portParam, userParam, passwordParam);
    }


}
