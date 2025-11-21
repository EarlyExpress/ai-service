package com.early_express.ai_service.global.presentation.exception;

public class DeliveryNotFoundException extends GlobalException{

    public DeliveryNotFoundException(String message) {
        super(GlobalErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
