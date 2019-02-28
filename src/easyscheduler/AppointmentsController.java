/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyscheduler;

import easyscheduler.util.DBConnection;
import easyscheduler.model.Appointment;
import easyscheduler.model.User;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class AppointmentsController {

    @FXML
    private TableView<Appointment> appointmentTableView;
    @FXML
    private TableColumn<Appointment, Integer> appointmentIdCol;
    @FXML
    private TableColumn<Appointment, LocalDateTime> startTimeCol;
    @FXML
    private TableColumn<Appointment, LocalDateTime> endTimeCol;
    @FXML
    private TableColumn<Appointment, String> titleCol;
    @FXML
    private TableColumn<Appointment, String> descriptionCol;
    @FXML
    private TableColumn<Appointment, String> typeCol;
    @FXML
    private TableColumn<Appointment, String> customerCol;
    @FXML
    private TableColumn<Appointment, String> agentCol;
    @FXML
    private Button appointmentNewBtn;
    @FXML
    private Button appointmentEditBtn;
    @FXML
    private Button appointmentDeleteBtn;
    private EasyScheduler mainApp;
    private User currentUser;
    private static Appointment modAppointmentSelected;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");

    public AppointmentsController () {
    }
    
    @FXML 
    private void handleNewAppointment(ActionEvent e) throws IOException {
        resetItemSelected();
        mainApp.showAppointmentFields(currentUser);
    }
    
    @FXML 
    private void handleEditAppointment(ActionEvent e) throws IOException {
        modAppointmentSelected = appointmentTableView.getSelectionModel().getSelectedItem();
        if (modAppointmentSelected != null)
            mainApp.showAppointmentFields(currentUser);
    }
    @FXML 
    private void handleDeleteAppointment(ActionEvent e) throws IOException {
        modAppointmentSelected = appointmentTableView.getSelectionModel().getSelectedItem();
        if (modAppointmentSelected == null)
        {
            Alert noApptAlert = new Alert (Alert.AlertType.WARNING);
            noApptAlert.setTitle("No Appointment Selected");
            noApptAlert.setHeaderText("Error: No Appointment Selected");
            noApptAlert.setContentText("Please select an appointment and try again.");
            noApptAlert.showAndWait();
        }
        else
        {
            Alert deleteApptAlert = new Alert (Alert.AlertType.CONFIRMATION);
            deleteApptAlert.setTitle("Delete Appointment?");
            deleteApptAlert.setHeaderText("Are you sure you want to delete this appointment?");
            Optional<ButtonType> result = deleteApptAlert.showAndWait();
            if (result.get() == ButtonType.OK)
            {
               try {
                String deleteQuery = "DELETE appointment.*"
                        + " FROM appointment"
                        + " WHERE appointment.appointmentId = ?";
                PreparedStatement deleteSmt = DBConnection.getConn().prepareStatement(deleteQuery);
                deleteSmt.setInt(1, modAppointmentSelected.getAppointmentId());
                deleteSmt.executeUpdate();
               } catch (SQLException exc)
               {
                   exc.printStackTrace();;
               }
            }
        }
    }
    
    @FXML 
    private void handleBackBtn(ActionEvent e) throws IOException {
        mainApp.showMainMenu(currentUser);
    }
    
    public void setupAppointments(EasyScheduler mainApp, User activeUser)
    {
        this.mainApp = mainApp;
        this.currentUser = activeUser;
        resetItemSelected();
        appointmentIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("start"));
        startTimeCol.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) 
                    setText("");
                else 
                    setText(formatter.format(date));
            }
        });
        endTimeCol.setCellValueFactory(new PropertyValueFactory<>("end"));
        endTimeCol.setCellFactory(column -> new TableCell<Appointment, LocalDateTime>() {
        
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) 
                    setText("");
                else 
                    setText(formatter.format(date));
            }
        });
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        agentCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        
        appointmentTableView.setRowFactory(ttv -> {
            TableRow<Appointment> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    setItemSelected(appointmentTableView.getSelectionModel().getSelectedItem());
                    mainApp.showAppointmentFields(currentUser);
                    
                }
            });
            return row;
        });
        
        appointmentTableView.getItems().setAll(getAppointmentData());
    }
    
    public static List<Appointment> getAppointmentData() {
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        try {
            String apptQuery = "SELECT appointment.appointmentId, appointment.title,"
                    + " appointment.description, customer.customerName, appointment.customerId,"
                    + " appointment.location, appointment.contact,"
                    + " appointment.start, appointment.end, user.username"   // using location field for appointment type & contact for agent
                    + " FROM appointment, customer, user"
                    + " WHERE appointment.customerId = customer.customerId"
                    + " AND user.userId = appointment.contact"
                    + " ORDER BY appointment.appointmentId";
            PreparedStatement smt = DBConnection.getConn().prepareStatement(apptQuery);
            
            ResultSet appointmentsFound = smt.executeQuery();
            while (appointmentsFound.next()) {
                Integer zOs = OffsetDateTime.now().getOffset().getTotalSeconds();

                Integer appointmentId = appointmentsFound.getInt("appointment.appointmentId");
                String customer = appointmentsFound.getString("customer.customerName");
                Integer customerId = appointmentsFound.getInt("appointment.customerId");
                String title = appointmentsFound.getString("appointment.title");
                String description = appointmentsFound.getString("appointment.description");
                String type = appointmentsFound.getString("appointment.location"); // using location field for appointment type 
                Integer userId = appointmentsFound.getInt("appointment.contact"); // using contact field for userId
                LocalDateTime start = appointmentsFound.getTimestamp("appointment.start").toLocalDateTime();
                LocalDateTime end  = appointmentsFound.getTimestamp("appointment.end").toLocalDateTime();
                String user = appointmentsFound.getString("user.userName");
                appointmentList.add(new Appointment (appointmentId, customerId, customer,
                        title, description, type, start, end, userId, user));
            }
          
        } catch (SQLException sqe) {
            System.out.println("Check SQL");
            sqe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Something besides the SQL went wrong.");
        }
        return appointmentList;
    }
    
    public static void resetItemSelected() {
        modAppointmentSelected = null;
    }
    
    public static Appointment getItemSelected() {
        return modAppointmentSelected;
    }
    
    public static void setItemSelected(Appointment a) {
        modAppointmentSelected = a;
    }
   
}
 