package ua.khpi.markup.util;

import ua.khpi.markup.exception.FileRenamingException;
import ua.khpi.markup.exception.FileSavingException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public static final String TXT_EXTENSION = ".txt";
    public static final String PROCESSED_FILE_EXTENSION = ".pd.txt";

    public static List<Path> getFilesToProcess(File directoryToLookup) throws IOException {
        try (Stream<Path> allFilesInPath = Files.walk(directoryToLookup.toPath())) {
            return allFilesInPath
                    .filter(FileUtils::isUnprocessedTxtFile)
                    .collect(Collectors.toList());
        }
    }

    public static String getFileContent(Path pathToFile) throws IOException {
        return new String(Files.readAllBytes(pathToFile), StandardCharsets.UTF_8);
    }

    public static void saveProcessedFile(Path pathToFile, String fileContent) {
        saveFileWithNewContent(pathToFile, fileContent);
        markFileAsProcessed(pathToFile);
    }

    private static String getUpdatedFileName(Path pathToFile) {
        String oldFileName = pathToFile.getFileName().toString();
        return oldFileName.replace(TXT_EXTENSION, PROCESSED_FILE_EXTENSION);
    }

    private static boolean isUnprocessedTxtFile(Path pathToFile) {
        String fileName = pathToFile.getFileName().toString();
        return !fileName.endsWith(PROCESSED_FILE_EXTENSION) && fileName.endsWith(TXT_EXTENSION);
    }

    private static void saveFileWithNewContent(Path pathToFile, String fileContent) {
        try {
            Files.write(pathToFile, fileContent.getBytes());
        } catch (IOException e) {
            throw new FileSavingException(e);
        }
    }

    private static void markFileAsProcessed(Path pathToFile) {
        try {
            Files.move(pathToFile, pathToFile.resolveSibling(getUpdatedFileName(pathToFile)));
        } catch (IOException e) {
            throw new FileRenamingException(e);
        }
    }
}
