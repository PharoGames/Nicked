package com.nicked.nick;

/** The cause of a nick change or removal. */
public enum NickCause {
    /** Player executed /nick or /unnick themselves. */
    COMMAND,
    /** Another plugin triggered the change via the Nicked API. */
    PLUGIN,
    /** The nick was randomly selected (/nick with no args, or /nickall). */
    RANDOM,
    /** Nick was restored from persistent storage on server start or player join. */
    RESTART_RESTORE,
    /** A non-player sender (console, command block, etc.) triggered the change via a command. */
    CONSOLE
}
