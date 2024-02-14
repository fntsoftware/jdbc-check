/*
 * FNT GmbH
 */
package com.fntsoftware.itservices.jdbcTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class JdbcTest
{
    private String hostnameOrUrl;

    private String port;

    private String dbname;

    private String username;

    private String decryptedPassword;

    private String query;

    private String dbType;

    private String jdbcDriver = null;

    private String jdbcUrl = null;

    private Connection conn = null;

    private Statement stmt = null;

    private int timeout = 1;

    boolean connect = false;

    public static final String JDBC_URL = "jdbcURL";

    public static final String DB_TYPE = "dbType";

    public static final String HOST_NAME = "hostName";

    public static final String PORT = "port";

    public static final String DB_USERNAME = "dbUsername";

    public static final String DB_PASSWORD = "dbPassword";

    public static final String DB_NAME = "dbName";

    public static final String TIMEOUT = "timeout";

    public static final String QUERY = "query";

    public static void main(final String[] args) throws SQLException, InterruptedException
    {
        final JdbcTest jdbcTest = new JdbcTest();

        final Options options = initOptions();
        final CommandLineParser parser = new DefaultParser();
        try
        {
            final CommandLine line = parser.parse(options, args);

            if (line.hasOption("help"))
            {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("JDBCTest", options);
                System.exit(0);
            }

            jdbcTest.query = line.getOptionValue(QUERY, null);
            jdbcTest.timeout = Integer.parseInt(line.getOptionValue(TIMEOUT, "1"));

            jdbcTest.validateManadtoryAttributes(line);

            if (line.hasOption(JDBC_URL))
            {
                jdbcTest.jdbcUrl = line.getOptionValue(JDBC_URL);
                jdbcTest.runTests();
            }
            else
            {
                if (line.hasOption(HOST_NAME) && line.hasOption(PORT) && line.hasOption(DB_NAME))
                {
                    jdbcTest.hostnameOrUrl = line.getOptionValue(HOST_NAME);
                    jdbcTest.port = line.getOptionValue(PORT);
                    jdbcTest.dbname = line.getOptionValue(DB_NAME);

                    jdbcTest.buildConnectionString();
                    jdbcTest.runTests();
                }
                else
                {
                    throw new RuntimeException(
                        "Not all required attributes are provided. \n Required attributes are:\n"
                            + HOST_NAME + " " + PORT + " " + DB_NAME + " " + DB_USERNAME + " "
                            + DB_PASSWORD);
                }
            }
        }
        catch (final ParseException e)
        {
            throw new RuntimeException("Parsing of attributes failed. Reason:" + e.getMessage(), e);
        }

        if (jdbcTest.connect)
        {
            System.exit(0);
        }
        else
        {
            System.exit(1);
        }
    }

    private static Options initOptions()
    {
        return new Options().addOption(DB_TYPE, true,
            "Type of the Database (postgres / oracle / oraclesid)")
            .addOption(HOST_NAME, true, "Name or URL from the DB host")
            .addOption(PORT, true, "Port of the DB instance").addOption(DB_NAME, true,
                "Name of the DB")
            .addOption(DB_USERNAME, true, "Name of the DatabaseUser")
            .addOption(DB_PASSWORD, true, "The decrypted Password of the Database")
            .addOption(JDBC_URL, true, "The wohle JDBC URL connection string")
            .addOption(TIMEOUT, true, "Time before trying is aborted")
            .addOption(QUERY, true, "Query String used for checking status of DB")
            .addOption("help", false, "Prints this message");
    }

    private void buildConnectionString()
    {
        switch (this.dbType) {
        case "oracle":
            this.jdbcUrl = "jdbc:oracle:thin:@" + this.hostnameOrUrl + ":" + this.port + "/"
                + this.dbname;
            break;
        case "oraclesid":
            this.jdbcUrl = "jdbc:oracle:thin:@" + this.hostnameOrUrl + ":" + this.port + ":"
                + this.dbname;
            break;
        case "postgres":
            this.jdbcUrl = "jdbc:postgresql://" + this.hostnameOrUrl + ":" + this.port + "/"
                + this.dbname;
            break;
        default:
            throw new RuntimeException("No valid DB typ given [postgres,oracle,oraclesid]!");
        }
    }

    private void validateManadtoryAttributes(final CommandLine line)
    {
        if (line.hasOption(DB_TYPE))
        {
            dbType = line.getOptionValue(DB_TYPE);

            switch (dbType) {
            case "oracle":
                this.jdbcDriver = "oracle.jdbc.driver.OracleDriver";
                this.query = isDefined(this.query) ? this.query : "select 1 from dual";
                break;
            case "postgres":
                this.jdbcDriver = "org.postgresql.Driver";
                this.query = isDefined(this.query) ? this.query : "select 1";
                break;
            default:
                throw new RuntimeException("No valid DB typ given [postgres,oracle,oraclesid]!");
            }
        }
        else
        {
            throw new RuntimeException("No dbType was provided");
        }

        if (line.hasOption(DB_USERNAME) && line.hasOption(DB_PASSWORD))
        {
            username = line.getOptionValue(DB_USERNAME);
            decryptedPassword = line.getOptionValue(DB_PASSWORD);
        }
        else
        {
            throw new RuntimeException("No dbUsername or dbPassword was provided");
        }
    }

    private void init() throws SQLException, ClassNotFoundException
    {
        debug("JdbcTest::init - START");

        debug("Using URL: " + this.jdbcUrl);
        debug("Using JDBC: " + this.jdbcDriver);
        Class.forName(this.jdbcDriver);
        debug("Initializing connection");
        this.conn = DriverManager.getConnection(this.jdbcUrl, this.username,
            this.decryptedPassword);
        debug("Connection initialized");
        debug("JdbcTest::init - END");
    }

    private void closeConnection() throws SQLException
    {
        debug("JdbcTest::closeConnection - START");
        if (this.conn != null)
        {
            debug("JdbcTest::closeConnection - Closing connection");

            this.conn.close();
        }
        debug("JdbcTest::closeConnection - END");
    }

    public void runTests() throws SQLException, InterruptedException
    {
        debug("JdbcTest::runTests - START");

        int run = 0;
        while (!this.connect)
        {

            if (run >= this.timeout)
            {
                break;
            }
            run++;
            try
            {
                init();
                debug("JdbcTest::runTests - Creating statement");
                this.stmt = this.conn.createStatement();
                debug("JdbcTest::runTests - Executing query '" + this.query + "'");
                final ResultSet rs = this.stmt.executeQuery(this.query);
                debug("JdbcTest::runTests - rs= " + rs);
                this.connect = true;
            }
            catch (final Exception e)
            {
                debug(e.getMessage());
                this.connect = false;
            }
            finally
            {
                closeConnection();
                Thread.sleep(1000);
            }
            debug("JdbcTest::runTests - END");
        }

    }

    public void debug(final String message)
    {
        System.out.println("[" + Calendar.getInstance().getTime() + "] " + message);
    }

    public static boolean isDefined(final String value)
    {
        return (value != null && value.length() > 0);
    }

}
