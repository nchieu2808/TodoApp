package com.nch.todoapp.ui.common

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun DueDateField(
    dueDate: Long?,
    onDueDateChange: (Long?) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    dueDate?.let { calendar.timeInMillis = it }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    onDueDateChange(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = if (dueDate != null) formatDateTime(dueDate) else "",
            onValueChange = { if (it.isEmpty()) onDueDateChange(null) },
            label = { Text("Due Date (Optional)") },
            modifier = Modifier.weight(0.8f),
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                if (dueDate != null) {
                    IconButton(onClick = { onDueDateChange(null) }, enabled = enabled) {
                        Text("X")
                    }
                }
            }
        )

        IconButton(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.weight(0.2f),
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = "Pick Date"
            )
        }
    }
}

@Composable
fun ImageUrlField(
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    onAddPhotoClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = imageUrl,
            onValueChange = onImageUrlChange,
            label = { Text("Image URL") },
            modifier = Modifier.weight(0.8f),
            enabled = enabled
        )

        IconButton(
            onClick = onAddPhotoClick,
            modifier = Modifier.weight(0.2f),
            enabled = enabled
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Add Image"
            )
        }
    }
}
