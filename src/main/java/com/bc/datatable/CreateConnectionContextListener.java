package com.bc.datatable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class CreateConnectionContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			Class.forName("org.h2.Driver");
			Connection conn = DriverManager.getConnection("jdbc:h2:mem:",
				"sa", "");
			initializeDatabase(conn);
			sce.getServletContext().setAttribute("conn", conn);
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private void initializeDatabase(Connection conn) throws Exception {
		ClassLoader tcl = Thread.currentThread().getContextClassLoader();
		InputStream is = tcl.getResourceAsStream("cities.sql");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0) {
					continue;
				}
				Statement stmt = conn.createStatement();
				stmt.execute(line);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			Connection conn = (Connection)sce.getServletContext()
				.getAttribute("conn");
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception exception) {
					throw new RuntimeException(exception);
				}
			}
		} finally {
			Enumeration<Driver> iterator = DriverManager.getDrivers();
			while (iterator.hasMoreElements()) {
				try {
					DriverManager.deregisterDriver(iterator.nextElement());
				} catch (SQLException exception) {
					// NOOP
				}
			}
		}
	}

}
