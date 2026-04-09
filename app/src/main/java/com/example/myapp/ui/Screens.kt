package com.example.myapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapp.data.Expense
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    navController: NavController
) {
    val expenses by viewModel.allExpenses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Трекер расходов") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_expense/-1") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(expenses) { expense ->
                ExpenseItem(
                    expense = expense,
                    onClick = { navController.navigate("add_expense/${expense.id}") },
                    onDelete = { viewModel.deleteExpense(expense) }
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.title, style = MaterialTheme.typography.titleMedium)
                if (expense.photoUri != null) {
                    Text(text = "📷 Фото прикреплено", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${expense.amount} ₽", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    navController: NavController,
    expenseId: Int
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Загружаем данные если это редактирование
    LaunchedEffect(expenseId) {
        if (expenseId != -1) {
            val expense = viewModel.getExpenseById(expenseId)
            if (expense != null) {
                title = expense.title
                amount = expense.amount.toString()
                photoUri = expense.photoUri?.let { Uri.parse(it) }
            }
        }
    }

    val context = LocalContext.current
    
    // Camera logic
    val photoFile = remember { 
        File(context.externalCacheDir, "expense_${System.currentTimeMillis()}.jpg") 
    }
    val tempUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                photoUri = tempUri
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (expenseId == -1) "Новый расход" else "Редактирование") })
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Сумма") },
                modifier = Modifier.fillMaxWidth()
            )

            // Отображение фото с кнопкой удаления
            if (photoUri != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Expense Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { cameraLauncher.launch(tempUri) }, // По клику можно переснять
                        contentScale = ContentScale.Crop
                    )
                    
                    // Кнопка удаления фото
                    IconButton(
                        onClick = { photoUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), 
                                shape = RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete, 
                            contentDescription = "Удалить фото",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Button(
                onClick = { cameraLauncher.launch(tempUri) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (photoUri != null) "Переснять фото" else "Сделать фото чека")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (title.isNotBlank() && amount.isNotBlank()) {
                        val amountVal = amount.toDoubleOrNull() ?: 0.0
                        if (expenseId == -1) {
                            viewModel.addExpense(title, amountVal, photoUri?.toString())
                        } else {
                            viewModel.updateExpense(expenseId, title, amountVal, photoUri?.toString())
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}