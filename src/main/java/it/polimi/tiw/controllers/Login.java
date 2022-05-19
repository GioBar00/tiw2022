package it.polimi.tiw.controllers;

import it.polimi.tiw.enums.ContextParameters;
import it.polimi.tiw.enums.TemplatePages;
import it.polimi.tiw.enums.TemplateResolverParameters;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class is the controller for the login page.
 */
@WebServlet(name = "Login", value = "/login")
public class Login extends HttpServlet {
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
     * @throws ServletException if the {@link Connection} to the database cannot be initialized.
     */
    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        try {
            String dbDriver = context.getInitParameter(ContextParameters.DB_DRIVER.getValue());
            String dbUrl = context.getInitParameter(ContextParameters.DB_URL.getValue());
            String dbUser = context.getInitParameter(ContextParameters.DB_USER.getValue());
            String dbPassword = context.getInitParameter(ContextParameters.DB_PASSWORD.getValue());
            Class.forName(dbDriver);
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (ClassNotFoundException e) {
            throw new UnavailableException("Can't load database driver");
        } catch (SQLException e) {
            throw new UnavailableException("Can't connect to database");
        }

        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix(TemplateResolverParameters.PREFIX.getValue());
        templateResolver.setSuffix(TemplateResolverParameters.SUFFIX.getValue());

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

    }

    /**
     * Loads the login page.
     * @param req   an {@link HttpServletRequest} object that
     *                  contains the request the client has made
     *                  of the servlet
     *
     * @param resp  an {@link HttpServletResponse} object that
     *                  contains the response the servlet sends
     *                  to the client
     *
     * @throws IOException if an input or output error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final WebContext webContext = new WebContext(req, resp, getServletContext(), req.getLocale());
        if (req.getParameter("error") != null)
            webContext.setVariable("error", true);
        templateEngine.process(String.valueOf(TemplatePages.LOGIN), webContext, resp.getWriter());
    }

    /**
     * Checks the credentials of the user and redirects to the home page if the credentials are correct.
     * @param req   an {@link HttpServletRequest} object that
     *                  contains the request the client has made
     *                  of the servlet
     *
     * @param resp  an {@link HttpServletResponse} object that
     *                  contains the response the servlet sends
     *                  to the client
     *
     * @throws IOException if an input or output error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            resp.sendRedirect(getServletContext().getContextPath() + "/login?error=true");
            return;
        }

        //FIXME: UserDAO, User
        String user = null;
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
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
