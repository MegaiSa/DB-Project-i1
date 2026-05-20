package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;
import java.util.stream.Collectors;

public class ArtistController {

    @FXML private TextField searchField;
    @FXML private ComboBox<Discipline> disciplineFilter;
    @FXML private TableView<Artist> artistTable;
    @FXML private TableColumn<Artist, String> nameColumn;
    @FXML private TableColumn<Artist, String> cityColumn;
    @FXML private TableColumn<Artist, String> emailColumn;
    @FXML private TableColumn<Artist, Integer> yearColumn;
    @FXML private TableColumn<Artist, String> disciplinesColumn;
    @FXML private Label statusLabel;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        disciplinesColumn.setCellValueFactory(cellData -> {
            String list = cellData.getValue().getDisciplines().stream()
                    .map(Discipline::getName)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(list);
        });

        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        refreshStatus();
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(
                artistService.searchArtists(query, dName, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle("Add artist");
        dialog.setHeaderText("Create a new artist");

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField nameF  = new TextField();   nameF.setPromptText("Name (unique)");
        TextField emailF = new TextField();   emailF.setPromptText("Contact email (unique)");
        TextField cityF  = new TextField();   cityF.setPromptText("City");
        TextField yearF  = new TextField();   yearF.setPromptText("Birth year");
        TextField bioF   = new TextField();   bioF.setPromptText("Short bio");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Name"),  0, 0); grid.add(nameF,  1, 0);
        grid.add(new Label("Email"), 0, 1); grid.add(emailF, 1, 1);
        grid.add(new Label("City"),  0, 2); grid.add(cityF,  1, 2);
        grid.add(new Label("Year"),  0, 3); grid.add(yearF,  1, 3);
        grid.add(new Label("Bio"),   0, 4); grid.add(bioF,   1, 4);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != saveType) return null;
            Integer year = null;
            try {
                if (!yearF.getText().isBlank()) year = Integer.parseInt(yearF.getText().trim());
            } catch (NumberFormatException ignore) { /* ignore */ }
            return new Artist(nameF.getText().trim(), bioF.getText().trim(), year,
                    emailF.getText().trim(), cityF.getText().trim());
        });

        Optional<Artist> result = dialog.showAndWait();
        result.ifPresent(a -> {
            try {
                artistService.createArtist(a);
                refreshTable();
                statusLabel.setText("Saved artist \"" + a.getName() + "\".");
            } catch (RuntimeException ex) {
                showError("Could not save artist", ex);
            }
        });
    }

    @FXML
    private void handleDelete() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an artist row first.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete artist \"" + selected.getName() + "\" and all their artworks?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirm delete");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.YES) {
            try {
                artistService.deleteArtist(selected.getName());
                refreshTable();
                statusLabel.setText("Deleted artist \"" + selected.getName() + "\".");
            } catch (RuntimeException ex) {
                showError("Could not delete artist", ex);
            }
        }
    }

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    private void refreshStatus() {
        statusLabel.setText(ServiceProvider.isUsingDatabase()
                ? "Connected to MySQL/MariaDB."
                : "In-memory mode (database unreachable).");
    }

    private void showError(String header, Throwable ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(header);
        a.setContentText(ex.getMessage());
        a.showAndWait();
    }
}
