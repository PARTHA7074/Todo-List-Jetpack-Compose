package com.partha.to_dopratilipi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import com.partha.to_dopratilipi.room.TaskEntity
import com.partha.to_dopratilipi.ui.theme.TODOPratilipiTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TODOPratilipiTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = "To-Do List") },
                            colors = TopAppBarDefaults.mediumTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = Color.White
                            )
                        )
                    }
                ) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.allTasks.observeAsState(emptyList())
    var showEditDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var currentItem by remember { mutableStateOf<TaskEntity?>(null) }
    var editedText by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                isEditing = false
                editedText = ""
                showEditDialog = true
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            itemsIndexed(tasks?: emptyList()) { _, task ->
                ListItem(
                    text = task.taskName,
                    onEdit = {
                        currentItem = task
                        editedText = task.taskName
                        isEditing = true
                        showEditDialog = true
                    },
                    onDelete = { viewModel.delete(task) }
                )
            }
        }

        if (showEditDialog) {
            EditItemDialog(
                title = if (isEditing) "Edit Task" else "Add Task",
                text = editedText,
                onDismiss = { showEditDialog = false },
                onConfirm = {
                    if (isEditing && currentItem != null) {
                        viewModel.update(currentItem!!.copy(taskName = editedText))
                    } else {
                        viewModel.insert(TaskEntity(taskName = editedText))
                    }
                    showEditDialog = false
                },
                onTextChange = { editedText = it }
            )
        }
    }
}

@Composable
fun ListItem(
    text: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Text(text = text, color = Color.White)

        // Menu Button for Edit and Delete
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun EditItemDialog(
    title: String = "Add Task",
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onTextChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            TextField(
                value = text,
                onValueChange = onTextChange,
                label = { Text("Task") }
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TODOPratilipiTheme {
        HomeScreen()
    }
}
