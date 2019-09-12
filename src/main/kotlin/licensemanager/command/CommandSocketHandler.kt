package licensemanager.command

import apibuilder.license.response.ResponseItem
import licensemanager.LicenseManagerRunner
import licensemanager.command.interfaces.ICommandSocketHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import propertystorage.PortProperties
import java.lang.Exception
import java.net.ServerSocket
import kotlin.concurrent.thread

object CommandSocketHandler: ICommandSocketHandler {
    private lateinit var serverSocket: ServerSocket
    private lateinit var clientSocket: CommandSocket
    private val port: Int = PortProperties.getLicensePort()
    private val logger: Logger = LoggerFactory.getLogger(CommandSocketHandler::class.java)

    init {
        thread {
            try {
                openSockets()
                acceptClients()
            } catch (ex: Exception) {
                logger.error("Error occurred while running socket handler!\n${ex.message}")
            } finally {
                closeSockets()
            }
        }
    }

    @Synchronized
    override fun sendResponseMessage(response: ResponseItem) {
        if (::clientSocket.isInitialized) {
            clientSocket.send(message = response.toJson())
        } else {
            logger.warn("Client socket isn't initialized yet!")
        }
    }

    private fun acceptClients() {
        while (LicenseManagerRunner.isRunnable()) {
            logger.info("Waiting for clients ...")
            clientSocket = CommandSocket(clientSocket = serverSocket.accept())
            clientSocket.start()
            logger.info("Client added")
        }
    }

    private fun openSockets() {
        logger.info("Opening socket on port '$port' ...")
        serverSocket = ServerSocket(port)
        logger.info("Socket opened")
    }

    @Synchronized
    override fun closeSockets() {
        try {
            logger.info("Closing sockets ...")

            if (::serverSocket.isInitialized) {
                serverSocket.close()
            }

            logger.info("Sockets closed")
        } catch (ex: Exception) {
            logger.error("Error occurred while closing sockets!\n${ex.message}")
        }
    }
}