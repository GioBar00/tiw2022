package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.SubFolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the Data Access Object for the Folder.
 */
public class FolderDAO {
    /**
     * {@link Connection} to the database.
     */
    private final Connection connection;

    /**
     * Constructor.
     * @param connection the {@link Connection} to the database.
     */
    public FolderDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * This method checks if a folder exists with the given id and owner.
     * @param id the id of the folder.
     * @return if the folder exists.
     * @throws SQLException if an error occurs during the query.
     */
    public boolean doesFolderExist(int id, int ownerId) throws SQLException {
        String query = "SELECT idfolder FROM folder WHERE idfolder = ? AND user_iduser = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setInt(2, ownerId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    /**
     * This method returns the {@link Folder} with the given id.
     * @param id the id of the folder.
     * @return the {@link Folder} with the given id, null if it does not exist.
     * @throws SQLException if an error occurs during the query.
     */
    public Folder getFolder(int id) throws SQLException {
        String query = "SELECT idfolder, name, creationDate, user_iduser FROM folder WHERE idfolder = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Folder(resultSet.getInt("idfolder"), resultSet.getString("name"),
                        resultSet.getDate("creationDate"), resultSet.getInt("ownerId"));
            }
        }
        return null;
    }

    public Map<Folder, List<SubFolder>> getFoldersWithSubFolders(int ownerId) throws SQLException {
        String query = "SELECT idfolder FROM folder f INNER JOIN subfolder s ON f.idfolder = s.folder_idfolder WHERE f.user_iduser = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, ownerId);
            ResultSet resultSet = statement.executeQuery();
            Map<Folder, List<SubFolder>> folders = new HashMap<>();
            while (resultSet.next()) {

            }
            return folders;
        }
    }
}
