package com.dakh.prettynotes.presentation.screens.editing

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dakh.prettynotes.R
import com.dakh.prettynotes.presentation.screens.editing.EditNoteCommand.DeleteImage
import com.dakh.prettynotes.presentation.screens.editing.EditNoteCommand.InputContent
import com.dakh.prettynotes.presentation.screens.editing.EditNoteCommand.InputTitle
import com.dakh.prettynotes.presentation.ui.theme.Content
import com.dakh.prettynotes.presentation.ui.theme.CustomIcons
import com.dakh.prettynotes.presentation.utils.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    id: Int,
    modifier: Modifier = Modifier,
    viewModel: EditNoteViewModel = hiltViewModel(
        creationCallback = { factory: EditNoteViewModel.Factory ->
            factory.create(id)
        }
    ),
    onFinished: () -> Unit,
) {
    val state = viewModel.state.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.processCommand(EditNoteCommand.AddImage(it))

            }
        }
    )

    when (val currentState = state.value) {
        is EditNoteState.Editing -> {
            Scaffold(
                modifier = modifier,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.edit_note),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        navigationIcon = {
                            Icon(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 8.dp)
                                    .clickable {
                                        viewModel.processCommand(EditNoteCommand.Back)
                                    },
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        },
                        actions = {
                                Icon(
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .clickable {
                                            imagePicker.launch("image/*")
                                        },
                                    imageVector = CustomIcons.AddPhoto,
                                    contentDescription = stringResource(R.string.add_photo_from_gallery),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .clickable {
                                            viewModel.processCommand(EditNoteCommand.Delete)
                                        },
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.delete_note)
                                )
                        }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = currentState.note.title,
                        onValueChange = { viewModel.processCommand(InputTitle(title = it)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.title),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                        }
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = DateFormatter.formatDateToString(currentState.note.updatedAt),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Content(
                        modifier = Modifier
                            .weight(1f),
                        content = currentState.note.content,
                        onDeleteImageClick = {
                            viewModel.processCommand(DeleteImage(it))
                        },
                        onTextChanged = { index, content ->
                            viewModel.processCommand(
                                InputContent(
                                    index = index,
                                    content = content
                                )
                            )
                        }
                    )
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        onClick = {
                            viewModel.processCommand(EditNoteCommand.SaveNote)
                        },
                        shape = RoundedCornerShape(10.dp),
                        enabled = currentState.isSaveEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.save_note),
                        )
                    }
                }
            }
        }

        EditNoteState.Finished -> {
            LaunchedEffect(key1 = Unit) {
                onFinished()
            }
        }

        EditNoteState.Initial -> {}
    }
}