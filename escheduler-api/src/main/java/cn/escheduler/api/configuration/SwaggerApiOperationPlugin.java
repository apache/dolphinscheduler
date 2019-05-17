package cn.escheduler.api.configuration;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.spring.web.readers.operation.DefaultTagsProvider;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.*;
import static springfox.documentation.service.Tags.emptyTags;


@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 10)
public class SwaggerApiOperationPlugin implements OperationBuilderPlugin {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerApiOperationPlugin.class);

    @Autowired
    private DescriptionResolver descriptions;
    @Autowired
    private DefaultTagsProvider tagsProvider;

    @Autowired
    private MessageSource messageSource;

    @Override
    public void apply(OperationContext context) {

        Locale locale = LocaleContextHolder.getLocale();

        Set<String> defaultTags = tagsProvider.tags(context);
        Sets.SetView<String> tags = union(operationTags(context), controllerTags(context));
        if (tags.isEmpty()) {
            context.operationBuilder().tags(defaultTags);
        } else {
            context.operationBuilder().tags(tags);
        }


        Optional<ApiOperation> apiOperationAnnotation = context.findAnnotation(ApiOperation.class);
        if (apiOperationAnnotation.isPresent()) {
            ApiOperation operation = apiOperationAnnotation.get();

            if (StringUtils.hasText(operation.nickname())) {
                // Populate the value of nickname annotation into uniqueId
                context.operationBuilder().uniqueId(operation.nickname());
                context.operationBuilder().codegenMethodNameStem(operation.nickname());
            }

            if (StringUtils.hasText(apiOperationAnnotation.get().notes())) {
                context.operationBuilder().notes(descriptions.resolve(messageSource.getMessage(apiOperationAnnotation.get().notes(), null, "", locale)));
            }

            if (apiOperationAnnotation.get().position() > 0) {
                context.operationBuilder().position(apiOperationAnnotation.get().position());
            }

            if (StringUtils.hasText(apiOperationAnnotation.get().value())) {
                context.operationBuilder().summary(descriptions.resolve(apiOperationAnnotation.get().value()));
            }

            context.operationBuilder().consumes(asSet(nullToEmpty(apiOperationAnnotation.get().consumes())));
            context.operationBuilder().produces(asSet(nullToEmpty(apiOperationAnnotation.get().produces())));
        }




    }


    private Set<String> controllerTags(OperationContext context) {
        Optional<Api> controllerAnnotation = context.findControllerAnnotation(Api.class);
        return controllerAnnotation.transform(tagsFromController()).or(Sets.<String>newHashSet());
    }

    private Set<String> operationTags(OperationContext context) {
        Optional<ApiOperation> annotation = context.findAnnotation(ApiOperation.class);
        return annotation.transform(tagsFromOperation()).or(Sets.<String>newHashSet());
    }

    private Function<ApiOperation, Set<String>> tagsFromOperation() {
        return new Function<ApiOperation, Set<String>>() {
            @Override
            public Set<String> apply(ApiOperation input) {
                Set<String> tags = newTreeSet();
                tags.addAll(from(newArrayList(input.tags())).filter(emptyTags()).toSet());
                return tags;
            }
        };
    }

    private Function<Api, Set<String>> tagsFromController() {
        return new Function<Api, Set<String>>() {
            @Override
            public Set<String> apply(Api input) {
                Set<String> tags = newTreeSet();
                tags.addAll(from(newArrayList(input.tags())).filter(emptyTags()).toSet());
                return tags;
            }
        };
    }
    private Set<String> asSet(String mediaTypes) {
        return newHashSet(Splitter.on(',')
                .trimResults()
                .omitEmptyStrings()
                .splitToList(mediaTypes));
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return SwaggerPluginSupport.pluginDoesApply(delimiter);
//        return true;
    }

}
