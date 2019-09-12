package licensemanager.handler.action

import apibuilder.license.AddLicenseItem
import apibuilder.license.objects.LicenseObject
import enumstorage.license.LicenseDatabaseCommand
import licensemanager.handler.LicenseHandler
import licensemanager.handler.interfaces.ICommandHandlerAction
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Add: ICommandHandlerAction {
    private val logger: Logger = LoggerFactory.getLogger(Add::class.java)
    private lateinit var item: AddLicenseItem

    @Synchronized
    override fun run(): List<LicenseObject>? {
        logger.info("Command '${LicenseDatabaseCommand.Activate.name}' will be executed ...")

        val license = LicenseObject().create(
                id = item.id,
                expiresAt = item.expiresAt,
                createdAt = item.createdAt,
                serialNumber = item.serialNumber,
                type = item.type,
                state = item.state,
                isDefault = false
        )

        LicenseHandler.addLicense(license = license)
        return null
    }

    @Synchronized
    override fun build(message: JSONArray): ICommandHandlerAction {
        item = AddLicenseItem().toObject(message = message)
        return this
    }
}