package com.faendir.zachtronics.bot.discord.commands.arg

import com.faendir.zachtronics.bot.discord.commands.ArgumentIntermediateMap
import com.faendir.zachtronics.bot.utils.ResultOrMessage

interface SimpleArgument<T : Any> : Argument<T, T> {
    override fun getActualResult(args: ArgumentIntermediateMap): ResultOrMessage<T> = ResultOrMessage.Success(args[this]!!)}