package com.cloud_kitchen.application.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends User {

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String designation;

    private Boolean superAdmin = false;
}
