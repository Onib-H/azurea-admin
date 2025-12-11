package com.harold.azureaadmin.ui.components.modals

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.harold.azureaadmin.data.models.Amenity
import com.harold.azureaadmin.ui.components.button.DropdownField
import com.harold.azureaadmin.ui.components.topbar.AppTopBar
import com.harold.azureaadmin.utils.calculateTotalSizeKB
import com.harold.azureaadmin.utils.clearCompressedImageCache
import com.harold.azureaadmin.utils.compressImage
import com.harold.azureaadmin.utils.validateRoomInputs
import kotlinx.coroutines.launch
import com.harold.azureaadmin.utils.compressMultipleImages


// Updated EditItemDialog with improved image compression
// Updated EditItemDialog with improved image compression
// Updated EditItemDialog with improved image compression
// Updated EditItemDialog with improved image compression
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
    var validationErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var existingImages by remember { mutableStateOf(initialImages) }
    var newImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val selectedAmenities = remember {
        mutableStateOf(initialAmenities.map { it.description }.toSet())
    }
    var isCompressing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(show) {
        inputs.clear()
        inputs.putAll(initialValues)
        validationErrors = emptyMap()
        existingImages = initialImages
        newImageUris = emptyList()
        selectedAmenities.value = initialAmenities.map { it.description }.toSet()
        // Clear old compressed images
        clearCompressedImageCache(context)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                // Calculate remaining slots (max 10 total images)
                val remainingSlots = 10 - existingImages.size
                val limitedUris = uris.take(remainingSlots)

                if (uris.size > remainingSlots) {
                    validationErrors = validationErrors + ("Images" to "Maximum 10 total images allowed. Only $remainingSlots more can be added.")
                }

                isCompressing = true
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val compressedUris = compressMultipleImages(context, limitedUris, maxSizeKB = 350)
                        val totalSizeKB = calculateTotalSizeKB(context, compressedUris)

                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            // Calculate existing images size (estimate ~250KB each if from server)
                            val estimatedExistingSize = existingImages.size * 250
                            val totalEstimatedSize = estimatedExistingSize + totalSizeKB

                            if (totalEstimatedSize > 10240) { // 10MB limit
                                validationErrors = validationErrors + ("Images" to "Total image size would exceed 10MB. Remove some existing images or select fewer new ones.")
                            } else {
                                newImageUris = compressedUris
                                if (uris.size <= remainingSlots) {
                                    validationErrors = validationErrors - "Images"
                                }
                            }
                            isCompressing = false
                        }
                    } catch (e: Exception) {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            validationErrors = validationErrors + ("Images" to "Error compressing images: ${e.message}")
                            isCompressing = false
                        }
                    }
                }
            }
        }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
            Column {
                AppTopBar(
                    title = "Edit ${if (type == ItemType.ROOM) "Room" else "Area"}",
                    onBack = onDismiss
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormTextField(
                        label = if (type == ItemType.ROOM) "Room Name" else "Area Name",
                        key = "Name",
                        inputs = inputs,
                        validationErrors = validationErrors
                    )

                    if (type == ItemType.ROOM) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DropdownField(
                                label = "Room Type",
                                value = (inputs["Room Type"] ?: "Premium").replaceFirstChar { it.uppercase() },
                                options = listOf("Premium", "Suites").map { it.replaceFirstChar { c -> c.uppercase() } },
                                onSelect = { inputs["Room Type"] = it.replaceFirstChar { c -> c.uppercase() } },
                                modifier = Modifier.weight(1f),
                                isError = validationErrors.containsKey("Room Type"),
                                errorMessage = validationErrors["Room Type"]
                            )

                            DropdownField(
                                label = "Bed Type",
                                value = (inputs["Bed Type"] ?: "Single").replaceFirstChar { it.uppercase() },
                                options = listOf("Single", "Double", "Twin", "King", "Queen").map { it.replaceFirstChar { c -> c.uppercase() } },
                                onSelect = { inputs["Bed Type"] = it.replaceFirstChar { c -> c.uppercase() } },
                                modifier = Modifier.weight(1f),
                                isError = validationErrors.containsKey("Bed Type"),
                                errorMessage = validationErrors["Bed Type"]
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormTextField(
                            label = "Capacity",
                            key = "Capacity",
                            numberOnly = true,
                            modifier = Modifier.weight(1f),
                            inputs = inputs,
                            validationErrors = validationErrors
                        )
                        DropdownField(
                            label = "Status",
                            value = (inputs["Status"] ?: "Available").replaceFirstChar { it.uppercase() },
                            options = listOf("Available", "Maintenance").map { it.replaceFirstChar { c -> c.uppercase() } },
                            onSelect = { inputs["Status"] = it },
                            modifier = Modifier.weight(1f),
                            isError = validationErrors.containsKey("Status"),
                            errorMessage = validationErrors["Status"]
                        )
                    }

                    FormTextField(
                        label = "Price (₱)",
                        key = "Price",
                        inputs = inputs,
                        validationErrors = validationErrors,
                        numberOnly = true
                    )

                    FormTextField(
                        label = "Description",
                        key = "Description",
                        inputs = inputs,
                        validationErrors = validationErrors,
                        multiLine = true
                    )

                    FormTextField(
                        label = "Discount (%)",
                        key = "Discount",
                        inputs = inputs,
                        validationErrors = validationErrors,
                        numberOnly = true
                    )

                    EditImagesSection(
                        existingImages = existingImages,
                        newImageUris = newImageUris,
                        validationErrors = validationErrors,
                        isCompressing = isCompressing,
                        onRemoveExisting = { url -> existingImages = existingImages - url },
                        onRemoveNew = { uri -> newImageUris = newImageUris - uri },
                        onPickImages = { imagePickerLauncher.launch("image/*") }
                    )

                    if (type == ItemType.ROOM) {
                        AmenitiesSection(
                            amenities = availableAmenities.map { it.description },
                            selectedAmenities = selectedAmenities,
                            validationErrors = validationErrors
                        )
                    }
                }

                SaveButton(
                    label = "Save Changes",
                    enabled = !isCompressing,
                    onClick = {
                        val hasImages = existingImages.isNotEmpty() || newImageUris.isNotEmpty()

                        val validation = validateRoomInputs(
                            inputs,
                            selectedAmenities.value,
                            if (hasImages) listOf(Uri.EMPTY) else emptyList(),
                            availableAmenities.map { it.description }
                        )

                        if (validation.isValid) {
                            onSave(inputs, selectedAmenities.value.toList(), newImageUris, existingImages)
                            onDismiss()
                        } else {
                            validationErrors = validation.errors
                        }
                    }
                )
            }
        }
    }
}

// Updated EditImagesSection with loading state
@Composable
fun EditImagesSection(
    existingImages: List<String>,
    newImageUris: List<Uri>,
    validationErrors: Map<String, String>,
    isCompressing: Boolean = false,
    onRemoveExisting: (String) -> Unit,
    onRemoveNew: (Uri) -> Unit,
    onPickImages: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Images", fontWeight = FontWeight.Medium)

        if (existingImages.isNotEmpty()) {
            Text(
                "Current Images (${existingImages.size}):",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(existingImages) { url ->
                    ImagePreviewBox(
                        painter = rememberAsyncImagePainter(url),
                        onRemove = { onRemoveExisting(url) }
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onPickImages,
            enabled = !isCompressing,
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(top = 8.dp)
        ) {
            if (isCompressing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compressing images...")
            } else {
                Text("Add New Images (${newImageUris.size} selected)")
            }
        }

        if (newImageUris.isNotEmpty()) {
            Text(
                "New Images (${newImageUris.size}):",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(newImageUris) { uri ->
                    ImagePreviewBox(
                        painter = rememberAsyncImagePainter(uri),
                        onRemove = { onRemoveNew(uri) }
                    )
                }
            }
        }

        validationErrors["Images"]?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ImagePreviewBox(
    painter: Any,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        androidx.compose.foundation.Image(
            painter = painter as androidx.compose.ui.graphics.painter.Painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color.Red.copy(alpha = 0.7f), CircleShape)
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



