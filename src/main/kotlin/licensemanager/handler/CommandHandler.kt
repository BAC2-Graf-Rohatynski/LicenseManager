package licensemanager.handler

import apibuilder.license.objects.LicenseObject
import apibuilder.license.response.ResponseItem
import apibuilder.license.header.Header
import enumstorage.license.LicenseDatabaseCommand
import licensemanager.command.CommandSocketHandler
import licensemanager.handler.action.*
import licensemanager.handler.interfaces.ICommandHandler
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CommandHandler: ICommandHandler {
    private val logger: Logger = LoggerFactory.getLogger(CommandHandler::class.java)

    @Synchronized
    override fun parseMessage(header: Header, message: JSONArray) {
        val response: ResponseItem = try {
            val licenses: List<LicenseObject>? = when (header.command) {
                LicenseDatabaseCommand.Activate.name -> Activate.build(message = message).run()
                LicenseDatabaseCommand.Deactivate.name -> Deactivate.build(message = message).run()
                LicenseDatabaseCommand.GetAllLicenses.name -> GetAll.build(message = message).run()
                LicenseDatabaseCommand.AddLicense.name -> Add.build(message = message).run()
                LicenseDatabaseCommand.ExtendExpirationDate.name -> ExtendExpirationDate.build(message = message).run()
                LicenseDatabaseCommand.Lock.name -> Lock.build(message = message).run()
                LicenseDatabaseCommand.LockAll.name -> LockAll.build(message = message).run()
                LicenseDatabaseCommand.Unlock.name -> Unlock.build(message = message).run()
                LicenseDatabaseCommand.Delete.name -> Delete.build(message = message).run()
                LicenseDatabaseCommand.GetActiveLicense.name -> GetActive.build(message = message).run()
                else -> throw Exception("Unknown command '${header.command}' received")
            }

            ResponseItem().create(message = message, licenses = licenses ?: listOf())
        } catch (ex: Exception) {
            logger.error("Error occurred while parsing message!\n${ex.message}")
            ResponseItem().create(message = message)
        }

        sendResponse(response = response)
    }

    private fun sendResponse(response: ResponseItem) {
        try {
            CommandSocketHandler.sendResponseMessage(response = response)
        } catch (ex: Exception) {
            logger.error("Error while sending response!\n${ex.message}")
        }
    }
}