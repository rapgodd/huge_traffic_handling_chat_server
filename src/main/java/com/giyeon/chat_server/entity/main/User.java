package com.giyeon.chat_server.entity.main;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Table(name = "users")
@Entity
@Getter
public class User {

    @Id
    private Long id;

    private String email;
    private String password;


}
