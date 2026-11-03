package org.example.schoolback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.schoolback.entity.Role;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^[a-zA-Z0-9]+$",
            message = "Логин может состоять только из латинских букв и цифр")
    @JsonProperty("login")
    private String login;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 4, message = "Пароль должен быть не короче четырёх знаков")
    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(message = "Роль обязательна")
    @JsonProperty("role")
    private Role role;

    @NotBlank(message = "Имя не может быть пустым")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    @JsonProperty("last_name")
    private String secondName;

    @JsonProperty("middle_name")
    private String patronymicName;

    //почта необязательна: пустое значение проверку проходит,
    //а непустое должно выглядеть как почтовый адрес
    @Email(message = "Неверный вид почтового адреса")
    @JsonProperty("email")
    private String email;

    @JsonProperty("date_of_birth")
    private LocalDate birthDate;

}
