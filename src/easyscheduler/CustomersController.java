package easyscheduler;

import static easyscheduler.AppointmentsController.setItemSelected;
import easyscheduler.model.Appointment;
import easyscheduler.util.DBConnection;
import easyscheduler.model.User;
import easyscheduler.model.Customer;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class CustomersController {

    @FXML
    private TableView<Customer> customerTableView;
    @FXML
    private TableColumn<Customer, Integer> customerIdCol;
    @FXML
    private TableColumn<Customer, String> nameCol;
    @FXML
    private TableColumn<Customer, String> addressCol;
    @FXML
    private TableColumn<Customer, String> address2Col;
    @FXML
    private TableColumn<Customer, String> cityCol;
    @FXML
    private TableColumn<Customer, String> countryCol;
    @FXML
    private TableColumn<Customer, String> postalCodeCol;
    @FXML
    private TableColumn<Customer, String> phoneCol;
    
    private EasyScheduler mainApp;
    private User currentUser;
    private static Customer modCustomerSelected;
    
    public CustomersController() {
    }
    
    @FXML
    private void handleNewCustomer(ActionEvent e) throws IOException {
        resetModCustomer();
        mainApp.showCustomerFields(currentUser);
    }
    
    @FXML 
    private void handleEditCustomer(ActionEvent e) throws IOException {
        // if there is an item selected in the TableView, then open CustomerFields.fxml
        modCustomerSelected = customerTableView.getSelectionModel().getSelectedItem();
        
        if (modCustomerSelected == null) {
            Alert noCustomerAlert = new Alert (Alert.AlertType.WARNING);
            noCustomerAlert.setTitle("No Customers Selected");
            noCustomerAlert.setHeaderText("Error: No Customers Selected");
            noCustomerAlert.setContentText("Please select a customer and try again.");
            noCustomerAlert.showAndWait();
        }
        else 
          mainApp.showCustomerFields(currentUser);
    }
    
    @FXML
    private void handleCustomerDeleteBtn(ActionEvent e) {
        
        // if there is an item selected in the TableView, then delete customer
        modCustomerSelected = customerTableView.getSelectionModel().getSelectedItem();

        if (modCustomerSelected == null) {
            Alert noCustomerAlert = new Alert (Alert.AlertType.WARNING);
            noCustomerAlert.setTitle("No Customers Selected");
            noCustomerAlert.setHeaderText("Error: No Customers Selected");
            noCustomerAlert.setContentText("Please select a customer and try again.");
            noCustomerAlert.showAndWait();
        }
        else {
            Alert deleteCustomerAlert = new Alert (Alert.AlertType.CONFIRMATION);
            deleteCustomerAlert.setTitle("Confirm Customer Deletion");
            deleteCustomerAlert.setHeaderText("Are you sure you want to delete " + modCustomerSelected.getName() + "?");
            deleteCustomerAlert.setContentText("This cannot be undone! All of " + modCustomerSelected.getName() + " will also be deleted.");
            Optional<ButtonType> result = deleteCustomerAlert.showAndWait();
            if (result.get() == ButtonType.OK) {
                try {
                String deleteQuery = "DELETE customer.*, address.*"
                        + " FROM customer, address"
                        + " WHERE customer.customerId = ?"
                        + " AND customer.addressId = address.addressId";
                PreparedStatement deleteSmt = DBConnection.getConn().prepareStatement(deleteQuery);
                deleteSmt.setInt(1, modCustomerSelected.getId());
                deleteSmt.executeUpdate();
               } catch (SQLException exc) {
                   exc.printStackTrace();;
               }
               // reload the customers list
               setupCustomers(mainApp, currentUser);
            }
        }
    }
    
    @FXML 
    private void handleBackButton (ActionEvent e) {
        mainApp.showMainMenu(currentUser);
    }

    // populates table with customer data
    public void setupCustomers(EasyScheduler mainApp, User activeUser) {
        this.mainApp = mainApp;
        this.currentUser = activeUser;
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        address2Col.setCellValueFactory(new PropertyValueFactory<>("address2"));
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        postalCodeCol.setCellValueFactory(new PropertyValueFactory<>("zip"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        
        //adding on double click event. Opens the customer field editor.
        customerTableView.setRowFactory(ttv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    setModCustomer(customerTableView.getSelectionModel().getSelectedItem());
                    mainApp.showCustomerFields(currentUser);
                }
            });
            return row;
        });
        
        customerTableView.getItems().setAll(getCustomerData());
    }
    
    public List<Customer> getCustomerData(){
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        try {
            String customerQuery = "SELECT customer.customerId, customer.customerName, "
                + "address.address, address.address2, address.postalCode, city.cityId, "
                + "city.city, country.country, address.phone " 
                + "FROM customer, address, city, country " 
                + "WHERE customer.addressId = address.addressId "
                + "AND address.cityId = city.cityId AND city.countryId = country.countryId "
                + "ORDER BY customer.customerId";
        
            PreparedStatement smt = DBConnection.getConn().prepareStatement(customerQuery);
            ResultSet customersFound = smt.executeQuery();

            while (customersFound.next()) {
                Integer dCustomerId = customersFound.getInt("customer.customerId");
                String dCustomerName = customersFound.getString("customer.customerName");
                String dAddress = customersFound.getString("address.address");
                String dAddress2 = customersFound.getString("address.address2");
                String dCity = customersFound.getString("city.city");
                String dPostalCode = customersFound.getString("address.postalCode");
                String dCountry = customersFound.getString("country.country");
                String dPhone = customersFound.getString("address.phone");
                customerList.add(new Customer (dCustomerId, dCustomerName, 
                        dAddress, dAddress2, dCity, dCountry, dPostalCode, dPhone));
            }
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customerList;
    }
    
    public static Customer getModCustomer() {
        return modCustomerSelected;
    }
    public static void resetModCustomer() {
        modCustomerSelected = null;
    }
    public static void setModCustomer(Customer c) {
        modCustomerSelected = c;
    }
}
   
    

