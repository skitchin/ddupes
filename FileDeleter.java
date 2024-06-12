import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class FileDeleter {

    public static void deleteFiles(Map<String, List<Data>> dataMap, List<String> preservePaths, boolean isDryRun) {
        Set<Path> directoriesToCheck = new HashSet<>();

        // Delete all but the most recently modified file for each duplicate hash
        dataMap.values().forEach(fileList -> {
            if (fileList.size() > 1) {
                // Sort the list by modification date in descending order (most recent first)
                fileList.sort((file1, file2) -> file2.getFileModifiedDate().compareTo(file1.getFileModifiedDate()));

                // Keep the first file and delete the rest
                boolean first = true;
                for (Data data : fileList) {
                    Path filePath = Paths.get(data.getFileName());
                    Path parentDir = filePath.getParent();
                    if (first) {
                        System.out.println("[+] Kept: " + data.getFileName());
                        first = false;
                    } else {
                        if (preservePaths.stream().noneMatch(preservePath -> filePath.startsWith(preservePath))) {
                            if (isDryRun) {
                                System.out.println("[-] Would delete: " + data.getFileName());
                            } else {
                                try {
                                    Files.deleteIfExists(filePath);
                                    System.out.println("[-] Deleted: " + data.getFileName());
                                    directoriesToCheck.add(parentDir);
                                } catch (IOException e) {
                                    System.err.println("Failed to delete: " + data.getFileName());
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            System.out.println("[!] Skipped deletion from preserved directory: " + data.getFileName());
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