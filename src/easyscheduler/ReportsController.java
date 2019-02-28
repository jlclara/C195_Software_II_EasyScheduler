/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyscheduler;

import easyscheduler.util.DBConnection;
import static easyscheduler.AppointmentsController.getAppointmentData;
import static easyscheduler.AppointmentsController.setItemSelected;
import easyscheduler.model.Appointment;
import easyscheduler.model.User;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javax.swing.JOptionPane;


/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class ReportsController {

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab report1Tab;
    @FXML
    private Tab report2Tab;
    @FXML
    private Button backBtn;
    @FXML
    private TableView<Appointment> appointmentTableView;
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
    private ChoiceBox<Integer> firstCb;
    @FXML
    private ChoiceBox<String> secondCb;
    @FXML
    private ChoiceBox<String> thirdCb;
    @FXML
    private Label resultsLbl;
    @FXML
    private Tab report3Tab;
    
    private EasyScheduler mainApp;
    private User currentUser;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");
    private ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
   
    @FXML
    private HBox hBox;


    public ReportsController() {
    }
  
    public void setUpReports(EasyScheduler mainApp, User currentUser) {
        this.mainApp = mainApp;
        this.currentUser = currentUser;
        
       // Set Up TableView with Date Formatting
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
        
        // Get AppointmentList for ALL appts, NOT loading into TableView yet
        appointmentList.addAll(getAppointmentData());
        
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

        showReport1();
        resultsLbl.setText(appointmentTableView.getItems().size() + " results found.");
    }
    
    // This method filters out appointmentList for results that matches the user's query and 
    // updates the results found label with the correct number of items found
    @FXML
    private void updateResults() {
        if (report1Tab.isSelected())
            appointmentTableView.getItems().setAll(appointmentList.filtered(isReport1List()));
        else if (report2Tab.isSelected())
            appointmentTableView.getItems().setAll(appointmentList.filtered(isReport2List()));
        else if(report3Tab.isSelected())
            appointmentTableView.getItems().setAll(appointmentList.filtered(isReport3List()));

        resultsLbl.setText(appointmentTableView.getItems().size() + " results found.");
    }
    
    @FXML
    private void showReport1() {
        //hBox contains ChoiceBoxes & update button for reports 1 & 2. Hidden for report 3
        if (!hBox.isVisible())
            hBox.setVisible(true); 

        // if the user returns to Tab 1 after leaving tab 1, do this
        report1Tab.setOnSelectionChanged(event -> showReport1());
        
        // since we are re-using the controls, need to clear them of items when switching tabs
        firstCb.getItems().clear();
        secondCb.getItems().clear();
        thirdCb.getItems().clear();
       
        // populate choiceboxes and pre-select the current month, year, and all types of appts
        firstCb.getItems().addAll(2017, 2018, 2019, 2020);
        firstCb.getSelectionModel().select(Integer.valueOf(LocalDateTime.now().getYear()));
        secondCb.getItems().addAll("All Months", "January", "February", "March", "April", "May", 
            "June", "July", "August", "September", "October", "November", "December");
        secondCb.getSelectionModel().select(LocalDateTime.now().getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        thirdCb.getItems().clear();
        thirdCb.getItems().addAll("All Types", "Consultation", "Meeting", "Kickoff", "Update");
        thirdCb.getSelectionModel().selectFirst();
        
        // filter for results according to isReport1ListPredicate and fills tableview
        updateResults();
    }
    
    // This predicate returns a filtered list of appts that matches the user's query for report 1
    private Predicate<Appointment> isReport1List() {

        return p -> (p.getType().equalsIgnoreCase(thirdCb.getSelectionModel().getSelectedItem()) 
                || thirdCb.getSelectionModel().getSelectedItem() == "All Types")
            && (p.getStart().getMonth().toString().equalsIgnoreCase(secondCb.getSelectionModel().getSelectedItem())
                    || secondCb.getSelectionModel().getSelectedItem() == "All Months")
            && p.getStart().getYear() == firstCb.getSelectionModel().getSelectedItem();
    }
    
    @FXML
    private void showReport2() {
        if (!hBox.isVisible())
            hBox.setVisible(true);

        // only need to clear the third box, the other two boxes are the same as report 1
        thirdCb.getItems().clear();
        
        // Populate third ChoiceBox with customer's name from existing customers database table
        try {
            String customerQuery = "SELECT customer.customerName"
                + " FROM customer" 
                + " WHERE customer.active = 1"
                + " ORDER BY customer.customerName";
            PreparedStatement smt = DBConnection.getConn().prepareStatement(customerQuery);
            ResultSet customersFound = smt.executeQuery();
            while (customersFound.next()) {
                String dCustomerName = customersFound.getString("customer.customerName");
                thirdCb.getItems().add(dCustomerName);
            }
        } catch (SQLException sqe) {
            System.out.println("Check Customer CB List SQL");
            sqe.printStackTrace();
        } 
        thirdCb.getItems().add(0, "[Select Customer]");
        thirdCb.getSelectionModel().selectFirst();
        
        updateResults();
    }
    
    private Predicate<Appointment> isReport2List() {
        return p -> p.getCustomerName().equalsIgnoreCase(thirdCb.getSelectionModel().getSelectedItem()) 
            && (p.getStart().getMonth().toString().equalsIgnoreCase(secondCb.getSelectionModel().getSelectedItem())
                    || secondCb.getSelectionModel().getSelectedItem() == "All Months")
            && p.getStart().getYear() == firstCb.getSelectionModel().getSelectedItem();
    }
    
    @FXML 
    private void showReport3() {
        // hiding ChoiceBoxes and update button for report 3
        hBox.setVisible(false);
        updateResults();
    }
    
    // filter for user's future appts only
    private Predicate<Appointment> isReport3List() {
        return p -> p.getUserId() == currentUser.getUserID()
                && p.getStart().isAfter(LocalDateTime.now());
    }
    
    @FXML
    private void handlePrint(ActionEvent e) throws IOException {
        Writer writer = null;
        ObservableList<Appointment> tableOutput = FXCollections.observableArrayList();
        tableOutput = appointmentTableView.getItems();
        String dialog;
        try{
            do{
                dialog = JOptionPane.showInputDialog("Rename your file (no spaces, /, \\, or .):");
            }while(dialog.contains(".") || dialog.contains("\\") || dialog.contains("/")|| dialog.isEmpty());
            
            String workingDir = System.getProperty("user.dir") + "\\src\\output\\" + dialog + ".txt";
    
            File file = new File(workingDir);
            writer = new BufferedWriter(new FileWriter(file));
            
            if (hBox.isVisible())
            {
                String queryInfoString = report1Tab.getText() + " Report" + "\r\nYear: " 
                    + firstCb.getSelectionModel().getSelectedItem() 
                    + ", Month: " + secondCb.getSelectionModel().getSelectedItem()
                    + ", Type/Customer: " + thirdCb.getSelectionModel().getSelectedItem() + "\r\n\r\n";
                writer.write(queryInfoString);
            }
            else writer.write("Your Upcoming Schedule\r\n\r\n");
            
            String headerString = "Start DateTime" + "\t" + 
                    "End DateTime" + "\t" + "Title" + "\t" + "Customer" + "\t" + 
                    "Agent" + "\t" + "Type" + "\r\n";
            writer.write(headerString);
            
            for (int i = 0; tableOutput.size()>i; i++){
            
                    Appointment apt = tableOutput.get(i);
                    String result = apt.getStartFormatted() 
                            + "\t" +  apt.getEnd().format(formatter)
                            + "\t" +  apt.getTitle() + "\t" + apt.getCustomerName() 
                            + "\t" + apt.getUserName() +"\t" + apt.getType() + "\r\n";
                    writer.write(result);
            }
            String count = "\r\nCount: " + tableOutput.size() + "\r\n\r\n";
            writer.write(count);
            
            Alert successAlert = new Alert(Alert.AlertType.CONFIRMATION);
            successAlert.setHeaderText("Successfully Generated Report!");
            successAlert.setContentText("Report Generated. Do you want "
                    + "to open the report file? ");
            Optional<ButtonType> result = successAlert.showAndWait();
            if (result.get() == ButtonType.OK)
                Desktop.getDesktop().open(file);
        }catch (Exception ex) {
        ex.printStackTrace();
        }
        finally {
            writer.flush();
             writer.close();
        }
    }
 
    @FXML
    private void handleBackButton(ActionEvent event) {
        mainApp.showMainMenu(currentUser);
    }
    
}
