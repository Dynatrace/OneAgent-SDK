package com.dynatrace.oneagent.sdk.samples;

import com.dynatrace.oneagent.sdk.OneAgentSDKFactory;
import com.dynatrace.oneagent.sdk.api.DatabaseRequestTracer;
import com.dynatrace.oneagent.sdk.api.OneAgentSDK;
import com.dynatrace.oneagent.sdk.api.enums.ChannelType;
import com.dynatrace.oneagent.sdk.api.infos.DatabaseInfo;

/**
 * This sample shows the usage of SDK for tracing outgoing database requests.
 */
public class DatabaseRequestSample {

	static class DBDriver {

		String dbName;
		String dbHost;

		DatabaseInfo databaseInfo;

		private DBDriver(String dbName, String dbHost) {
			this.dbName = dbName;
			this.dbHost = dbHost;

			databaseInfo = oneAgentSDK.createDatabaseInfo(dbName, "MyCoolDB", ChannelType.TCP_IP, dbHost);
		}

		public static DBDriver openConnection(String dbName, String dbHost) {
			return new DBDriver(dbName, dbHost);
		}

		public void query(String statement) {
			DatabaseRequestTracer databaseRequestTracer = oneAgentSDK.traceSQLDatabaseRequest(databaseInfo, statement);

			databaseRequestTracer.start();
			try {
				/*
				 *	actual execute code
				 */
				// optional attributes:
				databaseRequestTracer.setRowsReturned(327);
				databaseRequestTracer.setRoundTripCount(4);
			} catch (Exception e) {
				databaseRequestTracer.error(e.getMessage());
			} finally {
				databaseRequestTracer.end();
			}
		}

		public void execute(String statement) {
			DatabaseRequestTracer databaseRequestTracer = oneAgentSDK.traceSQLDatabaseRequest(databaseInfo, statement);

			databaseRequestTracer.start();
			try {
				/*
				 *	actual execute code
				 */
			} catch (Exception e) {
				databaseRequestTracer.error(e.getMessage());
			} finally {
				databaseRequestTracer.end();
			}
		}

		public void commit() {
			DatabaseRequestTracer databaseRequestTracer = oneAgentSDK.traceSQLDatabaseRequest(databaseInfo, "COMMIT");

			databaseRequestTracer.start();
			try {
				/*
				 *	actual commit code
				 */
			} catch (Exception e) {
				databaseRequestTracer.error(e.getMessage());
			} finally {
				databaseRequestTracer.end();
			}
		}

	}

	private static OneAgentSDK oneAgentSDK;

	public static void main(String[] args) {
		oneAgentSDK = OneAgentSDKFactory.createInstance();

		DBDriver dbDriver = DBDriver.openConnection("myDatabase", "192.168.0.56:6000");

		dbDriver.execute("CREATE TABLE table");
		dbDriver.execute("INSERT into table values blablabla");
		dbDriver.query("SELECT * from table");
		dbDriver.execute("DELETE TABLE table");
		dbDriver.commit();
	}

}
