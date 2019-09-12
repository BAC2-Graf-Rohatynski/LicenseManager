package licensemanager.handler.action

import apibuilder.license.GetAllLicensesItem
import apibuilder.license.objects.LicenseObject
import enumstorage.license.LicenseDatabaseCommand
import licensemanager.handler.LicenseHandler
import licensemanager.handler.interfaces.ICommandHandlerAction
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object GetAll: ICommandHandlerAction {
    private val logger: Logger = LoggerFactory.getLogger(GetAll::class.java)
    private lateinit var item: GetAllLicensesItem

    @Synchronized
    override fun run(): List<LicenseObject>? {
        logger.info("Command '${LicenseDatabaseCommand.Activate.name}' will be executed ...")
        return LicenseHandler.getAllLicenses()
    }

    @Synchronized
    override fun build(message: JSONArray): ICommandHandlerAction {
        item = GetAllLicensesItem().toObject(message = message)
        return this
    }
}