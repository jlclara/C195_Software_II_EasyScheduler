/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyscheduler;

import easyscheduler.util.DBConnection;
import easyscheduler.model.Appointment;
import easyscheduler.model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class CalendarController {

    @FXML
    private TreeTableView<Appointment> treeTable;
    @FXML
    private TreeTableColumn<Appointment, String> monthCol;
    @FXML
    private TreeTableColumn<Appointment, LocalDateTime> monthStartCol;
    @FXML
    private TreeTableColumn<Appointment, LocalDateTime> monthEndCol;
    @FXML
    private TreeTableColumn<Appointment, String> monthTitleCol;
    @FXML
    private TreeTableColumn<Appointment, String> monthCustCol;
    @FXML
    private TreeTableColumn<Appointment, String> monthAgentCol;
    @FXML
    private TreeTableColumn<Appointment, String> monthTypeCol;
    @FXML
    private RadioButton onlyAgentRb;
    @FXML
    private RadioButton allAgentsRb;
    @FXML
    private CheckBox upcomingCxBx;
    @FXML
    private CheckBox consultationCxBx;
    @FXML
    private CheckBox kickoffCxBx;
    @FXML
    private CheckBox meetingCxBx;
    @FXML
    private CheckBox updateCxBx;
    @FXML
    private TextField customerTf;
    @FXML
    private RadioButton monthRb, weekRb;
    @FXML
    private Label foundLabel;
   
    private User currentUser;
    private EasyScheduler mainApp;    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");
    ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
    @FXML
    private Pane pane;
    @FXML
    private ToggleGroup agents;
    @FXML
    private Button filterBtn;
    @FXML
    private ToggleGroup view;
    @FXML
    private Button backBtn;
    
    public CalendarController() {
    }
    
    public void setUpCalendar(EasyScheduler mainApp, User currentUser) {
        this.currentUser = currentUser;
        this.mainApp = mainApp;
        onlyAgentRb.setText(currentUser.getUsername());
        
        monthCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("url"));
        monthStartCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("start"));
        monthStartCol.setCellFactory(column -> {
            return new TreeTableCell<Appointment, LocalDateTime>(){
                @Override
                protected void updateItem(LocalDateTime date, boolean empty) {
                    super.updateItem(date, empty);     
                    if (date == null || empty)
                        setText("");
                    else
                        setText(formatter.format(date));
                }
            };
        });
        monthEndCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("end"));
        monthEndCol.setCellFactory(column -> {
            return new TreeTableCell<Appointment, LocalDateTime>(){
                @Override
                protected void updateItem(LocalDateTime date, boolean empty) {
                    super.updateItem(date, empty);     
                    if (date == null || empty)
                        setText("");
                    else
                        setText(formatter.format(date));
                }
            };
        });
        monthTitleCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("title"));
        monthCustCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("customerName"));
        monthAgentCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("userName"));
        monthTypeCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));
        
        treeTable.setRowFactory(ttv -> {
            TreeTableRow<Appointment> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() && treeTable.getSelectionModel().getSelectedItem().isLeaf()) {
                    AppointmentsController.setItemSelected(treeTable.getSelectionModel().getSelectedItem().getValue());
                    mainApp.showAppointmentFields(currentUser);
                }
            });
            return row;
        });
        
        // populate calendar table view with all appts, starting w/ month view
        getAppointments();
        showMonthView();
    }

    // method is called by "Update Results" button click and by setUpCalendar method
    // to get updated appointments based on user filter selections
    @FXML
    private void getAppointments() {
            appointmentList.clear();
            try {
                String apptQuery = "SELECT appointment.appointmentId, appointment.title,"
                    + " appointment.description, customer.customerName, appointment.customerId,"
                    + " appointment.location, appointment.contact," // using location field for appointment type & contact for agent username
                    + " appointment.start, appointment.end, user.username"   
                    + " FROM appointment, customer, user"
                    + " WHERE appointment.customerId = customer.customerId"
                    + " AND user.userId = appointment.contact";
            PreparedStatement smt = DBConnection.getConn().prepareStatement(apptQuery);
            
            ResultSet appointmentsFound = smt.executeQuery();
            while (appointmentsFound.next()) {
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
                
                // Filter results based on user selections 
                if (onlyAgentRb.isSelected())
                    if(userId != currentUser.getUserID())
                        continue;
                if(!customerTf.getText().isEmpty())
                    if(!customer.toLowerCase().contains(customerTf.getText().toLowerCase()))
                        continue;
                if(upcomingCxBx.isSelected())
                    if (start.isBefore(LocalDateTime.now()))
                        continue;
                if(!consultationCxBx.isSelected())
                    if (type.equalsIgnoreCase("consultation"))
                        continue;
                if(!kickoffCxBx.isSelected())
                    if (type.equalsIgnoreCase("kickoff"))
                        continue;
                if(!meetingCxBx.isSelected())
                    if (type.equalsIgnoreCase("meeting"))
                        continue;
                if(!updateCxBx.isSelected())
                    if (type.equalsIgnoreCase("update"))
                        continue;
                
                // only add appointments that are not filtered out to the appointment list
                appointmentList.add(new Appointment (appointmentId, customerId, customer,
                        title, description, type, start, end, userId, user));
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();        
        }
            
        foundLabel.setText(appointmentList.size() + " results found.");
           
        if (weekRb.isSelected())
            showWeekView();
        else if (monthRb.isSelected())
            showMonthView();
//        treeTable.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
//            if (e.getButton() == MouseButton.PRIMARY) {
//                if (treeTable.getSelectionModel().getSelectedItem().isLeaf())
//                    System.out.println("Testing left click");
//                e.consume();
//            }
//                });
    }
    
    @FXML
    private void showMonthView()
    {
        Appointment mainRootAppt = new Appointment("Month");
        TreeItem<Appointment> mainRoot = new TreeItem(mainRootAppt);
        
        ObservableList<TreeItem<Appointment>> subRootMonth = FXCollections.observableArrayList();
        ObservableList<TreeItem<Appointment>> removeSubRootMonth = FXCollections.observableArrayList(); // placeholder list

        String[] monthString = {"January", "February", "March", "April", "May", 
            "June", "July", "August", "September", "October", "November", "December", "Over 1 Year", "Elasped"}; 
        for (int i = 0; i < monthString.length; i++)
        {    
            subRootMonth.add(new TreeItem(new Appointment(monthString[i])));
        }
        
        
         /* lambda1
        This first lambda expression takes an Appointment parameter and adds
        the appointment as a TreeItem to the appropriate month's subroot node. 
        For appointments that have elasped, this expression adds the appointment 
        to a separate node called "Elasped" and likewise for appointments over 
        one year away for a cleaner, more useful calendar look. I used a lambda 
        expression for this task because the easiest way to iterate through the 
        and perform a complex action is using the forEach method.
        */
        Consumer<Appointment> lambda1 = (Appointment a) -> {
                    if (a.getStart().isBefore(LocalDateTime.now())) // elasped
                        subRootMonth.get(13).getChildren().add(new TreeItem(a));
                    
                    else if (a.getStart().isAfter(LocalDateTime.now().plusYears(1))) { // over 1 year
                    
                        subRootMonth.get(12).getChildren().add(new TreeItem(a));
                        subRootMonth.get(12).setExpanded(true);
                    }
                    else {
                        for (int i = 0; i<monthString.length; i++) // less than 1 year away
                        {
                            if(a.getStart().getMonth().toString().equalsIgnoreCase(monthString[i]))
                            {
                               subRootMonth.get(i).getChildren().add(new TreeItem(a));
                               subRootMonth.get(i).setExpanded(true);
                               break;
                            }
                        }
                    } 
                };
        
        
         /* lambda2
        This second lambda expression takes an Appointment TreeItem parameter 
        and removes the Appointment TreeItem from the mainRoot node if the passed
        in Appointment TreeItem (a month sub root) is empty. 
        
        Although this step is unnecessary, the appearance of the TreeTableView
        is improved because empty nodes are not shown. I used a lambda 
        expression for this task because the easiest way to iterate through the 
        and perform a complex action is using the forEach method.
        */
        Consumer<TreeItem<Appointment>> lambda2 = (TreeItem<Appointment> a) -> {
                    if (a.isLeaf())
                        removeSubRootMonth.add(a);
                };
       // add appointment to month sub root if the appointment start month matches
        appointmentList.forEach(lambda1); 
        
        // remove empty month nodes for cleaner appearance
        subRootMonth.forEach(lambda2);
        subRootMonth.removeAll(removeSubRootMonth);  
        
        mainRoot.getChildren().addAll(subRootMonth);
        treeTable.setRoot(mainRoot);
    }
    
    @FXML
    private void showWeekView() {
        TreeItem<Appointment> mainRoot = new TreeItem(new Appointment("Week"));
        ObservableList<TreeItem<Appointment>> subRootWeek = FXCollections.observableArrayList();
        ObservableList<TreeItem<Appointment>> removeSubRootWeek = FXCollections.observableArrayList();
        String [] weekString = new String[53];
        for (int i = 0; i<weekString.length; i++) {
            if (i == 0)
                weekString[i] = "This Week";
            else if (i == 1)
                weekString [i] = "Next Week";
            else if(i == 52)
                weekString [i] = "Elasped";
            else if (i == 51)
                weekString[i] = "Over 1 Year";
            else 
                weekString[i] = (i+1) + " Weeks";
            subRootWeek.add(new TreeItem(new Appointment(weekString[i])));
        }
        
        // the "week" equivalent to lambda1 in showMonthView() method
        Consumer<Appointment> lambda1Week = (Appointment a) -> {
                    if (a.getStart().isBefore(LocalDateTime.now())) // elapsed
                        subRootWeek.get(52).getChildren().add(new TreeItem(a));
                    else if (a.getStart().isAfter(LocalDateTime.now().plusYears(1))) {
                        subRootWeek.get(51).getChildren().add(new TreeItem(a));
                        subRootWeek.get(51).setExpanded(true);
                    }
                    else {
                        for (int i = 0; i<weekString.length; i++)
                        {
                            if (a.getStart().isAfter(LocalDateTime.now().plusWeeks(i)) 
                                    && a.getStart().isBefore(LocalDateTime.now().plusWeeks(i+1)))
                            {
                               subRootWeek.get(i).getChildren().add(new TreeItem(a));
                               subRootWeek.get(i).setExpanded(true);
                               break;
                            }
                        }
                    }
                };
        
        // the "week" equivalent to lambda2 in showMonthView() method
        Consumer<TreeItem<Appointment>> lambda2Week = (TreeItem<Appointment> a) -> {
                    if (a.isLeaf())
                        removeSubRootWeek.add(a);
                };
        
        appointmentList.forEach(lambda1Week);
        
        subRootWeek.forEach(lambda2Week);
        subRootWeek.removeAll(removeSubRootWeek);  
        
        mainRoot.getChildren().addAll(subRootWeek);
        treeTable.setRoot(mainRoot);

    }

    @FXML
    private void handleBackBtn(ActionEvent event) {
        mainApp.showMainMenu(currentUser);
    }
}
