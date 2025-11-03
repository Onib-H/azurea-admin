package com.example.azureaadmin.ui.components.modals

import com.example.azureaadmin.data.models.AreaDetail
import com.example.azureaadmin.data.models.RoomDetail

sealed class ItemDetail {
    data class AreaItem(val area: AreaDetail) : ItemDetail()
    data class RoomItem(val room: RoomDetail) : ItemDetail()
}