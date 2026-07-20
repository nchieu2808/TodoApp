package com.nch.todoapp.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nch.todoapp.data.model.TodoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTodoScreen(
    viewModel: ListTodoViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetails: (id: String) -> Unit
) {
    val todos by viewModel.todoList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTodos()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Tasks") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Text("+")
            }
        }
    ) {
        padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && todos.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(todos) {
                        item -> Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToDetails(item.id) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isCompleted,
                                onCheckedChange = { viewModel.toggleTodoCompletion(item) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.title,
                                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}