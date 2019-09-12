package licensemanager.handler.interfaces

import apibuilder.license.objects.LicenseObject

interface ILicenseHandler {
    fun showAllLicensesInDatabase()
    fun getActiveLicense(): List<LicenseObject>
    fun getAllLicenses(): List<LicenseObject>
    fun addLicense(license: LicenseObject)
    fun extendExpirationDateOfLicense(licenseId: Int, expirationDate: String)
    fun lockLicense(licenseId: Int)
    fun lockAllLicenses()
    fun unlockLicense(licenseId: Int)
    fun deactivateLicense(licenseId: Int)
    fun activateLicense(licenseId: Int)
    fun deleteLicense(licenseId: Int)
}