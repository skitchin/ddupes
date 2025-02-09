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

    Java 17 or higher
    Apache Commons Codec library

Usage

Command-Line Arguments

    -r: Recursively scan directories.
    -d: Delete duplicate files, keeping only the most recently modified file.
    -s: Display a summary of directories containing duplicates.
    -n: Perform a dry run (no actual deletions).
    -p [paths]: Preserve specified directories from deletion.
    -v: Display ddupes release version.

Example Usage

    java ddupes -rs /documents
    java ddupes -rdsn /documents -p /documents/test

Classes and Methods

[ddupes]

The main class that parses command-line arguments, processes directories, finds duplicates, and handles file deletions.

    * main(String[] args): The entry point of the application.
    * parseArguments(String[] args): Parses command-line arguments.

[DirectoryProcessor]

Processes directories to find and hash files, storing the results in a data map.

    * processDirectory(Path startPath, boolean isRecursive, Map<String, List<Data>> dataMap): Processes a directory to find and hash files.
    * md5hash(Path file): Computes the MD5 hash of a file.

[DuplicateFinder]

Finds and displays duplicate files based on their hash values.

    * findDupes(Map<String, List<Data>> dataMap, boolean isSummary): Finds and displays duplicate files.

[FileDeleter]

Deletes duplicate files and empty directories.

    * deleteFiles(Map<String, List<Data>> dataMap, List<String> preservePaths, boolean isDryRun): Deletes duplicate files.
    * deleteDirectories(Set<Path> directoriesToCheck, List<String> preservePaths): Deletes empty directories.
    * handleIOException(IOException e, Path path): Handles IOExceptions that may occur during file operations.

[Data]

Represents a file with its associated metadata.

    * data(String fileName, String fileHash, Long fileSize, String fileCreatedDate, String fileModifiedDate): Constructs a new Data object with the specified file metadata.
    * getFileName(): Returns the name of the file.
    * getFileHash(): Returns the hash of the file.
    * getFileSize(): Returns the size of the file in bytes.
    * getFileCreationDate(): Returns the creation date of the file.
    * getFileModifiedDate(): Returns the last modified date of the file.

Build and Run

1. Ensure Java is installed on your system.
2. Add Apache Commons Codec library to your project dependencies.
3. Compile the Java files:
    javac -cp .:commons-codec-1.15.jar *.java
4. Run the application:
    java -cp .:commons-codec-1.15.jar ddupes

LICENSE: This project is licensed under the MIT License. See the LICENSE file for details.

DISCLAIMER: DigitalBSD provides the ddupes program "as is" without warranty of any kind, either
express or implied, including, but not limited to, the implied warranties of merchantability and fitness
for a particular purpose. DigitalBSD assumes no responsibility for any data loss or other damages
that may occur from the use of the ddupes program. Users are solely responsible for their data and are
encouraged to back up important files before using the ddupes program.

Copyright (c) 2024 DigitalBSD. All rights reserved.
Copyright (c) 2024 Scott Kitchin. All rights reserved.