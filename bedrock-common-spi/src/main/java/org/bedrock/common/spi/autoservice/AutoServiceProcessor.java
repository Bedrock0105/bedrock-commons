package org.bedrock.common.spi.autoservice;


import org.bedrock.common.spi.utils.TypeHelper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Processes {@link AutoService} annotations and generates the service provider
 * configuration files described in {@link ServiceLoader}.
 * <p>
 * Processor Options:<ul>
 * <li>{@code -Adebug} - turns on debug statements</li>
 * <li>{@code -Averify=true} - turns on extra verification</li>
 * </ul>
 */
@SupportedOptions({"debug"})
public class AutoServiceProcessor extends AbstractProcessor {

    private static final String MISSING_SERVICES_ERROR = "No service interfaces provided for element!";

    private final Map<String, Set<String>> providers = new HashMap<>();

    private TypeHelper typeHelper;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(AutoService.class.getName());
    }

    /**
     * <ol>
     *  <li> For each class annotated with {@link AutoService}<ul>
     *      <li> Verify the {@link AutoService} interface value is correct
     *      <li> Categorize the class by its service interface
     *      </ul>
     *
     *  <li> For each {@link AutoService} interface <ul>
     *       <li> Create a file named {@code META-INF/services/<interface>}
     *       <li> For each {@link AutoService} annotated class for this interface <ul>
     *           <li> Create an entry in the file
     *           </ul>
     *       </ul>
     * </ol>
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            processImpl(annotations, roundEnv);
        } catch (Exception e) {
            // We don't allow exceptions of any kind to propagate to the compiler
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            fatalError(writer.toString());
        }
        return false;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.typeHelper = new TypeHelper(processingEnv);
    }

    private void processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            generateConfigFiles();
        } else {
            processAnnotations(annotations, roundEnv);
        }
    }


    private void generateConfigFiles() {
        Filer filer = processingEnv.getFiler();

        for (String providerInterface : providers.keySet()) {
            String resourceFile = "META-INF/services/" + providerInterface;
            log("Working on resource file: " + resourceFile);
            try {
                SortedSet<String> allServices = new TreeSet<>();
                try {

                    FileObject existingFile =
                            filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceFile);
                    log("Looking for existing resource file at " + existingFile.toUri());
                    Set<String> oldServices = ServicesFiles.readServiceFile(existingFile.openInputStream());
                    log("Existing service entries: " + oldServices);
                    allServices.addAll(oldServices);
                } catch (IOException e) {
                    // According to the javadoc, Filer.getResource throws an exception
                    // if the file doesn't already exist.  In practice this doesn't
                    // appear to be the case.  Filer.getResource will happily return a
                    // FileObject that refers to a non-existent file but will throw
                    // IOException if you try to open an input stream for it.
                    log("Resource file did not already exist.");
                }

                Set<String> newServices = providers.get(providerInterface);
                if (!allServices.addAll(newServices)) {
                    log("No new service entries being added.");
                    continue;
                }

                log("New service file contents: " + allServices);
                FileObject fileObject =
                        filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceFile);
                try (OutputStream out = fileObject.openOutputStream()) {
                    ServicesFiles.writeServiceFile(allServices, out);
                }
                log("Wrote to: " + fileObject.toUri());
            } catch (IOException e) {
                fatalError("Unable to create " + resourceFile + ", " + e);
                return;
            }
        }
    }


    private void processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AutoService.class);

        log(annotations.toString());
        log(elements.toString());

        for (Element e : elements) {
            // 描述(gak): check for error trees?
            TypeElement providerImplementer = (TypeElement) e;

            AnnotationMirror annotationMirror = getAnnotationMirror(e, AutoService.class);
            Set<DeclaredType> providerInterfaces = getValueFieldOfClasses(annotationMirror);
            if (providerInterfaces.isEmpty()) {
                error(MISSING_SERVICES_ERROR, e, annotationMirror);
                continue;
            }
            for (DeclaredType providerInterface : providerInterfaces) {
                TypeElement providerType = (TypeElement) (providerInterface.asElement());

                log("provider interface: " + providerType.getQualifiedName());
                log("provider implementer: " + providerImplementer.getQualifiedName());

                if (checkImplementer(providerImplementer, providerType, annotationMirror)) {
                    String value = typeHelper.getType(providerImplementer);
                    String key = typeHelper.getType(providerType);
                    Set<String> data = providers.get(key);
                    if (data == null || data.isEmpty()) {
						data = new HashSet<>();
						data.add(value);
                        //data = new HashSet<String>() {{
                        //    add(value);
                        //}};
                    } else {
                        data.add(value);
                    }
                    providers.put(key, data);
                } else {
                    String message =
                            "ServiceProviders must implement their service provider interface. "
                                    + providerImplementer.getQualifiedName()
                                    + " does not implement "
                                    + providerType.getQualifiedName();
                    error(message, e, annotationMirror);
                }
            }
        }
    }

    private boolean checkImplementer(
            TypeElement providerImplementer,
            TypeElement providerType,
            AnnotationMirror annotationMirror) {

        // We check that providerImplementer does indeed inherit from providerType, and that it is not
        // abstract (an abstract class or interface). For ServiceLoader, we could also check that it has
        // a public no-arg constructor. But it turns out that people also use AutoService in contexts
        // where the META-INF/services entries are read by things other than ServiceLoader. Those things
        // still require the class to exist and inherit from providerType, but they don't necessarily
        // require a public no-arg constructor.
        // More background: https://github.com/google/auto/issues/1505.

        Types types = processingEnv.getTypeUtils();

        if (types.isSubtype(providerImplementer.asType(), providerType.asType())) {
            return checkNotAbstract(providerImplementer, annotationMirror);
        }

        String message =
                "ServiceProviders must implement their service provider interface. "
                        + providerImplementer.getQualifiedName()
                        + " does not implement "
                        + providerType.getQualifiedName();
        error(message, providerImplementer, annotationMirror);
        return false;
    }

    private boolean checkNotAbstract(
            TypeElement providerImplementer, AnnotationMirror annotationMirror) {
        if (providerImplementer.getModifiers().contains(Modifier.ABSTRACT)) {
            error(
                    "@AutoService cannot be applied to an abstract class or an interface",
                    providerImplementer,
                    annotationMirror);
            return false;
        }
        return true;
    }

    private Set<DeclaredType> getValueFieldOfClasses(AnnotationMirror annotationMirror) {
        return getAnnotationValue(annotationMirror, "value")
                .accept(
                        new SimpleAnnotationValueVisitor8<Set<DeclaredType>, Void>(Collections.emptySet()) {
                            @Override
                            public Set<DeclaredType> visitType(TypeMirror typeMirror, Void v) {
                                // 描述(ronshapiro): class literals may not always be declared types, i.e.
                                // int.class, int[].class

                                return Collections.singleton((DeclaredType) typeMirror);
                            }

                            @Override
                            public Set<DeclaredType> visitArray(
                                    List<? extends AnnotationValue> values, Void v) {
                                return values.stream()
                                        .flatMap(value -> value.accept(this, null).stream())
                                        .collect(Collectors.toSet());
                            }
                        },
                        null);
    }

    private AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String value) {
        Iterator<Map.Entry<ExecutableElement, AnnotationValue>> iterator = getAnnotationValuesWithDefaults(annotationMirror).entrySet().iterator();

        Map.Entry<ExecutableElement, AnnotationValue> entry;
        do {
            if (!iterator.hasNext()) {
                throw new IllegalArgumentException(String.format("@%s does not define an element %s()", ((TypeElement) annotationMirror.getAnnotationType().asElement()).getQualifiedName(), value));
            }

            entry = iterator.next();
        } while (!(entry.getKey()).getSimpleName().contentEquals(value));
        return entry.getValue();
    }

    public static Map<ExecutableElement, AnnotationValue> getAnnotationValuesWithDefaults(AnnotationMirror annotation) {
        Map<ExecutableElement, AnnotationValue> values = new HashMap<>();
        Map<ExecutableElement, AnnotationValue> declaredValues = Collections.unmodifiableMap(annotation.getElementValues());
        Iterator var3 = ElementFilter.methodsIn(annotation.getAnnotationType().asElement().getEnclosedElements()).iterator();

        while (var3.hasNext()) {
            ExecutableElement method = (ExecutableElement) var3.next();
            if (declaredValues.containsKey(method)) {
                values.put(method, declaredValues.get(method));
            } else {
                if (method.getDefaultValue() == null) {
                    throw new IllegalStateException("Unset annotation value without default should never happen: " + ((TypeElement) method.getEnclosingElement()).getQualifiedName() + '.' + method.getSimpleName() + "()");
                }

                values.put(method, method.getDefaultValue());
            }
        }

        return values;
    }

    private AnnotationMirror getAnnotationMirror(Element element, Class<? extends Annotation> annotationClass) {
        String name = annotationClass.getCanonicalName();
        Iterator var2 = element.getAnnotationMirrors().iterator();

        AnnotationMirror annotationMirror;
        TypeElement annotationTypeElement;
        do {
            if (!var2.hasNext()) {
                return null;
            }
            annotationMirror = (AnnotationMirror) var2.next();
            annotationTypeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
        } while (!annotationTypeElement.getQualifiedName().contentEquals(name));
        return annotationMirror;
    }

//    public static Optional<AnnotationMirror> getAnnotationMirror(Element element,
//                                                                 Class<? extends Annotation> annotationClass) {
//        String annotationClassName = annotationClass.getCanonicalName();
//        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
//            TypeElement annotationTypeElement = asType(annotationMirror.getAnnotationType().asElement());
//            if (annotationTypeElement.getQualifiedName().contentEquals(annotationClassName)) {
//                return Optional.of(annotationMirror);
//            }
//        }
//        return Optional.absent();
//    }


    private void log(String msg) {
        if (processingEnv.getOptions().containsKey("debug")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
        }
    }

    private void warning(String msg, Element element, AnnotationMirror annotation) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg, element, annotation);
    }

    private void error(String msg, Element element, AnnotationMirror annotation) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element, annotation);
    }

    private void fatalError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }
}
