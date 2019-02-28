 package easyscheduler;

import easyscheduler.util.DBConnection;
import easyscheduler.model.User;
import easyscheduler.util.LoggerUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 *
 * @author Jenny Nguyen
 */
public class EasyScheduler extends Application {
    
    private static Connection connection;
    private Stage primaryStage;
    private Stage popStage = new Stage();
    private Scene lgscene, mscene, cuscene, ascene, cascene, rscene;

    
    @Override
    public void start(Stage primaryStage) throws Exception {        
        this.primaryStage = primaryStage;
        primaryStage.setTitle("easyScheduler");
        
//        try {
//        String query = "INSERT INTO user ("
//            + " userId,"
//            + " userName,"
//            + " password,"
//            + " active,"
//            + " createBy,"
//            + " createDate,"
//            + " lastUpdate,"
//            + " lastUpdatedBy) VALUES ("
//            + "?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";            
//            PreparedStatement st = connection.prepareStatement(query);
//            st.setInt(1, 6);
//            st.setString(2, "Test");
//            st.setString(3, "test");
//            st.setInt(4, 1); 
//            st.setString(5, "admin");
//            st.setString(6, "admin");
//            st.executeUpdate();
//            st.close();
//            } catch (SQLException ex) {
//                ex.printStackTrace();
//                }
        
        showLogin();
    }
    
    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Login.fxml"));
            Pane loginPane = (Pane) loader.load();
            LoginController lController = loader.getController();
            lController.setupLogin(this);
            lgscene = new Scene(loginPane);
            primaryStage.setScene(lgscene);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void showMainMenu (User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("MainMenu.fxml"));
            Pane menuLayout = (Pane) loader.load();
            mscene = new Scene(menuLayout);
            primaryStage.setScene(mscene);
            MainMenuController controller = loader.getController();
            controller.setupMenu(this, activeUser);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public void showCustomers (User activeUser) {
         try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Customers.fxml"));
            Pane customersLayout = (Pane) loader.load();
            cuscene = new Scene(customersLayout);
            primaryStage.setScene(cuscene);
            CustomersController controller = loader.getController();
            controller.setupCustomers(this, activeUser);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public void showAppointments (User activeUser) {
         try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Appointments.fxml"));
            Pane apptLayout = (Pane) loader.load();
            ascene = new Scene(apptLayout);
            primaryStage.setScene(ascene);
            AppointmentsController controller = loader.getController();
            controller.setupAppointments(this, activeUser);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public void showCalendar (User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Calendar.fxml"));
            Pane calendarLayout = (Pane) loader.load();
            cascene = new Scene(calendarLayout);
            primaryStage.setScene(cascene);
            CalendarController controller = loader.getController();
            controller.setUpCalendar(this, activeUser);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public void showReports(User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Reports.fxml"));
            Pane reportsLayout = (Pane) loader.load();
            rscene = new Scene(reportsLayout);
            primaryStage.setScene(rscene);
            ReportsController controller = loader.getController();
            controller.setUpReports(this, activeUser);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void showCustomerFields(User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("CustomerFields.fxml"));
            Pane customerFLayout = (Pane) loader.load();
            Scene scene = new Scene(customerFLayout);
            popStage.setScene(scene);     
            CustomerFieldsController controller = loader.getController();
            controller.setUpCustomerFields(this, activeUser);
            popStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }    
    }
    
    public void showAppointmentFields(User activeUser) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("AppointmentFields.fxml"));
            Pane appointmentFLayout = (Pane) loader.load();
            Scene scene = new Scene(appointmentFLayout);
            popStage.setScene(scene);
            AppointmentFieldsController controller = loader.getController();
            controller.setUpAppointmentFields(this, activeUser);
            popStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }    
    }
    public void closePopup(User currentUser) {
        popStage.close();
        
        // following code is used to refresh the active scene contents
        Scene activeScene = primaryStage.getScene();
        if (activeScene == ascene)
            showAppointments(currentUser);
        else if (activeScene == rscene)
            showReports(currentUser);
        else if (activeScene == cascene)
            showCalendar(currentUser);
        else if (activeScene == cuscene)
            showCustomers(currentUser);
            
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       
        DBConnection.init();
        connection = DBConnection.getConn();
        LoggerUtil.init();

        launch(args);
        DBConnection.closeConn();
    }
    
}
