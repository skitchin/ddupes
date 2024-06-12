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
import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectoryProcessor {
    private static final Logger logger = Logger.getLogger(DirectoryProcessor.class.getName());

    public static void processDirectory(Path startPath, boolean isRecursive, Map<String, List<Data>> dataMap) throws IOException, NoSuchAlgorithmException {
        try (Stream<Path> stream = isRecursive ? Files.walk(startPath).parallel() : Files.list(startPath).parallel()) {
            stream.filter(Files::isRegularFile)
                    .forEach(v -> {
                        try {
                            BasicFileAttributes attr = Files.readAttributes(v, BasicFileAttributes.class);
                            String fileHash = md5hash(v);
                            Data data = new Data(v.toString(), fileHash, attr.size(), attr.creationTime().toString(), attr.lastModifiedTime().toString());

                            // Synchronize access to the dataMap
                            synchronized (dataMap) {
                                dataMap.computeIfAbsent(fileHash, k -> new ArrayList<>()).add(data);
                            }
                        } catch (IOException | NoSuchAlgorithmException e) {
                            logger.log(Level.SEVERE, "Error processing file: " + v, e);
                        }
                    });
        }
    }

    public static String md5hash(Path file) throws IOException, NoSuchAlgorithmException {
        try (InputStream is = Files.newInputStream(file)) {
            if (is == null) {
                logger.log(Level.SEVERE, "Failed to create input stream for file: " + file);
                return "";
            }
            return DigestUtils.md5Hex(is);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException when reading file: " + file, e);
            throw e;
        }
    }
}