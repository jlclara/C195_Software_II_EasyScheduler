package easyscheduler;

import easyscheduler.model.User;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 *
 * @author Jenny Nguyen
 */
public class MainMenuController {
    
    private EasyScheduler mainApp;
    private User currentUser;
    @FXML
    private Button mainCustomers;
    @FXML
    private Button mainAppointments;
    @FXML
    private Button mainCalendar;
    @FXML
    private Button mainReports;
    @FXML
    private Button logoutBtn;
    private Stage primaryStage;
    public MainMenuController() {
    }
    
    @FXML
    private Label mainUsernameLbl;
    
    @FXML
    private void showCustomers(ActionEvent e) throws IOException {
        mainApp.showCustomers(currentUser);
    }
    
    @FXML
    private void showAppointments(ActionEvent e) throws IOException {
        mainApp.showAppointments(currentUser);
    }
    
    @FXML
    private void showCalendar(ActionEvent e) throws IOException {
        mainApp.showCalendar(currentUser);
    }
    
    @FXML
    private void showReports(ActionEvent e) throws IOException {
        mainApp.showReports(currentUser);
    }
    
    @FXML
    private void logoutBtnClicked(ActionEvent event) throws Exception {
        mainApp.showLogin();
        
    }
    
    public void setupMenu(EasyScheduler mainApp, User activeUser)
    {
        this.mainApp = mainApp;
        this.currentUser = activeUser;
        mainUsernameLbl.setText(currentUser.getUsername() + "!");
       
    }

 
}
