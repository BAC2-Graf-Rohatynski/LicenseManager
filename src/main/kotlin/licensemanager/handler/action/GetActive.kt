package licensemanager.handler.action

import apibuilder.license.GetActiveLicenseItem
import apibuilder.license.objects.LicenseObject
import enumstorage.license.LicenseDatabaseCommand
import licensemanager.handler.LicenseHandler
import licensemanager.handler.interfaces.ICommandHandlerAction
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object GetActive: ICommandHandlerAction {
    private val logger: Logger = LoggerFactory.getLogger(GetActive::class.java)
    private lateinit var item: GetActiveLicenseItem

    @Synchronized
    override fun run(): List<LicenseObject>? {
        logger.info("Command '${LicenseDatabaseCommand.Activate.name}' will be executed ...")
        return LicenseHandler.getActiveLicense()
    }

    @Synchronized
    override fun build(message: JSONArray): ICommandHandlerAction {
        item = GetActiveLicenseItem().toObject(message = message)
        return this
    }
}