package licensemanager

import at.nlc.error.ErrorClientRunner
import enumstorage.update.ApplicationName
import licensemanager.command.CommandSocketHandler
import licensemanager.handler.LicenseHandler
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LicenseManagerRunner {
    private val logger: Logger = LoggerFactory.getLogger(LicenseManagerRunner::class.java)

    @Volatile
    private var runApplication = true

    fun start() {
        logger.info("Starting application")
        ErrorClientRunner
        LicenseHandler
        CommandSocketHandler
    }

    @Synchronized
    fun isRunnable(): Boolean = runApplication

    fun stop() {
        logger.info("Stopping application")
        runApplication = false

        CommandSocketHandler.closeSockets()
        ErrorClientRunner.stop()
    }

    fun getUpdateInformation(): JSONObject = UpdateInformation.getAsJson(applicationName = ApplicationName.License.name)
}