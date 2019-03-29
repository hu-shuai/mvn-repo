package com.hs.fastService;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.hs.fastService.entities.Model;
import com.hs.fastService.enums.Connector;
import com.hs.fastService.enums.Operation;
import com.hs.fastService.util.AppUtil;
import com.hs.fastService.util.JsonUtil;
import com.hs.fastService.util.LogUtil;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.*;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
@ComponentScan
public class WebConfigurer implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 格式转换
        registry.addFormatterForFieldType(Date.class, new DateFormatter("yyyy-MM-dd HH:mm:ss"));
        registry.addFormatterForFieldType(Date.class, new DateFormatter("yyyy-MM-dd"));

        // 数据转换
        registry.addConverter(new GenericConverter() {
            @Nullable
            @Override
            public Set<ConvertiblePair> getConvertibleTypes() {
                Set<ConvertiblePair> set = new HashSet<>(4);
                set.add(new ConvertiblePair(String.class, Collection.class));
                set.add(new ConvertiblePair(String.class, Object[].class));
                set.add(new ConvertiblePair(Object.class, Connector.class));
                set.add(new ConvertiblePair(Object.class, Operation.class));
                set.add(new ConvertiblePair(String.class, Enum.class));
                set.add(new ConvertiblePair(Map.class, Model.class));
                set.add(new ConvertiblePair(String.class, Map.class));
                set.add(new ConvertiblePair(String.class, Model.class));
                return set;
            }
            @Nullable
            @Override
            public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
                try {
                    ResolvableType type = targetType.getResolvableType();
                    if (source instanceof Map) {
                        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(type.getRawClass());
                        beanWrapper.setConversionService(AppUtil.getBean(ConversionService.class));
                        beanWrapper.setPropertyValues(new MutablePropertyValues((Map<?, ?>) source), true, true);
                        return beanWrapper.getWrappedInstance();
                    } else if(type.getRawClass() == Connector.class) {
                        return Connector.ofNameOrValue(source);
                    } else if(type.getRawClass() == Operation.class) {
                        return Operation.ofNameOrValue(source);
                    } else if (type.getRawClass().isEnum()) {
                        return Enum.valueOf((Class)type.getRawClass(), (String)source);
                    } else if (Collection.class.isAssignableFrom(type.getRawClass())) {
                        try {
                            JavaType javaType = JsonUtil.getJsonParser()
                                    .getTypeFactory()
                                    .constructCollectionType(List.class, type.getSuperType().getGeneric(0).getRawClass());
                            return JsonUtil.getJsonParser().readValue((String) source, javaType);
                        } catch (Exception ignored) {
                        }
                    } else if (Map.class.isAssignableFrom(type.getRawClass())) {
                        try {
                            JavaType javaType = JsonUtil.getJsonParser()
                                    .getTypeFactory()
                                    .constructMapType(Map.class, type.getSuperType().getGeneric(0).getRawClass(), type.getSuperType().getGeneric(1).getRawClass());
                            return JsonUtil.getJsonParser().readValue((String) source, javaType);
                        } catch (Exception ignored) {
                        }
                    }
                    return JsonUtil.getJsonParser().readValue((String) source, type.getRawClass());
                } catch (Exception e) {
                    LogUtil.error("请求参数转换失败：", e);
                    throw new RuntimeException("参数转换失败！");
                }
            }
        });
    }

    @Bean("requestConfig")
    @ConditionalOnMissingBean(name = {"requestConfig"})
    public Map<String, RequestConfig> getRequestConfig() {
        ClassPathResource resource = new ClassPathResource("request.yml");
        if (!resource.exists()) {
            return null;
        }
        YamlMapFactoryBean factoryBean = new YamlMapFactoryBean();
        factoryBean.setResources(resource);
        Map<String, Object> map = factoryBean.getObject();
        Map<String, RequestConfig> configMap = new HashMap<>(map.size());
        map.forEach((key, value) -> {
            RequestConfig config = new RequestConfig();
            config.setName(key);
            Map<String, Object> detailMap = (Map<String, Object>) value;
            config.setType(RequestConfig.RequestType.valueOf((String) detailMap.get("type")));
            try {
                String entityClass = (String) detailMap.get("entityClass");
                config.setEntityClass(Class.forName(entityClass));
                String serviceClass = (String) detailMap.get("serviceClass");
                if (!StringUtils.isEmpty(serviceClass)) {
                    Class clazz = Class.forName(serviceClass);
                    config.setServiceClass(clazz);
                    String methodName = (String) detailMap.get("methodName");
                    try {
                        config.setCustomMethod(clazz.getMethod(methodName, Map.class));
                    } catch (NoSuchMethodException e) {
                        LogUtil.error("接口配置 {} 错误： 在 serviceClass: {} 中没有找到自定义的 {} 方法，请检查request.yml配置", key, serviceClass, methodName);
                        throw new RuntimeException(e);
                    }
                }
            } catch (ClassNotFoundException e) {
                LogUtil.error("接口配置 {} 错误： 对应的entityClass或serviceClass, 没有找到，请检查request.yml配置", key);
                throw new RuntimeException(e);
            }
            config.setExtraInfo((List<Map<String, Object>>) detailMap.get("extraInfo"));
            config.setRequestParams((Map<String, Object>) detailMap.get("requestParams"));
            config.setResponseExcept((Map<String, Object>) detailMap.get("responseExcept"));
            configMap.put(key, config);
        });

        return configMap;
    }

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(UTF_8));
        return restTemplate;
    }

    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        return new StringHttpMessageConverter(UTF_8);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customJackson() {
        return jacksonObjectMapperBuilder -> {
            jacksonObjectMapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL);
            jacksonObjectMapperBuilder.failOnUnknownProperties(false);
            jacksonObjectMapperBuilder.featuresToEnable(ALLOW_UNQUOTED_FIELD_NAMES);
            jacksonObjectMapperBuilder.featuresToEnable(ALLOW_SINGLE_QUOTES);
            SimpleFilterProvider filterProvider = new SimpleFilterProvider();
//            filterProvider.addFilter("", SimpleBeanPropertyFilter.serializeAllExcept());
            jacksonObjectMapperBuilder.filters(filterProvider);
        };
    }
}
