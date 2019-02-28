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
public class City {

    private String city;
    private Integer cityId;
    private Integer countryId;

    public City() {
    }

    public City(String city, Integer cityId, Integer countryId) {
        this.city = city;
        this.cityId = cityId;
        this.countryId = countryId;
    }
    
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }
    
    public static City validateCity(String aCity, Integer aCountry) {
        City vCity = new City(); 
        try{           
            PreparedStatement pst = DBConnection.getConn().prepareStatement("SELECT * "
                    + "FROM city WHERE city=? AND countryId=?");
            pst.setString(1, aCity); 
            pst.setInt(2, aCountry); 
            ResultSet rs = pst.executeQuery();                        
            if(rs.next()){
                vCity.setCityId(rs.getInt("cityId"));
                vCity.setCity(rs.getString("city"));
                vCity.setCountryId(rs.getInt("countryId"));
             
            } else {
                return null;    
            }            
                
        } catch(SQLException e){
            e.printStackTrace();
        }       
        return vCity;
    } 
    

}
