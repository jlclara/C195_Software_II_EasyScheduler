/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyscheduler.model;

import easyscheduler.util.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Jenny Nguyen
 */
public class Address {
    
    private Integer addressId;
    private String address;
    private String address2;
    private Integer cityId;
    private String zip;
    private String phone;

    public Address() {
    }

    public Address(String address, String address2, Integer cityId, String zip, String phone) {
        this.address = address;
        this.address2 = address2;
        this.cityId = cityId;
        this.zip = zip;
        this.phone = phone;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
    
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    // checks if an address already exists
    public static Address validateAddress(String aAddress, String aAddress2, Integer aCity, 
             String aPostal, String aPhone) {
        Address vAddress = new Address(); 
        try{           
            PreparedStatement pst = DBConnection.getConn()
                    .prepareStatement("SELECT * FROM address "
                            + "WHERE address.address=? AND address.cityId=? "
                            + "AND address.postalCode =? AND address.phone = ? "
                            + "AND address.address2=? ");
            pst.setString(1, aAddress); 
            pst.setInt(2, aCity); 
            pst.setString(3, aPostal); 
            pst.setString(4, aPhone); 
            pst.setString(5, aAddress2);
            ResultSet rs = pst.executeQuery();                        
            if(rs.next()){
                vAddress.setAddressId(rs.getInt("addressId"));
                vAddress.setAddress(rs.getString("address"));
                vAddress.setAddress2(rs.getString("address2"));
                vAddress.setCityId(rs.getInt("cityId"));
                vAddress.setZip(rs.getString("postalCode"));
                vAddress.setPhone(rs.getString("phone"));
                
            } else {
                return null;    
            }            
        } catch(SQLException e){
            e.printStackTrace();
        }       
        return vAddress;
    }
    
}
