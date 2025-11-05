package com.aluminate.aluminate_organization_backend.service;

import com.aluminate.aluminate_organization_backend.dto.MemberRowDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class AnnotationValidationService {

    private final Validator validator;

    public AnnotationValidationService(Validator validator) {
        this.validator = validator;
    }

    public List<MemberRowDTO> validateRows(List<MemberRowDTO> rows) {
        for(MemberRowDTO row : rows) {
            Set<ConstraintViolation<MemberRowDTO>> violations = validator.validate(row);
            if (!violations.isEmpty()) {
                row.setStatus("invalid");
                Map<String, String> errors = new HashMap<>();
                for (ConstraintViolation<MemberRowDTO> v : violations) {
                    errors.put(v.getPropertyPath().toString(), v.getMessage());
                }
                row.setErrors(errors);
            } else {
                row.setStatus("valid");
                row.setErrors(Collections.emptyMap());
            }
        }
        return rows;
    }
}
