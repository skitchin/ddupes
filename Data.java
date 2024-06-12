public class Data {
    String fileName;
    String fileHash;
    Long fileSize;
    String fileCreationDate;
    String fileModifiedDate;

    public Data(String fileName, String fileHash, Long fileSize, String fileCreatedDate, String fileModifiedDate) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.fileSize = fileSize;
        this.fileCreationDate = fileCreatedDate;
        this.fileModifiedDate = fileModifiedDate;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getFileHash() {
        return this.fileHash;
    }

    public String getFileSize() {
        return Long.toString(this.fileSize);
    }

    public String getFileCreationDate() {
        return this.fileCreationDate;
    }

    public String getFileModifiedDate() {
        return this.fileModifiedDate;
    }
}
