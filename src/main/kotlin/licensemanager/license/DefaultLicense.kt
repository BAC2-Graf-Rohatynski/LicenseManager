package licensemanager.license

import apibuilder.license.objects.LicenseObject
import enumstorage.license.LicenseState
import enumstorage.license.LicenseType
import licensemanager.license.interfaces.IDefaultLicense
import propertystorage.MasterProperties
import java.util.*

class DefaultLicense: IDefaultLicense {
    override fun create(isActivated: Boolean): LicenseObject {
        return LicenseObject().create(
                id = (1000000 + Random().nextInt(9000000)),
                type = LicenseType.Default.name,
                createdAt = "2019/01/01",
                expiresAt = "2099/12/31",
                state = if (isActivated) LicenseState.Active.name else LicenseState.Inactive.name,
                isDefault = true,
                serialNumber = MasterProperties.getSerial()
        )
    }
}