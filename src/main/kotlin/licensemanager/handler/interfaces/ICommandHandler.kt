package licensemanager.handler.interfaces

import apibuilder.license.header.Header
import org.json.JSONArray

interface ICommandHandler {
    fun parseMessage(header: Header, message: JSONArray)
}