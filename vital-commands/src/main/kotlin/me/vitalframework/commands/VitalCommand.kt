package me.vitalframework.commands

import jakarta.annotation.PostConstruct
import me.vitalframework.*
import me.vitalframework.VitalClassUtils.getRequiredAnnotation
import me.vitalframework.commands.VitalCommandUtils.getMappedArgExceptionHandlers
import me.vitalframework.commands.VitalCommandUtils.getMappedArgHandlers
import me.vitalframework.commands.VitalCommandUtils.getMappedArgs
import net.md_5.bungee.api.ProxyServer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.regex.Pattern
import kotlin.reflect.KClass

abstract class VitalCommand<P, CS : Any> protected constructor(val plugin: P, val commandSenderClass: Class<CS>) {
    val name: String
    val permission: String
    val playerOnly: Boolean
    private val args: Map<Pattern, Arg>
    private val argHandlers: Map<Arg, ArgHandlerContext>
    private val argExceptionHandlers: Map<Arg, Map<Class<out Throwable>, ArgExceptionHandlerContext>>

    init {
        val info = getRequiredAnnotation<Info>()
        name = info.name
        permission = info.permission
        playerOnly = info.playerOnly
        args = getMappedArgs()
        argHandlers = getMappedArgHandlers()
        argExceptionHandlers = getMappedArgExceptionHandlers()
    }

    abstract fun isPlayer(commandSender: CS): Boolean
    abstract fun hasPermission(commandSender: CS, permission: String): Boolean
    abstract fun getAllPlayerNames(): List<String>
    internal fun getArg(executedArg: String) = args.entries.filter { it.key.matcher(executedArg).matches() }.map { it.value }.firstOrNull()

    private fun executeArgExceptionHandlerMethod(sender: CS, exception: Throwable, executedArg: String, commandArg: Arg) {
        val exceptionHandlers = argExceptionHandlers[commandArg] ?: emptyMap()
        // we may or may not have an exception handler mapped for this execution context
        val context = exceptionHandlers.entries.filter { it.key.isAssignableFrom(exception.javaClass) }.map { it.value }.firstOrNull()
        if (context == null) {
            // we do not have any exception handler mapped for this argument
            // try to find a global exception handler
            val globalContext = VitalGlobalCommandExceptionHandlerProcessor.getGlobalExceptionHandler(exception.javaClass)
                ?: return onCommandError(sender, commandArg, exception)

            try {
                globalContext.handlerMethod(
                    globalContext.adviceInstance,
                    *VitalCommandUtils.getInjectableGlobalExceptionHandlerMethodParameters(
                        globalContext,
                        sender,
                        executedArg,
                        commandArg,
                        exception
                    )
                )
                return
            } catch (e: Exception) {
                throw VitalCommandException.ExecuteGlobalExceptionHandlerMethod(globalContext.handlerMethod, globalContext, e)
            }
        }

        try {
            context.handlerMethod(
                this,
                *VitalCommandUtils.getInjectableArgExceptionHandlerMethodParameters(context, sender, executedArg, commandArg, exception)
            )
        } catch (e: Exception) {
            throw VitalCommandException.ExecuteArgExceptionHandlerMethod(context.handlerMethod, context, e)
        }
    }

    private fun executeArgHandlerMethod(sender: CS, executedArg: String, commandArg: Arg, values: Array<String>): ReturnState {
        val context = argHandlers[commandArg] ?: throw VitalCommandException.UnmappedArgHandler(executedArg)
        return context.handlerMethod(
            this,
            *VitalCommandUtils.getInjectableArgHandlerMethodParameters(context, sender, executedArg, commandArg, values)
        ) as ReturnState
    }

    fun tabComplete(sender: CS, args: Array<String>): List<String> {
        val tabCompleted = mutableListOf<String>()
        for ((_, commandArg) in this.args) {
            val splitCommandArg = commandArg.name.split(" ")
            // the player has entered more arguments than the command arg supports, we can never have a hit here
            if (args.size > splitCommandArg.size) continue
            var commandArgMatches = true
            for ((i, enteredArg) in args.withIndex()) {
                if (i > 0) {
                    // we have previous entered arguments, check if they match with the command arg, so we know we are on the right node
                    for (prevI in 0..<i) {
                        val previousEnteredArg = args[prevI]
                        val previousCommandArg = splitCommandArg[prevI].replace(VARARG_REGEX.toRegex(), previousEnteredArg)
                            .replace(ARG_REGEX.toRegex(), previousEnteredArg)
                        if (previousCommandArg != previousEnteredArg) {
                            // our previously entered arg does not match with the registered command arg
                            commandArgMatches = false
                            break
                        }
                    }

                    if (!commandArgMatches) {
                        // any previous token do not match, we can stop right here
                        break
                    }
                }
                val matchableCommandArg =
                    splitCommandArg[i].replace(VARARG_REGEX.toRegex(), enteredArg).replace(ARG_REGEX.toRegex(), enteredArg)

                if (!matchableCommandArg.startsWith(enteredArg)) {
                    commandArgMatches = false
                    break
                }

                commandArgMatches = true
            }

            if (commandArgMatches) {
                tabCompleted.add(splitCommandArg.subList(args.size - 1, splitCommandArg.size).joinToString(" "))
                // only the last element should be converted to argument type check to avoid confusing tab completions
                val argType = Arg.Type.getTypeByPlaceholder(splitCommandArg[args.size - 1])
                argType?.action?.invoke(TabCompletionContext(tabCompleted, getAllPlayerNames()))
                tabCompleted.addAll(onCommandTabComplete(sender, splitCommandArg.subList(0, args.size).joinToString(" ")))
            }
        }

        return tabCompleted
    }

    fun execute(sender: CS, args: Array<String>) {
        val joinedPlayerArgs = args.joinToString(" ")
        if (playerOnly && !isPlayer(sender)) return onCommandRequiresPlayer(sender, joinedPlayerArgs, null)
        if (permission.isNotBlank() && !hasPermission(sender, permission)) {
            return onCommandRequiresPermission(sender, joinedPlayerArgs, null)
        }
        val executingArg = getArg(joinedPlayerArgs)
        // if the player has not put in any arguments, we may execute the base command handler method
        val commandReturnState = when {
            executingArg == null && joinedPlayerArgs.isBlank() -> try {
                onBaseCommand(sender)
            } catch (e: Exception) {
                return onCommandError(sender, null, e)
            }

            executingArg != null -> {
                if (executingArg.permission.isNotBlank() && hasPermission(sender, executingArg.permission)) {
                    ReturnState.NO_PERMISSION
                } else if (executingArg.playerOnly && !isPlayer(sender)) {
                    ReturnState.ONLY_PLAYER
                } else {
                    val values = mutableListOf<String>()
                    val commandArgs = executingArg.name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.map { it.lowercase() }

                    args.map(String::lowercase).filterNot(commandArgs::contains).forEach(values::add)

                    try {
                        executeArgHandlerMethod(sender, joinedPlayerArgs, executingArg, values.toTypedArray())
                    } catch (e: Exception) {
                        return if (e is InvocationTargetException) {
                            executeArgExceptionHandlerMethod(sender, e.targetException, joinedPlayerArgs, executingArg)
                        } else {
                            executeArgExceptionHandlerMethod(sender, e, joinedPlayerArgs, executingArg)
                        }
                    }
                }
            }

            else -> ReturnState.INVALID_ARGS
        }
        when (commandReturnState) {
            ReturnState.SUCCESS -> run {}
            ReturnState.INVALID_ARGS -> onCommandInvalidArgs(sender, joinedPlayerArgs)
            ReturnState.NO_PERMISSION -> onCommandRequiresPermission(sender, joinedPlayerArgs, executingArg)
            ReturnState.ONLY_PLAYER -> onCommandRequiresPlayer(sender, joinedPlayerArgs, executingArg)
        }
    }

    protected open fun onCommandError(sender: CS, commandArg: Arg?, e: Throwable): Unit = run { throw e }
    protected open fun onBaseCommand(sender: CS) = ReturnState.INVALID_ARGS
    protected open fun onCommandTabComplete(sender: CS, args: String) = listOf<String>()
    protected open fun onCommandInvalidArgs(sender: CS, args: String) {}
    protected open fun onCommandRequiresPermission(sender: CS, args: String, commandArg: Arg?) {}
    protected open fun onCommandRequiresPlayer(sender: CS, args: String, commandArg: Arg?) {}

    enum class ReturnState {
        INVALID_ARGS, SUCCESS, NO_PERMISSION, ONLY_PLAYER
    }

    @Component
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Info(
        val name: String,
        val description: String = "A Vital Command",
        val aliases: Array<String> = [],
        val usage: String = "",
        val permission: String = "",
        val playerOnly: Boolean = true,
    )

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Arg(val name: String, val permission: String = "", val playerOnly: Boolean = false) {
        enum class Type(val placeholder: String, val action: (TabCompletionContext) -> Unit) {
            PLAYER(
                "%PLAYER%",
                { context -> context.playerNames.filter { it !in context.completions }.forEach { context.completions.add(it) } }
            ),
            BOOLEAN("%BOOLEAN%", { it.completions.addAll(arrayOf("true", "false")) }),
            NUMBER("%NUMBER%", { it.completions.add("0") }),
            MATERIAL("%MATERIAL%", { context -> Material.entries.map { it.name }.forEach { context.completions.add(it) } });

            companion object {
                fun getTypeByPlaceholder(placeholder: String) = entries.firstOrNull { it.placeholder == placeholder }
            }
        }
    }

    @Repeatable
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    annotation class ArgHandler(val arg: Arg)

    @Repeatable
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ArgExceptionHandler(val arg: String, val type: KClass<out Throwable>)

    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Advice(val commandSenderClass: KClass<out Any>)

    @Repeatable
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class GlobalExceptionHandler(val type: KClass<out Throwable>)

    data class GlobalExceptionHandlerContext(
        val adviceInstance: Any,
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val commandArgIndex: Int?,
        val valuesIndex: Int?,
        val exceptionIndex: Int?,
    )

    data class TabCompletionContext(val completions: MutableList<String>, val playerNames: List<String>)

    data class ArgHandlerContext(
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val commandArgIndex: Int?,
        val valuesIndex: Int?,
    )

    data class ArgExceptionHandlerContext(
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val argIndex: Int?,
        val exceptionIndex: Int?,
    )

    abstract class Spigot(plugin: SpigotPlugin) : VitalCommand<SpigotPlugin, SpigotCommandSender>(plugin, SpigotCommandSender::class.java),
        VitalPluginCommand.Spigot {
        @PostConstruct
        fun init() = plugin.getCommand(name)!!.setExecutor(this)

        override fun onCommand(sender: SpigotCommandSender, command: Command, label: String, args: Array<String>) =
            true.also { execute(sender, args) }

        override fun onTabComplete(sender: SpigotCommandSender, command: Command, label: String, args: Array<String>) =
            tabComplete(sender, args)

        override fun isPlayer(commandSender: SpigotCommandSender) = commandSender is SpigotPlayer
        override fun hasPermission(commandSender: SpigotCommandSender, permission: String) = commandSender.hasPermission(permission)
        override fun getAllPlayerNames() = Bukkit.getOnlinePlayers().map { it.name }
    }

    abstract class Bungee(plugin: BungeePlugin) : VitalCommand<BungeePlugin, BungeeCommandSender>(plugin, BungeeCommandSender::class.java) {
        private lateinit var command: VitalPluginCommand.Bungee

        @PostConstruct
        fun init() {
            setupCommand()
            plugin.proxy.pluginManager.registerCommand(plugin, command)
        }

        private fun setupCommand() {
            this.command = object : VitalPluginCommand.Bungee(name) {
                override fun execute(sender: BungeeCommandSender, args: Array<String>) = this@Bungee.execute(sender, args)
                override fun onTabComplete(sender: BungeeCommandSender, args: Array<String>) = this@Bungee.tabComplete(sender, args)
            }
        }

        override fun isPlayer(commandSender: BungeeCommandSender) = commandSender is BungeePlayer
        override fun hasPermission(commandSender: BungeeCommandSender, permission: String) = commandSender.hasPermission(permission)
        override fun getAllPlayerNames() = ProxyServer.getInstance().players.map { it.name }
    }

    companion object {
        const val SPACE_REGEX = " "
        const val VARARG_REGEX = "%\\S*%\\*"
        const val ARG_REGEX = "%\\S*%(?!\\*)"
        const val SPACE_REPLACEMENT = " "
        const val VARARG_REPLACEMENT = ".+"
        const val ARG_REPLACEMENT = "\\\\S+"
    }
}