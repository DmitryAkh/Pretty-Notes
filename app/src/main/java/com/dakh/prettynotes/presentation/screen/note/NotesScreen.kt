package com.dakh.prettynotes.presentation.screen.note

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize.Min
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.dakh.prettynotes.R
import com.dakh.prettynotes.domain.entity.ContentItem
import com.dakh.prettynotes.domain.entity.Note
import com.dakh.prettynotes.presentation.ui.theme.OtherNotesColors
import com.dakh.prettynotes.presentation.ui.theme.PinnedNotesColors
import com.dakh.prettynotes.presentation.utils.DateFormatter
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
    onNoteClick: (Note) -> Unit,
    onAddNoteClick: () -> Unit,
) {
    val state by viewModel.state.collectAsState()


    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
                coroutineScope.launch {
                    viewModel.processCommand(NotesCommand.CloseAllSwipes)
                }

            }
    ) {
        Scaffold(
            modifier = modifier,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        onAddNoteClick()
                    },
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_note),
                        contentDescription = "Button add note"
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
            ) {

                item {
                    Title(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(R.string.all_notes)
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    SearchBar(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        query = state.query,
                        onQueryChange = {
                            viewModel.processCommand(NotesCommand.InputSearchQuery(it))
                        }
                    )
                }

                if (state.pinnedNotes.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    if (state.pinnedNotes.isNotEmpty()) {
                        item {
                            Subtitle(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                text = stringResource(R.string.pinned)
                            )
                        }
                    }

                    item {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 24.dp, start = 24.dp, end = 24.dp)
                        )
                        {
                            state.pinnedNotes.forEachIndexed { index, note ->
                                item(key = note.id) {
                                    NoteCard(
                                        modifier = Modifier
                                            .widthIn(max = 150.dp, min = 100.dp),
                                        note = note,
                                        onNoteClick = {
                                            onNoteClick(note)
                                        },
                                        onLongClick = {
                                            viewModel.processCommand(
                                                NotesCommand.SwitchPinnedStatus(
                                                    it.id
                                                )
                                            )
                                        },
                                        backgroundColor = PinnedNotesColors[index % PinnedNotesColors.size]
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    if (state.otherNotes.isNotEmpty()) {
                        item {
                            Subtitle(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                text = stringResource(R.string.others)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                itemsIndexed(
                    items = state.otherNotes,
                    key = { _, note -> note.id }
                ) { index, note ->

                    SwipeActionsNoteItem(
                        modifier = Modifier,
                        onDeleteClick = { viewModel.processCommand(NotesCommand.DeleteNote(note.id)) },
                        onPinClick = { viewModel.processCommand(NotesCommand.SwitchPinnedStatus(note.id)) },
                        noteId = note.id,
                        isSwiped = state.notesSwipeStatus[note.id] ?: false,
                        onSwipe = { viewModel.processCommand(NotesCommand.SwipeNote(note.id, true)) },
                        onClose = { viewModel.processCommand(NotesCommand.SwipeNote(note.id, false)) }
                    ) {
                        NoteCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            note = note,
                            onNoteClick = {
                                onNoteClick(note)
                            },
                            onLongClick = {
                                viewModel.processCommand(NotesCommand.SwitchPinnedStatus(it.id))
                            },
                            backgroundColor = OtherNotesColors[index % OtherNotesColors.size]
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                }
            }
            if (state.otherNotes.isEmpty() && state.pinnedNotes.isEmpty()) {
                Placeholder(modifier)
            }
        }
    }

}

@Composable
private fun Title(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    TextField(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ),
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = stringResource(R.string.search),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_notes),
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        trailingIcon = {
            if (query.isNotEmpty()) {
                Icon(
                    modifier = Modifier.clickable {
                        onQueryChange("")
                    },
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.clear_search_area),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
private fun Subtitle(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    backgroundColor: Color,
    onNoteClick: (Note) -> Unit,
    onLongClick: (Note) -> Unit,
) {
    val thereIsImage = note.content.filterIsInstance<ContentItem.Image>()
        .firstOrNull()?.url

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = { onNoteClick(note) },
                onLongClick = { onLongClick(note) },
            )
    ) {
        if (thereIsImage != null) {
            TitleAndDateWithImage(
                modifier = Modifier,
                url = thereIsImage,
                title = note.title,
                date = DateFormatter.formatDateToString(note.updatedAt)
            )
        } else {
            TitleAndDate(
                modifier = Modifier,
                title = note.title,
                date = DateFormatter.formatDateToString(note.updatedAt)
            )
        }
        note
            .content
            .filterIsInstance<ContentItem.Text>()
            .filter { it.content.isNotBlank() }
            .joinToString("\n") { it.content }
            .takeIf { it.isNotBlank() }
            ?.let {
                Text(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 16.dp, bottom = 16.dp),
                    text = it,
                    maxLines = 3,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis
                )
            }

    }
}

@Composable
fun Placeholder(modifier: Modifier) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.placeholder),
                contentDescription = stringResource(R.string.no_notes),
                tint = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = modifier.height(16.dp))
            Text(
                text = stringResource(R.string.add_your_first_note),
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun TitleAndDateWithImage(
    modifier: Modifier,
    url: String,
    title: String,
    date: String,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        AsyncImage(
            modifier = Modifier
                .heightIn(max = 105.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            model = url,
            contentDescription = stringResource(R.string.first_image_from_note),
            contentScale = ContentScale.FillWidth
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )
        Text(
            modifier = Modifier
                .padding(top = 24.dp, start = 24.dp),
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onPrimary,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .padding(top = 48.dp, start = 24.dp),
            text = date,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
fun TitleAndDate(
    modifier: Modifier,
    title: String,
    date: String,
) {
    Text(
        modifier = modifier
            .padding(top = 16.dp, start = 24.dp),
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        color = MaterialTheme.colorScheme.onSurface,
        overflow = TextOverflow.Ellipsis
    )
    Text(
        modifier = modifier
            .padding(top = 8.dp, start = 24.dp),
        text = date,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private enum class SwipeAnchor { Closed, Actions }


@Composable
private fun SwipeActionsNoteItem(
    modifier: Modifier = Modifier,
    noteId: Int,
    isSwiped: Boolean,
    onDeleteClick: () -> Unit,
    onPinClick: () -> Unit,
    onSwipe: (Int) -> Unit,
    onClose: (Int) -> Unit,
    content: @Composable () -> Unit,
) {

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val actionWidth = screenWidthDp * 0.85f

    val density = LocalDensity.current

    val actionsPx = with(density) { actionWidth.toPx() }
    val anchors = DraggableAnchors {
        SwipeAnchor.Closed at 0f
        SwipeAnchor.Actions at -actionsPx
    }

    val swipeState = remember {
        AnchoredDraggableState(
            initialValue = SwipeAnchor.Closed,
            anchors = anchors
        )
    }
    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = swipeState,
        positionalThreshold = { distance: Float -> distance * 0.5f },
    )

    LaunchedEffect(isSwiped) {
        if (isSwiped) {
            swipeState.animateTo(SwipeAnchor.Actions)
        } else {
            swipeState.animateTo(SwipeAnchor.Closed)
        }
    }

    LaunchedEffect(swipeState) {
        snapshotFlow { swipeState.currentValue }.collect { value ->
            when (value) {
                SwipeAnchor.Actions -> onSwipe(noteId)
                SwipeAnchor.Closed -> onClose(noteId)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Min)
    ) {
        Row(
            modifier = Modifier
                .matchParentSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundedActionButton(
                modifier = modifier,
                text = stringResource(R.string.pin),
                Color(0xFF8275FF),
                Icons.Default.Folder,
                contentDescription = stringResource(R.string.pin_note),
                onClick = onPinClick
            )
            Spacer(modifier = Modifier.width(16.dp))
            RoundedActionButton(
                modifier = modifier,
                text = stringResource(R.string.delete),
                Color(0xFFFF4B4B),
                Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_note),
                onClick = onDeleteClick
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(swipeState.requireOffset().roundToInt(), 0) }
                .anchoredDraggable(
                    state = swipeState,
                    orientation = Orientation.Horizontal,
                    flingBehavior = flingBehavior
                )
        ) {
            content()
        }
    }
}

@Composable
private fun RoundedActionButton(
    modifier: Modifier,
    text: String,
    color: Color,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text(text = text, color = Color.White, fontWeight = FontWeight.Medium)
    }
}


