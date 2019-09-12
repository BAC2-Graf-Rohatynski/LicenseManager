package licensemanager.handler.action

import apibuilder.license.LockLicenseItem
import apibuilder.license.objects.LicenseObject
import enumstorage.license.LicenseDatabaseCommand
import licensemanager.handler.LicenseHandler
import licensemanager.handler.interfaces.ICommandHandlerAction
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Lock: ICommandHandlerAction {
    private val logger: Logger = LoggerFactory.getLogger(Lock::class.java)
    private lateinit var item: LockLicenseItem

    @Synchronized
    override fun run(): List<LicenseObject>? {
        logger.info("Command '${LicenseDatabaseCommand.Lock.name}' will be executed ...")
        LicenseHandler.lockLicense(licenseId = item.id)
        return null
    }

    @Synchronized
    override fun build(message: JSONArray): ICommandHandlerAction {
        item = LockLicenseItem().toObject(message = message)
        return this
    }
}