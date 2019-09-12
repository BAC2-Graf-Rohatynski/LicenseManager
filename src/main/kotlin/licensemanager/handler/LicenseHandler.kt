package licensemanager.handler

import apibuilder.license.objects.LicenseObject
import enumstorage.license.LicenseInformation
import enumstorage.license.LicenseState
import enumstorage.license.LicenseType
import licensemanager.database.DatabaseHandler
import licensemanager.handler.interfaces.ILicenseHandler
import licensemanager.license.DefaultLicense
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import propertystorage.MasterProperties
import java.text.SimpleDateFormat
import java.util.Date

/**
 * This handler manages all database-related actions
 *
 * @author      Markus Graf
 * @see         java.lang.Exception
 * @see         java.sql.Connection
 * @see         java.sql.DriverManager
 * @see         java.sql.Statement
 * @see         java.text.SimpleDateFormat
 * @see         java.util.Random
 * @see         java.sql.ResultSet
 * @see         java.util.Date
 */
object LicenseHandler: ILicenseHandler {
    private val logger: Logger = LoggerFactory.getLogger(LicenseHandler::class.java)
    private val database = DatabaseHandler()

    init {
        checkLicensesForExpiration()
        checkForDefaultLicense()
        checkForActiveLicense()
        showAllLicensesInDatabase()
    }

    private fun getAllRecords() = database.getAllRecords()
    private fun activateLicenseInDatabase(id: Int) = database.activateLicenseInDatabase(id = id)
    private fun deactivateLicenseInDatabase(id: Int) = database.deactivateLicenseInDatabase(id = id)
    private fun lockLicenseInDatabase(id: Int) = database.lockLicenseInDatabase(id = id)
    private fun unlockLicenseInDatabase(id: Int) = database.unlockLicenseInDatabase(id = id)
    private fun activateDefaultLicenseInDatabase() = database.activateDefaultLicenseInDatabase()
    private fun getRecordById(id: Int) = database.getRecordById(id = id)
    private fun deleteLicenseInDatabase(id: Int) = database.deleteLicenseInDatabase(id = id)
    private fun checkSerialNumber(serialNumber: String): Boolean = (serialNumber == MasterProperties.getSerial())

    /**
     * Checks all licenses for its expiration date.
     *
     * @return  void
     */
    private fun checkLicensesForExpiration() {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd")
        val date = Date()
        val currentDate = dateFormat.format(date)
                .replace("[", "")
                .replace("]", "")
                .replace(", ", "/")
                .split("/")
        val resultSet = getAllRecords()
        val expiredLicenses = mutableListOf<Int>()

        while (resultSet.next()) {
            val id = resultSet.getInt(LicenseInformation.Id.name)
            val isDefault = resultSet.getBoolean(LicenseInformation.IsDefault.name)
            val state = resultSet.getString(LicenseInformation.State.name)
            val expiresAt = resultSet.getString(LicenseInformation.ExpiresAt.name)

            if (!isDefault && state != LicenseState.Locked.name) {
                val expirationDate = expiresAt.split("/")
                if (!checkExpirationTime(currentDate = currentDate, expirationDate = expirationDate)) {
                    logger.warn("License $id expired")
                    expiredLicenses.add(id)
                }
            }
        }

        expiredLicenses.forEach{
            lockLicenseInDatabase(id = it)
        }

        logger.info("Expiration date of licenses checked")
    }

    /**
     * Checks whether default license is unlocked and available. If not, the default license will be either unlocked
     * or created.
     *
     * @return  void
     */
    private fun checkForDefaultLicense() {
        val resultSet = getAllRecords()
        var defaultLicenseId = 0
        var defaultLicenseState = String()
        var defaultLicenseAvailable = false

        while (resultSet.next()) {
            val isDefault = resultSet.getBoolean(LicenseInformation.IsDefault.name)

            if (isDefault) {
                defaultLicenseId = resultSet.getInt(LicenseInformation.Id.name)
                defaultLicenseState = resultSet.getString(LicenseInformation.State.name)
                defaultLicenseAvailable = true
            }
        }

        if (defaultLicenseState == LicenseState.Locked.name) {
            logger.warn("Default license is locked! Unlocking ...")
            unlockLicenseInDatabase(id = defaultLicenseId)
        }

        if (!defaultLicenseAvailable) {
            logger.warn("Default license isn't available! Creating...")
            val defaultLicense = DefaultLicense().create(isActivated = false)
            database.addLicenseToDatabase(license = defaultLicense)
        }
    }

    /**
     * Checks whether a license is activated or not. If not, the default license will be activated.
     *
     * @return  void
     */
    private fun checkForActiveLicense() {
        val resultSet = getAllRecords()
        var isLicenseAlreadyActivated = false
        var deactivateAllLicenses = false

        while (resultSet.next()) {
            val state = resultSet.getString(LicenseInformation.State.name)

            if (state == LicenseState.Active.name) {
                if (isLicenseAlreadyActivated) {
                    deactivateAllLicenses = true
                } else {
                    isLicenseAlreadyActivated = true
                }
            }
        }

        if (deactivateAllLicenses || !isLicenseAlreadyActivated) {
            database.activateDefaultLicenseInDatabase()
        }
    }

    /**
     * Checks whether expiration date is later than the current date.
     *
     * @param   currentDate        Actual date. List with format yyyy/mm/dd
     * @param   expirationDate     Expiration date of license. List with format yyyy/mm/dd
     * @return  Boolean whether license is expired or not
     */
    private fun checkExpirationTime(currentDate: List<String>, expirationDate: List<String>): Boolean {
        if (currentDate.first().toInt() > expirationDate.first().toInt()) {
            return false
        } else if (currentDate.first().toInt() < expirationDate.first().toInt()) {
            return true
        }

        if (currentDate[1].toInt() > expirationDate[1].toInt()) {
            return false
        }

        if (currentDate.last().toInt() > expirationDate.last().toInt()) {
            return false
        }

        return true
    }

    /**
     * Checks whether license is available in database or not.
     *
     * @param   licenseId        License if of license to check if available in database
     * @return  Boolean whether license is available in database or not
     */
    private fun checkWhetherLicenseExistInDatabase(licenseId: Int): Boolean {
        val resultSet = getAllRecords()

        while (resultSet.next()) {
            if (resultSet.getInt(LicenseInformation.Id.name) == licenseId) {
                return true
            }
        }

        return false
    }

    @Synchronized
    override fun showAllLicensesInDatabase() = database.showAllLicensesInDatabase()

    /**
     * Returns all licenses saved in the database formatted as JSONArray.
     *
     * @return  JSONObject of the currently active license saved in the database
     */
    @Synchronized
    override fun getActiveLicense(): List<LicenseObject> {
        val resultSet = getAllRecords()

        while (resultSet.next()) {
            val state = when (resultSet.getString(LicenseInformation.State.name)) {
                LicenseState.Active.name -> LicenseState.Active
                LicenseState.Inactive.name -> LicenseState.Inactive
                else -> LicenseState.Locked
            }

            if (state == LicenseState.Active) {
                val id = resultSet.getInt(LicenseInformation.Id.name)
                val createdAt = resultSet.getString(LicenseInformation.CreatedAt.name)
                val isDefault = resultSet.getBoolean(LicenseInformation.IsDefault.name)
                val expiresAt = resultSet.getString(LicenseInformation.ExpiresAt.name)

                val type = when (resultSet.getString(LicenseInformation.Type.name)) {
                    LicenseType.Default.name -> LicenseType.Default
                    LicenseType.Extended.name -> LicenseType.Extended
                    LicenseType.Advanced.name -> LicenseType.Advanced
                    LicenseType.Full.name -> LicenseType.Full
                    else -> LicenseType.Basic
                }

                logger.info("Active license: $id")

                val license = LicenseObject().create(
                            id = id,
                            type = type.name,
                            createdAt = createdAt,
                            expiresAt = expiresAt,
                            state = state.name,
                            isDefault = isDefault,
                            serialNumber = MasterProperties.getSerial()
                )

                return mutableListOf(license)
            }
        }

        logger.info("No license active. Activate default license ...")

        val defaultLicense = DefaultLicense().create(isActivated = true)
        database.addLicenseToDatabase(license = defaultLicense)
        database.activateDefaultLicenseInDatabase()
        return mutableListOf(defaultLicense)
    }

    /**
     * Returns all licenses saved in the database formatted as JSONArray.
     *
     * @return  JSONArray of all licenses saved in the database
     */
    @Synchronized
    override fun getAllLicenses(): List<LicenseObject> {
        val licenses = mutableListOf<LicenseObject>()
        val resultSet = getAllRecords()

        while (resultSet.next()) {
            val id = resultSet.getInt(LicenseInformation.Id.name)
            val createdAt = resultSet.getString(LicenseInformation.CreatedAt.name)
            val isDefault = resultSet.getBoolean(LicenseInformation.IsDefault.name)
            val expiresAt = resultSet.getString(LicenseInformation.ExpiresAt.name)

            val type = when (resultSet.getString(LicenseInformation.Type.name)) {
                LicenseType.Default.name -> LicenseType.Default
                LicenseType.Extended.name -> LicenseType.Extended
                LicenseType.Advanced.name -> LicenseType.Advanced
                LicenseType.Full.name -> LicenseType.Full
                else -> LicenseType.Basic
            }

            val state = when (resultSet.getString(LicenseInformation.State.name)) {
                LicenseState.Active.name -> LicenseState.Active
                LicenseState.Inactive.name -> LicenseState.Inactive
                else -> LicenseState.Locked
            }

            val license = LicenseObject().create(
                    id = id,
                    type = type.name,
                    createdAt = createdAt,
                    expiresAt = expiresAt,
                    state = state.name,
                    isDefault = isDefault,
                    serialNumber = MasterProperties.getSerial()
            )

            logger.info("License $id stored in database")
            licenses.add(license)
        }

        return licenses
    }

    /**
     * Adds a license to the database and checks for licenses with the same id and its expiration date. If it's expired
     * this license will be locked again.
     *
     * @param   license        License object of license to be added
     * @return  void
     */
    @Synchronized
    override fun addLicense(license: LicenseObject) {
        if (checkWhetherLicenseExistInDatabase(licenseId = license.id)) {
            return logger.error("License ${license.id} already added!")
        }

        if (checkSerialNumber(license.serialNumber)) {
            return logger.error("Serial number ${license.serialNumber} doesn't match!")
        }

        if (license.id.toString().length != 7) {
            return logger.error("License ID length must be 7 digits. This id has ${license.id.toString().length} digits!")
        }

        database.addLicenseToDatabase(license = license)
        checkLicensesForExpiration()
    }

    /**
     * Extends the expiration date of a non-default license and checks its expiration date. If it's expired this license
     * will be locked again.
     *
     * @param   licenseId        Id of license of which the expiration date shall be changed
     * @param   expirationDate   New expiration date
     * @return  void
     */
    @Synchronized
    override fun extendExpirationDateOfLicense(licenseId: Int, expirationDate: String) {
        if (!checkWhetherLicenseExistInDatabase(licenseId = licenseId)) {
            return logger.warn("License doesn't exist in database")
        }

        val resultSet = database.getRecordById(id = licenseId)

        while (resultSet.next()) {
            val isDefault = resultSet.getBoolean(LicenseInformation.IsDefault.name)

            if (isDefault) {
                return logger.warn("Expiration date of default license cannot be changed")
            }
        }

        database.extendExpirationDateOfLicenseInDatabase(id = licenseId, expirationDate = expirationDate)
        checkLicensesForExpiration()
    }

    /**
     * Locks a non-default license in the database. If it's currently activated, the default license will be activated.
     * again.
     *
     * @param   licenseId        Id of license to be locked
     * @return  void
     */
    @Synchronized
    override fun lockLicense(licenseId: Int) {
        if (!checkWhetherLicenseExistInDatabase(licenseId = licenseId)) {
            return logger.warn("License doesn't exist in database")
        }

        val resultSet = getAllRecords()
        var activateDefaultLicense = false

        while (resultSet.next()) {
            val id = resultSet.getInt(LicenseInformation.Id.name)
            val isDefault = resultSet.getBoolean(LicenseInformation.IsDefault.name)

            if (id == licenseId) {
                val state = resultSet.getString(LicenseInformation.State.name)

                if (state == LicenseState.Locked.name) {
                    return logger.warn("License $id is already unlocked")
                }

                if (isDefault) {
                    return logger.warn("Default license cannot be locked")
                }

                if (state == LicenseState.Active.name) {
                    activateDefaultLicense = true
                }
            }
        }

        lockLicenseInDatabase(id = licenseId)

        if (activateDefaultLicense) {
            activateDefaultLicenseInDatabase()
        }
    }

    /**
     * Locks all licenses in the database and activates the default license
     *
     * @return  void
     */
    @Synchronized
    override fun lockAllLicenses() {
        val resultSet = getAllRecords()
        val ids = arrayListOf<Int>()

        while (resultSet.next()) {
            val id = resultSet.getInt(LicenseInformation.Id.name)
            val isDefault = resultSet.getBoolean(LicenseInformation.IsDefault.name)
            val state = resultSet.getString(LicenseInformation.State.name)

            if (!isDefault && state != LicenseState.Locked.name) {
                ids.add(id)
            }
        }

        ids.forEach{ lockLicenseInDatabase(id = it) }

        activateDefaultLicenseInDatabase()
        logger.info("All licenses locked. Default license activated")
    }

    /**
     * Unlocks a license in the database and checks its expiration date. If it's expired this license will be locked
     * again.
     *
     * @param   licenseId        Id of license to be unlocked
     * @return  void
     */
    @Synchronized
    override fun unlockLicense(licenseId: Int) {
        if (!checkWhetherLicenseExistInDatabase(licenseId = licenseId)) {
            return logger.warn("License doesn't exist in database")
        }

        val resultSet = getAllRecords()

        while (resultSet.next()) {
            val id = resultSet.getInt(LicenseInformation.Id.name)
            val state = resultSet.getString(LicenseInformation.State.name)

            if (id == licenseId) {
                if (state != LicenseState.Locked.name) {
                    return logger.warn("License $id is already unlocked")
                }
            }
        }

        unlockLicenseInDatabase(id = licenseId)
        checkLicensesForExpiration()
    }

    /**
     * Deactivates an active license in the database. The default license will be activated.
     *
     * @param   licenseId        Id of license to be deactivated
     * @return  void
     */
    @Synchronized
    override fun deactivateLicense(licenseId: Int) {
        if (!checkWhetherLicenseExistInDatabase(licenseId = licenseId)) {
            return logger.warn("License $licenseId doesn't exist in database")
        }

        val resultSet = getRecordById(id = licenseId)

        while (resultSet.next()) {
            val id = resultSet.getInt(LicenseInformation.Id.name)

            if (licenseId == id) {
                val state = resultSet.getString(LicenseInformation.State.name)

                if (state != LicenseState.Active.name) {
                    return logger.warn("License $licenseId isn't activated")
                }
            }
        }

        deactivateLicenseInDatabase(id = licenseId)
        checkForActiveLicense()
    }

    /**
     * Activates a license in the database. Other active licenses will be deactivated. A license can only be activated
     * when it's not locked.
     *
     * @param   licenseId        Id of license to be activated
     * @return  void
     */
    @Synchronized
    override fun activateLicense(licenseId: Int) {
        if (!checkWhetherLicenseExistInDatabase(licenseId = licenseId)) {
            return logger.warn("License $licenseId doesn't exist in database")
        }

        val licensesToDeactivate = mutableListOf<Int>()
        val resultSet = getAllRecords()

        while (resultSet.next()) {
            val id = resultSet.getInt(LicenseInformation.Id.name)
            val state = resultSet.getString(LicenseInformation.State.name)

            if (state == LicenseState.Active.name) {
                licensesToDeactivate.add(id)
            }
        }

        licensesToDeactivate.forEach{
            deactivateLicenseInDatabase(id = it)
        }

        activateLicenseInDatabase(id = licenseId)
    }

    /**
     * Deletes a non-default license in the database. If an active license should be deleted, the default license will
     * be activated.
     *
     * @param   licenseId        Id of license to be deleted
     * @return  void
     */
    @Synchronized
    override fun deleteLicense(licenseId: Int) {
        if (!checkWhetherLicenseExistInDatabase(licenseId = licenseId)) {
            return logger.warn("License $licenseId doesn't exist in database")
        }

        val resultSet = getRecordById(id = licenseId)
        var isActiveLicense = false

        while (resultSet.next()) {
            val isDefault = resultSet.getBoolean(LicenseInformation.IsDefault.name)
            val state = resultSet.getString(LicenseInformation.State.name)

            if (isDefault) {
                return logger.warn("Default license cannot be deleted")
            }

            if (state == LicenseState.Active.name) {
                isActiveLicense = true
            }
        }

        deleteLicenseInDatabase(id = licenseId)

        if (isActiveLicense) {
            activateDefaultLicenseInDatabase()
        }
    }
}