package licensemanager

import org.apache.log4j.BasicConfigurator

fun main(args: Array<String>) {
    BasicConfigurator.configure()
    LicenseManagerRunner.start()
}