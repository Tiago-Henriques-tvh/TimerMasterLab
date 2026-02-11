package com.tiagohenriques.project.timermasterlab.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

@Composable
fun <T> ReorderableLazyColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable (item: T, isDragging: Boolean) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val dragDropState = rememberDragDropListState(lazyListState, onMove)
    val scope = rememberCoroutineScope()

    LazyColumn(
        state = lazyListState,
        modifier = modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = { dragDropState.onDragStart(it) },
                onDragCancel = { dragDropState.onDragInterrupted() },
                onDragEnd = { dragDropState.onDragInterrupted() },
                onDrag = { change, offset ->
                    change.consume()
                    dragDropState.onDrag(offset)

                    dragDropState.checkOverscroll()
                        .takeIf { it != 0f }
                        ?.let { scroll ->
                            scope.launch {
                                lazyListState.scrollBy(scroll)
                            }
                        }
                }
            )
        }
    ) {
        itemsIndexed(
            items = items,
            key = if (key != null) { _, item -> key(item) } else null
        ) { index, item ->

            val displacement =
                dragDropState.elementDisplacement.takeIf {
                    index == dragDropState.currentIndexOfDraggedItem
                }

            Box(
                modifier = Modifier.graphicsLayer {
                    (displacement ?: 0f).also { translationY = it }
                }
            ) {
                itemContent(
                    item,
                    index == dragDropState.currentIndexOfDraggedItem
                )
            }
        }
    }
}

@Composable
fun rememberDragDropListState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit
): DragDropListState {
    return remember { DragDropListState(lazyListState, onMove) }
}

class DragDropListState(
    val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit
) {
    var draggingDistance by mutableFloatStateOf(0f)
        private set

    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)
        private set

    private var initiallyDraggedElement: LazyListItemInfo? = null

    val elementDisplacement: Float?
        get() {
            val current = currentIndexOfDraggedItem ?: return null
            val item = lazyListState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.index == current }
            return item?.let {
                draggingDistance - (it.offset - (initiallyDraggedElement?.offset ?: 0))
            }
        }

    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                offset.y.toInt() in item.offset..(item.offset + item.size)
            }
            ?.let {
                initiallyDraggedElement = it
                currentIndexOfDraggedItem = it.index
            }
    }

    fun onDrag(offset: Offset) {
        draggingDistance += offset.y

        val draggedItem = currentIndexOfDraggedItem ?: return
        val draggedInfo = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == draggedItem } ?: return

        val start = draggedInfo.offset + draggingDistance
        val end = start + draggedInfo.size

        lazyListState.layoutInfo.visibleItemsInfo
            .filterNot { it.index == draggedItem }
            .firstOrNull { item ->
                if (draggingDistance > 0) {
                    end > item.offset + item.size / 2
                } else {
                    start < item.offset + item.size / 2
                }
            }
            ?.let { target ->
                onMove(draggedItem, target.index)
                currentIndexOfDraggedItem = target.index
            }
    }

    fun onDragInterrupted() {
        draggingDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
    }

    fun checkOverscroll(): Float {
        val dragged = currentIndexOfDraggedItem ?: return 0f
        val item = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == dragged } ?: return 0f

        val viewportStart = lazyListState.layoutInfo.viewportStartOffset
        val viewportEnd = lazyListState.layoutInfo.viewportEndOffset

        return when {
            item.offset < viewportStart -> -20f
            item.offset + item.size > viewportEnd -> 20f
            else -> 0f
        }
    }
}

/*
val items = remember {
    mutableStateListOf("One", "Two", "Three", "Four", "Five")
}

ReorderableLazyColumn(
    items = items,
    onMove = { from, to ->
        items.add(to, items.removeAt(from))
    },
    key = { it }
) { item, isDragging ->
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = if (isDragging) 8.dp else 2.dp
    ) {
        Text(
            text = item,
            modifier = Modifier.padding(16.dp)
        )
    }
}
*/
