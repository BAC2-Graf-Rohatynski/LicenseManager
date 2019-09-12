package licensemanager.handler.action

import apibuilder.license.ActivateLicenseItem
import apibuilder.license.objects.LicenseObject
import enumstorage.license.LicenseDatabaseCommand
import licensemanager.handler.LicenseHandler
import licensemanager.handler.interfaces.ICommandHandlerAction
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Activate: ICommandHandlerAction {
    private val logger: Logger = LoggerFactory.getLogger(Activate::class.java)
    private lateinit var item: ActivateLicenseItem

    @Synchronized
    override fun run(): List<LicenseObject>? {
        logger.info("Command '${LicenseDatabaseCommand.Activate.name}' will be executed ...")
        LicenseHandler.activateLicense(licenseId = item.id)
        return null
    }

    @Synchronized
    override fun build(message: JSONArray): ICommandHandlerAction {
        item = ActivateLicenseItem().toObject(message = message)
        return this
    }
}