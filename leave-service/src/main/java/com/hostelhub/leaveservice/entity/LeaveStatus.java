package com.hostelhub.leaveservice.entity;

public enum LeaveStatus {

    /*
     * Student has submitted the leave request.
     */
    PENDING,

    /*
     * A warden has accepted/started handling the request.
     */
    IN_REVIEW,

    /*
     * Warden approved the leave.
     */
    APPROVED,

    /*
     * Warden rejected the leave.
     */
    REJECTED,

    /*
     * Student cancelled the leave while it is still
     * cancellable.
     */
    CANCELLED
}