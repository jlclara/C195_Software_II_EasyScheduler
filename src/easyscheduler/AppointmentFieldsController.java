/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyscheduler;

import easyscheduler.util.DBConnection;
import easyscheduler.model.Appointment;
import easyscheduler.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import static java.sql.Types.NULL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class AppointmentFieldsController {

    @FXML
    private Label errorMsgLbl;
    @FXML
    private TextField apptIdTf;
    @FXML
    private ChoiceBox<String> customerNameCb;
    @FXML
    private ChoiceBox<String> agentNameCb;
    @FXML
    private TextField apptTitleTf;
    @FXML
    private DatePicker dateDtp;
    @FXML
    private ChoiceBox<Integer> hourCb;
    @FXML
    private ChoiceBox<Integer> minuteCb;
    @FXML
    private ChoiceBox<Integer> durationCb;
    @FXML
    private ChoiceBox<String> typeCb;
    @FXML
    private TextArea apptDescriptionTa;
    @FXML
    private Button apptSaveBtn;
    @FXML
    private Button apptCancelBtn;
    private final  Connection connection = DBConnection.getConn();
    private User currentUser;
    private EasyScheduler mainApp;
    private Appointment selectedAppt;
    private Integer hoursOffset;

    @FXML
    private void handleApptCancel (ActionEvent e)
    {
        Alert cancelAlert = new Alert (Alert.AlertType.CONFIRMATION);
        cancelAlert.setTitle("Cancel Adding/Editing Appoitnments?");
        cancelAlert.setHeaderText("Are you sure you want to cancel? "
                + "Any unsaved progress will be lost.");
        Optional<ButtonType> result = cancelAlert.showAndWait();
        if (result.get() == ButtonType.OK) {
            AppointmentsController.resetItemSelected();
            mainApp.closePopup(currentUser);

        }
            
    }
    
    @FXML 
    private void handleApptSave (ActionEvent e)
    {
        String customerName = customerNameCb.getSelectionModel().getSelectedItem();
        String agentName = agentNameCb.getSelectionModel().getSelectedItem();
        String apptTitle = apptTitleTf.getText();
        LocalDate startDate = null;
        if (dateDtp.getValue()!=null)
            startDate = dateDtp.getValue();
        Integer hour = hourCb.getSelectionModel().getSelectedItem();
        Integer minute = minuteCb.getSelectionModel().getSelectedItem();
        Integer duration = durationCb.getSelectionModel().getSelectedItem();
        String type = typeCb.getSelectionModel().getSelectedItem();
        String description = apptDescriptionTa.getText();
        
        Integer customerId = null;
        Integer agentId = null;
        
        // Test if fields are completed. Description field is optional.
        if (customerNameCb.getSelectionModel().isEmpty() || customerName.length() == 0
                || agentNameCb.getSelectionModel().isEmpty() || agentName.length() == 0
                || apptTitle.length() == 0 
                || startDate == null || startDate.toString().length() == 0
                || hourCb.getSelectionModel().isEmpty()
                || minuteCb.getSelectionModel().isEmpty()
                || durationCb.getSelectionModel().isEmpty()
                || typeCb.getSelectionModel().isEmpty())
            errorMsgLbl.setText("All required fields must be completed.");
        else if((hour-hoursOffset)*60+minute+duration > 990)
            errorMsgLbl.setText("The appointment time slot is after our business hours: "
                    + "\r\n"+ (7+hoursOffset)+"00 to " + (4+hoursOffset+12) + "30 Local Time");
        else {
            LocalDateTime startLdt = LocalDateTime.of(startDate.getYear(), 
                startDate.getMonthValue(), startDate.getDayOfMonth(), hour, minute); 
            LocalDateTime endLdt = startLdt.plusMinutes(duration); 
            
            
            try {
                // retrieve customer ID using customer name
                PreparedStatement custIdSmt = connection.prepareStatement("SELECT customer.customerId"
                        + " FROM customer"
                        + " WHERE customer.customerName = ?");
                custIdSmt.setString(1, customerName);
                ResultSet custIdRs = custIdSmt.executeQuery();
                if (custIdRs.next())
                        customerId = custIdRs.getInt("customer.customerId");

                // retrieve user ID using name
                PreparedStatement agentIdSmt = connection.prepareStatement("SELECT user.userId"
                        + " FROM user"
                        + " WHERE user.userName = ?");
                agentIdSmt.setString(1, agentName);
                ResultSet AgentIdRs = agentIdSmt.executeQuery();
                if (AgentIdRs.next())
                        agentId = AgentIdRs.getInt("user.userId");
                    
                // test for overlapping appointments
                String overlappingQuery = "SELECT appointment.*"
                        + " FROM appointment"
                        + " WHERE ((appointment.contact = ?"
                        + " OR appointment.customerId = ?)"
                        + " AND (appointment.start BETWEEN ? AND ?"
                        + " OR appointment.end BETWEEN ? AND ?)"
                        + " AND appointment.appointmentId <> ?)"; // excludes selected appt if modifying
                PreparedStatement overlappingSmt = connection.prepareStatement(overlappingQuery);
                int i = 1;
                overlappingSmt.setString(i++, agentId.toString());
                overlappingSmt.setInt(i++, customerId);
                overlappingSmt.setTimestamp(i++, Timestamp.valueOf(startLdt));
                overlappingSmt.setTimestamp(i++, Timestamp.valueOf(endLdt.minusMinutes(1)));
                overlappingSmt.setTimestamp(i++, Timestamp.valueOf(startLdt.plusMinutes(1)));
                overlappingSmt.setTimestamp(i++, Timestamp.valueOf(endLdt));
                if (AppointmentsController.getItemSelected()== null)
                    overlappingSmt.setInt(i++, NULL);
                else
                    overlappingSmt.setInt(i++, AppointmentsController.getItemSelected().getAppointmentId());

                ResultSet overlappingRs = overlappingSmt.executeQuery();
                
                if (overlappingRs.next()) {
                    String overlappingPerson = null;
                    Integer userId = Integer.parseInt(overlappingRs.getString("contact"));
                    Integer customerIdfromQuery = overlappingRs.getInt("customerId");
                    try {
                    if (customerIdfromQuery == customerId && userId == agentId)
                        overlappingPerson = "Both " + agentName + " and " + customerName + " have ";
                    else if(userId == agentId)
                        overlappingPerson = agentName + " has " ;
                    else if (customerIdfromQuery == customerId)
                        overlappingPerson = customerName + " has ";
                    else {
                        overlappingPerson = "Something went wrong!";
                        throw new Exception("Something went wrong with adding an appointment.");
                    }
                    } catch (Exception E) {
                        E.printStackTrace();
                    }
                    overlappingPerson +="an overlapping appointment" + "\n";
                    System.out.println("Overlapping appointment");
                    Alert overlappingAlert = new Alert (Alert.AlertType.WARNING);
                    overlappingAlert.setTitle("Overlapping Appointment");
                    overlappingAlert.setHeaderText("Cannot add an overlapping appointment.");
                    String overlappingMessage = overlappingPerson + "Appt #" + Integer.toString(overlappingRs.getInt
                            ("appointmentId")) + ": '" + overlappingRs.getString("title")
                            + "' from " + overlappingRs.getTimestamp("start") 
                            + " to " + overlappingRs.getTimestamp("end");
                    overlappingAlert.setContentText(overlappingMessage);
                    overlappingAlert.showAndWait();
                }
                else  //if not overlapping insert/update
                {
                    selectedAppt = AppointmentsController.getItemSelected();
                    String apptQuery = null;
                    if (selectedAppt == null)
                    {
                        apptQuery = "INSERT INTO appointment ("
                                + " customerId,"
                                + " title,"
                                + " description,"
                                + " location," // appt type
                                + " contact,"  // user Id
                                + " url,"
                                + " start,"
                                + " end,"
                                + " createDate,"
                                + " createdBy,"
                                + " lastUpdate,"
                                + " lastUpdateBy) VALUES ("
                                + " ?, ?, ?, ?, ?, ?, ?, ?,"
                                + " CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";
                    }
                    else
                    {
                        apptQuery = "UPDATE appointment "
                                + " SET customerId = ?,"
                                + " title = ?,"
                                + " description = ?,"
                                + " location = ?," // appt type
                                + " contact = ?," // userId
                                + " url = ?,"
                                + " start = ?,"
                                + " end = ?,"
                                + " lastUpdate = CURRENT_TIMESTAMP,"
                                + " lastUpdateBy = ?"
                                + " WHERE appointmentId = ?";
                    }
                    PreparedStatement st = connection.prepareStatement(apptQuery);
                    i = 1;
                    st.setInt(i++, customerId);
                    st.setString(i++, apptTitle);
                    st.setString(i++, description);
                    st.setString(i++, type);
                    st.setInt(i++, agentId);
                    st.setString(i++, "Dummy URL");
                    st.setTimestamp(i++, Timestamp.valueOf(startLdt));
                    st.setTimestamp(i++, Timestamp.valueOf(endLdt));
                    st.setString(i++, currentUser.getUsername());
                    if (selectedAppt == null)
                        st.setString(i++, currentUser.getUsername());
                    else
                        st.setInt(i++, selectedAppt.getAppointmentId());
                    st.executeUpdate();
                    st.close();

                    mainApp.closePopup(currentUser);
                    
                }
            } catch (SQLException sqe) {
                sqe.printStackTrace();
            }
        }
    }
    
    public void setUpAppointmentFields(EasyScheduler scheduler, User activeUser){
        this.mainApp = scheduler;
        this.currentUser = activeUser;
        
        // populate form
        OffsetDateTime odt = OffsetDateTime.now ();
        ZoneOffset zoneOffset = odt.getOffset ();    
        hoursOffset = zoneOffset.getTotalSeconds()/60/60;
        hoursOffset = hoursOffset -(-7); // your local timezone vs PST timezone
        Callback<DatePicker, DateCell> dayCellFactory = this.getDayCellFactory();
        dateDtp.setDayCellFactory(dayCellFactory);
        typeCb.getItems().addAll("Consultation", "Meeting", "Kickoff", "Update");
        minuteCb.getItems().addAll(00, 15, 30, 45);
        hourCb.getItems().addAll(7+hoursOffset, 8+hoursOffset , 9+hoursOffset, 10+hoursOffset, 
                11+hoursOffset, 12+hoursOffset, 13+hoursOffset, 14+hoursOffset, 
                15+hoursOffset, 16+hoursOffset);
        durationCb.getItems().addAll(15, 30, 45, 60);
        
        // populate Customers ChoiceBox
        try {
            String customerQuery = "SELECT customer.customerName"
                + " FROM customer" 
                + " WHERE customer.active = 1"
                + " ORDER BY customer.customerName";
            PreparedStatement smt = connection.prepareStatement(customerQuery);
            ResultSet customersFound = smt.executeQuery();
            while (customersFound.next()) {
                String dCustomerName = customersFound.getString("customer.customerName");
                customerNameCb.getItems().add(dCustomerName);
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } 
        
        // populate Agents ChoiceBox
        try {
            String userQuery = "SELECT user.userName"
                + " FROM user" 
                + " WHERE user.active = 1"
                + " ORDER BY user.userName";
        
            PreparedStatement smt = connection.prepareStatement(userQuery);
            ResultSet usersFound = smt.executeQuery();

            while (usersFound.next()) {
                String dUserName = usersFound.getString("user.userName");
                agentNameCb.getItems().add(dUserName);
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        }
        
        // populate fields when editing appoinments with existing data
        Appointment modAppt = AppointmentsController.getItemSelected();
        if (modAppt != null)
        {
            Integer hour = modAppt.getStart().getHour();
            Integer minute = modAppt.getStart().getMinute();
            Integer endHour = modAppt.getEnd().getHour();
            Integer endMinute = modAppt.getEnd().getMinute();
            Integer duration = (endHour-hour)*60 + (endMinute-minute);
            
            apptIdTf.setText(modAppt.getAppointmentId().toString());
            customerNameCb.getSelectionModel().select(modAppt.getCustomerName());
            agentNameCb.getSelectionModel().select(modAppt.getUserName());
            apptTitleTf.setText(modAppt.getTitle());
            dateDtp.setValue(modAppt.getStart().toLocalDate());
            hourCb.getSelectionModel().select(hour);
            minuteCb.getSelectionModel().select(minute);
            durationCb.getSelectionModel().select(duration);
            typeCb.getSelectionModel().select(modAppt.getType());
            apptDescriptionTa.setText(modAppt.getDescription());
        }
    }
    
    // Disable weekends and past dates to prevent user from adding
    // an invalid date or a date that is outside of normal business hours
    private Callback<DatePicker, DateCell> getDayCellFactory() {
 
        final Callback<DatePicker, DateCell> dayCellFactory = (final DatePicker datePicker) -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item.isBefore(LocalDate.now()) ||
                        item.getDayOfWeek() == DayOfWeek.SUNDAY //
                        || item.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    setDisable(true);
                    setStyle("-fx-background-color: #D3D3D3;");
                }
            }
        };
        return dayCellFactory;
    }

 
}
