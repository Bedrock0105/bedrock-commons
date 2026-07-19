package org.bedrock.common.spi.configuration;


import org.bedrock.common.spi.utils.PropertiesMarshallerUtil;
import org.bedrock.common.spi.utils.TypeHelper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @ClassName AutoConfigurationProcessor
 * @Description 自动配置处理器：负责Spring Boot自动配置类（AutoConfiguration）的扫描、索引、优先级排序及加载预处理
 *
 * <p>核心定位：
 * 替代传统自动配置类的"全量扫描"机制，通过参考Spring官方{@code spring-context-indexer}的索引化设计，
 * 优化自动配置类的发现效率，减少启动时的类路径扫描开销，同时统一处理自动配置类的加载优先级（如@AutoConfigureBefore/@AutoConfigureAfter）。
 *
 * <p>参考来源：
 * 核心设计逻辑参考Spring Framework官方组件
 * <a href="https://github.com/spring-projects/spring-framework/tree/master/spring-context-indexer">spring-context-indexer</a>，
 * 主要借鉴其"预编译索引+运行时快速定位"的思想，适配自动配置类的特殊处理场景（如条件注解生效、依赖排序）。
 *
 * <p>设计逻辑：
 * 1. 启动阶段：读取预编译的自动配置类索引（或触发首次扫描生成索引）；
 *
 * <p>注意事项：
 * <ul>
 *   <li>需配合Spring Boot的自动配置导入机制（如META-INF/spring.factories或AutoConfiguration.imports文件）使用；
 *   <li>索引文件默认存储在classpath:META-INF/spring/目录下，若修改扫描路径需同步调整索引生成位置；
 *   <li>不支持动态新增自动配置类（运行时新增的配置类无法被索引捕获，需重启应用）；
 *   <li>若自动配置类依赖第三方组件，需确保依赖类在类路径下，否则会被条件注解过滤。
 * </ul>
 */
@SupportedOptions("debug")
@SupportedAnnotationTypes("*")
public class AutoConfigurationProcessor extends AbstractProcessor {


    private static final String COMPONENT_ANNOTATION = "org.springframework.stereotype.Component";

    private static final String ENABLE_AUTO_CONFIGURATION = "org.springframework.boot.autoconfigure.EnableAutoConfiguration";

    private static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";


    private static final String AUTO_CONFIGURATION_IMPORTS_LOCATION = "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    private static final String DEVTOOLS_RESOURCE_LOCATION = "META-INF/spring-devtools.properties";


    private static final String FEIGN_CLIENT_ANNOTATION = "org.springframework.cloud.openfeign.FeignClient";

    private static final String FEIGN_AUTO_CONFIGURE_KEY = "org.springframework.cloud.openfeign.EnableFeignClients";

    private final Map<String, Set<String>> config = new HashMap<>();


    private TypeHelper typeHelper;

    /**
     * 初始化数据
     */
    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.typeHelper = new TypeHelper(env);

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processElement(roundEnv);
        if (roundEnv.processingOver()) {
            writeMetaData();
        }
        return false;
    }

    private void writeMetaData() {
        if (this.config.isEmpty()) {
            return;
        }
        try (InputStream is = getMetadataResource(FACTORIES_RESOURCE_LOCATION).openInputStream()) {
            Map<String, Set<String>> read = PropertiesMarshallerUtil.read(is);
            read.forEach((k, v) -> {
                Set<String> typeData = config.get(k);
                if (typeData != null && !typeData.isEmpty()) {
                    v.addAll(typeData);
                }
                config.put(k, v);
            });

        } catch (IOException e) {
        }
        FileObject metadataResource = null;
        try {
            metadataResource = createMetadataResource(FACTORIES_RESOURCE_LOCATION);
            try (OutputStream outputStream = metadataResource.openOutputStream()) {
                PropertiesMarshallerUtil.write(config, outputStream);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to write metadata", e);
            }

            String classesPath = metadataResource.toUri().toString().split("classes")[0];
            Path projectPath = Paths.get(new URI(classesPath)).getParent();
            String projectName = projectPath.getFileName().toString();
            FileObject devToolsFile = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", DEVTOOLS_RESOURCE_LOCATION);
            try (OutputStream out = devToolsFile.openOutputStream()) {
                PropertiesMarshallerUtil.writeDevToolsFile(projectName, out);
            }
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Failed to write metadata", e);
        }


        Set<String> autoConfiguration = this.config.get(ENABLE_AUTO_CONFIGURATION);
        if (autoConfiguration == null || autoConfiguration.isEmpty()) {
            return;
        }
        try (InputStream is = getMetadataResource(AUTO_CONFIGURATION_IMPORTS_LOCATION).openInputStream()) {
            Set<String> readAutoConfiguration = PropertiesMarshallerUtil.readAutoConfiguration(is);
            if (!readAutoConfiguration.isEmpty()) {
                autoConfiguration.addAll(readAutoConfiguration);
            }
        } catch (IOException e) {
        }
        try (OutputStream outputStream = createMetadataResource(AUTO_CONFIGURATION_IMPORTS_LOCATION).openOutputStream()) {
            PropertiesMarshallerUtil.writeAutoConfiguration(autoConfiguration, outputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write import", e);
        }
    }


    public void processElement(RoundEnvironment roundEnv) {
        /**
         * 获取节点数据
         */
        Set<? extends Element> rootElements = roundEnv.getRootElements();
        rootElements.forEach(element -> {
            if (getStereotypes(element, FEIGN_CLIENT_ANNOTATION)) {
                Set<String> typeData = config.get(FEIGN_AUTO_CONFIGURE_KEY);
                if (typeData == null || typeData.isEmpty()) {
					config.put(FEIGN_AUTO_CONFIGURE_KEY, new HashSet<>(Collections.singletonList(typeHelper.getType(element))));
				} else {
                    typeData.add(typeHelper.getType(element));
                    config.put(FEIGN_AUTO_CONFIGURE_KEY, typeData);
                }
            } else if (getStereotypes(element, COMPONENT_ANNOTATION)) {
                Set<String> typeData = config.get(ENABLE_AUTO_CONFIGURATION);
                if (typeData == null || typeData.isEmpty()) {
					config.put(ENABLE_AUTO_CONFIGURATION, new HashSet<>(Collections.singletonList(typeHelper.getType(element))));
				} else {
                    typeData.add(typeHelper.getType(element));
                    config.put(ENABLE_AUTO_CONFIGURATION, typeData);
                }
            }


        });
    }


    public boolean getStereotypes(Element element, String annotationStr) {

        ElementKind kind = element.getKind();
        if (kind != ElementKind.CLASS && kind != ElementKind.INTERFACE) {
            return false;
        }
        Set<Element> seen = new HashSet<>();
        if (collectStereotypesOnAnnotations(seen, element, annotationStr)) {
            return true;
        }
        return false;
    }


    private boolean collectStereotypesOnAnnotations(Set<Element> seen, Element element, String annotationStr) {
        for (AnnotationMirror annotation : this.typeHelper.getAllAnnotationMirrors(element)) {
            if (isIndexedAnnotation(annotation, AutoIgnore.class.getName())) {
                return false;
            }
            if (isIndexedAnnotation(annotation, annotationStr)) {
                return true;
            }
            Element next = getCandidateAnnotationElement(seen, annotation, annotationStr);
            if (next != null) {
                if (collectStereotypesOnAnnotations(seen, next, annotationStr)) {
                    return true;
                }
            }
        }
        return false;
    }


    private Element getCandidateAnnotationElement(Set<Element> seen, AnnotationMirror annotation, String annotationStr) {
        Element element = annotation.getAnnotationType().asElement();
        if (seen.contains(element)) {
            return null;
        }
        // We need to visit all indexed annotations.
        if (!isIndexedAnnotation(annotation, annotationStr)) {
            seen.add(element);
        }
        return (!element.toString().startsWith("java.lang") ? element : null);
    }


    private boolean isIndexedAnnotation(AnnotationMirror annotation, String annotationStr) {
        return annotationStr.equals(annotation.getAnnotationType().toString());
    }

    private FileObject getMetadataResource(String str) throws IOException {
        return processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", str);
    }

    private FileObject createMetadataResource(String str) throws IOException {
        return processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", str);
    }

    protected void log(String msg) {
        if (processingEnv.getOptions().containsKey("debug")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
        }
    }

    protected void error(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
