package easyscheduler.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author Jenny Nguyen
 */
public class DBConnection {
    
    private static Connection connDB;
    
    public DBConnection(){}
    public static void init(){
        System.out.println("Connecting to the database");
        try{
            Class.forName("com.mysql.jdbc.Driver");
            connDB = DriverManager.getConnection("jdbc:mysql://52.206.157.109/U05CUo?" + 
                    "user=U05CUo&password=53688462503");
        }catch (ClassNotFoundException ce){
            System.out.println("Cannot find the right class.  Did you remember to add the mysql library to your Run Configuration?");
            ce.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
}
    }
    
    public static Connection getConn(){
    
        return connDB;
    }
    
    public static void closeConn(){
        try{
            connDB.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally{
            System.out.println("Database connection closed.");
        }
    }
    
}
