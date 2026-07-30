package com.nch.todoapp.ui.details

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.nch.todoapp.data.model.TodoItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    id: String,
    viewModel: DetailsViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val item by viewModel.todoItem.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editTitle by remember(item) { mutableStateOf(item?.title ?: "") }
    var editDescription by remember(item) { mutableStateOf(item?.description ?: "") }
    var editImageUrl by remember(item) { mutableStateOf(item?.imageUrl ?: "") }
    var editDueDate by remember(item) { mutableStateOf(item?.dueDate) }

    var showDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { editImageUrl = it.toString() } }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) tempPhotoUri?.let { editImageUrl = it.toString() }
        }
    )

    LaunchedEffect(id) {
        viewModel.loadDetails(id)
    }

    if (showDialog) {
        ImageSourceDialog(
            onDismiss = { showDialog = false },
            onLibrarySelect = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
                showDialog = false
            },
            onCameraSelect = {
                val file = createImageFile(context)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
                showDialog = false
            }
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
                onAddPhotoClick = { showDialog = true },
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
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = if (dueDate != null) formatDateTime(dueDate) else "",
            onValueChange = { },
            label = { Text("Due Date (Optional)") },
            modifier = Modifier.weight(0.8f),
            readOnly = true,
            trailingIcon = {
                if (dueDate != null) {
                    IconButton(onClick = { onDueDateChange(null) }) {
                        Text("X")
                    }
                }
            }
        )

        IconButton(
            onClick = { datePickerDialog.show() },
            modifier = Modifier.weight(0.2f)
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = "Pick Date"
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = imageUrl,
            onValueChange = onImageUrlChange,
            label = { Text("Image URL") },
            modifier = Modifier.weight(0.8f)
        )

        IconButton(
            onClick = onAddPhotoClick,
            modifier = Modifier.weight(0.2f)
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Add Image"
            )
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onSaveClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading && title.isNotBlank()
    ) {
        Text("Save Changes")
    }
}

@Composable
private fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onLibrarySelect: () -> Unit,
    onCameraSelect: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Image") },
        text = { Text("Choose a source for your image.") },
        confirmButton = {
            TextButton(onClick = onCameraSelect) {
                Text("Camera")
            }
        },
        dismissButton = {
            TextButton(onClick = onLibrarySelect) {
                Text("Library")
            }
        }
    )
}

private fun createImageFile(context: android.content.Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
