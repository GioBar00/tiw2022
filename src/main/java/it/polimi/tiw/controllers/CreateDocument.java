package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.DocumentDAO;
import it.polimi.tiw.dao.SubFolderDAO;
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
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "CreateDocument", value ="/documents")
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
     * Initialaze the {@link Connection} to the database and the {@link TemplateEngine}
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
     * Load the document page with details of the document
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
        int subFolderId;
        try {
            subFolderId = Integer.parseInt(request.getParameter("subFolderId"));

            SubFolderDAO subFolderDAO = new SubFolderDAO(this.connection);
            User user = (User) request.getSession().getAttribute("user");

            if (subFolderDAO.checkOwner(user.id(), subFolderId)) {

                request.getSession().setAttribute("subFolderId", subFolderId);
                String path = "WEB-INF/home/contentManagement";
                ServletContext servletContext = getServletContext();
                final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
                ctx.setVariable("userRequest", 2);
                templateEngine.process(path, ctx, response.getWriter());
            }

            else response.sendRedirect(String.valueOf(TemplatePages.HOME));
        } catch (NullPointerException | NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Parameters");
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while processing the request");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String format = request.getParameter("format");
        String summary = request.getParameter("summary");

        if(name != null && name.length()>0 && name.length()<=50)
            if( format != null && format.length()> 0 && format.length()<=10)
                if(summary != null && summary.length()>0 && summary.length()<= 200){
                    DocumentDAO documentDAO = new DocumentDAO(this.connection);
                    try {
                        if(documentDAO.createDocument(name,format,summary, (Integer) request.getSession().getAttribute("subFolderId"))){
                            request.getSession().removeAttribute("subFolderId");
                            response.sendRedirect(String.valueOf(TemplatePages.HOME));
                            return;
                        }
                    } catch (SQLException e) {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while processing the request");
                    }
                }
        
        final WebContext webContext = new WebContext(request, response, getServletContext(), request.getLocale());
        webContext.setVariable("error", true);
        templateEngine.process(request.getContextPath(), webContext, response.getWriter());

    }

    /**
     * Close the {@link Connection} to the database
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
