package org.example.schoolback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.schoolback.dto.response.UserResponse;
import org.example.schoolback.entity.OrderStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(value = "customer", access = JsonProperty.Access.READ_ONLY)
    private UserResponse customer;

    @JsonProperty(value = "present_id", access = JsonProperty.Access.READ_ONLY)
    private Long presentId;

    @JsonProperty(value = "status")
    private OrderStatus status;

    @JsonProperty(value = "date")
    private LocalDateTime date;

}
