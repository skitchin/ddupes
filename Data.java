/**
 * Represents a file with its associated metadata.
 */
public class Data {
    private String fileName;
    private String fileHash;
    private Long fileSize;
    private String fileCreationDate;
    private String fileModifiedDate;

    /**
     * Constructs a new Data object with the specified file metadata.
     *
     * @param fileName         The name of the file.
     * @param fileHash         The hash of the file.
     * @param fileSize         The size of the file in bytes.
     * @param fileCreatedDate  The creation date of the file.
     * @param fileModifiedDate The last modified date of the file.
     */
    public Data(String fileName, String fileHash, Long fileSize, String fileCreatedDate, String fileModifiedDate) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.fileSize = fileSize;
        this.fileCreationDate = fileCreatedDate;
        this.fileModifiedDate = fileModifiedDate;
    }

    /**
     * Returns the name of the file.
     *
     * @return The file name.
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Returns the hash of the file.
     *
     * @return The file hash.
     */
    public String getFileHash() {
        return this.fileHash;
    }

    /**
     * Returns the size of the file in bytes.
     *
     * @return The file size as a string.
     */
    public String getFileSize() {
        return Long.toString(this.fileSize);
    }

    /**
     * Returns the creation date of the file.
     *
     * @return The file creation date.
     */
    public String getFileCreationDate() {
        return this.fileCreationDate;
    }

    /**
     * Returns the last modified date of the file.
     *
     * @return The file modified date.
     */
    public String getFileModifiedDate() {
        return this.fileModifiedDate;
    }
}