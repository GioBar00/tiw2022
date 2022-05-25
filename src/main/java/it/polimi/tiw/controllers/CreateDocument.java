package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.DocumentDAO;
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
import java.sql.SQLException;

@WebServlet(name = "CreateDocument", value = "/create-document")
public class CreateDocument extends HttpServlet {
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
     * Loads the ContentManagement page with details of the {@link DocumentDAO}
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
            String subId = request.getParameter("subFolderId");

            if(subId == null || subId.isEmpty() || !InputValidator.isInt(subId, response)){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The data are not correct");
                return;
            }

            int subFolderId = Integer.parseInt(subId);
            SubFolderDAO subFolderDAO = new SubFolderDAO(this.connection);
            User user = (User) request.getSession().getAttribute("user");

            if (subFolderDAO.checkOwner(user.id(), subFolderId)) {

                ServletContext servletContext = getServletContext();
                final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
                ctx.setVariable("userRequest", 2);
                ctx.setVariable("subFolderId", subFolderId);
                templateEngine.process(TemplatePages.CONTENT_MANAGEMENT.getValue(), ctx, response.getWriter());

            } else response.sendRedirect(getServletContext().getContextPath() + "/");
        } catch (NullPointerException | NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Parameters");
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while processing the request");
        }
    }

    /**
     * Manages the creation of a {@link it.polimi.tiw.beans.Document}
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
        String name = request.getParameter("name");
        String format = request.getParameter("format");
        String summary = request.getParameter("summary");
        String subFolderId = request.getParameter("subFldrId");

        if(name == null || name.isEmpty())
            if(format == null || format.isEmpty())
                if(summary == null || summary.isEmpty())
                    if(subFolderId == null || subFolderId.isEmpty()){
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The data are not correct");
                        return;
                    }

        if (!InputValidator.isInt(subFolderId, response))
            return;

        SubFolderDAO subFolderDAO = new SubFolderDAO(this.connection);
        DocumentDAO documentDAO = new DocumentDAO(this.connection);
        User user = (User) request.getSession().getAttribute("user");

        try {
            if (subFolderDAO.checkOwner(user.id(), Integer.parseInt(subFolderId)))
                if (subFolderDAO.checkName(name))
                    if (documentDAO.checkName(name) && documentDAO.checkFormat(format) && documentDAO.checkSummary(summary)) {

                        if (documentDAO.createDocument(name, format, summary, Integer.parseInt(subFolderId))) {
                            response.sendRedirect(getServletContext().getContextPath() + "/");
                            return;
                        }

                    }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The data are not correct");
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
