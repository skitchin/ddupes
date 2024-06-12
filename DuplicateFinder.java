import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DuplicateFinder {

    public static void findDupes(Map<String, List<Data>> dataMap, boolean isSummary) {
        if (isSummary) {
            // Print only directories containing duplicates
            Set<Path> duplicateDirectories = dataMap.values().stream()
                    .filter(fileList -> fileList.size() > 1)
                    .flatMap(List::stream)
                    .map(data -> Path.of(data.getFileName()).getParent())
                    .collect(Collectors.toSet());

            System.out.println("Directories containing duplicates:");
            duplicateDirectories.forEach(System.out::println);
        } else {
            // Print duplicates with file names
            dataMap.forEach((key, fileList) -> {
                if (fileList.size() > 1) {
                    System.out.println("Duplicates for hash " + key + ":");
                    boolean first = true;
                    for (Data file : fileList) {
                        System.out.println((first ? "[+]" : "[-]") + " " + file.getFileName());
                        first = false;
                    }
                    System.out.println();
                }
            });
        }
    }
}