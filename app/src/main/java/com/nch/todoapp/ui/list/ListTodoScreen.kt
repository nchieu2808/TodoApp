package com.nch.todoapp.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nch.todoapp.data.model.TodoItem
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTodoScreen(
    viewModel: ListTodoViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetails: (id: String) -> Unit
) {
    val todos by viewModel.todoList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTodos()
    }

    val onDeleteTodo: (TodoItem) -> Unit = remember(viewModel, snackbarHostState, scope) {
        { item ->
            scope.launch {
                viewModel.hideTodo(item)
                
                snackbarHostState.currentSnackbarData?.dismiss()
                
                val displayName = item.title.truncate(10)
                
                val actionResult = withTimeoutOrNull(10.seconds) {
                    snackbarHostState.showSnackbar(
                        message = "$displayName deleted",
                        actionLabel = "revert",
                        duration = SnackbarDuration.Indefinite
                    )
                }
                
                if (actionResult == SnackbarResult.ActionPerformed) {
                    viewModel.undoHide(item)
                } else {
                    viewModel.commitDelete(item)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tasks") },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter")
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All") },
                                onClick = {
                                    viewModel.setFilter(TodoFilter.ALL)
                                    showFilterMenu = false
                                },
                                leadingIcon = { if (currentFilter == TodoFilter.ALL) Text("✓") }
                            )
                            DropdownMenuItem(
                                text = { Text("Completed") },
                                onClick = {
                                    viewModel.setFilter(TodoFilter.COMPLETED)
                                    showFilterMenu = false
                                },
                                leadingIcon = { if (currentFilter == TodoFilter.COMPLETED) Text("✓") }
                            )
                            DropdownMenuItem(
                                text = { Text("Pending") },
                                onClick = {
                                    viewModel.setFilter(TodoFilter.NOT_COMPLETED)
                                    showFilterMenu = false
                                },
                                leadingIcon = { if (currentFilter == TodoFilter.NOT_COMPLETED) Text("✓") }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = Color(0xFFEBE7CD),
                contentColor = Color.Black,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier.border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = FloatingActionButtonDefaults.shape
                )
            ) {
                Text(
                    text = "+",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        TodoListContent(
            isLoading = isLoading,
            todos = todos,
            padding = padding,
            onNavigateToDetails = onNavigateToDetails,
            onToggleTodo = { viewModel.toggleTodoCompletion(it) },
            onDeleteTodo = onDeleteTodo
        )
    }
}

@Composable
private fun TodoListContent(
    isLoading: Boolean,
    todos: List<TodoItem>,
    padding: PaddingValues,
    onNavigateToDetails: (String) -> Unit,
    onToggleTodo: (TodoItem) -> Unit,
    onDeleteTodo: (TodoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        if (isLoading && todos.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            return@Box
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = todos,
                key = { it.id }
            ) { item ->
                SwipeableTodoItem(
                    item = item,
                    onDelete = onDeleteTodo,
                    onToggle = onToggleTodo,
                    onClick = { onNavigateToDetails(item.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTodoItem(
    item: TodoItem,
    onDelete: (TodoItem) -> Unit,
    onToggle: (TodoItem) -> Unit,
    onClick: () -> Unit
) {
    val currentItem by rememberUpdatedState(item)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue != SwipeToDismissBoxValue.StartToEnd)
                return@rememberSwipeToDismissBoxState false

            onDelete(currentItem)
            true
        }
    )

    LaunchedEffect(item.id) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = false,
        backgroundContent = { DismissBackground(dismissState.targetValue) }
    ) {
        TodoRow(
            item = item,
            onToggle = { onToggle(item) },
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissBackground(targetValue: SwipeToDismissBoxValue) {
    val color = if (targetValue == SwipeToDismissBoxValue.StartToEnd) {
        Color.Red.copy(alpha = 0.8f)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.White
        )
    }
}

@Composable
private fun TodoRow(
    item: TodoItem,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                    style = MaterialTheme.typography.bodyLarge
                )
                item.dueDate?.let {
                    Text(
                        text = formatDateTime(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(start = 8.dp)
                    )
                }
            }
        HorizontalDivider()
    }
}

private fun String.truncate(maxLength: Int = 10): String =
    if (length > maxLength) "${take(maxLength)}..." else this

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
