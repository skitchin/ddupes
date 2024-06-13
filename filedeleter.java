import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Class for deleting duplicate files and empty directories.
 */
public class filedeleter {

    /**
     * Deletes duplicate files from the data map, keeping the most recently modified file.
     *
     * @param dataMap       The map containing file data, keyed by file hash.
     * @param preservePaths The list of directory paths to preserve from deletion.
     * @param isDryRun      Whether to perform a dry run (no actual deletions).
     */
    public static void deleteFiles(Map<String, List<data>> dataMap, List<String> preservePaths, boolean isDryRun) {
        Set<Path> directoriesToCheck = new HashSet<>();

        // Delete all but the most recently modified file for each duplicate hash
        dataMap.values().forEach(fileList -> {
            if (fileList.size() > 1) {
                // Sort the list by modification date in descending order (most recent first)
                fileList.sort((file1, file2) -> file2.getFileModifiedDate().compareTo(file1.getFileModifiedDate()));

                // Keep the first file and delete the rest
                boolean first = true;
                for (data d : fileList) {
                    Path filePath = Paths.get(d.getFileName());
                    Path parentDir = filePath.getParent();
                    if (first) {
                        System.out.println("[+] Kept: " + d.getFileName());
                        first = false;
                    } else {
                        if (preservePaths.stream().noneMatch(preservePath -> filePath.startsWith(preservePath))) {
                            if (isDryRun) {
                                System.out.println("[-] Would delete: " + d.getFileName());
                            } else {
                                try {
                                    Files.deleteIfExists(filePath);
                                    System.out.println("[-] Deleted: " + d.getFileName());
                                    directoriesToCheck.add(parentDir);
                                } catch (IOException e) {
                                    System.err.println("Failed to delete: " + d.getFileName());
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            System.out.println("[!] Skipped deletion from preserved directory: " + d.getFileName());
                        }
                    }
                }
            }
        });

        if (!isDryRun) {
            // Delete empty directories
            deleteDirectories(directoriesToCheck, preservePaths);
        }
    }

    /**
     * Deletes empty directories from the set of directories to check.
     *
     * @param directoriesToCheck The set of directories to check and delete if empty.
     * @param preservePaths      The list of directory paths to preserve from deletion.
     */
    public static void deleteDirectories(Set<Path> directoriesToCheck, List<String> preservePaths) {
        Set<Path> checkedDirectories = new HashSet<>();

        directoriesToCheck.forEach(dir -> {
            if (Files.exists(dir) && preservePaths.stream().noneMatch(preservePath -> dir.startsWith(preservePath))) {
                try (Stream<Path> walk = Files.walk(dir)) {
                    walk.sorted(Comparator.reverseOrder())
                            .filter(Files::isDirectory)
                            .forEach(d -> {
                                if (checkedDirectories.add(d)) {
                                    try {
                                        if (Files.list(d).findAny().isEmpty()) {
                                            Files.delete(d);
                                            System.out.println("Deleted empty directory: " + d);
                                        }
                                    } catch (IOException e) {
                                        handleIOException(e, d);
                                    }
                                }
                            });
                } catch (IOException e) {
                    handleIOException(e, dir);
                }
            }
        });
    }

    /**
     * Handles IOException that may occur during file operations.
     *
     * @param e    The IOException that occurred.
     * @param path The path associated with the IOException.
     */
    private static void handleIOException(IOException e, Path path) {
        if (e instanceof NoSuchFileException) {
            // Directory already deleted, ignore
        } else if (e instanceof AccessDeniedException) {
            System.err.println("Access denied! Cannot delete: " + path);
        } else {
            System.err.println("Failed to process directory: " + path);
            e.printStackTrace();
        }
    }
}