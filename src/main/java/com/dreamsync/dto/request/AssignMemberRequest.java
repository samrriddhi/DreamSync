package com.dreamsync.dto.request;

import com.dreamsync.enums.ProjectRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignMemberRequest {

    private Long userId;

    private ProjectRole role;
}