package org.example.schoolback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.example.schoolback.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDTO {

    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty(value = "teacher_id", access = JsonProperty.Access.WRITE_ONLY)
    private Long teacherId;

    @JsonProperty(value = "teacher", access = JsonProperty.Access.READ_ONLY)
    private UserResponse teacher;

    @JsonProperty(value = "students", access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<UserResponse> students;
}
