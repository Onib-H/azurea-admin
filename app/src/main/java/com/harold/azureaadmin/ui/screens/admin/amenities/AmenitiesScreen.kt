package com.harold.azureaadmin.ui.screens.admin.amenities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.harold.azureaadmin.data.models.Amenity
import com.harold.azureaadmin.ui.components.common.ListScreenContainer
import com.harold.azureaadmin.ui.components.modals.DeleteItemDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmenitiesScreen(
    viewModel: AmenitiesViewModel = hiltViewModel()
) {
    val amenities by viewModel.amenities.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedAmenity by remember { mutableStateOf<Amenity?>(null) }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshLock by remember { mutableStateOf(false) }
    var showBlackout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.fetchAmenities() }

    val filtered = if (searchQuery.isBlank()) amenities
    else amenities.filter { it.description.contains(searchQuery, ignoreCase = true) }

    val onRefresh = {
        if (!refreshLock) {
            scope.launch {
                refreshLock = true
                isRefreshing = true

                viewModel.fetchAmenities()
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
            title = "Manage Amenities",
            searchPlaceholder = "Search amenities",
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            showFilter = false,
            onFilterClick = {  },
            showNotification = false,

            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            showBlackout = showBlackout,

            loading = loading,
            error = error,
            items = filtered,

            emptyIcon = Icons.AutoMirrored.Outlined.List,
            emptyTitle = if (searchQuery.isBlank()) "No amenities added yet" else "No results found",
            emptySubtitle = if (searchQuery.isBlank()) "Add your first amenity to get started" else null,

            skeleton = { AmenitySkeleton() }
        ) { list ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(list, key = { it.id }) { amenity ->
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

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }

    AddAmenityDialog(
        show = showAddDialog,
        onDismiss = { showAddDialog = false },
        onSave = {
            viewModel.addAmenity(it)
            showAddDialog = false
        }
    )

    EditAmenityDialog(
        show = showEditDialog,
        currentValue = selectedAmenity?.description ?: "",
        onDismiss = { showEditDialog = false },
        onUpdate = { value ->
            selectedAmenity?.let { viewModel.updateAmenity(it.id, value) }
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
