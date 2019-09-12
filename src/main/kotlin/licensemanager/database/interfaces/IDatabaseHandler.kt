package licensemanager.database.interfaces

import apibuilder.license.objects.LicenseObject
import java.sql.ResultSet

interface IDatabaseHandler {
    fun showAllLicensesInDatabase()
    fun getRecordById(id: Int): ResultSet
    fun getAllRecords(): ResultSet
    fun lockLicenseInDatabase(id: Int)
    fun unlockLicenseInDatabase(id: Int)
    fun activateDefaultLicenseInDatabase()
    fun deactivateLicenseInDatabase(id: Int)
    fun activateLicenseInDatabase(id: Int)
    fun deleteLicenseInDatabase(id: Int)
    fun extendExpirationDateOfLicenseInDatabase(id: Int, expirationDate: String)
    fun addLicenseToDatabase(license: LicenseObject)
}