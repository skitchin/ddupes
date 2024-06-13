import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.*;

public class DDupes {
    private static final Logger logger = Logger.getLogger(DDupes.class.getName());
    private static final String LOGGING_PROPERTIES_PATH = "/Users/scott/logging.properties";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        configureLogging();
        Map<String, List<Data>> dataMap = new HashMap<>();

        // Example command-line arguments
        args = new String[]{"-rs", "/users/scott/misc"};
//        args = new String[]{"-rdsn", "/users/scott/documents", "/users/scott/documents copy", "-p", "/users/scott/documents/test"};

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

    private static void configureLogging() {
        // Check if logging.properties exists in resources
        InputStream inputStream = DDupes.class.getResourceAsStream(LOGGING_PROPERTIES_PATH);
        if (inputStream != null) {
            // If exists, load logging configuration from file
            try {
                LogManager.getLogManager().readConfiguration(inputStream);
                return;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error reading logging configuration: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // If logging.properties doesn't exist, create it with default configurations
        createDefaultLoggingProperties();
    }

    private static void createDefaultLoggingProperties() {
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(LOGGING_PROPERTIES_PATH))) {
            Properties properties = new Properties();
            properties.setProperty("handlers", "java.util.logging.ConsoleHandler");
            properties.setProperty(".level", "INFO");
            properties.setProperty("java.util.logging.ConsoleHandler.level", "INFO");
            properties.setProperty("java.util.logging.ConsoleHandler.formatter", "java.util.logging.SimpleFormatter");

            properties.store(outputStream, "Default logging properties");
            System.out.println("Created default logging.properties file.");
        } catch (IOException e) {
            System.err.println("Error creating default logging.properties: " + e.getMessage());
            e.printStackTrace();
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
