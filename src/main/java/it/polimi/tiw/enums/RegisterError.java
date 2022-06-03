package it.polimi.tiw.enums;

/**
 * Enum of the errors of the register page.
 */
public enum RegisterError {
    INVALID_EMAIL,
    USERNAME_NOT_AVAILABLE,
    EMAIL_ALREADY_USED,
    PASSWORD_MISMATCH;

    /**
     * Returns the {@link RegisterError} corresponding to the given ordinal or null if the ordinal is invalid.
     * @param ordinal the ordinal of the {@link RegisterError}
     * @return the {@link RegisterError} corresponding to the given ordinal or null if the ordinal is invalid.
     */
    public static RegisterError fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length)
            return null;
        return values()[ordinal];
    }
}
