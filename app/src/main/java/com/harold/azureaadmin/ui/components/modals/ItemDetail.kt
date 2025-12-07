package com.harold.azureaadmin.ui.components.modals

import com.harold.azureaadmin.data.models.AreaDetail
import com.harold.azureaadmin.data.models.RoomDetail

sealed class ItemDetail {
    data class AreaItem(val area: AreaDetail) : ItemDetail()
    data class RoomItem(val room: RoomDetail) : ItemDetail()
}