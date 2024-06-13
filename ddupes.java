/*
 * Copyright (c) 2024 DigitalBSD. All rights reserved.
 * Copyright (c) 2024 Scott Kitchin. All rights reserved.
 *
 * Licensed under the MIT License.
 *
 * DISCLAIMER: DigitalBSD provides the ddupes program "as is" without warranty of any kind, either
 * express or implied, including, but not limited to, the implied warranties of merchantability and fitness
 * for a particular purpose. DigitalBSD assumes no responsibility for any data loss or other damages
 * that may occur from the use of the ddupes program. Users are solely responsible for their data and are
 * encouraged to back up important files before using the ddupes program.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.*;

/**
 * Main class for the DDupes application.
 * It finds and processes duplicate files in specified directories based on command-line arguments.
 */
public class ddupes {
    private static final String VERSION = "0.0.1";
    private static final Logger logger = Logger.getLogger(ddupes.class.getName());
    private static final String LOGGING_PROPERTIES_PATH = "/logging.properties";

    /**
     * Main method for the DDupes application.
     *
     * @param args Command-line arguments specifying directories and options.
     * @throws IOException               If an I/O error occurs.
     * @throws NoSuchAlgorithmException If the specified algorithm is not available.
     */
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        configureLogging();
        Map<String, List<data>> dataMap = new HashMap<>();

        // Parse command-line arguments
        ParsedArgs parsedArgs = parseArguments(args);

        // Set the parallelism level based on the available processors
        ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        customThreadPool.submit(() -> {
            // Process each directory
            parsedArgs.directoryPaths.parallelStream().forEach(directoryPath -> {
                try {
                    directoryprocessor.processDirectory(Paths.get(directoryPath), parsedArgs.isRecursive, dataMap);
                } catch (IOException | NoSuchAlgorithmException e) {
                    logger.log(Level.SEVERE, "Error processing directory: " + directoryPath, e);
                }
            });
        }).join();

        dataMap.values().forEach(dataList -> dataList.forEach(temp ->
                System.out.println(temp.getFileName() + " :: " + temp.getFileHash() + " :: " + temp.getFileSize() + " :: " + temp.getFileCreationDate())
        ));

        duplicatefinder.findDupes(dataMap, parsedArgs.isSummary);

        if (parsedArgs.isDelete) {
            filedeleter.deleteFiles(dataMap, parsedArgs.preservePaths, parsedArgs.isDryRun);
        }
    }

    /**
     * Configures logging for the application.
     * If a logging properties file exists, it loads the configuration from the file.
     * Otherwise, it creates a default logging properties file.
     */
    private static void configureLogging() {
        // Check if logging.properties exists in resources
        InputStream inputStream = ddupes.class.getResourceAsStream(LOGGING_PROPERTIES_PATH);
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

    /**
     * Creates a default logging properties file with predefined configurations.
     */
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

    /**
     * Parses command-line arguments and returns a ParsedArgs object.
     *
     * @param args Command-line arguments to parse.
     * @return ParsedArgs object containing the parsed command-line arguments.
     */
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
                if (arg.equals("-v")) {
                    System.out.println("ddupes version: " + VERSION);
                }
            } else {
                directoryPaths.add(arg);
            }
        }

        return new ParsedArgs(isRecursive, isDelete, isSummary, isDryRun, preservePaths, directoryPaths);
    }

    /**
     * Class to hold parsed command-line arguments.
     */
    static class ParsedArgs {
        boolean isRecursive;
        boolean isDelete;
        boolean isSummary;
        boolean isDryRun;
        List<String> preservePaths;
        List<String> directoryPaths;

        /**
         * Constructor for ParsedArgs.
         *
         * @param isRecursive    Whether to process directories recursively.
         * @param isDelete       Whether to delete duplicate files.
         * @param isSummary      Whether to display a summary of duplicate files.
         * @param isDryRun       Whether to perform a dry run (no actual deletions).
         * @param preservePaths  List of paths to preserve.
         * @param directoryPaths List of directory paths to process.
         */
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
