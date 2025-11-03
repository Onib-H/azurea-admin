package com.example.azureaadmin.ui.screens.admin.rooms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.azureaadmin.data.models.Room
import com.example.azureaadmin.data.repository.AdminRepository
import com.example.azureaadmin.ui.components.FilterChipGroup
import com.example.azureaadmin.ui.components.FilterSection
import com.example.azureaadmin.ui.components.RadioOption
import com.example.azureaadmin.ui.components.SearchFilterHeader
import com.example.azureaadmin.ui.components.modals.AddItemDialog
import com.example.azureaadmin.ui.components.modals.DeleteItemDialog
import com.example.azureaadmin.ui.components.modals.EditItemDialog
import com.example.azureaadmin.ui.components.modals.ItemDetail
import com.example.azureaadmin.ui.components.modals.ItemType
import com.example.azureaadmin.ui.components.modals.ShowItemDialog
import com.example.azureaadmin.ui.screens.admin.amenities.AmenitiesViewModel
import com.example.azureaadmin.ui.theme.BlueSecondaryDark
import com.example.azureaadmin.ui.theme.GreenAvailable
import com.example.azureaadmin.ui.theme.NeutralOnSurface
import com.example.azureaadmin.utils.BaseViewModelFactory
import com.example.azureaadmin.utils.FormatPrice



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    repository: AdminRepository,
) {
    val viewModel: RoomsViewModel =
        viewModel(factory = BaseViewModelFactory { RoomsViewModel(repository) })

    val amenitiesViewModel: AmenitiesViewModel = viewModel(
        factory = BaseViewModelFactory {AmenitiesViewModel(repository)}
    )
    val rooms by viewModel.rooms.collectAsState()
    val amenities by amenitiesViewModel.amenities.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoomType by remember { mutableStateOf("All") }
    var selectedBedType by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var minPrice by remember { mutableStateOf(1.0) }
    var maxPrice by remember { mutableStateOf(50000.0) }
    var minGuests by remember { mutableStateOf(1) }
    var maxGuests by remember { mutableStateOf(10) }
    var showFilters by remember { mutableStateOf(false) }

    var showViewDialog by remember { mutableStateOf(false) }
    val roomDetail by viewModel.selectedRoomDetail.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf<Room?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchRooms()
        amenitiesViewModel.fetchAmenities()
    }

    val filteredRooms = rooms.filter { room ->
        val matchesSearch = searchQuery.isBlank() ||
                room.room_name.contains(searchQuery, ignoreCase = true)

        val matchesRoomType = selectedRoomType == "All" ||
                room.room_type.equals(selectedRoomType, ignoreCase = true)

        val matchesBedType = selectedBedType == "All" ||
                room.bed_type.equals(selectedBedType, ignoreCase = true)

        val matchesStatus = selectedStatus == "All" ||
                room.status.equals(selectedStatus, ignoreCase = true)

        val matchesPrice = room.price_per_night in minPrice..maxPrice

        val matchesGuests = room.max_guests in minGuests..maxGuests

        matchesSearch && matchesRoomType && matchesBedType && matchesStatus && matchesPrice && matchesGuests
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            SearchFilterHeader(
                title = "Manage Rooms",
                searchPlaceholder = "Search rooms…",
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onFilterClick = { showFilters = true }
            )

            // Room list content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    loading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading rooms...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = "Error",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error ?: "An unknown error occurred",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.fetchRooms() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Try Again")
                            }
                        }
                    }

                    filteredRooms.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Hotel,
                                contentDescription = "No rooms",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No rooms found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try a different search query or filter, or tap + to add a room",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = 80.dp
                            )
                        ) {
                            items(filteredRooms, key = { it.id }) { room ->
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
                .size(60.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Room",
                modifier = Modifier.size(32.dp)
            )
        }
    }

    // Dialogs remain the same...
    AddItemDialog(
        show = showAddDialog,
        type = ItemType.ROOM,
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
        ShowItemDialog(
            itemDetail = roomDetail?.let { ItemDetail.RoomItem(it) },
            onDismiss = {
                showViewDialog = false
                viewModel.clearRoomDetail()
            }
        )
    }

    EditItemDialog(
        show = showEditDialog,
        type = ItemType.ROOM,
        initialValues = selectedRoom?.let {
            mapOf(
                "Name" to it.room_name,
                "Room Type" to it.room_type.replaceFirstChar { c -> c.uppercase() },
                "Bed Type" to it.bed_type.replaceFirstChar { c -> c.uppercase() },
                "Capacity" to it.max_guests.toString(),
                "Status" to it.status.replaceFirstChar { c -> c.uppercase() },
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

    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Rooms",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { showFilters = false },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF5F5F5), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF666666))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Room Type Chips
                FilterChipGroup(
                    label = "Room Type",
                    icon = Icons.Outlined.Hotel,
                    options = listOf("All", "Premium", "Suites"),
                    selected = selectedRoomType,
                    onSelect = { selectedRoomType = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Bed Type Chips
                FilterChipGroup(
                    label = "Bed Type",
                    icon = Icons.Outlined.KingBed,
                    options = listOf("All", "Single", "Twin", "Double", "King", "Queen"),
                    selected = selectedBedType,
                    onSelect = { selectedBedType = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Status Chips
                FilterChipGroup(
                    label = "Status",
                    icon = Icons.Outlined.Circle,
                    options = listOf("All", "Available", "Maintenance"),
                    selected = selectedStatus,
                    onSelect = { selectedStatus = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Price Range Input
                Text(
                    text = "Price Range",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = if (minPrice == 0.0) "" else minPrice.toInt().toString(),
                        onValueChange = {
                            minPrice = it.toDoubleOrNull() ?: 0.0
                        },
                        label = { Text("Min", style = MaterialTheme.typography.bodySmall) },
                        placeholder = { Text("0") },
                        leadingIcon = { Text("₱", color = Color(0xFF666666)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = if (maxPrice == 50000.0) "" else maxPrice.toInt().toString(),
                        onValueChange = {
                            maxPrice = it.toDoubleOrNull() ?: 50000.0
                        },
                        label = { Text("Max", style = MaterialTheme.typography.bodySmall) },
                        placeholder = { Text("50000") },
                        leadingIcon = { Text("₱", color = Color(0xFF666666)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Guest Capacity Input
                Text(
                    text = "Guest Capacity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = if (minGuests == 1) "" else minGuests.toString(),
                        onValueChange = {
                            minGuests = it.toIntOrNull() ?: 1
                        },
                        label = { Text("Min", style = MaterialTheme.typography.bodySmall) },
                        placeholder = { Text("1") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF666666)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = if (maxGuests == 10) "" else maxGuests.toString(),
                        onValueChange = {
                            maxGuests = it.toIntOrNull() ?: 10
                        },
                        label = { Text("Max", style = MaterialTheme.typography.bodySmall) },
                        placeholder = { Text("20") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.People,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF666666)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedRoomType = "All"
                            selectedBedType = "All"
                            selectedStatus = "All"
                            minPrice = 0.0
                            maxPrice = 50000.0
                            minGuests = 1
                            maxGuests = 10
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset")
                    }

                    Button(
                        onClick = { showFilters = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}



