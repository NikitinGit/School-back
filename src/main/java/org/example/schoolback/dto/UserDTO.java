package org.example.schoolback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.schoolback.entity.Role;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty("login")
    private String login;

    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonProperty("role")
    private Role role;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String secondName;

    @JsonProperty("middle_name")
    private String patronymicName;

    @JsonProperty(value = "full_name", access = JsonProperty.Access.READ_ONLY)
    private String fullName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("date_of_birth")
    private LocalDate birthDate;

    @JsonProperty(value = "coins")
    private Long coins;

    //только для чтения: опыт и уровень меняются при начислении награды, а не правкой профиля
    @JsonProperty(value = "exp", access = JsonProperty.Access.READ_ONLY)
    private Long experience;

    @JsonProperty(value = "rank_level", access = JsonProperty.Access.READ_ONLY)
    private Integer rankLevel;
}