package com.example.snippetsearcher.execution.common.exception

import org.springframework.http.HttpStatus

class NotFoundException(message: String) : ApiException(HttpStatus.NOT_FOUND, message)

class BadRequestException(message: String) : ApiException(HttpStatus.BAD_REQUEST, message)

class UnauthorizedException(message: String = "Unauthorized") : ApiException(HttpStatus.UNAUTHORIZED, message)

class ForbiddenException(message: String = "Forbidden") : ApiException(HttpStatus.FORBIDDEN, message)
