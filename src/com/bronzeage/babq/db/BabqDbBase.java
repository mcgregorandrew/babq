/**
 * 
 */
package com.bronzeage.babq.db;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import com.bronzeage.babq.common.BabqConfig;
import com.bronzeage.babq.processing.BabqProcessor;

/**
 * @author andrew
 * 
 */
public class BabqDbBase {

	protected String tblName_m;
	private Connection conn_m;
	private Statement queryStmt_m;
	static DateFormat formatter_ms = new SimpleDateFormat("MMM d, yyyy");
	static DateFormat formatterYMD_ms = new SimpleDateFormat("yyyy-MM-dd");
	
	static Logger logger_m = Logger.getLogger(BabqProcessor.class.getPackage()
			.getName());

	BabqDbBase(String name, Connection conn) {
		tblName_m = name;
		conn_m = conn;
	}

	public PreparedStatement makePrepStmt(String sql) throws SQLException {
		return conn_m.prepareStatement(sql);
	}

	public void executePrepStmt(PreparedStatement prepStmt) throws SQLException {
		prepStmt.execute();
	}

	public ResultSet doQuery(String query) throws SQLException {
		if ((queryStmt_m != null) && (!queryStmt_m.isClosed()))
			queryStmt_m.close();
		queryStmt_m = conn_m.createStatement();
		return queryStmt_m.executeQuery(query);
	}

	public void closeQuery(ResultSet rs) throws SQLException {
		rs.close();
		queryStmt_m.close();

	}

	public int getIntValue(String sql) throws SQLException {
		Statement stmt = conn_m.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		rs.next();
		int retValue = rs.getInt(1);
		stmt.close();
		return retValue;
	}

	public Date getDateValue(String string) throws SQLException {
		Statement stmt = conn_m.createStatement();
		ResultSet rs = stmt.executeQuery(string);
		rs.next();
		Date retValue = rs.getDate(1);
		stmt.close();
		return retValue;
	}

	public void doUpdate(String operation) throws SQLException {
		Statement stmt = conn_m.createStatement();

		stmt.execute(operation);

		stmt.close();
	}

	public void dropTbl() {
		try {
			doUpdate("drop table if exists " + tblName_m);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Date stringToDate(String string) throws ParseException {
		if ((string == null) || (string.length() < 3))
			return null;
		if (string.equals("n/a"))
			return null;
		return new Date(formatter_ms.parse(string).getTime());
	}

	public static Date stringYMDToDate(String string) throws ParseException {
		if ((string == null) || (string.length() < 3))
			return null;
		if (string.equals("n/a"))
			return null;
		return new Date(formatterYMD_ms.parse(string).getTime());
	}

	protected void setString(PreparedStatement prep, int position, String string)
			throws SQLException {
		try {
			if (string == null)
			{
				prep.setString(position, null);
				return;
			}
			
			if (BabqConfig.isH2())
				prep.setString(position, string);
			else
				prep.setBytes(position, string.getBytes("UTF8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Connection makeConnection(String jdbcUri, String dbName_m)
			throws Exception {
		Statement stmt = null;
		Connection jdbcConn = null;
		try {
			String name = jdbcUri;
			// "jdbc:mysql://" + host + "/?user=" + user + "&password=" +
			// password;
 
			Class.forName(BabqConfig.getSqlDriver()).newInstance();

			if (!BabqConfig.isH2()) {
				jdbcConn = DriverManager.getConnection(name);
				stmt = jdbcConn.createStatement();
				stmt.execute("SET NAMES 'UTF8'");
				stmt.execute("USE " + dbName_m);
			} else {
				jdbcConn = DriverManager.getConnection(name, "", "");
				stmt = jdbcConn.createStatement();

			}
		} catch (SQLException e) {
			if (stmt != null) {
				stmt.executeUpdate("CREATE DATABASE " + dbName_m
						+ " CHARACTER SET 'UTF8'");
				stmt.execute("USE " + dbName_m);
			}
		}
		return jdbcConn;
	}
}
