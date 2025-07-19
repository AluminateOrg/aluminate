package com.aluminate.aluminate_organization_backend.service;

import com.aluminate.aluminate_organization_backend.dto.MemberRowDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class AnnotationValidationService {
    @Autowired
    private Validator validator;

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
            }
        }
        return rows;
    }
}
