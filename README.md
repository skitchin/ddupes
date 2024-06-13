ddupes

ddupes is a Java application for finding and handling duplicate files in specified directories. It provides functionality to recursively scan directories, compute file hashes, identify duplicates, and optionally delete duplicate files, keeping only the most recently modified file.

Features

    * Recursive directory scanning
    * File hashing using MD5
    * Duplicate file identification
    * Option to display summary or detailed duplicate information
    * Dry run mode to preview deletions
    * Preserves specified directories from deletions
    * Deletes empty directories after removing files

Requirements

    Java 8 or higher
    Apache Commons Codec library

Usage

Command-Line Arguments

    -r: Recursively scan directories.
    -d: Delete duplicate files, keeping only the most recently modified file.
    -s: Display a summary of directories containing duplicates.
    -n: Perform a dry run (no actual deletions).
    -p [paths]: Preserve specified directories from deletion.

Example Usage

    java ddupes -rs /documents
    java ddupes -rdsn /documents -p /documents/test

Classes and Methods

[ddupes]

The main class that initializes logging, parses command-line arguments, processes directories, finds duplicates, and handles file deletions.

    * main(String[] args): The entry point of the application.
    * configureLogging(): Configures logging based on a properties file or creates a default configuration.
    * parseArguments(String[] args): Parses command-line arguments.

[directoryprocessor]

Processes directories to find and hash files, storing the results in a data map.

    * processDirectory(Path startPath, boolean isRecursive, Map<String, List<Data>> dataMap): Processes a directory to find and hash files.
    * md5hash(Path file): Computes the MD5 hash of a file.

[duplicatefinder]

Finds and displays duplicate files based on their hash values.

    * findDupes(Map<String, List<data>> dataMap, boolean isSummary): Finds and displays duplicate files.

[filedeleter]

Deletes duplicate files and empty directories.

    * deleteFiles(Map<String, List<data>> dataMap, List<String> preservePaths, boolean isDryRun): Deletes duplicate files.
    * deleteDirectories(Set<Path> directoriesToCheck, List<String> preservePaths): Deletes empty directories.
    * handleIOException(IOException e, Path path): Handles IOExceptions that may occur during file operations.

[data]

Represents a file with its associated metadata.

    * data(String fileName, String fileHash, Long fileSize, String fileCreatedDate, String fileModifiedDate): Constructs a new Data object with the specified file metadata.
    * getFileName(): Returns the name of the file.
    * getFileHash(): Returns the hash of the file.
    * getFileSize(): Returns the size of the file in bytes.
    * getFileCreationDate(): Returns the creation date of the file.
    * getFileModifiedDate(): Returns the last modified date of the file.

Logging

Logging is configured using a properties file located at /Users/scott/logging.properties. If the file does not exist, a default configuration is created.

Build and Run

1. Ensure Java is installed on your system.
2. Add Apache Commons Codec library to your project dependencies.
3. Compile the Java files:
    javac -cp .:commons-codec-1.15.jar *.java
4. Run the application:
    java -cp .:commons-codec-1.15.jar ddupes

License

This project is licensed under the MIT License. See the LICENSE file for details.

This README provides an overview of the ddupes application, detailing its features, usage, and the purpose of each class and method. For further information or detailed documentation, refer to the source code comments and Javadoc annotations.
