package com.hostelhub.complaintservice.dto.request;

import com.hostelhub.complaintservice.entity.ComplaintCategory;
import com.hostelhub.complaintservice.entity.ComplaintPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateComplaintRequest(

        @NotNull(message = "Category is required")
        ComplaintCategory category,

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title cannot exceed 150 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        ComplaintPriority priority

) {
}