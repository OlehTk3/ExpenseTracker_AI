package com.example.myapp

import com.example.myapp.data.Expense
import com.example.myapp.data.ExpenseDao
import com.example.myapp.data.ExpenseRepository
import com.example.myapp.ui.ExpenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    private lateinit var viewModel: ExpenseViewModel
    private lateinit var repository: ExpenseRepository
    
    @Mock
    private lateinit var expenseDao: ExpenseDao

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Mock DAO behavior
        Mockito.`when`(expenseDao.getAllExpenses()).thenReturn(flowOf(emptyList()))

        repository = ExpenseRepository(expenseDao)
        viewModel = ExpenseViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addExpense calls insert in repository`() = runTest {
        val title = "Test Expense"
        val amount = 100.0
        
        viewModel.addExpense(title, amount)
        advanceUntilIdle() // Wait for coroutines

        // Verify that insertExpense was called with correct data
        Mockito.verify(expenseDao).insertExpense(
            org.mockito.kotlin.check { 
                assertEquals(title, it.title)
                assertEquals(amount, it.amount, 0.01)
            }
        )
    }

    @Test
    fun `deleteExpense calls delete in repository`() = runTest {
        val expense = Expense(id = 1, title = "Delete Me", amount = 50.0)
        
        viewModel.deleteExpense(expense)
        advanceUntilIdle()

        Mockito.verify(expenseDao).deleteExpense(expense)
    }
}