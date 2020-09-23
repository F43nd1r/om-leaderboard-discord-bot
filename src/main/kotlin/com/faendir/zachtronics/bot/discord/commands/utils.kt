package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.discord.commands.arg.Argument
import com.faendir.zachtronics.bot.discord.commands.arg.Match
import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.utils.ResultOrMessage
import net.dv8tion.jda.api.entities.Message
import org.springframework.core.annotation.AnnotationUtils
import java.lang.reflect.Method
import kotlin.reflect.KClass

fun parseArguments(game: Game<*, *, *, *>, expected: List<Argument<*, *>>, input: Message): ResultOrMessage<ArgumentMap> {
    val parts = input.contentRaw.split(Regex("\\s+")).filter { it.isNotBlank() }.drop(1).withIndex().toList()
    val cover = ArgumentIntermediateMap(findCollisionFreeFullCover(expected.map { it to it.getAllPossibleMatches(game, input, parts) }, parts.map { it.index })?.toMap()
        ?: return ResultOrMessage.Failure("sorry, I could not parse your command."))
    return ResultOrMessage.Success(ArgumentMap(cover.keys.map {
        val actualResult = it.getActualResult(cover)
        if (actualResult is ResultOrMessage.Failure) return actualResult.typed()
        it to (actualResult as ResultOrMessage.Success).result
    }.toMap()))
}

private fun findCollisionFreeFullCover(options: List<Pair<Argument<*, *>, List<Match<out Any>>>>, freeIndices: List<Int>): Map<Argument<*, *>, Any>? {
    if (options.isEmpty()) return emptyMap()
    if (freeIndices.isEmpty()) return null
    val option = options.first()
    val other = options.drop(1)
    for (match in option.second) {
        if (freeIndices.containsAll(match.consumedInputIndices)) {
            findCollisionFreeFullCover(other, freeIndices - match.consumedInputIndices)?.let { return it + (option.first to match.result) }
        }
    }
    return null
}

@Suppress("UNCHECKED_CAST")
class ArgumentIntermediateMap(private val map: Map<Argument<*, *>, Any>) {
    operator fun <T : Any> get(key: Argument<T, *>): T? {
        return map[key] as? T
    }

    inline fun <reified A : Argument<T, *>, T : Any> find(): T? = find(A::class)

    fun <T : Any> find(keyClass: KClass<out Argument<T, *>>): T? {
        return map.asIterable().find { keyClass.isInstance(it.key) }?.value as? T
    }

    inline fun <reified T : Argument<*, *>> findKey(): T? = findKey(T::class)

    fun <T : Argument<*, *>> findKey(keyClass: KClass<T>): T? {
        return map.keys.find { keyClass.isInstance(it) } as? T
    }

    val keys: Iterable<Argument<*, *>> = map.keys
}

@Suppress("UNCHECKED_CAST")
class ArgumentMap(private val map: Map<Argument<*, *>, Any>) {
    operator fun <T : Any> get(key: Argument<*, T>): T? {
        return map[key] as? T
    }

    inline fun <reified A : Argument<*, T>, T : Any> find(): T? = find(A::class)

    fun <T : Any> find(keyClass: KClass<out Argument<*, T>>): T? {
        return map.asIterable().find { keyClass.isInstance(it) }?.value as? T
    }
}

fun Method.getArgs(possibleArgs: List<Argument<*, *>>): List<Argument<*, *>> {
    return parameters.map { parameter ->
        possibleArgs.find { it.argumentType.isAssignableFrom(parameter.type) } ?: throw IllegalArgumentException("parameter $parameter not supported")
    }
}

inline fun <reified T : Annotation> Leaderboard.findAnnotatedMethod(): Method? = findAnnotatedMethod(T::class.java)

fun <T : Annotation> Leaderboard.findAnnotatedMethod(annotation: Class<T>): Method? {
    return javaClass.methods.find { method -> AnnotationUtils.findAnnotation(method, annotation) != null }
}

sealed class MethodResult<T> {
    class Success<T>(val args: ArgumentMap, val result: T) : MethodResult<T>()
    class NotSupported<T>() : MethodResult<T>()
    class ArgsNotParsed<T>(val message: String) : MethodResult<T>()
    class NotFound<T>() : MethodResult<T>()
}

inline fun <reified A : Annotation, T> invokeAnnotatedMethods(game: Game<*, *, *, *>, possibleArgs: List<Argument<*, *>>, input: Message): List<MethodResult<T>> {
    return game.leaderboards.map { leaderboard ->
        val method = leaderboard.findAnnotatedMethod<A>() ?: return@map MethodResult.NotSupported()
        val requiredArgs = method.getArgs(possibleArgs)
        val argsResult = parseArguments(game, requiredArgs, input)
        if (argsResult is ResultOrMessage.Failure) return@map MethodResult.ArgsNotParsed(argsResult.message)
        val args = (argsResult as ResultOrMessage.Success).result
        @Suppress("UNCHECKED_CAST") val result = method.invoke(leaderboard, *requiredArgs.mapIndexed { index, it -> method.parameterTypes[index].cast(args[it]) }.toTypedArray()) as T? ?: return@map MethodResult.NotFound()
        MethodResult.Success(args, result)
    }
}
