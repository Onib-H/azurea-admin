package com.example.azureaadmin.ui.screens.admin.amenities

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun EditAmenityDialog(
    show: Boolean,
    currentValue: String,
    onDismiss: () -> Unit,
    onUpdate: (String) -> Unit
) {
    if (show) {
        var description by remember { mutableStateOf(currentValue) }

        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Row {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        if (description.isNotBlank()) {
                            onUpdate(description)
                            onDismiss()
                        }
                    }) {
                        Text("Update")
                    }
                }
            },
            title = { Text("Edit Amenity", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            containerColor = Color.White
        )
    }
}








