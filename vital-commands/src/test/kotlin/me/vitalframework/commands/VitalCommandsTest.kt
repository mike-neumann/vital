package me.vitalframework.commands

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

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
        val testCommand =
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                override fun onBaseCommand(sender: CommandSender): ReturnState {
                    sender.sendMessage("onBaseCommand")
                    return ReturnState.SUCCESS
                }
            }
        val sender = VitalTestCommand.Player()

        testCommand.execute(sender, arrayOf(""))

        assert(sender.messages.size == 1)
        assert(sender.messages.first() == "onBaseCommand")
    }

    @Test
    fun `command arg should be executed`() {
        val testCommand =
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
                @ArgHandler(Arg("testArg"))
                fun onTestArg(sender: CommandSender): ReturnState {
                    sender.sendMessage("onTestArg")
                    return ReturnState.SUCCESS
                }
            }
        val sender = VitalTestCommand.Player()

        testCommand.execute(sender, arrayOf("testArg"))

        assert(sender.messages.size == 1)
        assert(sender.messages.first() == "onTestArg")
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
        val testCommand =
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
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
        val testCommand =
            @VitalCommand.Info("testCommand")
            object : VitalTestCommand() {
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
        assert(sender.messages.first() == "onTestArgException")
    }
}