package cn.escheduler.api.configuration;

import java.util.List;
import java.util.Locale;
import com.fasterxml.classmate.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
public class SwaggerI18nPlugin implements OperationBuilderPlugin {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerI18nPlugin.class);

    @Autowired
    private MessageSource messageSource;

    @Override
    public void apply(OperationContext context) {

        Locale locale = LocaleContextHolder.getLocale();

        List<ApiOperation> list = context.findAllAnnotations(ApiOperation.class);
        if (list.size() > 0) {
            for(ApiOperation api : list){
                context.operationBuilder().summary(messageSource.getMessage(api.value(), null, locale));
                context.operationBuilder().notes(messageSource.getMessage(api.notes(), null, locale));
            }
        }


    }


    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

}
