package com.example.auth.validator;

import com.example.auth.dto.TransactionDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;

@Component
public class TransactionValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return TransactionDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TransactionDto transactionDto = (TransactionDto) target;

        if (Objects.isNull(transactionDto.getBorrowedBy()) && Objects.isNull(transactionDto.getPaidBy())) {
            errors.rejectValue("borrowedBy", "transaction.invalid", "Either borrowedBy or paidBy must be provided");
        }
    }
}
