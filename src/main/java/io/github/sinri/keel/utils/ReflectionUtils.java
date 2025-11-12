package io.github.sinri.keel.utils;

import io.github.sinri.keel.logger.api.event.EventRecorder;
import io.github.sinri.keel.utils.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 2.6
 */
public class ReflectionUtils {

    private final static boolean virtualThreadsAvailable;

    static {
        boolean available = false;
        try {
            // noinspection JavaReflectionMemberAccess
            Thread.class.getMethod("isVirtual");
            available = true;
        } catch (NoSuchMethodException e) {
            // Virtual threads not available
        }
        virtualThreadsAvailable = available;
    }

    private ReflectionUtils() {
    }

    private static EventRecorder createEventRecorder() {
        return Keel.getRecorderFactory().createEventRecorder(ReflectionUtils.class.getName());
    }

    /**
     * @param <T> class of target annotation
     * @return target annotation
     * @since 1.13
     */
    @Nullable
    public static <T extends Annotation> T getAnnotationOfMethod(@Nonnull Method method, @Nonnull Class<T> classOfAnnotation,
                                                                 @Nullable T defaultAnnotation) {
        T annotation = method.getAnnotation(classOfAnnotation);
        if (annotation == null) {
            return defaultAnnotation;
        }
        return annotation;
    }

    /**
     * @since 2.6
     */
    @Nullable
    public static <T extends Annotation> T getAnnotationOfMethod(@Nonnull Method method, @Nonnull Class<T> classOfAnnotation) {
        return getAnnotationOfMethod(method, classOfAnnotation, null);
    }

    /**
     * @return Returns this element's annotation for the specified type if such an
     *         annotation is present, else null.
     * @throws NullPointerException – if the given annotation class is null
     *                              Note that any annotation returned by this method
     *                              is a declaration annotation.
     * @since 2.8
     */
    @Nullable
    public static <T extends Annotation> T getAnnotationOfClass(@Nonnull Class<?> anyClass,
                                                                @Nonnull Class<T> classOfAnnotation) {
        return anyClass.getAnnotation(classOfAnnotation);
    }

    /**
     * @since 3.1.8
     *         For the repeatable annotations.
     */
    @Nonnull
    public static <T extends Annotation> T[] getAnnotationsOfClass(@Nonnull Class<?> anyClass,
                                                                   @Nonnull Class<T> classOfAnnotation) {
        return anyClass.getAnnotationsByType(classOfAnnotation);
    }

    /**
     * @param packageName In this package
     * @param baseClass   seek any class implementations of this class
     * @param <R>         the target base class to seek its implementations
     * @return the sought classes in a set
     * @since 3.0.6
     * @since 3.2.12.1 rewrite
     */
    public static <R> Set<Class<? extends R>> seekClassDescendantsInPackage(
            @Nonnull String packageName,
            @Nonnull Class<R> baseClass
    ) {
        // Reflections reflections = new Reflections(packageName);
        // return reflections.getSubTypesOf(baseClass);

        Set<Class<? extends R>> set = new HashSet<>();

        List<String> classPathList = FileUtils.getClassPathList();
        //BaseEventRecorder baseEventRecorder = new BaseEventRecorder(ReflectionUtils.class.getName());
        for (String classPath : classPathList) {
            //baseEventRecorder.debug("[1] Seeking classes in class path: " + classPath);
            if (classPath.endsWith(".jar")) {
                Set<Class<? extends R>> classes = seekClassDescendantsInPackageForProvidedJar(classPath, packageName,
                        baseClass);
                set.addAll(classes);
            } else {
                Set<Class<? extends R>> classes = seekClassDescendantsInPackageForFileSystem(packageName, baseClass);
                set.addAll(classes);
            }
        }

        return set;
    }

    /**
     * As of 4.1.5, use {@link ClassLoader#getResources(String)} to fix the bug that test scope is not supported.
     *
     * @since 3.2.11
     */
    protected static <R> Set<Class<? extends R>> seekClassDescendantsInPackageForFileSystem(
            @Nonnull String packageName,
            @Nonnull Class<R> baseClass) {
        Set<Class<? extends R>> descendantClasses = new HashSet<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // in file system
        String packagePath = packageName.replace('.', File.separatorChar);
        //Keel.getLogger().debug("[2] Seeking classes in package through file system: " + packagePath);
        try {
            // Assuming classes are in a directory on the file system (e.g., not in a JAR)
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            if (!resources.hasMoreElements()) {
                //Keel.getLogger().debug("classLoader.getResource found null for package through file system: " + packagePath);
            }
            while (resources.hasMoreElements()) {
                var resource = resources.nextElement();
                //Keel.getLogger().debug("[3] resource: " + resource.toString());

                URI uri = resource.toURI();
                Path startPath = Paths.get(uri);
                //Keel.getLogger().debug("[4] startPath: " + startPath);
                Files.walkFileTree(startPath, new SimpleFileVisitor<>() {
                    @Nonnull
                    @Override
                    public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) {
                        //Keel.getLogger().debug("[5] Visiting file: " + file);
                        if (file.toString().endsWith(".class")) {
                            String className = file.toString().replace(".class", "").replace(File.separator, ".");
                            className = className.substring(className.indexOf(packageName));

                            try {
                                // since java 16, you may use:
                                // if (clazzAny instanceof Class<? extends R> clazzR) {
                                // // 在这里可以安全地使用 clazzR
                                // }
                                // Class<? extends R> clazz = (Class<? extends R>)
                                // classLoader.loadClass(className);
                                var clazz = classLoader.loadClass(className);
                                if (baseClass.isAssignableFrom(clazz)) {
                                    @SuppressWarnings("unchecked")
                                    Class<? extends R> castedClass = (Class<? extends R>) clazz;
                                    descendantClasses.add(castedClass);
                                }
                            } catch (Throwable e) {
                                //Keel.getLogger().debug(getClass() + " seekClassDescendantsInPackageForFileSystem for " + className + " error: " + e.getMessage());
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });

            }
        } catch (Exception e) {
            //Keel.getLogger().exception(e);
            Keel.getRecorderFactory().createEventRecorder(ReflectionUtils.class.getName()).exception(e);
        }
        return descendantClasses;
    }

    /**
     * @since 3.2.11
     */
    protected static <R> Set<Class<? extends R>> seekClassDescendantsInPackageForRunningJar(
            @Nonnull String packageName,
            @Nonnull Class<R> baseClass) {
        Set<Class<? extends R>> descendantClasses = new HashSet<>();
        Set<String> strings = FileUtils.seekPackageClassFilesInRunningJar(packageName);
        var eventRecorder = createEventRecorder();
        for (String s : strings) {
            try {
                Class<?> aClass = Class.forName(s);
                if (baseClass.isAssignableFrom(aClass)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends R> castedClass = (Class<? extends R>) aClass;
                    descendantClasses.add(castedClass);
                }
            } catch (Throwable e) {
                eventRecorder.debug(String.format(
                        "%s seekClassDescendantsInPackageForRunningJar for %s error: %s",
                        ReflectionUtils.class, s, e.getMessage()
                ));
            }
        }
        return descendantClasses;
    }

    /**
     * @since 3.2.11
     */
    protected static <R> Set<Class<? extends R>> seekClassDescendantsInPackageForProvidedJar(@Nonnull String jarInClassPath,
                                                                                             @Nonnull String packageName, @Nonnull Class<R> baseClass) {
        Set<Class<? extends R>> descendantClasses = new HashSet<>();
        List<String> classNames = FileUtils.traversalInJarFile(new File(jarInClassPath));
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        var eventRecorder = createEventRecorder();
        classNames.forEach(className -> {
            if (className.startsWith(packageName + ".")) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends R> clazz = (Class<? extends R>) classLoader.loadClass(className);
                    if (baseClass.isAssignableFrom(clazz)) {
                        descendantClasses.add(clazz);
                    }
                } catch (Throwable e) {
                    eventRecorder.debug("%s seekClassDescendantsInPackageForProvidedJar for %s error: %s".formatted(ReflectionUtils.class, className, e.getMessage()));
                }
            }
        });
        return descendantClasses;
    }

    /**
     * @return Whether the given `baseClass` is the base of the given
     *         `implementClass`.
     * @since 3.0.10
     */
    public static boolean isClassAssignable(@Nonnull Class<?> baseClass, @Nonnull Class<?> implementClass) {
        return baseClass.isAssignableFrom(implementClass);
    }

    /**
     * @since 4.1.0
     */
    public static boolean isVirtualThreadsAvailable() {
        return virtualThreadsAvailable;
    }
}
