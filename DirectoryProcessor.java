import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Processes directories to find and hash files, storing the results in a data map.
 */
public class DirectoryProcessor {

    /**
     * Processes a directory to find and hash files, storing the results in a data map.
     *
     * @param startPath  The path to start processing.
     * @param isRecursive Whether to process directories recursively.
     * @param dataMap     The map to store file data, keyed by file hash.
     * @throws IOException               If an I/O error occurs.
     * @throws NoSuchAlgorithmException  If the MD5 algorithm is not available.
     */
    public static void processDirectory(Path startPath, boolean isRecursive, Map<String, List<Data>> dataMap) throws IOException, NoSuchAlgorithmException {
        try (Stream<Path> stream = isRecursive ? Files.walk(startPath).parallel() : Files.list(startPath).parallel()) {
            stream.filter(Files::isRegularFile)
                    .forEach(v -> {
                        try {
                            BasicFileAttributes attr = Files.readAttributes(v, BasicFileAttributes.class);
                            String fileHash = md5hash(v);
                            Data d = new Data(v.toString(), fileHash, attr.size(), attr.creationTime().toString(), attr.lastModifiedTime().toString());

                            // Synchronize access to the dataMap
                            synchronized (dataMap) {
                                dataMap.computeIfAbsent(fileHash, k -> new ArrayList<>()).add(d);
                            }
                        } catch (IOException | NoSuchAlgorithmException e) {
                            System.out.println("Error processing file: " + v);
                        }
                    });
        }
    }

    /**
     * Computes the MD5 hash of a file.
     *
     * @param file The file to hash.
     * @return The MD5 hash of the file as a hex string.
     * @throws IOException               If an I/O error occurs.
     * @throws NoSuchAlgorithmException  If the MD5 algorithm is not available.
     */
    public static String md5hash(Path file) throws IOException, NoSuchAlgorithmException {
        try (InputStream is = Files.newInputStream(file)) {
            if (is == null) {
                System.out.println("Failed to create input stream for file: " + file);
                return "";
            }
            return DigestUtils.md5Hex(is);
        } catch (IOException e) {
            System.out.println("IOException when reading file: " + file);
            throw e;
        }
    }
}
