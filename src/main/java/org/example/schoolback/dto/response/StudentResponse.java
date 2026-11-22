package org.example.schoolback.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class StudentResponse {
    private Long id;
    private String fullName;
    private String login;
    @JsonProperty("date_of_birth")
    private LocalDate birthDate;
    private String coins;
}