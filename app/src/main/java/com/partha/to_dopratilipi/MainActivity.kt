package com.partha.to_dopratilipi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.partha.to_dopratilipi.room.TaskEntity
import com.partha.to_dopratilipi.ui.theme.TODOPratilipiTheme
import com.partha.to_dopratilipi.util.DraggableItem
import com.partha.to_dopratilipi.util.dragContainer
import com.partha.to_dopratilipi.util.rememberDragDropState

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
    val tasks = remember { mutableStateListOf<TaskEntity>() }
    val observedTasks by viewModel.allTasks.observeAsState(emptyList())
    LaunchedEffect(observedTasks) {
        tasks.clear()
        tasks.addAll(observedTasks)
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var currentItem by remember { mutableStateOf<TaskEntity?>(null) }
    var editedText by remember { mutableStateOf("") }
    var editedIndex by remember { mutableIntStateOf(-1) }
    //val context = LocalContext.current

    // Drag and drop state
    val listState = rememberLazyListState()
    val dragDropState = rememberDragDropState(listState) { fromIndex, toIndex ->
        tasks.add(toIndex, tasks.removeAt(fromIndex))
        //Toast.makeText(context, "Order Updated", Toast.LENGTH_SHORT).show()
    }

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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .dragContainer(dragDropState),
            state = listState
        ) {
            itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                DraggableItem(dragDropState, index) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 4.dp else 1.dp)
                    ListItem(
                        text = task.taskName,
                        onEdit = {
                            currentItem = task
                            editedText = task.taskName
                            isEditing = true
                            showEditDialog = true
                            editedIndex = index
                        },
                        onDelete = {
                            viewModel.delete(task)
                            tasks.removeAt(index)
                        },
                        elevation = elevation
                    )
                }
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
                        tasks[editedIndex] = currentItem!!.copy(taskName = editedText)
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
    onDelete: () -> Unit,
    elevation: Dp = 1.dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
            .zIndex(if (elevation > 1.dp) 1f else 0f)
            .graphicsLayer {
                if (elevation > 1.dp) {
                    translationY = elevation.value
                }
            }
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

@Preview(showBackground = true)
@Composable
fun ListItemPreview() {
    TODOPratilipiTheme {
        ListItem(text = "Test Task", onEdit = {}, onDelete = {})
    }
}

