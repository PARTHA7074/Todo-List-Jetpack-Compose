package com.partha.to_dopratilipi.room

class TaskRepository(private val taskDao: TaskDao) {

    suspend fun getAllTasks(): List<TaskEntity>? {
        return taskDao.getAllTasks()
    }

    suspend fun insert(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun update(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun delete(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun clearAllTasks() {
        taskDao.clearAllTasks()
    }

    suspend fun insertTasks(tasks: List<TaskEntity>) {
        taskDao.insertTasks(tasks)
    }

    suspend fun replaceAllTasks(tasks: List<TaskEntity>) {
        taskDao.replaceAllTasks(tasks)
    }

}