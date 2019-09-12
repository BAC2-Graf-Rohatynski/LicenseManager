package licensemanager.command.interfaces

import org.json.JSONArray

interface ICommandSocket {
    fun send(message: JSONArray)
}