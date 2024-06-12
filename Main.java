import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        Map<String, List<Data>> dataMap = new HashMap<>();

        // Example command-line arguments
        args = new String[]{"-rs", "/users/scott/misc"};
//        args = new String[]{"-rdsn", "/users/scott/documents1", "/users/scott/documents1 copy", "-p", "/users/scott/preserve1", "/users/scott/preserve2"};

        // Parse command-line arguments
        ParsedArgs parsedArgs = parseArguments(args);

        // Set the parallelism level based on the available processors
        ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        customThreadPool.submit(() -> {
            // Process each directory
            parsedArgs.directoryPaths.parallelStream().forEach(directoryPath -> {
                try {
                    DirectoryProcessor.processDirectory(Paths.get(directoryPath), parsedArgs.isRecursive, dataMap);
                } catch (IOException | NoSuchAlgorithmException e) {
                    logger.log(Level.SEVERE, "Error processing directory: " + directoryPath, e);
                }
            });
        }).join();

        dataMap.values().forEach(dataList -> dataList.forEach(temp ->
                System.out.println(temp.getFileName() + " :: " + temp.getFileHash() + " :: " + temp.getFileSize() + " :: " + temp.getFileCreationDate())
        ));

        DuplicateFinder.findDupes(dataMap, parsedArgs.isSummary);

        if (parsedArgs.isDelete) {
            FileDeleter.deleteFiles(dataMap, parsedArgs.preservePaths, parsedArgs.isDryRun);
        }
    }

    private static ParsedArgs parseArguments(String[] args) {
        boolean isRecursive = false;
        boolean isDelete = false;
        boolean isSummary = false;
        boolean isDryRun = false;
        List<String> preservePaths = new ArrayList<>();
        List<String> directoryPaths = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                if (arg.contains("r")) isRecursive = true;
                if (arg.contains("d")) isDelete = true;
                if (arg.contains("s")) isSummary = true;
                if (arg.contains("n")) isDryRun = true;
                if (arg.equals("-p")) {
                    while (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        preservePaths.add(args[++i]);
                    }
                }
            } else {
                directoryPaths.add(arg);
            }
        }

        return new ParsedArgs(isRecursive, isDelete, isSummary, isDryRun, preservePaths, directoryPaths);
    }

    static class ParsedArgs {
        boolean isRecursive;
        boolean isDelete;
        boolean isSummary;
        boolean isDryRun;
        List<String> preservePaths;
        List<String> directoryPaths;

        ParsedArgs(boolean isRecursive, boolean isDelete, boolean isSummary, boolean isDryRun, List<String> preservePaths, List<String> directoryPaths) {
            this.isRecursive = isRecursive;
            this.isDelete = isDelete;
            this.isSummary = isSummary;
            this.isDryRun = isDryRun;
            this.preservePaths = preservePaths;
            this.directoryPaths = directoryPaths;
        }
    }
}
