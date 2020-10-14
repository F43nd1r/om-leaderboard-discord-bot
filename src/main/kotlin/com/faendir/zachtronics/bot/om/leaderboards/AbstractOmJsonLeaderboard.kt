package com.faendir.zachtronics.bot.om.leaderboards

import com.faendir.zachtronics.bot.main.git.GitRepository
import com.faendir.zachtronics.bot.model.Leaderboard
import com.faendir.zachtronics.bot.model.UpdateResult
import com.faendir.zachtronics.bot.om.imgur.ImgurService
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.utils.plusIf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File
import javax.annotation.PostConstruct

abstract class AbstractOmJsonLeaderboard<J>(private val gitRepo: GitRepository, private val imgurService: ImgurService,
                                            private val directoryCategories: Map<String, List<OmCategory>>, private val serializer: KSerializer<J>) :
    Leaderboard<OmCategory, OmScore, OmPuzzle, OmRecord> {
    companion object {
        private const val scoreFileName = "scores.json"
    }


    override val supportedCategories: List<OmCategory>
        get() = directoryCategories.values.flatten()

    @PostConstruct
    fun onStartUp() {
        gitRepo.access {
            directoryCategories.forEach { (dirName, categories) ->
                val dir = File(repo, dirName)
                File(dir, scoreFileName).takeIf { it.exists() }?.let { file ->
                    updatePage(dir, categories, Json.decodeFromString(serializer, file.readText()))
                }
            }
            if (status().run { added.isNotEmpty() || changed.isNotEmpty() }) {
                commitAndPush("Update page formatting")
            }
        }
    }

    override fun update(puzzle: OmPuzzle, record: OmRecord): UpdateResult<OmCategory, OmScore> {
        return gitRepo.access {
            val betterExists = mutableMapOf<OmCategory, OmScore>()
            val success = mutableMapOf<OmCategory, OmScore?>()
            val rehostedLink by lazy { imgurService.tryRehost(record.link) }
            var paretoUpdate = false
            directoryCategories.forEach { (dirName, dirCategories) ->
                val dir = File(repo, dirName)
                val scoreFile = File(dir, scoreFileName)
                val records = Json.decodeFromString(serializer, scoreFile.takeIf { it.exists() }?.readText() ?: "{}")
                val categories = dirCategories.filter { it.supportsPuzzle(puzzle) && it.supportsScore(record.score) }
                var changed = false
                for (category in categories) {
                    val oldRecord = records.getRecord(puzzle, category)
                    if (oldRecord == null || category.scoreComparator.compare(record.score, oldRecord.score).let {
                            it < 0 || it == 0 && oldRecord.link != record.link
                        }) {
                        records.setRecord(puzzle, category, OmRecord(category.normalizeScore(record.score), rehostedLink))
                        changed = true
                        success[category] = oldRecord?.score
                    } else {
                        betterExists[category] = oldRecord.score
                    }
                }
                val localParetoUpdate = paretoUpdate(puzzle, record, records)
                if (changed || localParetoUpdate) {
                    scoreFile.writeText(Json { prettyPrint = true }.encodeToString(serializer, records))
                    add(scoreFile)
                    updatePage(dir, dirCategories, records)
                }
                paretoUpdate = paretoUpdate || localParetoUpdate
            }
            if (status().run { added.isNotEmpty() || changed.isNotEmpty() }) {
                commitAndPush(record.author, puzzle, record.score, success.map { it.key.displayName }.plusIf(paretoUpdate, "PARETO"))
            }
            when {
                success.isNotEmpty() -> UpdateResult.Success(success)
                paretoUpdate -> UpdateResult.ParetoUpdate()
                betterExists.isNotEmpty() -> UpdateResult.BetterExists(betterExists)
                else -> UpdateResult.NotSupported()
            }
        }
    }

    override fun get(puzzle: OmPuzzle, category: OmCategory): OmRecord? {
        return gitRepo.access {
            val (dirName, _) = directoryCategories.asIterable().find { it.value.contains(category) } ?: return@access null
            val dir = File(repo, dirName)
            val scoreFile = File(dir, scoreFileName)
            val records = Json.decodeFromString(serializer, scoreFile.readText())
            return@access records.getRecord(puzzle, category)?.let { record ->
                val rehostedLink = imgurService.tryRehost(record.link)
                if (rehostedLink != record.link) {
                    val rehostedRecord = OmRecord(record.score, rehostedLink)
                    update(puzzle, rehostedRecord)
                    rehostedRecord
                } else {
                    record
                }
            }
        }
    }

    protected open fun paretoUpdate(puzzle: OmPuzzle, record: OmRecord, records: J): Boolean = false

    protected abstract fun J.getRecord(puzzle: OmPuzzle, category: OmCategory): OmRecord?

    protected abstract fun J.setRecord(puzzle: OmPuzzle, category: OmCategory, record: OmRecord)

    protected abstract fun GitRepository.AccessScope.updatePage(dir: File, categories: List<OmCategory>, records: J)
}