package licensemanager.command

import apibuilder.license.header.HeaderBuilder
import licensemanager.LicenseManagerRunner
import licensemanager.command.interfaces.ICommandSocket
import licensemanager.handler.CommandHandler
import licensemanager.handler.LicenseHandler
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class CommandSocket(private val clientSocket: Socket): Thread(), ICommandSocket {
    private lateinit var printWriter: PrintWriter
    private lateinit var bufferedReader: BufferedReader
    private val logger: Logger = LoggerFactory.getLogger(CommandSocket::class.java)

    override fun run() {
        try {
            openSockets()
            receive()
        } catch (ex: Exception) {
            logger.error("Error occurred while running socket!\n${ex.message}")
        } finally {
            closeSockets()
        }
    }

    @Synchronized
    override fun send(message: JSONArray) {
        try {
            if (::printWriter.isInitialized) {
                printWriter.println(message.toString())
                logger.info("Message '$message' sent to system")
            } else {
                throw Exception("Print writer instance not started yet!")
            }
        } catch (ex: Exception) {
            logger.error("Error occurred while sending message!\n${ex.message}")
        }
    }

    private fun openSockets() {
        try {
            logger.info("Opening sockets ...")
            printWriter = PrintWriter(clientSocket.getOutputStream(), true)
            bufferedReader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            logger.info("Sockets opened")
        } catch (ex: Exception) {
            logger.error("Error occurred while opening sockets!\n${ex.message}")
        }
    }

    private fun receive() {
        logger.info("Hearing for messages ...")

        bufferedReader.use {
            while (LicenseManagerRunner.isRunnable()) {
                try {
                    val inputLine = bufferedReader.readLine()

                    if (inputLine != null) {
                        val message = JSONArray(inputLine)
                        logger.info("Message '$message' received ...")
                        val header = HeaderBuilder().build(message = message)
                        CommandHandler.parseMessage(message = message, header = header)
                        LicenseHandler.showAllLicensesInDatabase()
                    }
                } catch (ex: Exception) {
                    logger.error("Error occurred while parsing message!\n${ex.message}")
                }
            }
        }
    }

    private fun closeSockets() {
        try {
            logger.info("Closing sockets ...")

            if (::printWriter.isInitialized) {
                printWriter.close()
            }

            if (::bufferedReader.isInitialized) {
                bufferedReader.close()
            }

            if (!clientSocket.isClosed) {
                clientSocket.close()
            }

            logger.info("Sockets closed")
        } catch (ex: Exception) {
            logger.error("Error occurred while closing sockets!\n${ex.message}")
        }
    }
}