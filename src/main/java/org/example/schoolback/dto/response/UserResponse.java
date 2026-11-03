package org.example.schoolback.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.schoolback.entity.Role;

import java.time.LocalDate;

/** Данные пользователя, возвращаемые клиенту. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("login")
    private String login;
    @JsonProperty("role")
    private Role role;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String secondName;
    @JsonProperty("middle_name")
    private String patronymicName;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("email")
    private String email;
    @JsonProperty("date_of_birth")
    private LocalDate birthDate;
    @JsonProperty("coins")
    private Long coins;
    @JsonProperty("exp")
    private Long experience;
    @JsonProperty("rank_level")
    private Integer rankLevel;
}
