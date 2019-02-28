package easyscheduler;

import easyscheduler.util.DBConnection;
import easyscheduler.model.Address;
import easyscheduler.model.User;
import easyscheduler.model.Customer;
import easyscheduler.model.Country;
import easyscheduler.model.City;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author Jenny Nguyen
 */
public class CustomerFieldsController {

    @FXML
    private TextField customerIdTf;
    @FXML
    private TextField customerNameTf;
    @FXML
    private TextField customerStreetTf;
    @FXML
    private TextField customerStreet2Tf;
    @FXML
    private TextField customerCityTf;
    @FXML
    private TextField customerPostalTf;
    @FXML
    private TextField customerCountryTf;
    @FXML
    private TextField customerPhoneTf;
    @FXML
    private Button customerSaveBtn;
    @FXML
    private Text errorMsgTxt;
    private EasyScheduler mainApp;
    private User currentUser;
    private final  Connection connection = DBConnection.getConn();
    
    public CustomerFieldsController(){
    }
    
    private String checkUserInput()
    {
        String name = customerNameTf.getText();
        String street = customerStreetTf.getText();
        String city = customerCityTf.getText();
        String postal = customerPostalTf.getText();
        String country = customerCountryTf.getText();
        String phone = customerPhoneTf.getText();
        String errorMsg = "";
        
        if (name.length() == 0)
            errorMsg += "Please enter the customer's name. \n";
        if (street.length() == 0)
            errorMsg += "Please enter the customer's street address. \n";
        if (city.length() == 0)
            errorMsg += "Please enter the customer's city. \n";
        if (postal.length() == 0)
            errorMsg += "Please enter the customer's postal code. \n";
        else if (postal.length() < 5)
            errorMsg += "Please enter a valid postal code. \n";
        if (country.length() == 0)
            errorMsg += "Please enter the customer's country. \n";
        if (phone.length() == 0)
            errorMsg += "Please enter the customer's phone number. \n";
        else if (phone.length() < 8 || phone.length() > 15)  // shortest phone number country is Solomon Islands with 8 digits including country code
            errorMsg += "Please enter a valid phone number including "
                    + "\n country code and area code. \n";

        return errorMsg;
    }
    @FXML
    private void handleCustomerSaveBtn(ActionEvent e) {
        
        // store user input in variables
        String name = customerNameTf.getText();
        String street = customerStreetTf.getText();
        String street2 = customerStreet2Tf.getText();
        String city = customerCityTf.getText();
        String postal = customerPostalTf.getText();
        String country = customerCountryTf.getText();
        String phone = customerPhoneTf.getText();
        
        String addressQuery = null;
        Country currentCountry = null;
        Address currentAddress = null;
        
        // test if fields are filled out
        String customerInputErrMsg = checkUserInput();
        if (!customerInputErrMsg.isEmpty())
            errorMsgTxt.setText(customerInputErrMsg);
        
        // if user input is valid, start by adding the country if it does not exist
        else {
            if (CustomersController.getModCustomer()== null){ // ADDING CUSTOMER, NOT EDITING
            try {            
                currentCountry = Country.validateCountry(country);
                if (currentCountry == null)  // country does not exist yet, insert
                {
                    String countryQuery = "INSERT INTO country ("
                    + " country, createDate, createdBy,"
                    + " lastUpdate, lastUpdateBy) VALUES ("
                    + " ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)"; 

                    try (PreparedStatement countrySmt = connection.
                            prepareStatement(countryQuery)) {
                         countrySmt.setString(1, country);
                         countrySmt.setString(2, currentUser.getUsername());
                         countrySmt.setString(3, currentUser.getUsername());
                         countrySmt.executeUpdate();
                         currentCountry = Country.validateCountry(country);
                     }
                }          
               // first need to test if city exists 
               City currentCity = City.validateCity(city, currentCountry.getCountryId());
               if (currentCity == null) // city does not exist yet
               {
                    String cityQuery = "INSERT INTO city ("
                    + " city, countryId, createDate, createdBy,"
                    + " lastUpdate, lastUpdateBy) VALUES ("
                    + " ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)"; 
                    PreparedStatement citySmt = connection.
                            prepareStatement(cityQuery);
                    citySmt.setString(1, city);
                    citySmt.setInt(2, currentCountry.getCountryId());
                    citySmt.setString(3, currentUser.getUsername());
                    citySmt.setString(4, currentUser.getUsername());
                    citySmt.executeUpdate();
                    currentCity = City.validateCity(city, currentCountry.getCountryId());
                    citySmt.close();
               }
               
               currentAddress = Address.validateAddress(street, street2, 
                       currentCity.getCityId(), postal, phone);
               if (currentAddress == null)  // address does not exist yet
               {
                    addressQuery = "INSERT INTO address ("
                    + " address, address2, cityId,"
                    + " postalCode, phone, createDate, createdBy,"
                    + " lastUpdate, lastUpdateBy) VALUES ("
                    + " ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";
               }
              
                PreparedStatement addressSmt = connection.prepareStatement(addressQuery);
                addressSmt.setString(1, street);
                addressSmt.setString(2, street2);
                addressSmt.setInt(3, currentCity.getCityId());
                addressSmt.setString(4, postal);
                addressSmt.setString(5, phone);
                addressSmt.setString(6, currentUser.getUsername());
                addressSmt.setString(7, currentUser.getUsername());
                
                addressSmt.executeUpdate();
                currentAddress = Address.validateAddress(street, street2, currentCity.getCityId(), postal, phone);
                addressSmt.close();
                
                String query = "INSERT INTO customer ("              
                + " customerName,"
                + " addressId,"
                + " active,"
                + " createDate,"
                + " createdBy,"
                + " lastUpdate,"
                + " lastUpdateBy) VALUES ("
                + "?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?)";            
                PreparedStatement st = connection.prepareStatement(query);
                st.setString(1, name);
                st.setInt(2, currentAddress.getAddressId());  
                st.setInt(3, 1); 
                st.setString(4, currentUser.getUsername());
                st.setString(5, currentUser.getUsername());
                st.executeUpdate();
                st.close();
                
            } catch (SQLException ex) {
                ex.printStackTrace();
                }
        } // end of adding customer  
               
        // UPDATE Customer if a customer is selected from Customers screen
        else if (CustomersController.getModCustomer()!= null) 
        {
            try {
                String updateQuery = "UPDATE customer, address, city, country"
                + " SET customerName = ?,"
                + " customer.lastUpdate = CURRENT_TIMESTAMP,"
                + " customer.lastUpdateBy = ?,"
                + " address.address = ?,"
                + " address.address2 = ?,"
                + " address.postalCode = ?,"
                + " address.phone = ?,"        
                + " address.lastUpdate = CURRENT_TIMESTAMP,"
                + " address.lastUpdateBy = ?,"
                + " city.city = ?,"
                + " city.lastUpdate = CURRENT_TIMESTAMP,"
                + " city.lastUpdateby = ?,"
                + " country.country = ?,"
                + " country.lastUpdate = CURRENT_TIMESTAMP,"
                + " country.lastUpdateBy = ?"
                + " WHERE customer.customerId = ?"
                + " AND customer.addressId = address.addressId"
                + " AND address.cityId = city.cityId"
                + " AND city.countryId = country.countryId";
                int i = 1;
                PreparedStatement st = connection.prepareStatement(updateQuery);
                st.setString(i++, name);
                st.setString(i++, currentUser.getUsername());
                st.setString(i++, street);
                st.setString(i++, street2);
                st.setString(i++, postal);
                st.setString(i++, phone);
                st.setString(i++, currentUser.getUsername());
                st.setString(i++, city);
                st.setString(i++, currentUser.getUsername());
                st.setString(i++, country);
                st.setString(i++, currentUser.getUsername());
                st.setInt(i++, CustomersController.getModCustomer().getId());
                st.executeUpdate();
                st.close();
             } catch (SQLException ex) {
                ex.printStackTrace();
                }
            }
            CustomersController.resetModCustomer();
            mainApp.closePopup(currentUser);  
        }
    }
    
    @FXML
    private void handleCustomerCancelBtn(ActionEvent e) {
        Alert cancelAlert = new Alert (AlertType.CONFIRMATION);
        cancelAlert.setTitle("Cancel Adding/Editing Customers?");
        cancelAlert.setHeaderText("Are you sure you want to cancel "
                + "and return to the customers screen?");
        Optional<ButtonType> result = cancelAlert.showAndWait();
        if (result.get() == ButtonType.OK) {
            CustomersController.resetModCustomer();
            mainApp.closePopup(currentUser);

        }
    }
    
    public void setUpCustomerFields(EasyScheduler mainApp, User activeUser){
        this.mainApp = mainApp;
        this.currentUser = activeUser;
        
        // load customer data if edit is selected from the customers scene
        Customer modCustomer = CustomersController.getModCustomer();
        if (modCustomer != null)
        {
            customerIdTf.setText(Integer.toString(modCustomer.getId()));
            customerNameTf.setText(modCustomer.getName());
            customerStreetTf.setText(modCustomer.getAddress());
            customerStreet2Tf.setText(modCustomer.getAddress2());
            customerCityTf.setText(modCustomer.getCity());
            customerPostalTf.setText(modCustomer.getZip());
            customerCountryTf.setText(modCustomer.getCountry());
            customerPhoneTf.setText(modCustomer.getPhone());
        }
    }
}
