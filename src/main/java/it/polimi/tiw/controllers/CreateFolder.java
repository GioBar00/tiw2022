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

@WebServlet(name = "CreateFolder", value = "/create-folder")
public class CreateFolder extends HttpServlet {
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
     * Loads the page to create a new folder.
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
        final WebContext ctx = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        if (req.getAttribute("error") != null)
            ctx.setVariable("error", true);
        ctx.setVariable("userRequest", 0);
        templateEngine.process(TemplatePages.CONTENT_MANAGEMENT.getValue(), ctx, resp.getWriter());
    }

    /**
     * Creates a new folder.
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
        String name = req.getParameter("folder");
        if (name == null || name.isEmpty() || !FolderDAO.isNameValid(name)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is not valid");
            return;
        }
        User user = (User) req.getSession().getAttribute("user");
        try {
            FolderDAO folderDAO = new FolderDAO(connection);
            if (folderDAO.doesFolderWithNameExist(name, user.id())) {
                resp.sendRedirect(getServletContext().getContextPath() + "/create-folder?error=true");
            }
            else {
                if (folderDAO.createFolder(name, user.id()))
                    resp.sendRedirect(getServletContext().getContextPath() + "/");
                else
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while creating folder");
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while creating folder");
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
