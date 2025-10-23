package com.example.EVProject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "eaccount_no")
    private String eaccount_no;

    @Column(name = "password")
    private String password;

//    @Column(name = "role", length = 255)
//    private String role;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "username"),  // FK to user.username
            inverseJoinColumns = @JoinColumn(name = "role_id") // FK to role.id
    )
    private Set<Role> roles;

    @Column(name = "enabled")
    private Boolean enabled = Boolean.FALSE;

    @Column(name = "last_otp", length = 10)
    private String lastOtp;

    @Column(name = "otp_expiry")
    private OffsetDateTime otpExpiry;

    // getters and setters

//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
}
