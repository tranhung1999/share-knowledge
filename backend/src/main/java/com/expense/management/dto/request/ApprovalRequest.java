package com.expense.management.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalRequest {

    @Size(max = 1000, message = "Comment must be at most 1000 characters")
    private String comment;
}
