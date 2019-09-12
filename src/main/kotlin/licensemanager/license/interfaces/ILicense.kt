package licensemanager.license.interfaces

import enumstorage.license.LicenseState
import enumstorage.license.LicenseType
import org.json.JSONObject

interface ILicense {
    fun getId(): Int
    fun getType(): LicenseType
    fun getCreateDate(): String
    fun getExpirationDate(): String
    fun getState(): LicenseState
    fun getDefaultState(): Boolean
    fun getSerialNumber(): String
    fun getJsonObject(): JSONObject
}