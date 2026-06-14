package uk.co.inkbinder.noto.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import uk.co.inkbinder.noto.data.local.db.dao.DayEntryDao
import uk.co.inkbinder.noto.data.local.db.dao.TagDao
import uk.co.inkbinder.noto.data.local.db.entity.DayEntryEntity
import uk.co.inkbinder.noto.data.local.db.entity.DayTagCrossRef
import uk.co.inkbinder.noto.data.local.db.entity.TagEntity

@Database(
    entities = [
        TagEntity::class,
        DayEntryEntity::class,
        DayTagCrossRef::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class NotoDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao
    abstract fun dayEntryDao(): DayEntryDao
}

