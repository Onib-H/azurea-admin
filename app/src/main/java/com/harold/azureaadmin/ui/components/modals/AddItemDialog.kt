package com.harold.azureaadmin.ui.components.modals

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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.harold.azureaadmin.ui.components.button.DropdownField
import com.harold.azureaadmin.ui.screens.admin.amenities.AmenitiesViewModel
import com.harold.azureaadmin.utils.compressImage
import com.harold.azureaadmin.utils.validateRoomInputs

//private val ValidationResult.errors: Map<String, String>
//private val ValidationResult.isValid: Boolean


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    show: Boolean,
    type: ItemType,
    onDismiss: () -> Unit,
    onSave: (Map<String, String>, List<String>, List<Uri>?) -> Unit,
    viewModel: AmenitiesViewModel = hiltViewModel()
) {
    if (!show) return

    val inputs = remember { mutableStateMapOf<String, String>() }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var validationErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val selectedAmenities = remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(show) {
        if (type == ItemType.ROOM) viewModel.fetchAmenities()
        validationErrors = emptyMap()
    }

    val amenities by viewModel.amenities.collectAsState()

    val context = LocalContext.current


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                selectedImageUris = uris.map { compressImage(context, it) }
                validationErrors -= "Images"
            }
        }
    )



    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column {
                DialogTopBar(
                    title = "Add New ${if (type == ItemType.ROOM) "Room" else "Area"}",
                    onDismiss = onDismiss
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormTextField(
                        label = if (type == ItemType.ROOM) "Room Name" else "Area Name",
                        key = "Name",
                        inputs = inputs,
                        validationErrors = validationErrors
                    )

                    if (type == ItemType.ROOM) {
                        RoomSelectors(inputs, validationErrors)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormTextField(
                            label = if (type == ItemType.ROOM) "Max Guests" else "Capacity",
                            key = "Capacity",
                            inputs = inputs,
                            validationErrors = validationErrors,
                            modifier = Modifier.weight(1f),
                            numberOnly = true
                        )
                        FormTextField(
                            label = "Price (₱)",
                            key = "Price",
                            inputs = inputs,
                            validationErrors = validationErrors,
                            modifier = Modifier.weight(1f),
                            numberOnly = true
                        )
                    }

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

                    ImagePickerSection(
                        selectedImageUris = selectedImageUris,
                        validationErrors = validationErrors,
                        onPickImages = { imagePickerLauncher.launch("image/*") },
                        onRemoveImage = { uri -> selectedImageUris -= uri }
                    )

                    if (type == ItemType.ROOM) {
                        AmenitiesSection(
                            amenities = amenities.map { it.description },
                            selectedAmenities = selectedAmenities,
                            validationErrors = validationErrors
                        )
                    }
                }

                SaveButton(
                    label = "Create ${if (type == ItemType.ROOM) "Room" else "Area"}",
                    onClick = {
                        val validation = validateRoomInputs(
                            inputs,
                            selectedAmenities.value,
                            selectedImageUris
                        )

                        if (validation.isValid) {
                            onSave(inputs, selectedAmenities.value.toList(), selectedImageUris)
                            inputs.clear()
                            selectedAmenities.value = emptySet()
                            selectedImageUris = emptyList()
                            validationErrors = emptyMap()
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogTopBar(title: String, onDismiss: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        )
    )
}



@Composable
fun FormTextField(
    label: String,
    key: String,
    inputs: MutableMap<String, String>,
    validationErrors: Map<String, String>,
    modifier: Modifier = Modifier.fillMaxWidth(),
    numberOnly: Boolean = false,
    multiLine: Boolean = false
) {
    OutlinedTextField(
        value = inputs[key] ?: "",
        onValueChange = {
            inputs[key] = it
            validationErrors - key
        },
        label = { Text(label.replaceFirstChar { it.uppercase() }) },
        keyboardOptions = if (numberOnly) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        singleLine = !multiLine,
        maxLines = if (multiLine) 3 else 1,
        isError = validationErrors.containsKey(key),
        supportingText = {
            validationErrors[key]?.let { Text(it) }
        },
        modifier = modifier
    )
}


@Composable
fun RoomSelectors(inputs: MutableMap<String, String>, validationErrors: Map<String, String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DropdownField(
            label = "Room Type",
            value = inputs["Room Type"] ?: "Premium",
            options = listOf("Premium", "Suites"),
            onSelect = { inputs["Room Type"] = it },
            modifier = Modifier.weight(1f),
            isError = validationErrors.containsKey("Room Type"),
            errorMessage = validationErrors["Room Type"]
        )

        DropdownField(
            label = "Bed Type",
            value = inputs["Bed Type"] ?: "Single",
            options = listOf("Single", "Double", "Twin", "King", "Queen"),
            onSelect = { inputs["Bed Type"] = it },
            modifier = Modifier.weight(1f),
            isError = validationErrors.containsKey("Bed Type"),
            errorMessage = validationErrors["Bed Type"]
        )
    }
}


@Composable
fun ImagePickerSection(
    selectedImageUris: List<Uri>,
    validationErrors: Map<String, String>,
    onPickImages: () -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    Column {
        OutlinedButton(
            onClick = onPickImages,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Upload Images (${selectedImageUris.size} selected)")
        }

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
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Selected Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = { onRemoveImage(uri) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                            .size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        validationErrors["Images"]?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
fun AmenitiesSection(
    amenities: List<String>,
    selectedAmenities: MutableState<Set<String>>,
    validationErrors: Map<String, String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Amenities", fontWeight = FontWeight.Medium)

        amenities.forEach { amenity ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = amenity in selectedAmenities.value,
                    onCheckedChange = { isChecked ->
                        selectedAmenities.value =
                            if (isChecked) selectedAmenities.value + amenity
                            else selectedAmenities.value - amenity
                    }
                )
                Text(amenity)
            }
        }

        validationErrors["Amenities"]?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
fun SaveButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(label)
    }
}


