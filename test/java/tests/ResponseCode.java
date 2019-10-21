package tests;

public enum ResponseCode {
    /**
     * The message accepts no response
     */
    NOTAPPLICABLE,
    /**
     * The node provided no response
     */
    SUCCESS,
    /**
     * The data provided to the message
     * is invalid. Message cannot be sent.
     */
    ERR__INVALID_ARG,
    /**
     * The node returned 0 or null.
     */
    ERR__RETURNED_ZERO_BYTES,

    /**
     * The node replies with this code
     * if the request is not valid.
     */
    ERR_REQUESTED_INVALID,

    ERR__NO_RESPONSE,

    ERR__NOT_A_RESPONSE,
}
