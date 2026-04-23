package com.alaaeltaweel.thikrallah.quran.labs.androidquran.dao.bookmark

import com.alaaeltaweel.thikrallah.quran.labs.androidquran.dao.RecentPage
import com.alaaeltaweel.thikrallah.quran.labs.androidquran.dao.Tag

data class BookmarkData(val tags: List<Tag> = emptyList(),
                        val bookmarks: List<Bookmark> = emptyList(),
                        val recentPages: List<RecentPage> = emptyList())
