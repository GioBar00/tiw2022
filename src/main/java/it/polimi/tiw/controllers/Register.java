package it.polimi.tiw.controllers;

import it.polimi.tiw.enums.RegisterError;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.ConstrainValidator;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.enums.TemplatePages;
import it.polimi.tiw.utils.TemplateHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class is the controller for the register page.
 */
@WebServlet(name = "Register", value = "/register")
public class Register extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * {@link Connection} to the database.
     */
    private Connection connection;
    /**
     * {@link TemplateEngine} to render the template.
     */
    private TemplateEngine templateEngine;

    /**
     * Initialize the {@link Connection} to the database and the {@link TemplateEngine}.
     *
     * @throws ServletException if the {@link Connection} to the database cannot be initialized.
     */
    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        connection = ConnectionHandler.getConnection(context);
        templateEngine = TemplateHandler.getTemplateEngine(context);
    }

    /**
     * Loads the register page.
     *
     * @param req  an {@link HttpServletRequest} object that
     *             contains the request the client has made
     *             of the servlet
     * @param resp an {@link HttpServletResponse} object that
     *             contains the response the servlet sends
     *             to the client
     * @throws IOException if an input or output error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final WebContext webContext = new WebContext(req, resp, getServletContext(), req.getLocale());
        if (req.getParameter("error") != null) {
            String errorCode = req.getParameter("error");
            try {
                RegisterError error = RegisterError.fromOrdinal(Integer.parseInt(errorCode));
                if (error == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid error code");
                    return;
                }
                webContext.setVariable("error", error);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid error code");
                return;
            }
        }

        templateEngine.process(String.valueOf(TemplatePages.REGISTER), webContext, resp.getWriter());
    }

    /**
     * Checks parameters of the user and redirects to the login page if the parameters are valid.
     *
     * @param req  an {@link HttpServletRequest} object that
     *             contains the request the client has made
     *             of the servlet
     * @param resp an {@link HttpServletResponse} object that
     *             contains the response the servlet sends
     *             to the client
     * @throws IOException if an input or output error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String name = req.getParameter("name");
        String surname = req.getParameter("surname");

        if (username == null || username.isEmpty() ||
                email == null || email.isEmpty() ||
                password == null || password.isEmpty() ||
                confirmPassword == null || confirmPassword.isEmpty() ||
                name == null || name.isEmpty() ||
                surname == null || surname.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        UserDAO userDAO = new UserDAO(connection);
        try {

            RegisterError error;

            if (!ConstrainValidator.isValidUsername(username))
                error = RegisterError.INVALID_USERNAME;
            else if (!ConstrainValidator.isValidEmail(email))
                error = RegisterError.INVALID_EMAIL;
            else if (!ConstrainValidator.isValidPassword(password))
                error = RegisterError.INVALID_PASSWORD;
            else if (!ConstrainValidator.isValidName(name))
                error = RegisterError.INVALID_NAME;
            else if (!ConstrainValidator.isValidSurname(surname))
                error = RegisterError.INVALID_SURNAME;
            else if (userDAO.doesUsernameExist(username))
                error = RegisterError.USERNAME_NOT_AVAILABLE;
            else if (userDAO.doesEmailExist(email))
                error = RegisterError.EMAIL_ALREADY_USED;
            else if (!password.equals(confirmPassword))
                error = RegisterError.PASSWORD_MISMATCH;
            else
                error = null;

            if (error != null) {
                resp.sendRedirect(req.getContextPath() + "/register?error=" + error.ordinal());
                return;
            }

            if (userDAO.addUser(username, email, password, name, surname))
                resp.sendRedirect(req.getContextPath() + "/login?success=true");
            else
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown error while creating account");

        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error with the database");
        }
    }

    /**
     * Close the {@link Connection} to the database.
     */
    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException ignored) {
        }
    }
}
