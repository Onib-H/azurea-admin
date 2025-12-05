package com.example.azureaadmin.ui.screens.admin.areas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.azureaadmin.data.models.Area
import com.example.azureaadmin.data.repository.AdminRepository
import com.example.azureaadmin.ui.components.filters.FilterChipGroup
import com.example.azureaadmin.ui.components.filters.SearchFilterHeader
import com.example.azureaadmin.ui.components.modals.AddItemDialog
import com.example.azureaadmin.ui.components.modals.DeleteItemDialog
import com.example.azureaadmin.ui.components.modals.EditItemDialog
import com.example.azureaadmin.ui.components.modals.ItemDetail
import com.example.azureaadmin.ui.components.modals.ItemType
import com.example.azureaadmin.ui.components.modals.ShowItemDialog
import com.example.azureaadmin.utils.BaseViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreasScreen(
    repository: AdminRepository,
) {
    val viewModel: AreasViewModel = viewModel(factory = BaseViewModelFactory { AreasViewModel(repository)})
    val areas by viewModel.areas.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedAreaDetail by viewModel.selectedArea.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("All") }
    var minPrice by remember { mutableStateOf(1.0) }
    var maxPrice by remember { mutableStateOf(10000.0) }
    var minCapacity by remember { mutableStateOf(1) }
    var maxCapacity by remember { mutableStateOf(500) }

    var showViewDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedArea by remember { mutableStateOf<Area?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var isRefreshInProgress by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()
    var showBlackout by remember { mutableStateOf(false) }

    val onRefresh = {
        if (!isRefreshInProgress) {
            scope.launch {
                isRefreshInProgress = true
                isRefreshing = true

                viewModel.fetchAreas()
                delay(300)

                showBlackout = true
                delay(150)
                showBlackout = false

                isRefreshing = false
                isRefreshInProgress = false
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchAreas()
    }

    val filteredAreas = areas.filter { area ->
        val matchesSearch =
            searchQuery.isEmpty() || area.area_name.contains(searchQuery, ignoreCase = true)
        val matchesPrice = area.price_per_hour_numeric in minPrice..maxPrice
        val matchesCapacity = area.capacity in minCapacity..maxCapacity
        val matchesStatus =
            selectedStatus == "All" || area.status.equals(selectedStatus, ignoreCase = true)

        matchesSearch && matchesPrice && matchesCapacity && matchesStatus
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            SearchFilterHeader(
                title = "Manage Areas",
                searchPlaceholder = "Search areas…",
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onFilterClick = { showFilters = true }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !showBlackout,
                    enter = androidx.compose.animation.fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(
                            200
                        )
                    ),
                    exit = androidx.compose.animation.fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(
                            150
                        )
                    )
                ) {
                    PullToRefreshBox(
                        state = pullState,
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            when {
                                loading && areas.isEmpty() -> {
                                    AreaSkeleton()
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
                                            onClick = { viewModel.fetchAreas() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text("Try Again")
                                        }
                                    }
                                }

                                filteredAreas.isEmpty() -> {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.MeetingRoom,
                                            contentDescription = "No areas",
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "No areas found",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Try a different search query or tap + to add a new area",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.7f
                                            ),
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
                                        items(
                                            items = filteredAreas,
                                            key = { it.id }
                                        ) { area ->
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
                            }
                        }
                    }

                    if (showBlackout) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
                                .zIndex(10f)
                        )
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
                        contentDescription = "Add Area",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            ShowItemDialog(
                itemDetail = selectedAreaDetail?.let { ItemDetail.AreaItem(it) },
                onDismiss = {
                    showViewDialog = false
                    viewModel.clearAreaDetail()
                }
            )

            AddItemDialog(
                show = showAddDialog,
                type = ItemType.AREA,
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
                type = ItemType.AREA,
                initialValues = mapOf(
                    "Name" to (selectedArea?.area_name ?: ""),
                    "Capacity" to (selectedArea?.capacity?.toString() ?: ""),
                    "Price" to (selectedArea?.price_per_hour_numeric?.toString() ?: ""),
                    "Description" to (selectedArea?.description ?: ""),
                    "Discount" to (selectedArea?.discount_percent?.toString() ?: ""),
                    "Status" to (selectedArea?.status?.lowercase()
                        ?.replaceFirstChar { it.uppercase() }
                        ?: "Available")
                ),
                initialImages = selectedArea?.images?.map { it.area_image } ?: emptyList(),
                onDismiss = { showEditDialog = false },
                onSave = onSave@{ updatedInputs, _, newImageUris, existingImages ->
                    val id = selectedArea?.id ?: return@onSave
                    val name = updatedInputs["Name"] ?: ""
                    val capacity = updatedInputs["Capacity"]?.toIntOrNull() ?: 0
                    val status = updatedInputs["Status"] ?: "Available"
                    val price = updatedInputs["Price"] ?: "0.0"
                    val description = updatedInputs["Description"] ?: ""
                    val discount = updatedInputs["Discount"]?.toIntOrNull() ?: 0

                    viewModel.editArea(
                        areaId = id,
                        name = name,
                        description = description,
                        capacity = capacity,
                        status = status,
                        pricePerHour = price,
                        discountPercent = discount,
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
                    selectedArea?.let {
                        viewModel.deleteArea(it.id)
                    }
                    showDeleteDialog = false
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
                                text = "Filter Venues",
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
                            text = "Price per Hour",
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
                                value = if (maxPrice == 10000.0) "" else maxPrice.toInt()
                                    .toString(),
                                onValueChange = {
                                    maxPrice = it.toDoubleOrNull() ?: 10000.0
                                },
                                label = { Text("Max", style = MaterialTheme.typography.bodySmall) },
                                placeholder = { Text("10000") },
                                leadingIcon = { Text("₱", color = Color(0xFF666666)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Capacity Input
                        Text(
                            text = "Capacity",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = if (minCapacity == 1) "" else minCapacity.toString(),
                                onValueChange = {
                                    minCapacity = it.toIntOrNull() ?: 1
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
                                value = if (maxCapacity == 500) "" else maxCapacity.toString(),
                                onValueChange = {
                                    maxCapacity = it.toIntOrNull() ?: 500
                                },
                                label = { Text("Max", style = MaterialTheme.typography.bodySmall) },
                                placeholder = { Text("500") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Groups,
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
                                    selectedStatus = "All"
                                    minPrice = 0.0
                                    maxPrice = 10000.0
                                    minCapacity = 1
                                    maxCapacity = 500
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
    }
}