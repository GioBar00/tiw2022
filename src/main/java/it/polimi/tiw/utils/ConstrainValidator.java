package it.polimi.tiw.utils;

/**
 * This class is used to validate the constraints of the database.
 */
public abstract class ConstrainValidator {
    /**
     * This method checks if the username is valid.
     * @param username the username to check.
     * @return true if the username is valid, false otherwise.
     */
    public static boolean isValidUsername(String username) {
        return username.matches("[a-zA-Z\\d]{3,20}");
    }

    /**
     * This method checks if the email is valid.
     * @param email the email to check.
     * @return true if the email is valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        return email.matches("^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}])|(([a-zA-Z\\-\\d]+\\.)+[a-zA-Z]{2,}))$")
                && email.length() <= 50;
    }

    /**
     * This method checks if the password is valid.
     * @param password the password to check.
     * @return true if the password is valid, false otherwise.
     */
    public static boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
                && password.length() <= 50;
    }

    /**
     * This method checks if the name is valid.
     * @param name the name to check.
     * @return true if the name is valid, false otherwise.
     */
    public static boolean isValidName(String name) {
        return name.matches("[a-zA-Z\\s]{3,20}");
    }

    /**
     * This method checks if the surname is valid.
     * @param surname the surname to check.
     * @return true if the surname is valid, false otherwise.
     */
    public static boolean isValidSurname(String surname) {
        return surname.matches("[a-zA-Z\\s]{3,20}");
    }
}
