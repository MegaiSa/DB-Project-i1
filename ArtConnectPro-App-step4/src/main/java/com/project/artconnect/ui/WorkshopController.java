package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;

public class WorkshopController {

    @FXML private TableView<Workshop> workshopTable;
    @FXML private TableColumn<Workshop, String> titleColumn;
    @FXML private TableColumn<Workshop, LocalDateTime> dateColumn;
    @FXML private TableColumn<Workshop, String> instructorColumn;
    @FXML private TableColumn<Workshop, Double> priceColumn;
    @FXML private TableColumn<Workshop, String> levelColumn;
    @FXML private ComboBox<CommunityMember> memberCombo;
    @FXML private Label statusLabel;

    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final CommunityService communityService = ServiceProvider.getCommunityService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));

        instructorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getInstructor() != null
                        ? cellData.getValue().getInstructor().getName()
                        : "Unknown"));

        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
        memberCombo.setItems(FXCollections.observableArrayList(communityService.getAllMembers()));
    }

    @FXML
    private void handleBook() {
        Workshop w = workshopTable.getSelectionModel().getSelectedItem();
        CommunityMember m = memberCombo.getValue();
        if (w == null) {
            statusLabel.setText("Select a workshop row first.");
            return;
        }
        if (m == null) {
            statusLabel.setText("Pick a member from the dropdown first.");
            return;
        }
        try {
            workshopService.bookWorkshop(w, m);
            statusLabel.setText("Booked " + m.getName() + " for \"" + w.getTitle() + "\".");
        } catch (RuntimeException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Booking failed");
            a.setHeaderText("The database rejected this booking");
            a.setContentText(ex.getMessage());
            a.showAndWait();
            statusLabel.setText("Booking refused (see error dialog).");
        }
    }
}
