package com.harold.azureaadmin.ui.components.filters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.IconButton
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.unit.sp

// Reuse your existing FilterChipGroup for chips UI
// FilterSectionTemplate describes the initial configuration each screen will pass in
sealed class FilterSectionTemplate {
    data class ChipSection(
        val id: String,
        val label: String,
        val icon: ImageVector? = null,
        val options: List<String>,
        val selected: String = options.firstOrNull() ?: "All"
    ) : FilterSectionTemplate()

    data class NumberRangeSection(
        val id: String,
        val label: String,
        val minInitial: Double,
        val maxInitial: Double,
        val minPlaceholder: String = "",
        val maxPlaceholder: String = ""
    ) : FilterSectionTemplate()
}

data class FilterResult(
    val chipValues: Map<String, String> = emptyMap(),
    val numberRanges: Map<String, Pair<Double, Double>> = emptyMap()
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    sections: List<FilterSectionTemplate>,
    onApply: (FilterResult) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val chipState = remember { mutableStateMapOf<String, String>() }
    val rangeState = remember { mutableStateMapOf<String, Pair<String, String>>() }

    // Initialize once
    LaunchedEffect(sections) {
        sections.forEach { s ->
            when (s) {
                is FilterSectionTemplate.ChipSection ->
                    chipState.putIfAbsent(s.id, s.selected)

                is FilterSectionTemplate.NumberRangeSection -> {
                    val minStr = if (s.minInitial == 0.0) "" else s.minInitial.toInt().toString()
                    val maxStr = if (s.maxInitial == 0.0) "" else s.maxInitial.toInt().toString()
                    rangeState.putIfAbsent(s.id, Pair(minStr, maxStr))
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Render each filter section
            sections.forEach { s ->
                when (s) {
                    is FilterSectionTemplate.ChipSection -> {
                        Spacer(modifier = Modifier.height(12.dp))

                        FilterChipGroup(
                            label = s.label,
                            icon = s.icon ?: Icons.Outlined.Circle,
                            options = s.options,
                            selected = chipState[s.id] ?: s.selected,
                            onSelect = { value -> chipState[s.id] = value }
                        )
                    }

                    is FilterSectionTemplate.NumberRangeSection -> {
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = s.label,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val current = rangeState[s.id] ?: Pair("", "")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = current.first,
                                onValueChange = {
                                    rangeState[s.id] = it to current.second
                                },
                                label = { Text("Min", fontSize = 12.sp) },
                                placeholder = { Text(s.minPlaceholder) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = current.second,
                                onValueChange = {
                                    rangeState[s.id] = current.first to it
                                },
                                label = { Text("Max", fontSize = 12.sp) },
                                placeholder = { Text(s.maxPlaceholder) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedButton(
                    onClick = {
                        chipState.clear()
                        rangeState.clear()

                        sections.forEach { s ->
                            when (s) {
                                is FilterSectionTemplate.ChipSection ->
                                    chipState[s.id] = s.selected

                                is FilterSectionTemplate.NumberRangeSection -> {
                                    val minStr = if (s.minInitial == 0.0) "" else s.minInitial.toInt().toString()
                                    val maxStr = if (s.maxInitial == 0.0) "" else s.maxInitial.toInt().toString()
                                    rangeState[s.id] = minStr to maxStr
                                }
                            }
                        }
                        onReset()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset")
                }

                // ✅ Primary filled button
                Button(
                    onClick = {
                        val chips = chipState.toMap()
                        val ranges = rangeState.mapValues { entry ->
                            val min = entry.value.first.toDoubleOrNull() ?: 0.0
                            val max = entry.value.second.toDoubleOrNull() ?: 0.0
                            min to max
                        }

                        onApply(
                            FilterResult(
                                chipValues = chips,
                                numberRanges = ranges
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Apply", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
