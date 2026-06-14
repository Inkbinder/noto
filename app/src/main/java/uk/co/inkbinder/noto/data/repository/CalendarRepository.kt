package uk.co.inkbinder.noto.data.repository

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import uk.co.inkbinder.noto.data.local.db.dao.DayEntryDao
import uk.co.inkbinder.noto.data.local.db.dao.TagDao
import uk.co.inkbinder.noto.data.local.db.entity.DayEntryEntity
import uk.co.inkbinder.noto.data.local.db.entity.DayTagCrossRef
import uk.co.inkbinder.noto.data.local.db.entity.TagEntity
import uk.co.inkbinder.noto.data.preferences.UserPreferencesRepository
import uk.co.inkbinder.noto.domain.calendar.MonthGridBuilder
import uk.co.inkbinder.noto.domain.model.CalendarDaySummary
import uk.co.inkbinder.noto.domain.model.DayDetail
import uk.co.inkbinder.noto.domain.model.MonthOverview
import uk.co.inkbinder.noto.domain.model.Tag
import uk.co.inkbinder.noto.domain.prediction.PeriodPredictionEngine

class CalendarRepository(
    private val dayEntryDao: DayEntryDao,
    private val tagDao: TagDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val predictionEngine: PeriodPredictionEngine = PeriodPredictionEngine(),
) {
    fun observeMonthOverview(month: YearMonth): Flow<MonthOverview> {
        val monthStart = month.atDay(1)
        val monthEnd = month.atEndOfMonth()

        return combine(
            tagDao.observeAll(),
            dayEntryDao.observeRefsBetween(monthStart.toString(), monthEnd.toString()),
            dayEntryDao.observePeriodDates(),
            userPreferencesRepository.userPreferences,
        ) { tagEntities, tagRefs, periodDateStrings, preferences ->
            val tagsById = tagEntities.associateBy(TagEntity::id)
            val colorsByDate = buildTagColorsByDate(tagRefs, tagsById)
            val weeks = MonthGridBuilder.build(
                month = month,
                weekStartsOn = preferences.weekStartsOn.dayOfWeek,
                visibleTagColorsByDate = colorsByDate,
                today = LocalDate.now(),
                maxVisibleSlices = 6,
            )

            MonthOverview(
                month = month,
                bannerText = predictionEngine.buildBanner(
                    periodDateStrings = periodDateStrings,
                    preferences = preferences,
                    today = LocalDate.now(),
                ),
                weeks = weeks,
                weekStartsOn = preferences.weekStartsOn.dayOfWeek,
            )
        }
    }

    fun observeDayDetail(date: LocalDate): Flow<DayDetail> = combine(
        tagDao.observeActive(),
        dayEntryDao.observeRefsForDate(date.toString()),
    ) { tagEntities, tagRefs ->
        DayDetail(
            date = date,
            availableTags = tagEntities.map { entity -> entity.toModel() },
            selectedTagIds = tagRefs.mapTo(linkedSetOf(), DayTagCrossRef::tagId),
        )
    }

    suspend fun toggleTag(date: LocalDate, tagId: String) {
        val dayKey = date.toString()
        dayEntryDao.upsertEntry(
            DayEntryEntity(
                date = dayKey,
                note = null,
                updatedAt = Instant.now().toString(),
            ),
        )

        if (dayEntryDao.countRef(dayKey, tagId) > 0) {
            dayEntryDao.deleteRef(dayKey, tagId)
        } else {
            dayEntryDao.insertRef(DayTagCrossRef(date = dayKey, tagId = tagId))
        }

        dayEntryDao.deleteEntryIfEmpty(dayKey)
    }

    private fun buildTagColorsByDate(
        tagRefs: List<DayTagCrossRef>,
        tagsById: Map<String, TagEntity>,
    ): Map<String, List<String>> = tagRefs
        .groupBy(DayTagCrossRef::date)
        .mapValues { (_, refsForDate) ->
            refsForDate
                .mapNotNull { ref -> tagsById[ref.tagId] }
                .filterNot(TagEntity::isArchived)
                .sortedBy(TagEntity::sortOrder)
                .map(TagEntity::colorHex)
        }

    private fun TagEntity.toModel(): Tag = Tag(
        id = id,
        label = label,
        colorHex = colorHex,
        isPeriodTag = isPeriodTag,
        isArchived = isArchived,
        sortOrder = sortOrder,
    )
}
