package com.nch.todoapp.ui.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nch.todoapp.data.model.TodoItem
import com.nch.todoapp.ui.common.DueDateField
import com.nch.todoapp.ui.common.ImageSourcePickerDialog
import com.nch.todoapp.ui.common.ImageUrlField
import com.nch.todoapp.ui.common.formatDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    id: String,
    viewModel: DetailsViewModel,
    onBack: () -> Unit,
) {
    val item by viewModel.todoItem.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editTitle by remember(item) { mutableStateOf(item?.title ?: "") }
    var editDescription by remember(item) { mutableStateOf(item?.description ?: "") }
    var editImageUrl by remember(item) { mutableStateOf(item?.imageUrl ?: "") }
    var editDueDate by remember(item) { mutableStateOf(item?.dueDate) }
    var showImagePicker by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        viewModel.loadDetails(id)
    }

    if (showImagePicker) {
        ImageSourcePickerDialog(
            onDismiss = { showImagePicker = false },
            onImageSelected = { editImageUrl = it }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = { Button(onClick = onBack, enabled = !isLoading) { Text("<") } },
                actions = {
                    DetailActions(
                        itemExists = item != null,
                        isLoading = isLoading,
                        onEditClick = { isEditing = !isEditing },
                        onDeleteClick = { viewModel.deleteTodo(id, onBack) }
                    )
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            DetailContentWrapper(
                isLoading = isLoading,
                error = error,
                item = item,
                isEditing = isEditing,
                editTitle = editTitle,
                onTitleChange = { editTitle = it },
                editDescription = editDescription,
                onDescriptionChange = { editDescription = it },
                editImageUrl = editImageUrl,
                onImageUrlChange = { editImageUrl = it },
                editDueDate = editDueDate,
                onDueDateChange = { editDueDate = it },
                onAddPhotoClick = { showImagePicker = true },
                onSaveClick = {
                    viewModel.updateTodo(editTitle, editDescription, editImageUrl, editDueDate)
                    isEditing = false
                }
            )
        }
    }
}

@Composable
private fun DetailActions(
    itemExists: Boolean,
    isLoading: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    if (!itemExists) return
    IconButton(onClick = onEditClick, enabled = !isLoading) {
        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
    }
    IconButton(onClick = onDeleteClick, enabled = !isLoading) {
        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
    }
}

@Composable
private fun DetailContentWrapper(
    isLoading: Boolean,
    error: String?,
    item: TodoItem?,
    isEditing: Boolean,
    editTitle: String,
    onTitleChange: (String) -> Unit,
    editDescription: String,
    onDescriptionChange: (String) -> Unit,
    editImageUrl: String,
    onImageUrlChange: (String) -> Unit,
    editDueDate: Long?,
    onDueDateChange: (Long?) -> Unit,
    onAddPhotoClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isLoading && item == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        if (item == null) {
            Text("Task not found.")
            return
        }

        if (isEditing) {
            EditDetailContent(
                title = editTitle,
                onTitleChange = onTitleChange,
                description = editDescription,
                onDescriptionChange = onDescriptionChange,
                imageUrl = editImageUrl,
                onImageUrlChange = onImageUrlChange,
                dueDate = editDueDate,
                onDueDateChange = onDueDateChange,
                onAddPhotoClick = onAddPhotoClick,
                onSaveClick = onSaveClick,
                isLoading = isLoading
            )
        } else {
            ViewDetailContent(item = item)
        }
    }

    if (isLoading && item != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ViewDetailContent(item: TodoItem) {
    Text(text = item.title, style = MaterialTheme.typography.headlineMedium)

    item.dueDate?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Due: ${formatDateTime(it)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }

    item.description?.let { desc ->
        if (desc.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = desc, style = MaterialTheme.typography.bodyLarge)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    item.imageUrl?.let { url ->
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
    Text(
        text = if (item.isCompleted) "Status: Completed" else "Status: Pending",
        color = if (item.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    )
}

@Composable
private fun EditDetailContent(
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
    isLoading: Boolean
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text("Title") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text("Description (Optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
    Spacer(modifier = Modifier.height(16.dp))

    DueDateField(
        dueDate = dueDate,
        onDueDateChange = onDueDateChange,
        enabled = !isLoading
    )

    Spacer(modifier = Modifier.height(16.dp))
    ImageUrlField(
        imageUrl = imageUrl,
        onImageUrlChange = onImageUrlChange,
        onAddPhotoClick = onAddPhotoClick,
        enabled = !isLoading
    )

    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onSaveClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading && title.isNotBlank()
    ) {
        Text("Save Changes")
    }
}
