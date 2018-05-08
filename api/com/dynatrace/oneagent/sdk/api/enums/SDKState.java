package com.dynatrace.oneagent.sdk.api.enums;

/**
 * Defines the state, in which the SDK can be.
 */
public enum SDKState {

    /** SDK is connected to agent and capturing data. */
    ACTIVE,

    /**
     * SDK is connected to agent, but capturing is disabled. In this state, SDK
     * user can skip creating SDK transactions and save CPU time. SDK state
     * should be checked regularly as it may change at every point of time.
     */
    TEMPORARILY_INACTIVE,

    /**
     * SDK isn't connected to agent. So it will never capture data. This SDK
     * state will never change in current JVM life time. It is good practice to
     * never call any SDK api and safe CPU time therefore.
     */
    PERMANENTLY_INACTIVE;

}
