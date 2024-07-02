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
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * Main class for the DDupes application.
 * It finds and processes duplicate files in specified directories based on command-line arguments.
 */
public class ddupes {
    private static final String VERSION = "0.0.2";

    /**
     * Main method for the DDupes application.
     *
     * @param args Command-line arguments specifying directories and options.
     * @throws IOException               If an I/O error occurs.
     * @throws NoSuchAlgorithmException If the specified algorithm is not available.
     */
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        var dataMap = new HashMap<String, List<Data>>();

        //
        args = new String[]{"-v", "-m", "256", "/users/scott/temppp"};

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
                    System.out.println("Error processing directory: " + directoryPath);
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
        Long size = (long) 0;
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
                if (arg.equals("-mt")) {
                    size = Long.parseLong(args[i + 1]);
                    System.out.println(args[i+1]);
                    i++;
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
