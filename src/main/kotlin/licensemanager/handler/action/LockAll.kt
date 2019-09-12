package licensemanager.handler.action

import apibuilder.license.LockAllLicensesItem
import apibuilder.license.objects.LicenseObject
import enumstorage.license.LicenseDatabaseCommand
import licensemanager.handler.LicenseHandler
import licensemanager.handler.interfaces.ICommandHandlerAction
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LockAll: ICommandHandlerAction {
    private val logger: Logger = LoggerFactory.getLogger(LockAll::class.java)
    private lateinit var item: LockAllLicensesItem

    @Synchronized
    override fun run(): List<LicenseObject>? {
        logger.info("Command '${LicenseDatabaseCommand.LockAll.name}' will be executed ...")
        LicenseHandler.lockAllLicenses()
        return null
    }

    @Synchronized
    override fun build(message: JSONArray): ICommandHandlerAction {
        item = LockAllLicensesItem().toObject(message = message)
        return this
    }
}