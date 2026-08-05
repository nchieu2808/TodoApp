package com.nch.todoapp.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nch.todoapp.ui.common.DueDateField
import com.nch.todoapp.ui.common.ImageSourcePickerDialog
import com.nch.todoapp.ui.common.ImageUrlField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTodoScreen(
    viewModel: CreateTodoViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val error by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }

    if (showImagePicker) {
        ImageSourcePickerDialog(
            onDismiss = { showImagePicker = false },
            onImageSelected = { imageUrl = it }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Task") },
                navigationIcon = { Button(onClick = onBack, enabled = !isLoading) { Text("<") } }
            )
        }
    ) { padding ->
        CreateTodoContent(
            title = title,
            onTitleChange = { title = it; viewModel.clearError() },
            description = description,
            onDescriptionChange = { description = it },
            imageUrl = imageUrl,
            onImageUrlChange = { imageUrl = it },
            dueDate = dueDate,
            onDueDateChange = { dueDate = it },
            onAddPhotoClick = { showImagePicker = true },
            onSaveClick = { viewModel.saveTodo(title, description, imageUrl, dueDate, onSuccess) },
            isLoading = isLoading,
            error = error,
            padding = padding
        )
    }
}

@Composable
private fun CreateTodoContent(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    dueDate: Long?,
    onDueDateChange: (Long?) -> Unit,
    onAddPhotoClick: () -> Unit,
    onSaveClick: () -> Unit,
    isLoading: Boolean,
    error: String?,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            enabled = !isLoading
        )

        DueDateField(
            dueDate = dueDate,
            onDueDateChange = onDueDateChange,
            enabled = !isLoading
        )

        ImageUrlField(
            imageUrl = imageUrl,
            onImageUrlChange = onImageUrlChange,
            onAddPhotoClick = onAddPhotoClick,
            enabled = !isLoading
        )

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && title.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                return@Button
            }
            Text("Save Task")
        }
    }
}
