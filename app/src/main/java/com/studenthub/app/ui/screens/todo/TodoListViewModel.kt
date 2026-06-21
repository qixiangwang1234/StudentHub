package com.studenthub.app.ui.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studenthub.app.data.entity.TodoEntity
import com.studenthub.app.domain.repository.TodoRepository
import com.studenthub.app.domain.repository.TodoWithCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TodoFilter { ALL, COURSE, STANDALONE }

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(TodoFilter.ALL)
    val filter: StateFlow<TodoFilter> = _filter.asStateFlow()

    val allTodos: StateFlow<List<TodoWithCourse>> = todoRepository.getAllTodosWithCourse()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTodos: StateFlow<List<TodoWithCourse>> = combine(
        allTodos, _filter
    ) { todos, filter ->
        when (filter) {
            TodoFilter.ALL -> todos
            TodoFilter.COURSE -> todos.filter { it.course != null }
            TodoFilter.STANDALONE -> todos.filter { it.course == null }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> = allTodos.map { todos ->
        todos.count { !it.todo.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setFilter(filter: TodoFilter) {
        _filter.value = filter
    }

    fun toggleTodo(todoWithCourse: TodoWithCourse) {
        viewModelScope.launch {
            todoRepository.toggleTodo(todoWithCourse.todo)
        }
    }

    fun deleteTodo(todoWithCourse: TodoWithCourse) {
        viewModelScope.launch {
            todoRepository.deleteTodo(todoWithCourse.todo)
        }
    }
}
