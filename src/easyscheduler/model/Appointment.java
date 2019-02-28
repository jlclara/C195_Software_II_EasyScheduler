/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package easyscheduler.model;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Jenny Nguyen
 */

public class Appointment {
   
    private Integer appointmentId;
    private Integer customerId;
    private String customerName;
    private String title;
    private String description;
    private String type;
    private String url;  // optional
    private String location; 
    private String contact;
    private LocalDateTime start;
    private LocalDateTime end;
    private Integer userId;
    private String userName;

    public Appointment() {
    }
    
    public Appointment(String monthRoot)
    {   
        this.url = monthRoot;
    }

    public Appointment(Integer appointmentId, Integer customerId, 
            String customerName, String title, String description, String type, 
            LocalDateTime start, LocalDateTime end, Integer userId, 
            String userName) {
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.title = title;
        this.description = description;
        this.type = type;
        this.start = start;
        this.end = end;
        this.userId = userId;
        this.userName = userName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStart() {
        return start;
    }
    
    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public String getStartFormatted(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm");
        String DateString = formatter.format(start);
        return DateString;
    }
    
    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getCustomer() {
        return customerId;
    }

    public void setCustomer(Integer customer) {
        this.customerId = customer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    
}
