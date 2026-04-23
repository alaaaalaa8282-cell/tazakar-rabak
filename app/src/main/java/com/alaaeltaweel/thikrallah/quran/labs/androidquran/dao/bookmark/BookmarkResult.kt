package com.alaaeltaweel.thikrallah.quran.labs.androidquran.dao.bookmark

import com.alaaeltaweel.thikrallah.quran.labs.androidquran.dao.Tag
import com.alaaeltaweel.thikrallah.quran.labs.androidquran.ui.helpers.QuranRow

data class BookmarkResult(val rows: List<QuranRow>, val tagMap: Map<Long, Tag>)
