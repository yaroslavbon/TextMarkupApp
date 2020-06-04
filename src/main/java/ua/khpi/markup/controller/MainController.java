package ua.khpi.markup.controller;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ua.khpi.markup.exception.FileRenamingException;
import ua.khpi.markup.exception.FileSavingException;
import ua.khpi.markup.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static ua.khpi.markup.util.ControllerUtils.showAlert;

public class MainController {

    private static final String DEFAULT_MARKUP_PREFIX = "<complex>";
    private static final String DEFAULT_MARKUP_SUFFIX = "</complex>";

    private String markupPrefix;
    private String markupSuffix;

    private Iterator<Path> iterator;
    private Path currentFile;
    private Deque<String> currentFileChangeHistory;

    @FXML
    private TextField fileChooserText;

    @FXML
    private TextArea textArea;

    @FXML
    private Label currentTextLabel;

    @FXML
    private Button saveBtn;

    @FXML
    private Button skipBtn;

    @FXML
    private Button undoBtn;

    @FXML
    void choosePathToTexts(ActionEvent event) {
        processingFinished();

        File directoryWithTexts = getDirectoryWithTextsToProcess(event);
        if (directoryWithTexts == null) return;

        try {
            initializeProcessing(directoryWithTexts);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "IOException",
                    "An error occurred while trying to get texts from chosen directory");
        }
    }

    @FXML
    void markTextSegment(KeyEvent event) {
        if (event.getCode() != KeyCode.SPACE) return;
        String selectedTextSegment = textArea.getSelectedText();
        if (selectedTextSegment.trim().isEmpty()) return;

        String markedTextSegment = markupPrefix + selectedTextSegment + markupSuffix;

        String text = textArea.getText();
        String updatedText = text.replace(selectedTextSegment, markedTextSegment);

        textArea.deselect();
        textArea.setText(updatedText);
        currentFileChangeHistory.push(updatedText);
    }

    @FXML
    void saveText(ActionEvent event) {
        String processedText = textArea.getText();

        if (processedText.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Text is blank", "You can not save empty text");
            return;
        }

        if (currentFileChangeHistory.peekFirst() == currentFileChangeHistory.peekLast()) {
            Optional<ButtonType> changesDetected = showAlert(Alert.AlertType.CONFIRMATION, "No changes detected",
                    "You are trying to mark file as processed however you did not make any changes to it. Proceed?");
            if ((changesDetected.isPresent()) && !(changesDetected.get() == ButtonType.OK)) {
                return;
            }
        }

        try {
            FileUtils.saveProcessedFile(currentFile, processedText);
        } catch (FileSavingException e) {
            showAlert(Alert.AlertType.ERROR, "Unable to save file",
                    "Unable to save file, skipping it for now");
        } catch (FileRenamingException e) {
            showAlert(Alert.AlertType.ERROR, "Unable to mark file as processed",
                    "Your markup was saved but file was not marked as processed");
        }
        processNextFile();
    }

    @FXML
    void skipText(ActionEvent event) {
        Optional<ButtonType> wannaSkip = showAlert(Alert.AlertType.CONFIRMATION, "Are you sure?",
                "This file will be skipped but will not be marked as processed");
        if ((wannaSkip.isPresent()) && (wannaSkip.get() == ButtonType.OK)) {
            processNextFile();
        }
    }

    @FXML
    void undo(ActionEvent event) {
        if (currentFileChangeHistory.size() == 1) return;

        currentFileChangeHistory.pop();
        textArea.setText(currentFileChangeHistory.peek());
    }

    //what an ugly stuff...
    @FXML
    void openSettings(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/settings.fxml"));
        Parent parent = fxmlLoader.load();
        SettingsController settingsController = fxmlLoader.getController();
        settingsController.init(this);

        Scene scene = new Scene(parent, 400, 100);
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public void init() {
        markupPrefix = DEFAULT_MARKUP_PREFIX;
        markupSuffix = DEFAULT_MARKUP_SUFFIX;
        currentFileChangeHistory = new LinkedList<>();
        tuneTextArea();
    }

    private void tuneTextArea() {
        textArea.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        textArea.setOnKeyPressed(this::markTextSegment);
        textArea.setEditable(false);
        textArea.setStyle("-fx-opacity: 1;");
    }

    private File getDirectoryWithTextsToProcess(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Choose a folder");
        return dirChooser.showDialog(((Node) event.getSource()).getScene().getWindow());
    }

    private void initializeProcessing(File directoryWithTexts) throws IOException {
        List<Path> filesToProcess = FileUtils.getFilesToProcess(directoryWithTexts);

        if (filesToProcess.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No files found",
                    "Found 0 files with .txt extension that was not processed");
            return;
        }

        Optional<ButtonType> confirmProcessing = showAlert(Alert.AlertType.CONFIRMATION, "Files was found",
                String.format("Found %s files to process, proceed?", filesToProcess.size()));

        if (confirmProcessing.isPresent() && confirmProcessing.get() == ButtonType.OK) {
            fileChooserText.setText(directoryWithTexts.getAbsolutePath());
            this.iterator = filesToProcess.iterator();
            setTextProcessingButtonsEnabled(true);
            processNextFile();
        }
    }

    private void processNextFile() {
        if (!iterator.hasNext()) {
            processingFinished();
            return;
        }

        currentFile = iterator.next();
        currentFileChangeHistory.clear();
        currentTextLabel.setText(String.format("Processing %s", currentFile.getFileName()));
        try {
            String fileContent = FileUtils.getFileContent(currentFile);
            textArea.setText(fileContent);
            currentFileChangeHistory.push(fileContent);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Can not open file",
                    String.format("Exception occurred while opening %s\nTrying to open next file in list", currentFile.getFileName()));
            processNextFile();
        }
    }

    private void processingFinished() {
        currentTextLabel.setText("Processing finished");
        textArea.clear();
        setTextProcessingButtonsEnabled(false);
    }

    private void setTextProcessingButtonsEnabled(boolean enabled) {
        saveBtn.setDisable(!enabled);
        undoBtn.setDisable(!enabled);
        skipBtn.setDisable(!enabled);
    }

    public String getMarkupPrefix() {
        return markupPrefix;
    }

    public void setMarkupPrefix(String markupPrefix) {
        this.markupPrefix = markupPrefix;
    }

    public String getMarkupSuffix() {
        return markupSuffix;
    }

    public void setMarkupSuffix(String markupSuffix) {
        this.markupSuffix = markupSuffix;
    }
}

