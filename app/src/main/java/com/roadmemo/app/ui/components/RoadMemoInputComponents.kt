package com.roadmemo.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.roadmemo.app.ui.theme.RoadMemoIcons
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun RoadMemoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { hint ->
            { Text(hint) }
        },
        supportingText = supportingText?.let { text ->
            { Text(text) }
        },
        singleLine = singleLine,
        minLines = minLines,
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
            capitalization = capitalization,
        ),
        shape = MaterialTheme.shapes.large,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadMemoDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val selectedDateMillis = remember(value) { value.toDateMillisOrNull() }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        label = { Text(label) },
        placeholder = placeholder?.let { hint ->
            { Text(hint) }
        },
        supportingText = supportingText?.let { text ->
            { Text(text) }
        },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = RoadMemoIcons.Calendar,
                    contentDescription = "选择日期",
                )
            }
        },
        singleLine = true,
        readOnly = true,
        isError = isError,
        shape = MaterialTheme.shapes.large,
    )

    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.toLocalDateText()
                            ?.let(onValueChange)
                        showDatePicker = false
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun String.toDateMillisOrNull(): Long? = runCatching {
    LocalDate.parse(trim())
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli()
}.getOrNull()

private fun Long.toLocalDateText(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toString()
