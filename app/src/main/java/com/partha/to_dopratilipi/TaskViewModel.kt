package com.partha.to_dopratilipi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.partha.to_dopratilipi.room.TaskDatabase
import com.partha.to_dopratilipi.room.TaskEntity
import com.partha.to_dopratilipi.room.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    val allTasks = MutableLiveData<List<TaskEntity>>()

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        viewModelScope.launch(Dispatchers.IO) {
            allTasks.postValue(repository.getAllTasks()?: emptyList())
        }
    }

    fun insert(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(task)
        // Refresh the task list to sync the newly added ID
        allTasks.postValue(repository.getAllTasks())
    }

    fun update(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(task)
    }

    fun delete(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(task)
    }

    fun insertTasks(newTasks: List<TaskEntity>) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTasks(newTasks)
    }
}
