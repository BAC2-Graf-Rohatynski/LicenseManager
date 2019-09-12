package licensemanager.handler.interfaces

import apibuilder.license.objects.LicenseObject
import org.json.JSONArray

interface ICommandHandlerAction {
    fun run(): List<LicenseObject>?
    fun build(message: JSONArray): ICommandHandlerAction
}