package com.definefunction.transfer.model.pojo;

public enum Status {
    ACTIVE, INACTIVE, ERROR, FAILED

    // Active means running normally
    // Inactive means that the user disabled the route and it is not running.
    // ERROR means that something went wrong while transferring and the route is set to error.
    // FAILED means that the transfer could not be added to the CamelContext and is thus defined incorrectly.
}
