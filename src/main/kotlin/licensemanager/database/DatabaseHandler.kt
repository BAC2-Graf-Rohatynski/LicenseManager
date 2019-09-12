package licensemanager.database

import apibuilder.license.objects.LicenseObject
import enumstorage.database.DatabaseType
import enumstorage.license.LicenseInformation
import enumstorage.license.LicenseState
import licensemanager.database.interfaces.IDatabaseHandler
import licensemanager.license.DefaultLicense
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import propertystorage.DatabaseProperties
import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class DatabaseHandler: IDatabaseHandler {
    @Volatile
    private lateinit var connection: Connection

    @Volatile
    private lateinit var statement: Statement

    private val logger: Logger = LoggerFactory.getLogger(DatabaseHandler::class.java)
    private val tableName: String = DatabaseType.Licenses.name

    init {
        try {
            logger.info("Starting license database ...")
            connectToDatabase()
            checkWhetherTableExists()
            logger.info("License database started")
        } catch (ex: Exception) {
            logger.error("Error while starting database database!\n${ex.message}")
        }
    }

    /**
     * Connects to the MySQL database.
     *
     * @return  void
     */
    private fun connectToDatabase() {
        try {
            logger.info("Connecting to database ...")
            connection = DriverManager.getConnection(DatabaseProperties.getUrl(), DatabaseProperties.getUsername(), DatabaseProperties.getPassword())
            statement = connection.createStatement()
            logger.info("Connected to database")
        } catch (ex: Exception) {
            logger.error("Error occurred while connecting to database!\n${ex.message}")
        }
    }

    /**
     * Checks whether the license table exists or not. If not, the table will be created.
     *
     * @return  void
     */
    private fun checkWhetherTableExists() {
        val metaData = connection.metaData
        val resultSet = metaData.getTables(null, null, tableName, arrayOf("TABLE"))

        if (resultSet.next()) {
            logger.info("Table $tableName not created yet")
            createTable()
            val defaultLicense = DefaultLicense().create(isActivated = true)
            logger.info("Adding the default licence")
            addLicenseToDatabase(license = defaultLicense)
        } else {
            logger.info("Table $tableName already created")
        }
    }

    /**
     * Creates the license table.
     *
     * @return  void
     */
    private fun createTable() {
        val query = "CREATE TABLE $tableName" +
                "(${LicenseInformation.Id.name} INTEGER not NULL, " +
                " ${LicenseInformation.Type.name} VARCHAR(255), " +
                " ${LicenseInformation.CreatedAt.name} VARCHAR(255), " +
                " ${LicenseInformation.ExpiresAt.name} VARCHAR(255), " +
                " ${LicenseInformation.State.name} VARCHAR(255), " +
                " ${LicenseInformation.IsDefault.name} VARCHAR(255), " +
                " ${LicenseInformation.SerialNumber.name} VARCHAR(255), " +
                " PRIMARY KEY (${LicenseInformation.Id.name}))"

        statement.executeUpdate(query)
        logger.info("Table $tableName created")
    }

    /**
     * Shows all licenses in the database in the logger.
     *
     * @return  void
     */
    override fun showAllLicensesInDatabase() {
        logger.info("Licenses in database:")
        val resultSet = getAllRecords()

        while (resultSet.next()) {
            val id = resultSet.getInt(LicenseInformation.Id.name)
            val state = resultSet.getString(LicenseInformation.State.name)
            val expiresAt = resultSet.getString(LicenseInformation.ExpiresAt.name)
            val isDefault = resultSet.getBoolean(LicenseInformation.IsDefault.name)
            logger.info("\tId: $id || State: $state || Expires at: $expiresAt || isDefault: $isDefault")
        }
    }

    override fun getRecordById(id: Int): ResultSet {
        val query = "SELECT * FROM $tableName WHERE ${LicenseInformation.Id.name} = $id"
        return statement.executeQuery(query)
    }

    /**
     * Returns all licenses in the database.
     *
     * @return  ResultSet of all licenses in the database
     */
    override fun getAllRecords(): ResultSet {
        val query = "SELECT * FROM $tableName"
        return statement.executeQuery(query)
    }

    /**
     * Locks a license in the database.
     *
     * @param   id          License id of license to be changed
     * @return  void
     */
    override fun lockLicenseInDatabase(id: Int) {
        val query = "UPDATE $tableName SET ${LicenseInformation.State.name} = '${LicenseState.Locked.name}' WHERE ${LicenseInformation.Id.name} = $id"
        statement.executeUpdate(query)
        logger.info("License $id locked")
    }

    /**
     * Unlocks a license in the database.
     *
     * @param   id          License id of license to be changed
     * @return  void
     */
    override fun unlockLicenseInDatabase(id: Int) {
        val query = "UPDATE $tableName SET ${LicenseInformation.State.name} = '${LicenseState.Inactive.name}' WHERE ${LicenseInformation.Id.name} = $id"
        statement.executeUpdate(query)
        logger.info("License $id unlocked")
    }

    /**
     * Activates the default license in the database.
     *
     * @return  void
     */
    override fun activateDefaultLicenseInDatabase() {
        val query = "UPDATE $tableName SET ${LicenseInformation.State.name} = '${LicenseState.Active.name}' WHERE isDefault = 'true'"
        statement.executeUpdate(query)
        logger.info("Default license activated")
    }

    /**
     * Deactivates a license in the database.
     *
     * @param   id          License id of license to be changed
     * @return  void
     */
    override fun deactivateLicenseInDatabase(id: Int) {
        val query = "UPDATE $tableName SET ${LicenseInformation.State.name} = '${LicenseState.Inactive.name}' WHERE ${LicenseInformation.Id.name} = $id"
        statement.executeUpdate(query)
        logger.info("All active licenses deactivated")
    }

    /**
     * Activates a license in the database.
     *
     * @param   id          License id of license to be changed
     * @return  void
     */
    override fun activateLicenseInDatabase(id: Int) {
        val query = "UPDATE $tableName SET ${LicenseInformation.State.name} = '${LicenseState.Active.name}' WHERE ${LicenseInformation.Id.name} = $id"
        statement.executeUpdate(query)
        logger.info("License $id activated")
    }

    /**
     * Deletes a license in the database.
     *
     * @param   id          License id of license to be changed
     * @return  void
     */
    override fun deleteLicenseInDatabase(id: Int) {
        val query = "DELETE FROM $tableName WHERE ${LicenseInformation.Id.name} = $id"
        statement.executeUpdate(query)
        logger.info("License $id deleted")
    }

    /**
     * Extends the expiration date of a license in the database.
     *
     * @param   id                  License id of license to be changed
     * @param   expirationDate      New expiration date
     * @return  void
     */
    override fun extendExpirationDateOfLicenseInDatabase(id: Int, expirationDate: String) {
        val query = "UPDATE $tableName SET ${LicenseInformation.ExpiresAt.name} = '$expirationDate' WHERE ${LicenseInformation.Id.name} = $id"
        statement.executeUpdate(query)
        logger.info("License $id expiration date changed to $expirationDate")
    }

    /**
     * Adds a license to the database.
     *
     * @param   license        License object to be added
     * @return  void
     */
    override fun addLicenseToDatabase(license: LicenseObject) {
        val query = "INSERT INTO $tableName VALUES " +
                "(${license.id} , " +
                "'${license.type}', " +
                "'${license.createdAt}', " +
                "'${license.expiresAt}', " +
                "'${license.state}', " +
                "'${license.isDefault}, " +
                "'${license.serialNumber}'')"
        statement.executeUpdate(query)
        logger.info("License ${license.id} added to database")
    }
}