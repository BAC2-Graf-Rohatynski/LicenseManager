package licensemanager.handler.action

import apibuilder.license.DeleteLicenseItem
import apibuilder.license.objects.LicenseObject
import enumstorage.license.LicenseDatabaseCommand
import licensemanager.handler.LicenseHandler
import licensemanager.handler.interfaces.ICommandHandlerAction
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Delete: ICommandHandlerAction {
    private val logger: Logger = LoggerFactory.getLogger(Delete::class.java)
    private lateinit var item: DeleteLicenseItem

    @Synchronized
    override fun run(): List<LicenseObject>? {
        logger.info("Command '${LicenseDatabaseCommand.Delete.name}' will be executed ...")
        LicenseHandler.deleteLicense(licenseId = item.id)
        return null
    }

    @Synchronized
    override fun build(message: JSONArray): ICommandHandlerAction {
        item = DeleteLicenseItem().toObject(message = message)
        return this
    }
}