package com.cloud_kitchen.application.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "students")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Student extends User {

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String college;

    private String hostelName;

    private String roomNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getHostelName() { return hostelName; }
    public void setHostelName(String hostelName) { this.hostelName = hostelName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
