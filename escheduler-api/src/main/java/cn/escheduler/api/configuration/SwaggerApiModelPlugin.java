package cn.escheduler.api.configuration;



import com.fasterxml.classmate.TypeResolver;
import io.swagger.annotations.ApiModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static springfox.documentation.schema.ResolvedTypes.*;
import static springfox.documentation.swagger.common.SwaggerPluginSupport.*;

/**
 * NOTE : not useful
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
public class SwaggerApiModelPlugin implements ModelBuilderPlugin {

    @Autowired
    private TypeResolver typeResolver;
    @Autowired
    private TypeNameExtractor typeNameExtractor;
    @Autowired
    private MessageSource messageSource;

    @Override
    public void apply(ModelContext context) {
        ApiModel annotation = AnnotationUtils.findAnnotation(forClass(context), ApiModel.class);
        if (annotation != null) {
            List<ModelReference> modelRefs = new ArrayList<ModelReference>();
            for (Class<?> each : annotation.subTypes()) {
                modelRefs.add(modelRefFactory(context, typeNameExtractor)
                        .apply(typeResolver.resolve(each)));
            }
            Locale locale = LocaleContextHolder.getLocale();

            context.getBuilder()
                    .description(messageSource.getMessage(annotation.description(), null, locale))
                    .discriminator(annotation.discriminator())
                    .subTypes(modelRefs);
        }
    }

    private Class<?> forClass(ModelContext context) {
        return typeResolver.resolve(context.getType()).getErasedType();
    }


//    @Override
//    public boolean supports(DocumentationType delimiter) {
//        return pluginDoesApply(delimiter);
//    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}

