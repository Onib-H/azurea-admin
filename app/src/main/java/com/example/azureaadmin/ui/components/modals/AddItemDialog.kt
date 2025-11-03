package com.example.azureaadmin.ui.components.modals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.azureaadmin.data.repository.AdminRepository
import com.example.azureaadmin.ui.components.button.DropdownField
import com.example.azureaadmin.ui.screens.admin.amenities.AmenitiesViewModel
import com.example.azureaadmin.utils.BaseViewModelFactory
import com.example.azureaadmin.utils.validateRoomInputs

//private val ValidationResult.errors: Map<String, String>
//private val ValidationResult.isValid: Boolean



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    show: Boolean,
    type: ItemType,
    onDismiss: () -> Unit,
    onSave: (Map<String, String>, List<String>, List<Uri>?) -> Unit
) {
    if (!show) return
    val context = LocalContext.current
    val repository = remember { AdminRepository(context) }
    val viewModel: AmenitiesViewModel = viewModel(
        factory = BaseViewModelFactory {AmenitiesViewModel(repository)}
    )

    val inputs = remember { mutableStateMapOf<String, String>() }
    val selectedAmenities = remember { mutableStateOf(setOf<String>()) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var validationErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Fetch amenities when dialog is shown
    LaunchedEffect(show) {
        if (type == ItemType.ROOM) {
            viewModel.fetchAmenities()
        }
        // Reset validation errors when dialog is shown
        validationErrors = emptyMap()
    }

    val amenities by viewModel.amenities.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                selectedImageUris = uris
                // Clear image validation error when images are selected
                if (validationErrors.containsKey("Images")) {
                    validationErrors = validationErrors.filterKeys { it != "Images" }
                }
            }
        }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(0.dp),
            color = Color.White,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            "Add New ${if (type == ItemType.ROOM) "Room" else "Area"}",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black
                    )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputs["Name"] ?: "",
                        onValueChange = {
                            inputs["Name"] = it
                            if (validationErrors.containsKey("Name")) {
                                validationErrors = validationErrors.filterKeys { key -> key != "Name" }
                            }
                        },
                        label = { Text(if (type == ItemType.ROOM) "Room Name" else "Area Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = validationErrors.containsKey("Name"),
                        supportingText = {
                            if (validationErrors.containsKey("Name")) {
                                Text(validationErrors["Name"] ?: "")
                            }
                        }
                    )

                    if (type == ItemType.ROOM) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DropdownField(
                                label = "Room Type",
                                value = inputs["Room Type"] ?: "Premium",
                                options = listOf("Premium", "Suites"),
                                onSelect = {
                                    inputs["Room Type"] = it
                                    if (validationErrors.containsKey("Room Type")) {
                                        validationErrors = validationErrors.filterKeys { key -> key != "Room Type" }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                isError = validationErrors.containsKey("Room Type"),
                                errorMessage = validationErrors["Room Type"]
                            )
                            DropdownField(
                                label = "Bed Type",
                                value = inputs["Bed Type"] ?: "Single",
                                options = listOf("Single", "Double", "Twin", "King", "Queen"),
                                onSelect = {
                                    inputs["Bed Type"] = it
                                    if (validationErrors.containsKey("Bed Type")) {
                                        validationErrors = validationErrors.filterKeys { key -> key != "Bed Type" }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                isError = validationErrors.containsKey("Bed Type"),
                                errorMessage = validationErrors["Bed Type"]
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputs["Capacity"] ?: "",
                            onValueChange = {
                                inputs["Capacity"] = it
                                if (validationErrors.containsKey("Capacity")) {
                                    validationErrors = validationErrors.filterKeys { key -> key != "Capacity" }
                                }
                            },
                            label = { Text(if (type == ItemType.ROOM) "Max Guests" else "Capacity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            isError = validationErrors.containsKey("Capacity"),
                            supportingText = {
                                if (validationErrors.containsKey("Capacity")) {
                                    Text(validationErrors["Capacity"] ?: "")
                                }
                            }
                        )
                        OutlinedTextField(
                            value = inputs["Price"] ?: "",
                            onValueChange = {
                                inputs["Price"] = it
                                if (validationErrors.containsKey("Price")) {
                                    validationErrors = validationErrors.filterKeys { key -> key != "Price" }
                                }
                            },
                            label = { Text(if (type == ItemType.ROOM) "Price(₱)" else "Price(₱)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            isError = validationErrors.containsKey("Price"),
                            supportingText = {
                                if (validationErrors.containsKey("Price")) {
                                    Text(validationErrors["Price"] ?: "")
                                }
                            }
                        )
                    }

                    OutlinedTextField(
                        value = inputs["Description"] ?: "",
                        onValueChange = {
                            inputs["Description"] = it
                            if (validationErrors.containsKey("Description")) {
                                validationErrors = validationErrors.filterKeys { key -> key != "Description" }
                            }
                        },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        maxLines = 3,
                        isError = validationErrors.containsKey("Description"),
                        supportingText = {
                            if (validationErrors.containsKey("Description")) {
                                Text(validationErrors["Description"] ?: "")
                            }
                        }
                    )

                    OutlinedTextField(
                        value = inputs["Discount"] ?: "",
                        onValueChange = {
                            inputs["Discount"] = it
                            if (validationErrors.containsKey("Discount")) {
                                validationErrors = validationErrors.filterKeys { key -> key != "Discount" }
                            }
                        },
                        label = { Text("Discount (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = validationErrors.containsKey("Discount"),
                        supportingText = {
                            if (validationErrors.containsKey("Discount")) {
                                Text(validationErrors["Discount"] ?: "")
                            }
                        }
                    )

                    // Image Upload Section
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Upload Images (${selectedImageUris.size} selected)")
                        }

                        // Display selected images
                        if (selectedImageUris.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedImageUris) { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = rememberAsyncImagePainter(uri),
                                            contentDescription = "Selected Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        IconButton(
                                            onClick = {
                                                selectedImageUris = selectedImageUris.filter { it != uri }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .background(Color.White.copy(alpha = 0.7f), shape = CircleShape)
                                                .size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Image",
                                                tint = Color.Black,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Image validation error
                        if (validationErrors.containsKey("Images")) {
                            Text(
                                text = validationErrors["Images"] ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    if (type == ItemType.ROOM) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Amenities",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            amenities.forEach { amenity ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = amenity.description in selectedAmenities.value,
                                        onCheckedChange = {
                                            selectedAmenities.value =
                                                if (it) selectedAmenities.value + amenity.description
                                                else selectedAmenities.value - amenity.description
                                            if (validationErrors.containsKey("Amenities")) {
                                                validationErrors = validationErrors.filterKeys { key -> key != "Amenities" }
                                            }
                                        }
                                    )
                                    Text(amenity.description)
                                }
                            }
                            // Amenities validation error
                            if (validationErrors.containsKey("Amenities")) {
                                Text(
                                    text = validationErrors["Amenities"] ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val validation = validateRoomInputs(inputs, selectedAmenities.value, selectedImageUris)
                        if (validation.isValid) {
                            onSave(inputs, selectedAmenities.value.toList(), selectedImageUris.takeIf { it.isNotEmpty() })
                            // Reset form
                            inputs.clear()
                            selectedAmenities.value = emptySet()
                            selectedImageUris = emptyList()
                            validationErrors = emptyMap()
                            onDismiss()
                        } else {
                            validationErrors = validation.errors
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Create ${if (type == ItemType.ROOM) "Room" else "Area"}")
                }
            }
        }
    }
}

