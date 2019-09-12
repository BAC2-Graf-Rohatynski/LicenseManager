package licensemanager.command.interfaces

import apibuilder.license.response.ResponseItem

interface ICommandSocketHandler {
    fun sendResponseMessage(response: ResponseItem)
    fun closeSockets()
}