package licensemanager.handler.action

import apibuilder.license.UnlockLicenseItem
import apibuilder.license.objects.LicenseObject
import enumstorage.license.LicenseDatabaseCommand
import licensemanager.handler.LicenseHandler
import licensemanager.handler.interfaces.ICommandHandlerAction
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Unlock: ICommandHandlerAction {
    private val logger: Logger = LoggerFactory.getLogger(Unlock::class.java)
    private lateinit var item: UnlockLicenseItem

    @Synchronized
    override fun run(): List<LicenseObject>? {
        logger.info("Command '${LicenseDatabaseCommand.Unlock.name}' will be executed ...")
        LicenseHandler.unlockLicense(licenseId = item.id)
        return null
    }

    @Synchronized
    override fun build(message: JSONArray): ICommandHandlerAction {
        item = UnlockLicenseItem().toObject(message = message)
        return this
    }
}