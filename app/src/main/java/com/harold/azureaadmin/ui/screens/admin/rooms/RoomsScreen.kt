package com.harold.azureaadmin.ui.screens.admin.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.KingBed
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.harold.azureaadmin.data.models.Room
import com.harold.azureaadmin.ui.components.common.ListScreenContainer
import com.harold.azureaadmin.ui.components.filters.FilterBottomSheet
import com.harold.azureaadmin.ui.components.filters.FilterSectionTemplate
import com.harold.azureaadmin.ui.components.modals.AddItemDialog
import com.harold.azureaadmin.ui.components.modals.DeleteItemDialog
import com.harold.azureaadmin.ui.components.modals.EditItemDialog
import com.harold.azureaadmin.ui.screens.admin.amenities.AmenitiesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    viewModel: RoomsViewModel = hiltViewModel(),
    amenitiesViewModel: AmenitiesViewModel = hiltViewModel()
) {
    val rooms by viewModel.rooms.collectAsState()
    val amenities by amenitiesViewModel.amenities.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoomType by remember { mutableStateOf("All") }
    var selectedBedType by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var minPrice by remember { mutableStateOf(0.0) }
    var maxPrice by remember { mutableStateOf(10000000.0) }
    var minGuests by remember { mutableStateOf(1) }
    var maxGuests by remember { mutableStateOf(10) }
    var showFilters by remember { mutableStateOf(false) }

    var showViewDialog by remember { mutableStateOf(false) }
    val roomDetail by viewModel.selectedRoomDetail.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf<Room?>(null) }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshLock by remember { mutableStateOf(false) }
    var showBlackout by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchRooms()
        amenitiesViewModel.fetchAmenities()
    }

    val filteredRooms = rooms.filter { room ->
        val matchesSearch = searchQuery.isBlank() || room.room_name.contains(searchQuery, ignoreCase = true)
        val matchesRoomType = selectedRoomType == "All" || room.room_type.equals(selectedRoomType, ignoreCase = true)
        val matchesBedType = selectedBedType == "All" || room.bed_type.equals(selectedBedType, ignoreCase = true)
        val matchesStatus = selectedStatus == "All" || room.status.equals(selectedStatus, ignoreCase = true)
        val matchesPrice = room.price_per_night in minPrice..maxPrice
        val matchesGuests = room.max_guests in minGuests..maxGuests
        matchesSearch && matchesRoomType && matchesBedType && matchesStatus && matchesPrice && matchesGuests
    }

    val onRefresh = {
        if (!refreshLock) {
            scope.launch {
                refreshLock = true
                isRefreshing = true
                viewModel.fetchRooms()
                delay(300)
                showBlackout = true
                delay(150)
                showBlackout = false
                isRefreshing = false
                refreshLock = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ListScreenContainer(
            title = "Manage Rooms",
            searchPlaceholder = "Search rooms…",
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            showFilter = true,
            onFilterClick = { showFilters = true },

            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            showBlackout = showBlackout,

            loading = loading,
            error = error,
            items = filteredRooms,

            emptyIcon = Icons.Outlined.Hotel,
            emptyTitle = if (searchQuery.isEmpty()) "No rooms yet" else "No matching rooms",
            emptySubtitle = if (searchQuery.isEmpty()) "Add your first room to get started" else "Try a different search or adjust your filters",

            skeleton = { RoomSkeleton() }
        ) { list ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(list, key = { it.id }) { room ->
                    RoomCard(
                        room = room,
                        onViewClick = {
                            selectedRoom = room
                            viewModel.fetchRoomDetail(room.id)
                            showViewDialog = true
                        },
                        onEditClick = {
                            selectedRoom = room
                            showEditDialog = true
                        },
                        onDeleteClick = {
                            selectedRoom = room
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Room")
        }

        if (showFilters) {
            val sections = listOf(
                FilterSectionTemplate.ChipSection(
                    id = "roomType",
                    label = "Room Type",
                    icon = Icons.Outlined.Hotel,
                    options = listOf("All", "Premium", "Suites"),
                    selected = selectedRoomType
                ),
                FilterSectionTemplate.ChipSection(
                    id = "bedType",
                    label = "Bed Type",
                    icon = Icons.Outlined.KingBed,
                    options = listOf("All", "Single", "Twin", "Double", "King", "Queen"),
                    selected = selectedBedType
                ),
                FilterSectionTemplate.ChipSection(
                    id = "status",
                    label = "Status",
                    icon = Icons.Outlined.Circle,
                    options = listOf("All", "Available", "Maintenance"),
                    selected = selectedStatus
                ),
                FilterSectionTemplate.NumberRangeSection(
                    id = "price",
                    label = "Price Range",
                    minInitial = minPrice,
                    maxInitial = maxPrice,
                    minPlaceholder = "0",
                    maxPlaceholder = "10000000"
                ),
                FilterSectionTemplate.NumberRangeSection(
                    id = "guests",
                    label = "Guest Capacity",
                    minInitial = minGuests.toDouble(),
                    maxInitial = maxGuests.toDouble(),
                    minPlaceholder = "1",
                    maxPlaceholder = "10"
                )
            )

            FilterBottomSheet(
                sections = sections,
                onApply = { result ->
                    result.chipValues["roomType"]?.let { selectedRoomType = it }
                    result.chipValues["bedType"]?.let { selectedBedType = it }
                    result.chipValues["status"]?.let { selectedStatus = it }
                    result.numberRanges["price"]?.let { (min, max) ->
                        minPrice = min
                        maxPrice = max
                    }
                    result.numberRanges["guests"]?.let { (min, max) ->
                        minGuests = min.toInt()
                        maxGuests = max.toInt()
                    }
                },
                onReset = {
                    selectedRoomType = "All"
                    selectedBedType = "All"
                    selectedStatus = "All"
                    minPrice = 0.0
                    maxPrice = 10000000.0
                    minGuests = 1
                    maxGuests = 10
                    showFilters = false
                },
                onDismiss = { showFilters = false }
            )
        }

        // Dialogs (unchanged)
        AddItemDialog(
            show = showAddDialog,
            type = com.harold.azureaadmin.ui.components.modals.ItemType.ROOM,
            onDismiss = { showAddDialog = false },
            onSave = { roomInputs, amenityDescriptions, imageUris ->
                val name = roomInputs["Name"] ?: ""
                val type = roomInputs["Room Type"] ?: ""
                val bedType = roomInputs["Bed Type"] ?: ""
                val guests = roomInputs["Capacity"]?.toIntOrNull() ?: 2
                val price = roomInputs["Price"]?.toDoubleOrNull() ?: 0.0
                val description = roomInputs["Description"] ?: ""
                val discount = roomInputs["Discount"]?.toIntOrNull() ?: 0

                val amenityIds = amenities.filter { amenity ->
                    amenity.description in amenityDescriptions
                }.map { amenity -> amenity.id }

                viewModel.addRoom(
                    name = name,
                    roomType = type,
                    bedType = bedType,
                    maxGuests = guests,
                    price = price,
                    description = description,
                    discountPercent = discount,
                    amenityIds = amenityIds,
                    images = imageUris,
                    context = context
                )

                showAddDialog = false
            }
        )

        if (showViewDialog && roomDetail != null) {
            com.harold.azureaadmin.ui.components.modals.ShowItemDialog(
                itemDetail = roomDetail?.let { com.harold.azureaadmin.ui.components.modals.ItemDetail.RoomItem(it) },
                onDismiss = {
                    showViewDialog = false
                    viewModel.clearRoomDetail()
                }
            )
        }

        EditItemDialog(
            show = showEditDialog,
            type = com.harold.azureaadmin.ui.components.modals.ItemType.ROOM,
            initialValues = selectedRoom?.let {
                mapOf(
                    "Name" to it.room_name,
                    "Room Type" to it.room_type,
                    "Bed Type" to it.bed_type,
                    "Capacity" to it.max_guests.toString(),
                    "Status" to it.status,
                    "Price" to it.price_per_night.toString(),
                    "Description" to it.description,
                    "Discount" to it.discount_percent.toString()
                )
            } ?: emptyMap(),
            initialAmenities = selectedRoom?.amenities ?: emptyList(),
            initialImages = selectedRoom?.images?.map { it.room_image } ?: emptyList(),
            availableAmenities = amenities,
            onDismiss = { showEditDialog = false },
            onSave = { updatedInputs, amenityDescriptions, newImageUris, existingImageUrls ->
                selectedRoom?.let { room ->
                    val name = updatedInputs["Name"] ?: room.room_name
                    val type = updatedInputs["Room Type"] ?: room.room_type
                    val bedType = updatedInputs["Bed Type"] ?: room.bed_type
                    val guests = updatedInputs["Capacity"]?.toIntOrNull() ?: room.max_guests
                    val price = updatedInputs["Price"]?.toDoubleOrNull() ?: room.price_per_night
                    val description = updatedInputs["Description"] ?: room.description
                    val discount = updatedInputs["Discount"]?.toIntOrNull() ?: room.discount_percent
                    val status = updatedInputs["Status"] ?: room.status

                    val amenityIds = amenities.filter { amenity ->
                        amenity.description in amenityDescriptions
                    }.map { amenity -> amenity.id }

                    viewModel.editRoom(
                        roomId = room.id,
                        name = name,
                        roomType = type,
                        bedType = bedType,
                        maxGuests = guests,
                        price = price,
                        description = description,
                        discountPercent = discount,
                        status = status,
                        amenityIds = amenityIds,
                        newImages = newImageUris.takeIf { it.isNotEmpty() },
                        existingImages = existingImageUrls,
                        context = context
                    )
                }
                showEditDialog = false
            }
        )

        DeleteItemDialog(
            show = showDeleteDialog,
            itemLabel = "Room",
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                selectedRoom?.let { viewModel.deleteRoom(it.id) }
            }
        )
    }
}
