package com.harold.azureaadmin.ui.screens.admin.areas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.MeetingRoom
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
import com.harold.azureaadmin.data.models.Area
import com.harold.azureaadmin.ui.components.common.ListScreenContainer
import com.harold.azureaadmin.ui.components.filters.FilterBottomSheet
import com.harold.azureaadmin.ui.components.filters.FilterSectionTemplate
import com.harold.azureaadmin.ui.components.modals.AddItemDialog
import com.harold.azureaadmin.ui.components.modals.DeleteItemDialog
import com.harold.azureaadmin.ui.components.modals.EditItemDialog
import com.harold.azureaadmin.ui.components.modals.ShowItemDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreasScreen(
    viewModel: AreasViewModel = hiltViewModel()
) {
    val areas by viewModel.areas.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedAreaDetail by viewModel.selectedArea.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("All") }
    var minPrice by remember { mutableStateOf(0.0) }
    var maxPrice by remember { mutableStateOf(10000000.0) }
    var minCapacity by remember { mutableStateOf(1) }
    var maxCapacity by remember { mutableStateOf(500) }

    var showViewDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedArea by remember { mutableStateOf<Area?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshLock by remember { mutableStateOf(false) }
    var showBlackout by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.fetchAreas() }

    val filteredAreas = areas.filter { area ->
        val matchesSearch = searchQuery.isBlank() || area.area_name.contains(searchQuery, ignoreCase = true)
        val matchesPrice = area.price_per_hour_numeric in minPrice..maxPrice
        val matchesCapacity = area.capacity in minCapacity..maxCapacity
        val matchesStatus = selectedStatus == "All" || area.status.equals(selectedStatus, ignoreCase = true)
        matchesSearch && matchesPrice && matchesCapacity && matchesStatus
    }

    val onRefresh = {
        if (!refreshLock) {
            scope.launch {
                refreshLock = true
                isRefreshing = true
                viewModel.fetchAreas()
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
            title = "Manage Areas",
            searchPlaceholder = "Search areas…",
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            showFilter = true,
            onFilterClick = { showFilters = true },

            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            showBlackout = showBlackout,

            loading = loading,
            error = error,
            items = filteredAreas,

            emptyIcon = Icons.Outlined.MeetingRoom,
            emptyTitle = if (searchQuery.isEmpty()) "No areas yet" else "No matching areas",
            emptySubtitle = if (searchQuery.isEmpty()) "Create an area to get started" else "Try a different search query",

            skeleton = { AreaSkeleton() }
        ) { list ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(list, key = { it.id }) { area ->
                    AreaCard(
                        area = area,
                        onViewClick = {
                            selectedArea = area
                            viewModel.showArea(area.id)
                            showViewDialog = true
                        },
                        onEditClick = {
                            selectedArea = area
                            showEditDialog = true
                        },
                        onDeleteClick = {
                            selectedArea = area
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        // Floating add button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Area")
        }

        // Filter bottom sheet trigger
        if (showFilters) {
            val sections = listOf(
                FilterSectionTemplate.ChipSection(
                    id = "status",
                    label = "Status",
                    icon = Icons.Outlined.Circle,
                    options = listOf("All", "Available", "Maintenance"),
                    selected = selectedStatus
                ),
                FilterSectionTemplate.NumberRangeSection(
                    id = "price",
                    label = "Price per Hour",
                    minInitial = minPrice,
                    maxInitial = maxPrice,
                    minPlaceholder = "1",
                    maxPlaceholder = "10000000"
                ),
                FilterSectionTemplate.NumberRangeSection(
                    id = "capacity",
                    label = "Capacity",
                    minInitial = minCapacity.toDouble(),
                    maxInitial = maxCapacity.toDouble(),
                    minPlaceholder = "1",
                    maxPlaceholder = "500"
                )
            )

            FilterBottomSheet(
                sections = sections,
                onApply = { result ->
                    // apply filter results
                    result.chipValues["status"]?.let { selectedStatus = it }
                    result.numberRanges["price"]?.let { (min, max) ->
                        minPrice = min
                        maxPrice = max
                    }
                    result.numberRanges["capacity"]?.let { (min, max) ->
                        minCapacity = min.toInt()
                        maxCapacity = max.toInt()
                    }
                },
                onReset = {
                    selectedStatus = "All"
                    minPrice = 0.0
                    maxPrice = 10000000.0
                    minCapacity = 1
                    maxCapacity = 500
                    showFilters = false
                },
                onDismiss = { showFilters = false }
            )
        }

        // Dialogs (unchanged)
        ShowItemDialog(
            itemDetail = selectedAreaDetail?.let { com.harold.azureaadmin.ui.components.modals.ItemDetail.AreaItem(it) },
            onDismiss = {
                showViewDialog = false
                viewModel.clearAreaDetail()
            }
        )

        AddItemDialog(
            show = showAddDialog,
            type = com.harold.azureaadmin.ui.components.modals.ItemType.AREA,
            onDismiss = { showAddDialog = false },
            onSave = { areaInputs, _, selectedImageUris ->
                val name = areaInputs["Name"] ?: ""
                val capacity = areaInputs["Capacity"]?.toIntOrNull() ?: 0
                val price = areaInputs["Price"] ?: "0.0"
                val description = areaInputs["Description"] ?: ""
                val discount = areaInputs["Discount"]?.toIntOrNull() ?: 0

                viewModel.addArea(
                    name = name,
                    description = description,
                    capacity = capacity,
                    pricePerHour = price,
                    discountPercent = discount,
                    images = selectedImageUris ?: emptyList(),
                    context = context
                )

                showAddDialog = false
            }
        )

        EditItemDialog(
            show = showEditDialog,
            type = com.harold.azureaadmin.ui.components.modals.ItemType.AREA,
            initialValues = mapOf(
                "Name" to (selectedArea?.area_name ?: ""),
                "Capacity" to (selectedArea?.capacity?.toString() ?: ""),
                "Price" to (selectedArea?.price_per_hour_numeric?.toString() ?: ""),
                "Description" to (selectedArea?.description ?: ""),
                "Discount" to (selectedArea?.discount_percent?.toString() ?: ""),
                "Status" to (selectedArea?.status ?: "Available")
            ),
            initialImages = selectedArea?.images?.map { it.area_image } ?: emptyList(),
            onDismiss = { showEditDialog = false },
            onSave = { updatedInputs, _, newImageUris, existingImages ->
                val id = selectedArea?.id ?: return@EditItemDialog
                viewModel.editArea(
                    areaId = id,
                    name = updatedInputs["Name"] ?: "",
                    description = updatedInputs["Description"] ?: "",
                    capacity = updatedInputs["Capacity"]?.toIntOrNull() ?: 0,
                    status = updatedInputs["Status"] ?: "Available",
                    pricePerHour = updatedInputs["Price"] ?: "0.0",
                    discountPercent = updatedInputs["Discount"]?.toIntOrNull() ?: 0,
                    images = newImageUris,
                    existingImages = existingImages,
                    context = context
                )
                showEditDialog = false
            }
        )

        DeleteItemDialog(
            show = showDeleteDialog,
            itemLabel = "Area",
            onDismiss = { showDeleteDialog = false },
            onDelete = {
                selectedArea?.let { viewModel.deleteArea(it.id) }
            }
        )
    }
}
