package me.vitalframework.commands;

/**
 * Enum representing possible return states for {@link VitalCommand}.
 * Defines different states that a command execution can result in.
 *
 * @author xRa1ny
 */
public enum VitalCommandReturnState {
    /**
     * Indicates that the command was executed with invalid arguments.
     */
    INVALID_ARGS,

    /**
     * Indicates that the command execution was successful.
     */
    SUCCESS,

    /**
     * Indicates that the sender did not have permission to execute the command.
     */
    NO_PERMISSION
}
