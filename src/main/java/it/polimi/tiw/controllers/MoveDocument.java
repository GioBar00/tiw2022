package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.SubFolder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.DocumentDAO;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(name = "MoveDocument", value = "/move-document")
public class MoveDocument extends HttpServlet {

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
     * Loads the document page with details of the {@link Folder}s and {@link SubFolder}s
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

        String docId = request.getParameter("documentId");

        if (docId == null || docId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The data is not correct");
            return;
        }
        if (!InputValidator.isInt(docId, response))
            return;

        DocumentDAO documentDAO = new DocumentDAO(this.connection);
        SubFolderDAO subFolderDAO = new SubFolderDAO(this.connection);
        User user = (User) request.getSession().getAttribute("user");

        try {
            Document document = documentDAO.getDocument(Integer.parseInt(docId));
            if (document != null) {
                int fromSubFolder = document.subFolderId();
                if (subFolderDAO.checkOwner(user.id(), fromSubFolder)) {
                    SubFolder subFolder = subFolderDAO.getSubFolder(fromSubFolder);
                    FolderDAO folderDAO = new FolderDAO(this.connection);
                    Map<Folder, List<SubFolder>> folders = folderDAO.getFoldersWithSubFolders(user.id());

                    ServletContext servletContext = getServletContext();
                    final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
                    ctx.setVariable("move", true);
                    ctx.setVariable("document", document);
                    ctx.setVariable("fromSubFolder", subFolder);
                    ctx.setVariable("folders", folders);
                    templateEngine.process(TemplatePages.HOME.getValue(), ctx, response.getWriter());
                    return;
                }
            }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid subFolder id");
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while processing the request");
        }
    }

    /**
     * This method processes the request of moving a {@link Document} to a {@link SubFolder}
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
        String selectedSubFolder = request.getParameter("selectedSubFolder");
        String documentId = request.getParameter("documentId");

        if (selectedSubFolder == null || documentId == null || selectedSubFolder.isEmpty() || documentId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The data is not correct");
            return;
        }


        if (!InputValidator.isInt(selectedSubFolder, response) || !InputValidator.isInt(documentId, response))
            return;

        DocumentDAO documentDAO = new DocumentDAO(this.connection);
        SubFolderDAO subFolderDAO = new SubFolderDAO(this.connection);
        User user = (User) request.getSession().getAttribute("user");

        try {
            if (subFolderDAO.checkOwner(user.id(), Integer.parseInt(selectedSubFolder))) {
                Document document = documentDAO.getDocument(Integer.parseInt(documentId));
                if (document != null) {
                    int fromSubFolder = document.subFolderId();
                    if (subFolderDAO.checkOwner(user.id(), fromSubFolder)) {
                        if (documentDAO.moveDocument(document.id(), Integer.parseInt(selectedSubFolder))) {
                            String path = getServletContext().getContextPath() + "/home";
                            response.sendRedirect(path);
                            return;
                        }
                    }
                }
            }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
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
