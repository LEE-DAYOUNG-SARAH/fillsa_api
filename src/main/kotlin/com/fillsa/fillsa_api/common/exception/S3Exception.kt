package com.fillsa.fillsa_api.common.exception

class S3Exception(message: String, throwable: Throwable): RuntimeException(message, throwable) {}