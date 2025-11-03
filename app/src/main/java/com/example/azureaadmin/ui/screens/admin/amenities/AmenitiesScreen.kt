package com.example.azureaadmin.ui.screens.admin.amenities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.azureaadmin.data.models.Amenity
import com.example.azureaadmin.data.repository.AdminRepository
import androidx.compose.material3.TextFieldDefaults
import com.example.azureaadmin.ui.components.SearchFilterHeader
import com.example.azureaadmin.ui.components.modals.DeleteItemDialog
import com.example.azureaadmin.utils.BaseViewModelFactory


@Composable
fun AmenitiesScreen(
    repository: AdminRepository,
) {
    val viewModel: AmenitiesViewModel = viewModel(factory = BaseViewModelFactory { AmenitiesViewModel(repository) })
    val amenities by viewModel.amenities.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // 🔹 Dialog states
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedAmenity by remember { mutableStateOf<Amenity?>(null) }

    LaunchedEffect(Unit) { viewModel.fetchAmenities() }

    // Filtered list based on search query
    val filteredAmenities = if (searchQuery.isBlank()) {
        amenities
    } else {
        amenities.filter { it.description.contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            SearchFilterHeader(
                title = "Manage Amenities",
                searchPlaceholder = "Search amenities…",
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onFilterClick = { /* TODO: open filter dialog */ },
                showFilter = false
            )

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    loading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading amenities…")
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
                            Text(error ?: "Unknown error")
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { viewModel.fetchAmenities() }) {
                                Text("Try Again")
                            }
                        }
                    }

                    filteredAmenities.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.List,
                                contentDescription = "No amenities",
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isEmpty()) "No amenities yet"
                                else "No amenities match your search",
                                style = MaterialTheme.typography.titleMedium
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

        // 🔹 FAB to add
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
        itemLabel = "Amenity",
        onDismiss = { showDeleteDialog = false },
        onDelete = {
            selectedAmenity?.let { viewModel.deleteAmenity(it.id) }
            showDeleteDialog = false
        }
    )


}






