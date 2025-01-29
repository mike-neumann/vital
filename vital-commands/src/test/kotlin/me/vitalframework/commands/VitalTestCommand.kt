package me.vitalframework.commands

abstract class VitalTestCommand :
    VitalCommand<VitalTestCommand.Plugin, VitalTestCommand.CommandSender>(Plugin(), CommandSender::class.java) {
    class Plugin
    class Player : CommandSender()
    open class CommandSender {
        val messages = mutableListOf<String>()

        fun sendMessage(message: String) {
            messages.add(message)
        }
    }

    override fun isPlayer(commandSender: CommandSender) = commandSender is Player
    override fun hasPermission(commandSender: CommandSender, permission: String) = true
    override fun getAllPlayerNames() = listOf("")
}