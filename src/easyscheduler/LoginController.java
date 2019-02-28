package easyscheduler;

import easyscheduler.util.DBConnection;
import easyscheduler.util.LoggerUtil;
import easyscheduler.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class LoginController {

    @FXML
    private TextField usernameTf;
    @FXML
    private PasswordField passwordPf;
    @FXML
    private Label errorMsgLbl;
    @FXML
    private Button loginBtn;
    @FXML
    private Label usernameLbl;
    @FXML
    private Label passwordLbl;
    
    private EasyScheduler mainApp;
    private User user = new User();
    
    // set up for Locale, DB connection, and logger
    ResourceBundle rb = ResourceBundle.getBundle("resources/login", Locale.getDefault());
    private final Connection connection = DBConnection.getConn();
    private final static Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());
    
    public LoginController () {
    }
    
    @FXML
    private void loginBtnClicked (ActionEvent e) {
        String username = usernameTf.getText();
        String password = passwordPf.getText();
        if (username.length()==0 || password.length()==0)
            errorMsgLbl.setText(rb.getString("empty"));
        else {
            User user = checkUserCreds(username,password);
            if (user == null) {
                errorMsgLbl.setText(rb.getString("incorrect"));
                return;
            }
            // log if user successfully logs in
            LOGGER.log(Level.INFO, "{0} logged in", user.getUsername());

            showReminderAlert();
            mainApp.showMainMenu(user);
        }
    }

    private User checkUserCreds(String username, String password) {
        try {
            PreparedStatement pst = DBConnection.getConn().prepareStatement
                ("SELECT * FROM user WHERE userName=? AND password =?");
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            if (rs.next()){
                String passwordHolder = rs.getString("password");
                if (passwordHolder.contentEquals(password))
                    System.out.println("password matches");
                else
                    return null;
                user.setUsername(rs.getString("userName"));
                user.setPassword(rs.getString("password"));
                user.setUserID(rs.getInt("userId"));
              }
            else {
                return null;
              }
            } catch (SQLException e) {
                e.printStackTrace();
            }
         return user;
    }

    public void setupLogin(EasyScheduler mainApp) {
        this.mainApp = mainApp;
        // set text based on locale
        usernameLbl.setText(rb.getString("username"));
        passwordLbl.setText(rb.getString("password"));
        loginBtn.setText(rb.getString("signin"));
    }

    // show reminder if user has appt within 15 minutes of logging in
    private void showReminderAlert(){
        try{
             String upcomingQuery = "SELECT appointment.*"
                         + " FROM appointment"
                         + " WHERE (appointment.contact = ? AND appointment.start BETWEEN ? AND ?)";
             PreparedStatement upcomingSmt = connection.prepareStatement(upcomingQuery);
             int i = 1;
             upcomingSmt.setString(i++, Integer.toString(user.getUserID()));
             upcomingSmt.setTimestamp(i++, Timestamp.valueOf(LocalDateTime.now()));
             upcomingSmt.setTimestamp(i++, Timestamp.valueOf(LocalDateTime.now()
                     .plusMinutes(15)));
             ResultSet upcomingRs = upcomingSmt.executeQuery();
             if (upcomingRs.next()) {
                 Alert upcomingAlert = new Alert(Alert.AlertType.INFORMATION);
                 upcomingAlert.setTitle("Upcoming Appointment");
                 upcomingAlert.setHeaderText("Reminder: Upcoming Appointment");
                 Long minutesUntil = ((upcomingRs.getTimestamp("start").getTime())-
                         Timestamp.valueOf(LocalDateTime.now()
                         ).getTime())/60000;
                 upcomingAlert.setContentText("You have an upcoming appointment in " 
                         + minutesUntil + " minutes. Appt #: " 
                         + upcomingRs.getInt("appointmentId") + ": " 
                         + upcomingRs.getString("title"));
                 upcomingAlert.showAndWait();
             }

         } catch (SQLException exc) {
                 exc.printStackTrace();
         }
    }

  }

