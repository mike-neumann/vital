package me.vitalframework.commands

import org.bukkit.Material
import org.junit.jupiter.api.*

class VitalCommandsTest {
    @Test
    fun `arg handler parameter mapping should fail`() {
        assertThrows<VitalCommandException.InvalidArgHandlerSignature> {
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgHandler(Arg("testArg"))
                fun onTestArg(i: Int) = ReturnState.SUCCESS
            }
        }
    }

    @Test
    fun `arg handler parameter mapping should work`() {
        assertDoesNotThrow {
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgHandler(Arg("testArg"))
                fun onTestArg() = ReturnState.SUCCESS
            }
        }
    }

    @Test
    fun `arg exception handler parameter mapping should fail`() {
        assertThrows<VitalCommandException.InvalidArgExceptionHandlerSignature> {
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgHandler(Arg("testArg"))
                fun onTestArg() = ReturnState.SUCCESS

                @ArgExceptionHandler("testArg", type = RuntimeException::class)
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

                @ArgExceptionHandler("testArg", type = RuntimeException::class)
                fun onTestArgException() = ReturnState.SUCCESS
            }
        }
    }

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
    fun `command arg should be executed`() {
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
    fun `arg exception handler mapping should fail`() {
        assertThrows<VitalCommandException.UnmappedArgExceptionHandlerArg> {
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgExceptionHandler("testArg", type = Exception::class)
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
            @ArgHandler(Arg("testArg"))
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
            @ArgHandler(Arg("testArg"))
            fun onTestArg() = ReturnState.SUCCESS
        }
        val sender = VitalTestCommand.Player()
        val completions = testCommand.tabComplete(sender, arrayOf("d"))

        assert(completions.isEmpty())
    }

    @Test
    fun `tab completions should contain arg`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(Arg("testArg"))
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
            @ArgHandler(Arg("testArg testSubArg"))
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
            @ArgHandler(Arg("%PLAYER%"))
            fun onTestArg() = ReturnState.SUCCESS
        }
        val sender = VitalTestCommand.Player()
        val tabCompletions = testCommand.tabComplete(sender, arrayOf(""))

        assert(tabCompletions.size == 1)
        assert(tabCompletions.component1() == "xRa1ny")
    }

    @Test
    fun `tab completions should contain boolean`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(Arg("%BOOLEAN%"))
            fun onTestArg() = ReturnState.SUCCESS
        }
        val sender = VitalTestCommand.Player()
        val tabCompletions = testCommand.tabComplete(sender, arrayOf(""))

        assert(tabCompletions.size == 2)
        assert(tabCompletions.component1() == "true")
        assert(tabCompletions.component2() == "false")
    }

    @Test
    fun `tab completions should contain number`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(Arg("%NUMBER%"))
            fun onTestArg() = ReturnState.SUCCESS
        }
        val sender = VitalTestCommand.Player()
        val tabCompletions = testCommand.tabComplete(sender, arrayOf(""))

        assert(tabCompletions.size == 1)
        assert(tabCompletions.component1() == "0")
    }

    @Test
    fun `tab completions should contain all materials`() {
        val testCommand = @VitalCommand.Info("testCommand") object : VitalTestCommand() {
            @ArgHandler(Arg("%MATERIAL%"))
            fun onTestArg() = ReturnState.SUCCESS
        }
        val sender = VitalTestCommand.Player()
        val tabCompletions = testCommand.tabComplete(sender, arrayOf(""))

        assert(tabCompletions.size == Material.entries.size)
        tabCompletions.zip(Material.entries) { a, b -> assert(a == b.name) }
    }
}