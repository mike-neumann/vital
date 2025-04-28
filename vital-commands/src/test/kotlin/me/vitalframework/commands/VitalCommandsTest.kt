package me.vitalframework.commands

import org.bukkit.Material
import org.junit.jupiter.api.*

class VitalCommandsTest {
    @Test
    fun `arg handler return type mapping should fail`() {
        assertThrows<VitalCommandException.InvalidArgHandlerReturnSignature> {
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgHandler(Arg("testArg"))
                fun onTestArg() {
                }
            }
        }
    }

    @Test
    fun `arg handler parameter mapping should fail`() {
        assertThrows<VitalCommandException.InvalidArgHandlerParameterSignature> {
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgHandler(Arg("testArg"))
                fun onTestArg(i: Int) = ReturnState.SUCCESS
            }
        }
    }

    @Test
    fun `arg handler return type mapping should work`() {
        assertDoesNotThrow {
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgHandler(Arg("testArg"))
                fun onTestArg() = ReturnState.SUCCESS
            }
        }
    }

    @Test
    fun `arg handler parameter mapping should work`() {
        assertDoesNotThrow {
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgHandler(Arg("testArg"))
                fun onTestArg(sender: CommandSender, executedArg: String, commandArg: Arg, values: Array<String>) =
                    ReturnState.SUCCESS
            }
        }
    }

    @Test
    fun `arg handler parameter mapping with variables should work`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(Arg("testArg %PLAYER%"))
            fun onTestArg(sender: CommandSender, executedArg: String, commandArg: Arg, values: Array<String>): ReturnState {
                sender.sendMessage(values.contentToString())
                return ReturnState.SUCCESS
            }
        }
        val sender = VitalTestCommand.Player()

        testCommand.execute(sender, arrayOf("testArg", "xRa1ny"))
        println(sender.messages)
        assert(sender.messages.size == 1)
        assert(sender.messages.component1() == "[xra1ny]")
    }

    @Test
    fun `arg exception handler mapping should fail`() {
        assertThrows<VitalCommandException.UnmappedArgExceptionHandlerArg> {
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgExceptionHandler("testArg", Exception::class)
                fun onTestArgException() = ReturnState.SUCCESS
            }
        }
    }

    @Test
    fun `arg exception handler mapping should work`() {
        assertDoesNotThrow {
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgHandler(Arg("testArg"))
                fun onTestArg() = ReturnState.SUCCESS

                @ArgExceptionHandler("testArg", type = Exception::class)
                fun onTestArgException() = ReturnState.SUCCESS
            }
        }
    }

    @Test
    fun `arg exception handler parameter mapping should fail`() {
        assertThrows<VitalCommandException.InvalidArgExceptionHandlerMethodSignature> {
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgHandler(Arg("testArg"))
                fun onTestArg() = ReturnState.SUCCESS

                @ArgExceptionHandler("testArg", RuntimeException::class)
                fun onTestArgException(i: Int) = ReturnState.SUCCESS
            }
        }
    }

    @Test
    fun `arg exception handler parameter mapping should work`() {
        assertDoesNotThrow {
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgHandler(Arg("testArg"))
                fun onTestArg() = ReturnState.SUCCESS

                @ArgExceptionHandler("testArg", RuntimeException::class)
                fun onTestArgException(sender: CommandSender, executedArg: String, commandArg: Arg, e: RuntimeException) =
                    ReturnState.SUCCESS
            }
        }
    }

//    TODO: cannot test global command exception handlers yet
//     tests are not spring boot tests, also command advices are created dynamically in testcases
//    @Test
//    fun `global command exception handler parameter mapping should fail`() {
//        assertThrows<VitalCommandException.InvalidGlobalExceptionHandlerMethodSignature> {
//            @VitalCommand.Advice(VitalTestCommand.CommandSender::class)
//            object {
//                @VitalCommand.GlobalExceptionHandler(Exception::class)
//                fun onException(i: Int) {
//                }
//            }
//        }
//    }
//
//    @Test
//    fun `global command exception handler parameter mapping should work`() {
//        assertDoesNotThrow {
//            @VitalCommand.Advice(VitalTestCommand.CommandSender::class)
//            object {
//                @VitalCommand.GlobalExceptionHandler(Exception::class)
//                fun onException(sender: VitalTestCommand.CommandSender, executedArg: String, commandArg: VitalCommand.Arg?, e: Exception) {
//                }
//            }
//        }
//    }

    @Test
    fun `base command should be executed`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            override fun onBaseCommand(sender: CommandSender): ReturnState {
                sender.sendMessage("onBaseCommand")
                return ReturnState.SUCCESS
            }
        }
        val sender = VitalTestCommand.Player()

        testCommand.execute(sender, arrayOf(""))

        assert(sender.messages.size == 1)
        assert(sender.messages.component1() == "onBaseCommand")
    }

    @Test
    fun `arg handler should be executed`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(Arg("testArg"))
            fun onTestArg(sender: CommandSender): ReturnState {
                sender.sendMessage("onTestArg")
                return ReturnState.SUCCESS
            }
        }
        val sender = VitalTestCommand.Player()

        testCommand.execute(sender, arrayOf("testArg"))

        assert(sender.messages.size == 1)
        assert(sender.messages.component1() == "onTestArg")
    }

    @Test
    fun `arg exception handler should not be executed`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(Arg("testArg"))
            fun onTestArg(sender: CommandSender) = ReturnState.SUCCESS

            @ArgExceptionHandler("testArg", type = Exception::class)
            fun onTestArgException(sender: CommandSender): ReturnState {
                sender.sendMessage("onTestArgException")
                return ReturnState.SUCCESS
            }
        }
        val sender = VitalTestCommand.Player()

        testCommand.execute(sender, arrayOf("testArg"))

        assert(sender.messages.isEmpty())
    }

    @Test
    fun `arg exception handler should be executed`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(arg = Arg("testArg"))
            fun onTestArg(): ReturnState {
                throw RuntimeException("test exception")
            }

            @ArgExceptionHandler("testArg", type = RuntimeException::class)
            fun onTestArgException(sender: CommandSender): ReturnState {
                sender.sendMessage("onTestArgException")
                return ReturnState.SUCCESS
            }
        }
        val sender = VitalTestCommand.Player()

        testCommand.execute(sender, arrayOf("testArg"))

        assert(sender.messages.size == 1)
        assert(sender.messages.component1() == "onTestArgException")
    }

    @Test
    fun `tab completions should be empty`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(arg = Arg("testArg"))
            fun onTestArg() = ReturnState.SUCCESS
        }
        val sender = VitalTestCommand.Player()
        val completions = testCommand.tabComplete(sender, arrayOf("d"))

        assert(completions.isEmpty())
    }

    @Test
    fun `tab completions should contain arg`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(arg = Arg("testArg"))
            fun onTestArg() = ReturnState.SUCCESS
        }
        val sender = VitalTestCommand.Player()
        val tabCompletions = testCommand.tabComplete(sender, arrayOf("t"))

        assert(tabCompletions.size == 1)
        assert(tabCompletions.component1() == "testArg")
    }

    @Test
    fun `tab completions should contain sub arg`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(arg = Arg("testArg testSubArg"))
            fun onTestArg() = ReturnState.SUCCESS
        }
        val sender = VitalTestCommand.Player()
        val tabCompletions = testCommand.tabComplete(sender, arrayOf("testArg", ""))

        assert(tabCompletions.size == 1)
        assert(tabCompletions.component1() == "testSubArg")
    }

    @Test
    fun `tab completions should contain player name`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(arg = Arg("%PLAYER%"))
            fun onTestArg() = ReturnState.SUCCESS
        }
        val sender = VitalTestCommand.Player()
        val tabCompletions = testCommand.tabComplete(sender, arrayOf(""))
        // 2 since we also have %PLAYER% in the completions
        assert(tabCompletions.size == 2)
        assert("xRa1ny" in tabCompletions)
    }

    @Test
    fun `tab completions should contain boolean`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(arg = Arg("%BOOLEAN%"))
            fun onTestArg() = ReturnState.SUCCESS
        }
        val sender = VitalTestCommand.Player()
        val tabCompletions = testCommand.tabComplete(sender, arrayOf(""))
        // 3 since we also have %BOOLEAN% in the completions
        assert(tabCompletions.size == 3)
        assert("true" in tabCompletions)
        assert("false" in tabCompletions)
    }

    @Test
    fun `tab completions should contain number`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(arg = Arg("%NUMBER%"))
            fun onTestArg() = ReturnState.SUCCESS
        }
        val sender = VitalTestCommand.Player()
        val tabCompletions = testCommand.tabComplete(sender, arrayOf(""))
        // 2 since we also have %NUMBER% in the tab-completions
        assert(tabCompletions.size == 2)
        assert("0" in tabCompletions)
    }

    @Test
    fun `tab completions should contain all materials`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(arg = Arg("%MATERIAL%"))
            fun onTestArg() = ReturnState.SUCCESS
        }
        val sender = VitalTestCommand.Player()
        val tabCompletions = testCommand.tabComplete(sender, arrayOf(""))
        // +1 since we also have %MATERIAL% in the completions
        assert(tabCompletions.size == Material.entries.size + 1)

        for (material in Material.entries) {
            assert(material.name in tabCompletions)
        }
    }

    @Test
    fun `arg handler parameter matching placeholder should be executed`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(Arg("testArg %PLAYER%"))
            fun onTestArg(sender: CommandSender, values: Array<String>): ReturnState {
                sender.sendMessage(values.contentToString())
                return ReturnState.SUCCESS
            }
        }
        val sender = VitalTestCommand.Player()

        testCommand.execute(sender, arrayOf("testArg", "%PLAYER%"))
        println(sender.messages)
        assert(sender.messages.size == 1)
        assert(sender.messages.component1() == "[%player%]")
    }
}