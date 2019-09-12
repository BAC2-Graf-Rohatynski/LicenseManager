package licensemanager.license.interfaces

import apibuilder.license.objects.LicenseObject

interface IDefaultLicense {
    fun create(isActivated: Boolean): LicenseObject
}