package databaseServices;

/**
 * Interface used for connecting to databases. <br>
 * The other database services should carry this around for authentication and connection to their respective DB
 * @author luttredn
 *
 */
public interface DatabaseConnectionService {
	// connect to database
	public boolean connect(String username, String password);
	
	// close connection to database
	public void closeConnection();
}
