package com.kksionek.queuedroid.model

data class PictureUrl(val url: String)
data class PictureData(val data: PictureUrl)
data class Picture(val picture: PictureData)
data class FriendData(val id: String, val name: String, val picture: Picture)

data class PagingData(val after: String?)
data class CursorsData(val cursors: PagingData?)

data class FriendDataList(
    val data: List<FriendData> = emptyList(),
    val paging: CursorsData? = null
)