package com.dynatrace.oneagent.sdk.api;

/**
 * Interface for outgoing database tracer.
 * <a href="https://github.com/Dynatrace/OneAgent-SDK#database">https://github.com/Dynatrace/OneAgent-SDK#database</a>
 */
public interface DatabaseRequestTracer extends Tracer {

	/**
	 * Adds optional information about retrieved rows of the traced database request.
	 * 
	 * @param rowsReturned number of rows returned by this traced database request. Only positive values are allowed. 
	 */
	public void setRowsReturned(int rowsReturned);
	
	/**
	 * Adds optional information about round-trip count to database server.
	 * 
	 * @param roundTripCount count of round-trips that took place. Only positive values are allowed.
	 */
	public void setRoundTripCount(int roundTripCount);
	
}
