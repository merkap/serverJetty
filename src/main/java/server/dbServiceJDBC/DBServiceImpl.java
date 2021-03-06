package server.dbServiceJDBC;

import org.h2.jdbcx.JdbcDataSource;
import server.dbServiceJDBC.dao.UsersDAO;
import server.dbServiceJDBC.dataSets.UsersDataSet;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBServiceImpl implements DBService {
    private final Connection connection;

    public DBServiceImpl() {
        this.connection = getConnection();
    }

    public static Connection getMysqlConnection() {
        try {
            DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver")
                    .getDeclaredConstructor().newInstance());

            StringBuilder url = new StringBuilder();

            url.append("jdbc:mysql://")
                    .append("localhost:")
                    .append("3306/")
                    .append("db_examle?")
                    .append("user=test&")
                    .append("password=test");

            Connection connection = DriverManager.getConnection(url.toString());
            return connection;

        } catch (InstantiationException | InvocationTargetException | SQLException |
                NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Connection getConnection() {
        try {
            String url = "jdbc:h2:./h2db";
            String name = "test";
            String pass = "test";

            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL(url);
            ds.setUser(name);
            ds.setPassword(pass);

            Connection connection = DriverManager.getConnection(url, name, pass);
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UsersDataSet getUser(long id) throws DBException {
        try {
            return (new UsersDAO(connection).getId(id));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public UsersDataSet getUser(String name) throws DBException {
        try {
            return (new UsersDAO(connection).getUser(name));
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public long addUser(String name, String pass) throws DBException {
        try {
            connection.setAutoCommit(false);
            UsersDAO dao = new UsersDAO(connection);
            dao.createTable();
            dao.insertUser(name, pass);
            connection.commit();
            return dao.getUserId(name);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignore) {
            }
            throw new DBException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignore) {
            }
        }
    }

    public void cleanUp() throws DBException {
        UsersDAO dao = new UsersDAO(connection);
        try {
            dao.dropTable();
        } catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public void printConnectionInfo() {
        try {
            System.out.println("DB name: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("DB version: " + connection.getMetaData().getDatabaseProductVersion());
            System.out.println("Driver: " + connection.getMetaData().getDriverName());
            System.out.println("Autocommit: " + connection.getAutoCommit());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
