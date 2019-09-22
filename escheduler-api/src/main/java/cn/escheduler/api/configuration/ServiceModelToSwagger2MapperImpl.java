/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.api.configuration;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.swagger.models.*;
import io.swagger.models.parameters.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiListing;
import springfox.documentation.service.Documentation;
import springfox.documentation.service.ResourceListing;
import springfox.documentation.swagger2.mappers.*;

import java.util.*;

import static com.google.common.collect.Maps.newTreeMap;

/**
 * application configuration
 */
@Component(value = "ServiceModelToSwagger2Mapper")
@Primary
public class ServiceModelToSwagger2MapperImpl extends ServiceModelToSwagger2Mapper {


    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ParameterMapper parameterMapper;
    @Autowired
    private SecurityMapper securityMapper;
    @Autowired
    private LicenseMapper licenseMapper;
    @Autowired
    private VendorExtensionsMapper vendorExtensionsMapper;

    @Autowired
    private MessageSource messageSource;

    @Override
    public Swagger mapDocumentation(Documentation from) {

        if (from == null) {
            return null;
        }

        Swagger swagger = new Swagger();

        swagger.setVendorExtensions(vendorExtensionsMapper.mapExtensions(from.getVendorExtensions()));
        swagger.setSchemes(mapSchemes(from.getSchemes()));
        swagger.setPaths(mapApiListings(from.getApiListings()));
        swagger.setHost(from.getHost());
        swagger.setDefinitions(modelsFromApiListings( from.getApiListings() ) );
        swagger.setSecurityDefinitions(securityMapper.toSecuritySchemeDefinitions(from.getResourceListing()));
        ApiInfo info = fromResourceListingInfo(from);
        if (info != null) {
            swagger.setInfo(mapApiInfo(info));
        }
        swagger.setBasePath(from.getBasePath());
        swagger.setTags(tagSetToTagList(from.getTags()));
        List<String> list2 = from.getConsumes();
        if (list2 != null) {
            swagger.setConsumes(new ArrayList<String>(list2));
        } else {
            swagger.setConsumes(null);
        }
        List<String> list3 = from.getProduces();
        if (list3 != null) {
            swagger.setProduces(new ArrayList<String>(list3));
        } else {
            swagger.setProduces(null);
        }

        return swagger;
    }


    @Override
    protected Info mapApiInfo(ApiInfo from) {

        if (from == null) {
            return null;
        }

        Info info = new Info();

        info.setLicense(licenseMapper.apiInfoToLicense(from));
        info.setVendorExtensions(vendorExtensionsMapper.mapExtensions(from.getVendorExtensions()));
        info.setTermsOfService(from.getTermsOfServiceUrl());
        info.setContact(map(from.getContact()));
        info.setDescription(from.getDescription());
        info.setVersion(from.getVersion());
        info.setTitle(from.getTitle());

        return info;
    }

    @Override
    protected Contact map(springfox.documentation.service.Contact from) {

        if (from == null) {
            return null;
        }

        Contact contact = new Contact();

        contact.setName(from.getName());
        contact.setUrl(from.getUrl());
        contact.setEmail(from.getEmail());

        return contact;
    }

    @Override
    protected io.swagger.models.Operation mapOperation(springfox.documentation.service.Operation from) {

        if (from == null) {
            return null;
        }

        Locale locale = LocaleContextHolder.getLocale();

        io.swagger.models.Operation operation = new io.swagger.models.Operation();

        operation.setSecurity(mapAuthorizations(from.getSecurityReferences()));
        operation.setVendorExtensions(vendorExtensionsMapper.mapExtensions(from.getVendorExtensions()));
        operation.setDescription(messageSource.getMessage(from.getNotes(), null, from.getNotes(), locale));
        operation.setOperationId(from.getUniqueId());
        operation.setResponses(mapResponseMessages(from.getResponseMessages()));
        operation.setSchemes(stringSetToSchemeList(from.getProtocol()));
        Set<String> tagsSet = new HashSet<>(1);

        if(from.getTags() != null && from.getTags().size() > 0){

            List<String> list = new ArrayList<String>(tagsSet.size());

            Iterator<String> it = from.getTags().iterator();
            while(it.hasNext())
            {
               String tag = it.next();
               list.add(StringUtils.isNotBlank(tag) ? messageSource.getMessage(tag, null, tag, locale) : " ");
            }

            operation.setTags(list);
        }else {
            operation.setTags(null);
        }

        operation.setSummary(from.getSummary());
        Set<String> set1 = from.getConsumes();
        if (set1 != null) {
            operation.setConsumes(new ArrayList<String>(set1));
        } else {
            operation.setConsumes(null);
        }

        Set<String> set2 = from.getProduces();
        if (set2 != null) {
            operation.setProduces(new ArrayList<String>(set2));
        } else {
            operation.setProduces(null);
        }


        operation.setParameters(parameterListToParameterList(from.getParameters()));
        if (from.getDeprecated() != null) {
            operation.setDeprecated(Boolean.parseBoolean(from.getDeprecated()));
        }

        return operation;
    }

    @Override
    protected Tag mapTag(springfox.documentation.service.Tag from) {

        if (from == null) {
            return null;
        }

        Locale locale = LocaleContextHolder.getLocale();

        Tag tag = new Tag();

        tag.setVendorExtensions(vendorExtensionsMapper.mapExtensions(from.getVendorExtensions()));
        tag.setName(messageSource.getMessage(from.getName(), null, from.getName(), locale));
        tag.setDescription(from.getDescription());

        return tag;
    }


    private ApiInfo fromResourceListingInfo(Documentation documentation) {

        if (documentation == null) {
            return null;
        }
        ResourceListing resourceListing = documentation.getResourceListing();
        if (resourceListing == null) {
            return null;
        }
        ApiInfo info = resourceListing.getInfo();
        if (info == null) {
            return null;
        }
        return info;
    }

    protected List<Tag> tagSetToTagList(Set<springfox.documentation.service.Tag> set) {

        if (set == null) {
            return null;
        }

        List<Tag> list = new ArrayList<Tag>(set.size());
        for (springfox.documentation.service.Tag tag : set) {
            list.add(mapTag(tag));
        }

        return list;
    }

    protected List<Scheme> stringSetToSchemeList(Set<String> set) {
        if (set == null) {
            return null;
        }

        List<Scheme> list = new ArrayList<Scheme>(set.size());
        for (String string : set) {
            list.add(Enum.valueOf(Scheme.class, string));
        }

        return list;
    }

    protected List<Parameter> parameterListToParameterList(List<springfox.documentation.service.Parameter> list) {
        if (list == null) {
            return null;
        }

        List<Parameter> list1 = new ArrayList<Parameter>(list.size());

        Locale locale = LocaleContextHolder.getLocale();

        for (springfox.documentation.service.Parameter param : list) {
            String description = messageSource.getMessage(param.getDescription(), null, param.getDescription(), locale);

            springfox.documentation.service.Parameter parameter = new springfox.documentation.service.Parameter(param.getName(),description,param.getDefaultValue(),param.isRequired(),param.isAllowMultiple(),param.isAllowEmptyValue(),param.getModelRef(),param.getType(),param.getAllowableValues(),param.getParamType(),param.getParamAccess(),param.isHidden(),param.getPattern(),param.getCollectionFormat(),param.getOrder(),param.getScalarExample(),param.getExamples() ,param.getVendorExtentions());
            list1.add(parameterMapper.mapParameter(parameter));
        }

        return list1;
    }


    Map<String, Model> modelsFromApiListings(Multimap<String, ApiListing> apiListings) {
        Map<String, springfox.documentation.schema.Model> definitions = newTreeMap();
        for (ApiListing each : apiListings.values()) {
            definitions.putAll(each.getModels());
        }
        return modelMapper.mapModels(definitions);
    }






//
//
//
//    private static final VendorExtensionsMapper vendorMapper = new VendorExtensionsMapper();
//
//
//
//    public Parameter mapParameter(springfox.documentation.service.Parameter source) {
//        Parameter bodyParameter = bodyParameter(source);
//        return SerializableParameterFactories.create(source).or(bodyParameter);
//    }
//
//    private Parameter bodyParameter(springfox.documentation.service.Parameter source) {
//        BodyParameter parameter = new BodyParameter()
//                .description(source.getDescription())
//                .name(source.getName())
//                .schema(fromModelRef(source.getModelRef()));
//        parameter.setIn(source.getParamType());
//        parameter.setAccess(source.getParamAccess());
//        parameter.setPattern(source.getPattern());
//        parameter.setRequired(source.isRequired());
//        parameter.getVendorExtensions().putAll(vendorMapper.mapExtensions(source.getVendorExtentions()));
//        for (Map.Entry<String, Collection<Example>> each : source.getExamples().asMap().entrySet()) {
//            Optional<Example> example = FluentIterable.from(each.getValue()).first();
//            if (example.isPresent() && example.get().getValue() != null) {
//                parameter.addExample(each.getKey(), String.valueOf(example.get().getValue()));
//            }
//        }
//
//        //TODO: swagger-core Body parameter does not have an enum property
//        return parameter;
//    }
//
//    Model fromModelRef(ModelReference modelRef) {
//        if (modelRef.isCollection()) {
//            if (modelRef.getItemType().equals("byte")) {
//                ModelImpl baseModel = new ModelImpl();
//                baseModel.setType("string");
//                baseModel.setFormat("byte");
//                return maybeAddAllowableValuesToParameter(baseModel, modelRef.getAllowableValues());
//            } else if (modelRef.getItemType().equals("file")) {
//                ArrayModel files = new ArrayModel();
//                files.items(new FileProperty());
//                return files;
//            }
//            ModelReference itemModel = modelRef.itemModel().get();
//            return new ArrayModel()
//                    .items(maybeAddAllowableValues(itemTypeProperty(itemModel), itemModel.getAllowableValues()));
//        }
//        if (modelRef.isMap()) {
//            ModelImpl baseModel = new ModelImpl();
//            ModelReference itemModel = modelRef.itemModel().get();
//            baseModel.additionalProperties(
//                    maybeAddAllowableValues(
//                            itemTypeProperty(itemModel),
//                            itemModel.getAllowableValues()));
//            return baseModel;
//        }
//        if (isBaseType(modelRef.getType())) {
//            Property property = property(modelRef.getType());
//            ModelImpl baseModel = new ModelImpl();
//            baseModel.setType(property.getType());
//            baseModel.setFormat(property.getFormat());
//            return maybeAddAllowableValuesToParameter(baseModel, modelRef.getAllowableValues());
//
//        }
//        return new RefModel(modelRef.getType());
//    }
//
//
//   private static class Properties {
//        private static final Map<String, Function<String, ? extends Property>> typeFactory
//                = ImmutableMap.<String, Function<String, ? extends Property>>builder()
//                .put("int", newInstanceOf(IntegerProperty.class))
//                .put("long", newInstanceOf(LongProperty.class))
//                .put("float", newInstanceOf(FloatProperty.class))
//                .put("double", newInstanceOf(DoubleProperty.class))
//                .put("string", newInstanceOf(StringProperty.class))
//                .put("boolean", newInstanceOf(BooleanProperty.class))
//                .put("date", newInstanceOf(DateProperty.class))
//                .put("date-time", newInstanceOf(DateTimeProperty.class))
//                .put("bigdecimal", newInstanceOf(DecimalProperty.class))
//                .put("biginteger", newInstanceOf(BaseIntegerProperty.class))
//                .put("uuid", newInstanceOf(UUIDProperty.class))
//                .put("object", newInstanceOf(ObjectProperty.class))
//                .put("byte", bytePropertyFactory())
//                .put("__file", filePropertyFactory())
//                .build();
//
//        private Properties() {
//            throw new UnsupportedOperationException();
//        }
//
//        public static Property property(final String typeName) {
//            String safeTypeName = nullToEmpty(typeName);
//            Function<String, Function<String, ? extends Property>> propertyLookup
//                    = forMap(typeFactory, voidOrRef(safeTypeName));
//            return propertyLookup.apply(safeTypeName.toLowerCase()).apply(safeTypeName);
//        }
//
//        public static Property property(final ModelReference modelRef) {
//            if (modelRef.isMap()) {
//                return new MapProperty(property(modelRef.itemModel().get()));
//            } else if (modelRef.isCollection()) {
//                if ("byte".equals(modelRef.itemModel().transform(toTypeName()).or(""))) {
//                    return new ByteArrayProperty();
//                }
//                return new ArrayProperty(
//                        maybeAddAllowableValues(itemTypeProperty(modelRef.itemModel().get()), modelRef.getAllowableValues()));
//            }
//            return property(modelRef.getType());
//        }
//
//        private static Function<? super ModelReference, String> toTypeName() {
//            return new Function<ModelReference, String>() {
//                @Override
//                public String apply(ModelReference input) {
//                    return input.getType();
//                }
//            };
//        }
//
//        public static Property itemTypeProperty(ModelReference paramModel) {
//            if (paramModel.isCollection()) {
//                return new ArrayProperty(
//                        maybeAddAllowableValues(itemTypeProperty(paramModel.itemModel().get()), paramModel.getAllowableValues()));
//            }
//            return property(paramModel.getType());
//        }
//
//        private static <T extends Property> Function<String, T> newInstanceOf(final Class<T> clazz) {
//            return new Function<String, T>() {
//                @Override
//                public T apply(String input) {
//                    try {
//                        return clazz.newInstance();
//                    } catch (Exception e) {
//                        //This is bad! should never come here
//                        throw new IllegalStateException(e);
//                    }
//                }
//            };
//        }
//
//        static Ordering<String> defaultOrdering(Map<String, ModelProperty> properties) {
//            return Ordering.from(byPosition(properties)).compound(byName());
//        }
//
//        private static Function<String, ? extends Property> voidOrRef(final String typeName) {
//            return new Function<String, Property>() {
//                @Override
//                public Property apply(String input) {
//                    if (typeName.equalsIgnoreCase("void")) {
//                        return null;
//                    }
//                    return new RefProperty(typeName);
//                }
//            };
//        }
//
//        private static Function<String, ? extends Property> bytePropertyFactory() {
//            return new Function<String, Property>() {
//                @Override
//                public Property apply(String input) {
//                    final IntegerProperty integerProperty = new IntegerProperty();
//                    integerProperty.setFormat("int32");
//                    integerProperty.setMaximum(BigDecimal.valueOf(Byte.MAX_VALUE));
//                    integerProperty.setMinimum(BigDecimal.valueOf(Byte.MIN_VALUE));
//                    return integerProperty;
//                }
//            };
//        }
//
//        private static Function<String, ? extends Property> filePropertyFactory() {
//            return new Function<String, Property>() {
//                @Override
//                public Property apply(String input) {
//                    return new FileProperty();
//                }
//            };
//        }
//
//        private static Comparator<String> byName() {
//            return new Comparator<String>() {
//                @Override
//                public int compare(String first, String second) {
//                    return first.compareTo(second);
//                }
//            };
//        }
//
//        private static Comparator<String> byPosition(final Map<String, ModelProperty> modelProperties) {
//            return new Comparator<String>() {
//                @Override
//                public int compare(String first, String second) {
//                    ModelProperty p1 = modelProperties.get(first);
//                    ModelProperty p2 = modelProperties.get(second);
//                    return Ints.compare(p1.getPosition(), p2.getPosition());
//                }
//            };
//        }
//
//        static Predicate<Map.Entry<String, ModelProperty>> voidProperties() {
//            return new Predicate<Map.Entry<String, ModelProperty>>() {
//                @Override
//                public boolean apply(Map.Entry<String, ModelProperty> input) {
//                    return isVoid(input.getValue().getType())
//                            || collectionOfVoid(input.getValue().getType())
//                            || arrayTypeOfVoid(input.getValue().getType().getArrayElementType());
//                }
//            };
//        }
//
//        private static boolean arrayTypeOfVoid(ResolvedType arrayElementType) {
//            return arrayElementType != null && isVoid(arrayElementType);
//        }
//
//        private static boolean collectionOfVoid(ResolvedType type) {
//            return isContainerType(type) && isVoid(collectionElementType(type));
//        }
}
