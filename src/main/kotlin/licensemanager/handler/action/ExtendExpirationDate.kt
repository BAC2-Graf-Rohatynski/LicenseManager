package licensemanager.handler.action

import apibuilder.license.ExtendExpirationDateItem
import apibuilder.license.objects.LicenseObject
import enumstorage.license.LicenseDatabaseCommand
import licensemanager.handler.LicenseHandler
import licensemanager.handler.interfaces.ICommandHandlerAction
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ExtendExpirationDate: ICommandHandlerAction {
    private val logger: Logger = LoggerFactory.getLogger(ExtendExpirationDate::class.java)
    private lateinit var item: ExtendExpirationDateItem

    @Synchronized
    override fun run(): List<LicenseObject>? {
        logger.info("Command '${LicenseDatabaseCommand.Activate.name}' will be executed ...")
        LicenseHandler.extendExpirationDateOfLicense(licenseId = item.id, expirationDate = item.expiresAt)
        return null
    }

    @Synchronized
    override fun build(message: JSONArray): ICommandHandlerAction {
        item = ExtendExpirationDateItem().toObject(message = message)
        return this
    }
}