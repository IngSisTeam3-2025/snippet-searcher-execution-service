package com.example.snippetsearcher.common.exception

import org.springframework.http.HttpStatus

class InternalServerErrorException(message: String = "An unexpected error occurred") :
    ApiException(HttpStatus.INTERNAL_SERVER_ERROR, message)

class NotFoundException(message: String) : ApiException(HttpStatus.NOT_FOUND, message)

class ServiceRequestException(status: HttpStatus, message: String) : ApiException(status, message)
