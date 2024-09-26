package com.partha.to_dopratilipi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
fun HomeScreen(modifier: Modifier = Modifier) {
    val items = remember { mutableStateListOf("Task 1", "Task 2", "Task 3") }
    var showEditDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var currentItemIndex by remember { mutableIntStateOf(-1) }
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
            itemsIndexed(items) { index, item ->
                ListItem(
                    text = item,
                    onEdit = {
                        currentItemIndex = index
                        editedText = items[index]
                        isEditing = true
                        showEditDialog = true
                    },
                    onDelete = {
                        items.removeAt(index)
                    },
                    index = index
                )
            }
        }

        if (showEditDialog) {
            EditItemDialog(
                title = if (isEditing) "Edit Task" else "Add Task",
                text = editedText,
                onDismiss = { showEditDialog = false },
                onConfirm = {
                    if (isEditing && currentItemIndex >= 0) {
                        items[currentItemIndex] = editedText
                    } else {
                        items.add(editedText)
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
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    index: Int
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
            IconButton(onClick = { onEdit(index) }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { onDelete(index) }) {
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
