package me.vitalframework.commands

abstract class VitalTestCommand : VitalCommand<VitalTestCommand.CommandSender>(CommandSender::class.java) {
    class Player : CommandSender()

    open class CommandSender {
        val messages = mutableListOf<String>()

        fun sendMessage(message: String) {
            messages.add(message)
        }
    }

    override fun isPlayer(commandSender: CommandSender) = commandSender is Player

    override fun hasPermission(
        commandSender: CommandSender,
        permission: String,
    ) = true

    override fun getAllPlayerNames() = listOf("xRa1ny")
}
