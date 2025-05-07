package io.github.sinri.keel.core.helper;

import io.vertx.core.Future;
import io.vertx.core.file.FileSystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * A utility class for file operations in the Keel framework.
 * Provides methods for file manipulation, JAR operations, and class path management.
 * 
 * @since 2.6
 */
public class KeelFileHelper {
    private static final KeelFileHelper instance = new KeelFileHelper();
    private final FileSystem fileSystem;

    private KeelFileHelper() {
        this.fileSystem = Keel.getVertx().fileSystem();
    }

    static KeelFileHelper getInstance() {
        return instance;
    }

    /**
     * Checks if a file exists at the specified path.
     *
     * @param filePath the path to check
     * @return Future containing true if the file exists, false otherwise
     * @since 4.0.12
     */
    public Future<Boolean> exists(String filePath) {
        return fileSystem.exists(filePath);
    }

    /**
     * Creates a directory and all necessary parent directories.
     *
     * @param dirPath the directory path to create
     * @return Future that completes when the directory is created
     * @since 4.0.12
     */
    public Future<Void> mkdirs(String dirPath) {
        return fileSystem.mkdirs(dirPath);
    }

    /**
     * Deletes a file or directory.
     *
     * @param path the path to delete
     * @param recursive if true, recursively deletes directories
     * @return Future that completes when the deletion is done
     * @since 4.0.12
     */
    public Future<Void> delete(String path, boolean recursive) {
        return fileSystem.deleteRecursive(path, recursive);
    }

    /**
     * Copies a file from source to destination.
     *
     * @param source the source file path
     * @param destination the destination file path
     * @return Future that completes when the copy is done
     * @since 4.0.12
     */
    public Future<Void> copy(String source, String destination) {
        return fileSystem.copy(source, destination);
    }

    /**
     * Moves a file from source to destination.
     *
     * @param source the source file path
     * @param destination the destination file path
     * @return Future that completes when the move is done
     * @since 4.0.12
     */
    public Future<Void> move(String source, String destination) {
        return fileSystem.move(source, destination);
    }

    public @Nonnull byte[] readFileAsByteArray(@Nonnull String filePath, boolean seekInsideJarWhenNotFound) throws IOException {
        try {
            return Files.readAllBytes(Path.of(filePath));
        } catch (IOException e) {
            if (seekInsideJarWhenNotFound) {
                try (
                        InputStream resourceAsStream = KeelFileHelper.class.getClassLoader().getResourceAsStream(filePath)
                ) {
                    if (resourceAsStream == null) {
                        // not found resource
                        throw new IOException("file also not in jar", e);
                    }
                    return resourceAsStream.readAllBytes();
                }
            } else {
                throw e;
            }
        }
    }

    /**
     * @param filePath path string of the target file, or directory
     * @return the URL of target file; if not there, null return.
     * @since 3.2.12.1 original name is `getUrlOfFileInJar`.
     */
    @Nullable
    public URL getUrlOfFileInRunningJar(@Nonnull String filePath) {
        return KeelFileHelper.class.getClassLoader().getResource(filePath);
    }

    /**
     * Seek in JAR, under the root (exclusive)
     *
     * @param root ends with '/' for a directory
     * @return list of JarEntry
     * @since 3.2.12.1 original name is `traversalInJar`.
     */
    @Nonnull
    public List<JarEntry> traversalInRunningJar(@Nonnull String root) {
        List<JarEntry> jarEntryList = new ArrayList<>();
        try {
            // should root ends with '/'?
            URL url = KeelFileHelper.class.getClassLoader().getResource(root);
            if (url == null) {
                throw new RuntimeException("Resource is not found");
            }
            if (!url.toString().contains("!/")) {
                throw new RuntimeException("Resource is not in JAR");
            }
            String jarPath = url.toString().substring(0, url.toString().indexOf("!/") + 2);

            URL jarURL = new URL(jarPath);
            JarURLConnection jarCon = (JarURLConnection) jarURL.openConnection();
            JarFile jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            var baseJarEntry = jarFile.getJarEntry(root);
            var pathOfBaseJarEntry = Path.of(baseJarEntry.getName());

            while (jarEntries.hasMoreElements()) {
                JarEntry entry = jarEntries.nextElement();

                Path entryPath = Path.of(entry.getName());
                if (entryPath.getParent() == null) {
                    continue;
                }
                if (entryPath.getParent().compareTo(pathOfBaseJarEntry) == 0) {
                    jarEntryList.add(entry);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jarEntryList;
    }

    /**
     * @return absolute created Temp File path
     * @since 3.0.0
     */
    public Future<String> crateTempFile(@Nullable String prefix, @Nullable String suffix) {
        return Keel.getVertx().fileSystem().createTempFile(prefix, suffix);
    }

    /**
     * @since 3.2.11
     * @since 3.2.12.1 Changed the implementation with checking class paths.
     * Check if this process is running with JAR file.
     */
    public boolean isRunningFromJAR() {
        List<String> classPathList = getClassPathList();
        for (var classPath : classPathList) {
            if (!classPath.endsWith(".jar")) {
                return false;
            }
        }
        return true;
    }

    /**
     * @since 3.2.12.1
     */
    public List<String> getClassPathList() {
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);
        return new ArrayList<>(Arrays.asList(classpathEntries));
    }

    /**
     * The in-class classes, i.e. subclasses, would be neglected.
     *
     * @since 3.2.12.1 original name is `seekPackageClassFilesInJar`.
     */
    public Set<String> seekPackageClassFilesInRunningJar(@Nonnull String packageName) {
        Set<String> classes = new HashSet<>();
        // Get the current class's class loader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Get the URL of the JAR file containing the current class
        String currentClassUrlInJarFile = getClass().getName().replace('.', '/') + ".class";
        URL jarUrl = classLoader.getResource(currentClassUrlInJarFile);

        if (jarUrl != null && jarUrl.getProtocol().equals("jar")) {
            // Extract the JAR file path
            String jarPath = jarUrl.getPath().substring(5, jarUrl.getPath().indexOf("!"));

            // Open the JAR file
            try (JarFile jarFile = new JarFile(jarPath)) {
                // Iterate through the entries of the JAR file
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    // Check if the entry is a class
                    if (entryName.endsWith(".class")) {
                        // Convert the entry name to a fully qualified class name
                        String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                        if (className.startsWith(packageName + ".") && !className.contains("$")) {
                            classes.add(className);
                        }
                    }
                }
            } catch (IOException e) {
                Keel.getLogger()
                    .debug(getClass() + " seekPackageClassFilesInRunningJar for package " + packageName + " error: " + e.getMessage());
            }
        }

        return classes;
    }

    /**
     * @param jarFile File built from JAR in class path.
     * @since 3.2.12.1
     */
    public List<String> traversalInJarFile(File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            List<String> list = new ArrayList<>();

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (
                        entryName.endsWith(".class")
                                && !entryName.contains("$")
                                && !entryName.startsWith("META-INF")
                ) {
                    // 将路径形式的类名转换为 Java 类名
                    String className = entryName.replace("/", ".").replace(".class", "");
                    list.add(className);
                }
            }

            return list;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lists the contents of a directory.
     *
     * @param dirPath the directory path to list
     * @return Future containing list of file names in the directory
     * @since 4.0.12
     */
    public Future<List<String>> listDir(String dirPath) {
        return fileSystem.readDir(dirPath);
    }

    /**
     * Gets file properties (size, creation time, last access time, last modified time).
     *
     * @param filePath the file path
     * @return Future containing file properties
     * @since 4.0.12
     */
    public Future<io.vertx.core.file.FileProps> getFileProps(String filePath) {
        return fileSystem.props(filePath);
    }

    /**
     * Creates a symbolic link.
     *
     * @param link the path of the symbolic link to create
     * @param target the target of the symbolic link
     * @return Future that completes when the link is created
     * @since 4.0.12
     */
    public Future<Void> createSymLink(String link, String target) {
        return fileSystem.link(link, target);
    }

    /**
     * Reads a file as a string with a specific charset.
     *
     * @param filePath the file path
     * @param charset the charset to use
     * @return Future containing the file contents as a string
     * @since 4.0.12
     */
    public Future<String> readFileAsString(String filePath, String charset) {
        return fileSystem.readFile(filePath)
            .map(buffer -> buffer.toString(java.nio.charset.Charset.forName(charset)));
    }

    /**
     * Writes a string to a file with a specific charset.
     *
     * @param filePath the file path
     * @param content the content to write
     * @param charset the charset to use
     * @return Future that completes when the write is done
     * @since 4.0.12
     */
    public Future<Void> writeFile(String filePath, String content, String charset) {
        return fileSystem.writeFile(
            filePath,
            io.vertx.core.buffer.Buffer.buffer(content.getBytes(java.nio.charset.Charset.forName(charset)))
        );
    }

    /**
     * Appends content to a file.
     *
     * @param filePath the file path
     * @param content the content to append
     * @return Future that completes when the append is done
     * @since 4.0.12
     */
    public Future<Void> appendFile(String filePath, String content) {
        return fileSystem.open(filePath, new io.vertx.core.file.OpenOptions().setAppend(true))
            .compose(file -> file.write(io.vertx.core.buffer.Buffer.buffer(content))
                .compose(v -> file.close()));
    }

    /**
     * Extracts a JAR file to a directory.
     *
     * @param jarPath the path to the JAR file
     * @param targetDir the target directory
     * @return Future that completes when the extraction is done
     * @since 4.0.12
     */
    public Future<Void> extractJar(String jarPath, String targetDir) {
        return Future.succeededFuture()
            .compose(v -> {
                try (JarFile jar = new JarFile(jarPath)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        File file = new File(targetDir, entry.getName());
                        if (entry.isDirectory()) {
                            file.mkdirs();
                        } else {
                            file.getParentFile().mkdirs();
                            try (InputStream in = jar.getInputStream(entry);
                                 java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
                                in.transferTo(out);
                            }
                        }
                    }
                    return Future.succeededFuture();
                } catch (IOException e) {
                    return Future.failedFuture(e);
                }
            });
    }

    /**
     * Creates a new JAR file from a directory.
     *
     * @param sourceDir the source directory
     * @param jarPath the path where the JAR file will be created
     * @return Future that completes when the JAR is created
     * @since 4.0.12
     */
    public Future<Void> createJar(String sourceDir, String jarPath) {
        return Future.succeededFuture()
            .compose(v -> {
                try (java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(
                    new java.io.FileOutputStream(jarPath))) {
                    File source = new File(sourceDir);
                    addToJar(source, source, jos);
                    return Future.succeededFuture();
                } catch (IOException e) {
                    return Future.failedFuture(e);
                }
            });
    }

    private void addToJar(File root, File source, java.util.jar.JarOutputStream jos) throws IOException {
        if (source.isDirectory()) {
            String dirPath = source.getPath().substring(root.getPath().length() + 1).replace('\\', '/');
            if (!dirPath.isEmpty()) {
                if (!dirPath.endsWith("/")) {
                    dirPath += "/";
                }
                JarEntry entry = new JarEntry(dirPath);
                jos.putNextEntry(entry);
                jos.closeEntry();
            }
            for (File file : source.listFiles()) {
                addToJar(root, file, jos);
            }
        } else {
            String entryPath = source.getPath().substring(root.getPath().length() + 1).replace('\\', '/');
            JarEntry entry = new JarEntry(entryPath);
            jos.putNextEntry(entry);
            try (java.io.FileInputStream fis = new java.io.FileInputStream(source)) {
                fis.transferTo(jos);
            }
            jos.closeEntry();
        }
    }

    /**
     * Gets the size of a file.
     *
     * @param filePath the file path
     * @return Future containing the file size in bytes
     * @since 4.0.12
     */
    public Future<Long> getFileSize(String filePath) {
        return fileSystem.props(filePath).map(props -> props.size());
    }

    /**
     * Checks if a path is a directory.
     *
     * @param path the path to check
     * @return Future containing true if the path is a directory
     * @since 4.0.12
     */
    public Future<Boolean> isDirectory(String path) {
        return fileSystem.props(path).map(props -> props.isDirectory());
    }

    /**
     * Gets the last modified time of a file.
     *
     * @param filePath the file path
     * @return Future containing the last modified time
     * @since 4.0.12
     */
    public Future<Long> getLastModifiedTime(String filePath) {
        return fileSystem.props(filePath).map(props -> props.lastModifiedTime());
    }

    /**
     * Gets the creation time of a file.
     *
     * @param filePath the file path
     * @return Future containing the creation time
     * @since 4.0.12
     */
    public Future<Long> getCreatedTime(String filePath) {
        return fileSystem.props(filePath).map(props -> props.creationTime());
    }
}
