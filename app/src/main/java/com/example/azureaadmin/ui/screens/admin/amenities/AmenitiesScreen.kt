package com.example.azureaadmin.ui.screens.admin.amenities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.azureaadmin.data.models.Amenity
import com.example.azureaadmin.data.repository.AdminRepository
import com.example.azureaadmin.ui.components.filters.SearchFilterHeader
import com.example.azureaadmin.ui.components.modals.DeleteItemDialog
import com.example.azureaadmin.utils.BaseViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// AmenitiesScreen.kt - Add blackout effect after refresh
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmenitiesScreen(
    repository: AdminRepository,
) {
    val viewModel: AmenitiesViewModel = viewModel(factory = BaseViewModelFactory { AmenitiesViewModel(repository) })
    val amenities by viewModel.amenities.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedAmenity by remember { mutableStateOf<Amenity?>(null) }

    // Pull-to-refresh states
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var isRefreshInProgress by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    // Blackout overlay
    var showBlackout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.fetchAmenities() }

    val filteredAmenities = if (searchQuery.isBlank()) {
        amenities
    } else {
        amenities.filter { it.description.contains(searchQuery, ignoreCase = true) }
    }

    val onRefresh = {
        if (!isRefreshInProgress) {
            scope.launch {
                isRefreshInProgress = true
                isRefreshing = true

                viewModel.fetchAmenities()
                delay(300)

                showBlackout = true
                delay(150)
                showBlackout = false

                isRefreshing = false
                isRefreshInProgress = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchFilterHeader(
                title = "Manage Amenities",
                searchPlaceholder = "Search amenities",
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onFilterClick = { },
                showFilter = false
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = !showBlackout,
                enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(200)),
                exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
            ) {
                PullToRefreshBox(
                    state = pullState,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            loading && amenities.isEmpty() -> {
                                AmenitySkeleton()
                            }

                            error != null && amenities.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(horizontal = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Unable to load amenities",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = error ?: "Please check your connection",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(onClick = { viewModel.fetchAmenities() }) {
                                        Text("Retry")
                                    }
                                }
                            }

                            filteredAmenities.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(horizontal = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.List,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (searchQuery.isEmpty()) "No amenities added yet"
                                        else "No results found",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (searchQuery.isEmpty()) "Add your first amenity to get started"
                                        else "Try a different search term",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                        items = filteredAmenities,
                                        key = { it.id }
                                    ) { amenity ->
                                        AmenityCard(
                                            amenity = amenity,
                                            onEditClick = {
                                                selectedAmenity = amenity
                                                showEditDialog = true
                                            },
                                            onDeleteClick = {
                                                selectedAmenity = amenity
                                                showDeleteDialog = true
                                            }
                                        )
                                    }
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
                contentDescription = "Add Amenity",
                modifier = Modifier.size(32.dp)
            )
        }
    }

    // Dialogs remain the same...
    AddAmenityDialog(
        show = showAddDialog,
        onDismiss = { showAddDialog = false },
        onSave = { description ->
            viewModel.addAmenity(description)
            showAddDialog = false
        }
    )

    EditAmenityDialog(
        show = showEditDialog,
        currentValue = selectedAmenity?.description ?: "",
        onDismiss = { showEditDialog = false },
        onUpdate = { newValue ->
            selectedAmenity?.let {
                viewModel.updateAmenity(it.id, newValue)
            }
            showEditDialog = false
        }
    )

    DeleteItemDialog(
        show = showDeleteDialog,
        itemLabel = "amenity",
        onDismiss = { showDeleteDialog = false },
        onDelete = {
            selectedAmenity?.let { viewModel.deleteAmenity(it.id) }
            showDeleteDialog = false
        }
    )
}