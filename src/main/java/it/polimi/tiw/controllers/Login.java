package it.polimi.tiw.controllers;

import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.beans.User;
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
 * This class is the controller for the login page.
 */
@WebServlet(name = "Login", value = "/login")
public class Login extends HttpServlet {
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
     * Loads the login page.
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
        if (req.getParameter("error") != null)
            webContext.setVariable("error", true);
        else if (req.getParameter("success") != null)
            webContext.setVariable("success", true);
        templateEngine.process(TemplatePages.LOGIN.getValue(), webContext, resp.getWriter());
    }

    /**
     * Checks the credentials of the user and redirects to the home page if the credentials are correct.
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
        String identifier = req.getParameter("identifier");
        String password = req.getParameter("password");

        if (identifier == null || identifier.isEmpty() || password == null || password.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username or password cannot be empty");
            return;
        }

        UserDAO userDAO = new UserDAO(connection);
        User user = null;
        try {
            if (UserDAO.isValidEmail(identifier) && userDAO.doesEmailExist(identifier)) {
                user = userDAO.checkEmailCredentials(identifier, password);
            } else if (UserDAO.isValidUsername(identifier) && userDAO.doesUsernameExist(identifier)) {
                user = userDAO.checkUsernameCredentials(identifier, password);
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while checking credentials");
            return;
        }

        String path;
        if (user == null)
            path = getServletContext().getContextPath() + "/login?error=true";
        else {
            req.getSession().setAttribute("user", user);
            path = getServletContext().getContextPath() + "/";
        }
        resp.sendRedirect(path);
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
