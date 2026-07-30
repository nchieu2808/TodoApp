package com.nch.todoapp.ui.create

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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTodoScreen(
    viewModel: CreateTodoViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val error by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    
    var showDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { imageUrl = it.toString() } }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) tempPhotoUri?.let { imageUrl = it.toString() }
        }
    )

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
            onAddPhotoClick = { showDialog = true },
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = if (dueDate != null) formatDateTime(dueDate) else "",
                onValueChange = { if (it.isEmpty()) onDueDateChange(null) },
                label = { Text("Due Date (Optional)") },
                modifier = Modifier.weight(0.8f),
                readOnly = true,
                enabled = !isLoading,
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
                modifier = Modifier.weight(0.2f),
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Pick Date"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = onImageUrlChange,
                label = { Text("Image URL") },
                modifier = Modifier.weight(0.8f),
                enabled = !isLoading
            )

            IconButton(
                onClick = onAddPhotoClick,
                modifier = Modifier.weight(0.2f),
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Add Image"
                )
            }
        }

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
