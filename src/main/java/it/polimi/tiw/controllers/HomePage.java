package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.FolderDAO;
import it.polimi.tiw.enums.TemplatePages;
import it.polimi.tiw.utils.ConnectionHandler;
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
 * This class is the controller for the home page.
 */
@WebServlet(name = "HomePage", value = "/")
public class HomePage extends HttpServlet {
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
     * Loads the home page.
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
        try {
            FolderDAO folderDAO = new FolderDAO(connection);
            final WebContext ctx = new WebContext(req, resp, req.getServletContext(), req.getLocale());
            User user = (User) req.getSession().getAttribute("user");
            ctx.setVariable("folders", folderDAO.getFoldersWithSubFolders(user.id()));
            templateEngine.process(TemplatePages.HOME.getValue(), ctx, resp.getWriter());
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while retrieving folders");
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
