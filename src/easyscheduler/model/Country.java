package easyscheduler.model;

import easyscheduler.util.DBConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Jenny Nguyen
 */
public class Country {
    
    private String country;
    private Integer countryId;

    public Country() {
    }

    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }
    
    public static Country validateCountry(String aCountry) {
        Country vCountry = new Country(); 
        try{           
            PreparedStatement pst = DBConnection.getConn().prepareStatement("SELECT * FROM country WHERE country=?");
            pst.setString(1, aCountry); 
            ResultSet rs = pst.executeQuery();                        
            if(rs.next()){
                vCountry.setCountry(rs.getString("country"));
                vCountry.setCountryId(rs.getInt("countryId"));
            } else {
                return null;    
            }            
                
        } catch(SQLException e){
            e.printStackTrace();
        }       
        return vCountry;
    }
}
