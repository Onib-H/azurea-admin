package com.harold.azureaadmin.ui.components.modals

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.harold.azureaadmin.data.models.Amenity
import com.harold.azureaadmin.ui.components.button.DropdownField
import com.harold.azureaadmin.utils.validateRoomInputs


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemDialog(
    show: Boolean,
    type: ItemType,
    initialValues: Map<String, String>,
    initialAmenities: List<Amenity> = emptyList(),
    initialImages: List<String> = emptyList(),
    availableAmenities: List<Amenity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Map<String, String>, List<String>, List<Uri>, List<String>) -> Unit
) {
    if (!show) return

    val inputs = remember { mutableStateMapOf<String, String>() }
    val selectedAmenities = remember { mutableStateOf(initialAmenities.map { it.description }.toSet()) }
    var newImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var existingImages by remember { mutableStateOf(initialImages) }
    var validationErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(show) {
        if (show) {
            inputs.clear()
            inputs.putAll(initialValues)
            existingImages = initialImages
            newImageUris = emptyList()
            selectedAmenities.value = initialAmenities.map { it.description }.toSet()
            validationErrors = emptyMap()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                newImageUris = uris
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
                    title = { Text("Edit ${if (type == ItemType.ROOM) "Room" else "Area"}") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black,
                        actionIconContentColor = Color.Black
                    )
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                value = inputs["Room Type"] ?: "",
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
                                value = inputs["Bed Type"] ?: "",
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
                            label = { Text("Capacity") },
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
                        DropdownField(
                            label = "Status",
                            value = inputs["Status"] ?: "Available",
                            options = listOf("Available", "Maintenance"),
                            onSelect = {
                                inputs["Status"] = it
                                if (validationErrors.containsKey("Status")) {
                                    validationErrors = validationErrors.filterKeys { key -> key != "Status" }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            isError = validationErrors.containsKey("Status"),
                            errorMessage = validationErrors["Status"]
                        )
                    }

                    OutlinedTextField(
                        value = inputs["Price"] ?: "",
                        onValueChange = {
                            inputs["Price"] = it
                            if (validationErrors.containsKey("Price")) {
                                validationErrors = validationErrors.filterKeys { key -> key != "Price" }
                            }
                        },
                        label = { Text("Price (₱)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = validationErrors.containsKey("Price"),
                        supportingText = {
                            if (validationErrors.containsKey("Price")) {
                                Text(validationErrors["Price"] ?: "")
                            }
                        }
                    )

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

                    // Image Management Section
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Images",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        // Existing Images
                        if (existingImages.isNotEmpty()) {
                            Text(
                                "Current Images:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            LazyRow(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(existingImages) { imageUrl ->
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = rememberAsyncImagePainter(imageUrl),
                                            contentDescription = "Existing Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        IconButton(
                                            onClick = {
                                                existingImages = existingImages.filter { it != imageUrl }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .background(Color.Red.copy(alpha = 0.7f), shape = CircleShape)
                                                .size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Image",
                                                tint = Color.White,
                                                modifier = Modifier.size(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Add New Images Button
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add New Images (${newImageUris.size} selected)")
                        }

                        // New Images Preview
                        if (newImageUris.isNotEmpty()) {
                            Text(
                                "New Images:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            LazyRow(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(newImageUris) { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = rememberAsyncImagePainter(uri),
                                            contentDescription = "New Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        IconButton(
                                            onClick = {
                                                newImageUris = newImageUris.filter { it != uri }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .background(Color.Red.copy(alpha = 0.7f), shape = CircleShape)
                                                .size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove Image",
                                                tint = Color.White,
                                                modifier = Modifier.size(8.dp)
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
                            availableAmenities.forEach { amenity ->
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
                        val allImages = existingImages.isNotEmpty() || newImageUris.isNotEmpty()
                        val validation = validateRoomInputs(
                            inputs,
                            selectedAmenities.value,
                            if (allImages) listOf(Uri.EMPTY) else emptyList() // Simulate having images
                        )
                        if (validation.isValid) {
                            onSave(inputs, selectedAmenities.value.toList(), newImageUris, existingImages)
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
                    Text("Save Changes")
                }
            }
        }
    }
}
