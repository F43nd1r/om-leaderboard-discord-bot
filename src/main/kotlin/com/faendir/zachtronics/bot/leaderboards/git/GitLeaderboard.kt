package com.faendir.zachtronics.bot.leaderboards.git

import com.faendir.zachtronics.bot.config.GitProperties
import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.leaderboards.OmLeaderboard
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.Author
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Link
import com.faendir.zachtronics.bot.model.Score
import com.faendir.zachtronics.bot.model.om.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.annotation.PostConstruct

@Component
class GitLeaderboard(gitProperties: GitProperties) : GitRepository(gitProperties, "om-leaderboard", "https://github.com/F43nd1r/om-leaderboard.git"),
                                                     OmLeaderboard {
    companion object {
        private const val scoreFileName = "scores.json"
    }

    override val supportedCategories: Collection<OmCategory> = listOf(OmCategory.WIDTH, OmCategory.HEIGHT)

    @PostConstruct
    fun onStartUp() {
        access {
            File(repo, scoreFileName).takeIf { it.exists() }?.let { file ->
                generatePage(Json.decodeFromString(file.readText()))
                if (status().changed.isNotEmpty()) {
                    commitAndPush("Update page formatting")
                }
            }
        }
    }

    override fun update(user: Author, puzzle: OmPuzzle, categories: List<OmCategory>, score: OmScore, link: Link): UpdateResult {
        return access {
            val scoreFile = File(repo, scoreFileName)
            val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
            val betterExists = mutableMapOf<Category<*, *, *>, Score>()
            val success = mutableMapOf<Category<*, *, *>, Score?>()
            for (category in categories) {
                val oldRecord = recordList[puzzle]?.records?.find { it.category == category }
                if (oldRecord == null || category.isBetterOrEqual(score, oldRecord.score) && oldRecord.link != link.url) {
                    recordList[puzzle] = (recordList[puzzle] ?: PuzzleEntry(mutableListOf())).apply {
                        if (oldRecord != null) records.remove(oldRecord)
                        records.add(OmRecord(category, category.normalizeScore(score), link.url))
                    }
                    success[category] = oldRecord?.score
                } else {
                    betterExists[category] = oldRecord.score
                }
            }
            return@access if (success.isNotEmpty()) {
                updateRemote(scoreFile, recordList, user.name, puzzle, score, success.keys.map { it.displayName })
                UpdateResult.Success(success)
            } else {
                UpdateResult.BetterExists(betterExists)
            }
        }
    }

    private fun GitRepository.AccessScope.updateRemote(scoreFile: File, recordList: RecordList, user: String, puzzle: OmPuzzle, score: OmScore, updated: Collection<String>) {
        scoreFile.writeText(Json { prettyPrint = true }.encodeToString(recordList))
        add(scoreFile)
        generatePage(recordList)
        commitAndPush(user, puzzle, score, updated)
    }

    private fun AccessScope.generatePage(recordList: RecordList) {
        val folder = File(repo, "templates")
        val mainTemplate = File(folder, "main.html").readText()
        val groupTemplate = File(folder, "group.html").readText()
        val puzzleTemplate = File(folder, "puzzle.html").readText()
        val blockTemplate = File(folder, "block.html").readText()
        val scoreTemplate = File(folder, "score.html").readText()
        val gifTemplate = File(folder, "gif.html").readText()
        val videoTemplate = File(folder, "video.html").readText()
        fun generateRecord(record: OmRecord): String {
            return scoreTemplate.format(record.link,
                record.score.toDisplayString({ record.category.sortScoreParts(this) }) { _, value -> format(value) },
                if (record.link.endsWith("mp4") || record.link.endsWith("webm")) {
                    videoTemplate.format(record.link)
                } else {
                    gifTemplate.format(record.link)
                })
        }

        val text = mainTemplate.format(OffsetDateTime.now(ZoneOffset.UTC), OmGroup.values().joinToString("\n") { group ->
            val puzzles = OmPuzzle.values().filter { it.group == group && it.type != OmType.PRODUCTION }
            if (puzzles.isNotEmpty()) {
                groupTemplate.format(group.displayName, puzzles.joinToString("\n") { puzzle ->
                    val heightRecord = recordList[puzzle]?.records?.find { it.category == OmCategory.HEIGHT }
                    val widthRecord = recordList[puzzle]?.records?.find { it.category == OmCategory.WIDTH }
                    puzzleTemplate.format(puzzle.displayName,
                        heightRecord?.let { generateRecord(it) } ?: "",
                        if (puzzle.type == OmType.INFINITE) blockTemplate else widthRecord?.let { generateRecord(it) } ?: "")
                })
            } else {
                ""
            }
        })
        val file = File(repo, "index.html")
        val old = file.readText()
        if (old.lines().filter { !it.contains("last updated on") } != text.lines().filter { !it.contains("last updated on") }) {
            file.writeText(text)
            add(file)
        }
    }

    override fun get(puzzle: OmPuzzle, category: OmCategory): OmRecord? {
        return access {
            val scoreFile = File(repo, scoreFileName)
            val recordList: RecordList = Json.decodeFromString(scoreFile.readText())
            recordList[puzzle]?.records?.find { it.category == category }?.let { OmRecord(category, it.score, it.link) }
        }
    }
}

@Serializable
data class PuzzleEntry(val records: MutableList<OmRecord>)

typealias RecordList = MutableMap<OmPuzzle, PuzzleEntry>