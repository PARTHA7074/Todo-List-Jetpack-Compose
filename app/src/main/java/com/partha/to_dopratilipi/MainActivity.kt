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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
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
                Scaffold/*(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = "To-Do List") },
                            colors = TopAppBarDefaults.mediumTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceDim,
                                titleContentColor = Color.Black
                            )
                        )
                    }
                )*/ { innerPadding ->
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

    var newTaskText by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var currentItem by remember { mutableStateOf<TaskEntity?>(null) }
    var editedText by remember { mutableStateOf("") }
    var editedIndex by remember { mutableIntStateOf(-1) }

    // Drag and drop state
    val listState = rememberLazyListState()
    val dragDropState = rememberDragDropState(listState, onMove = { fromIndex, toIndex ->
        // ID swapping is needed for storing the order in database
        val id = tasks[fromIndex].id
        tasks[fromIndex].id = tasks[toIndex].id
        tasks[toIndex].id = id

        tasks.add(toIndex, tasks.removeAt(fromIndex))
    }, onDragEnd = {
        viewModel.insertTasks(tasks)
    })

    Scaffold(
        modifier = modifier
    ) {
        Column(modifier = modifier) {
            Text(
                text = "To-Do List",
                fontSize = 25.sp,
                modifier = Modifier.padding(bottom = 8.dp, start = 16.dp, end = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // New Task Input with Add Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    placeholder = { Text("Add your new todo") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),

                )
                Button(
                    onClick = {
                        if (newTaskText.isNotEmpty()) {
                            viewModel.insert(TaskEntity(taskName = newTaskText))
                            newTaskText = ""
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
                }
            }

            LazyColumn(
                modifier = Modifier
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .zIndex(if (elevation > 1.dp) 1f else 0f),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
                .graphicsLayer {
                    if (elevation > 1.dp) {
                        translationY = elevation.value
                    }
                }
        ) {
            Text(
                text = text,
                letterSpacing = 0.01.em,
                fontSize = 16.sp
            )

            // Menu Button for Edit and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
}



@Composable
fun EditItemDialog(
    title: String = "Add Task",
    text: String = "",
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
    onTextChange: (String) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            TextField(
                value = text,
                onValueChange = onTextChange
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

@Preview(showBackground = true)
@Composable
fun EditItemDialogPreview() {
    TODOPratilipiTheme {
        EditItemDialog()
    }
}

