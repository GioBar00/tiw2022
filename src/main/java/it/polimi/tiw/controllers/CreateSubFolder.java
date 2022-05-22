package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.FolderDAO;
import it.polimi.tiw.dao.SubFolderDAO;
import it.polimi.tiw.enums.TemplatePages;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.InputValidator;
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
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

@WebServlet(name = "CreateSubFolder", value = "/create-subfolder")
public class CreateSubFolder extends HttpServlet {

    /**
     * {@link Connection} to the database
     */
    private Connection connection;

    /**
     * {@link Connection} to render the template
     */
    private TemplateEngine templateEngine;

    /**
     * Initializes the {@link Connection} to the database and the {@link TemplateEngine}
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
     * Loads the ContentManagement page with details of the {@link it.polimi.tiw.beans.Folder}
     *
     * @param request  an {@link HttpServletRequest} object that
     *                 contains the request the client has made
     *                 of the servlet
     * @param response an {@link HttpServletResponse} object that
     *                 contains the response the servlet sends
     *                 to the client
     * @throws IOException if an input or output error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String id = request.getParameter("folderId");

            if (!InputValidator.isInt(id, response))
                return;

            int folderId = Integer.parseInt(id);
            FolderDAO folderDAO = new FolderDAO(this.connection);
            User user = (User) request.getSession().getAttribute("user");

            if (folderDAO.doesFolderExist(user.id(), folderId)) {
                ServletContext servletContext = getServletContext();
                final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
                ctx.setVariable("userRequest", 1);
                ctx.setVariable("fldrId", folderId);
                templateEngine.process(TemplatePages.CONTENT_MANAGEMENT.getValue(), ctx, response.getWriter());
            } else response.sendRedirect(getServletContext().getContextPath() + "/");
        } catch (NullPointerException | NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Parameters");
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while processing the request");
        }
    }

    /**
     * Manages the creation of a new {@link it.polimi.tiw.beans.SubFolder}
     *
     * @param request  an {@link HttpServletRequest} object that
     *                 contains the request the client has made
     *                 of the servlet
     * @param response an {@link HttpServletResponse} object that
     *                 contains the response the servlet sends
     *                 to the client
     * @throws IOException if an input or output error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String folderId = request.getParameter("folderId");
        String subFolder = request.getParameter("subFolder");

        if (!InputValidator.isInt(folderId, response))
            return;

        FolderDAO folderDAO = new FolderDAO(this.connection);
        User user = (User) request.getSession().getAttribute("user");
        try {
            if (folderDAO.doesFolderExist(Integer.parseInt(folderId), user.id())) {
                SubFolderDAO subFolderDAO = new SubFolderDAO(this.connection);
                java.util.Date date = new java.util.Date();
                long timeInMilliSeconds = date.getTime();
                if (subFolderDAO.createSubFolder(subFolder, new Date(timeInMilliSeconds), Integer.parseInt(folderId))) {
                    response.sendRedirect(getServletContext().getContextPath() + "/");
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The data are not correct");
                }
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while processing the request");
        }
    }

    /**
     * Closes the {@link Connection} to the database
     */
    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException ignored) {
        }
    }
}
