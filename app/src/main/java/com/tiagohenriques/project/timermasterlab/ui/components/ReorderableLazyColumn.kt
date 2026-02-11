package com.tiagohenriques.project.timermasterlab.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

@Composable
fun <T> ReorderableLazyColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    key: ((item: T) -> Any)? = null,
    state: LazyListState = rememberLazyListState(),
    itemContent: @Composable (item: T, isDragging: Boolean, dragModifier: Modifier) -> Unit
) {
    val lazyListState = state
    val dragDropState = rememberDragDropListState(lazyListState)
    val scope = rememberCoroutineScope()

    LazyColumn(
        state = lazyListState,
        modifier = modifier
    ) {
        itemsIndexed(
            items = items,
            key = if (key != null) { _, item -> key(item) } else null
        ) { index, item ->

            val displacement =
                dragDropState.elementDisplacement.takeIf {
                    index == dragDropState.currentIndexOfDraggedItem
                }

            val dragModifier = Modifier.pointerInput(index) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        val itemInfo = lazyListState.layoutInfo.visibleItemsInfo
                            .firstOrNull { it.index == index }

                        val absolute = if (itemInfo != null) {
                            Offset(offset.x, itemInfo.offset + offset.y)
                        } else {
                            offset
                        }

                        dragDropState.onDragStart(absolute)
                    },
                    onDragCancel = { dragDropState.onDragInterrupted() },
                    onDragEnd = {
                        val from = dragDropState.currentIndexOfDraggedItem
                        val target = dragDropState.computeDropTarget()
                        if (from != null && target != null && from != target) {
                            onMove(from, target)
                        }

                        dragDropState.onDragInterrupted()
                    },
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

            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .zIndex(if (index == dragDropState.currentIndexOfDraggedItem) 1f else 0f)
                    .graphicsLayer { (displacement ?: 0f).also { translationY = it } }
            ) {
                itemContent(
                    item,
                    index == dragDropState.currentIndexOfDraggedItem,
                    dragModifier
                )
            }
        }
    }
}

@Composable
fun rememberDragDropListState(
    lazyListState: LazyListState,
): DragDropListState {
    return remember { DragDropListState(lazyListState) }
}

class DragDropListState(
    val lazyListState: LazyListState,
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

    fun computeDropTarget(): Int? {
        val dragged = currentIndexOfDraggedItem ?: return null
        val baselineInfo = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == dragged }
            ?: initiallyDraggedElement
            ?: return null

        val draggedCenter = baselineInfo.offset + baselineInfo.size / 2f + draggingDistance

        val others = lazyListState.layoutInfo.visibleItemsInfo
            .filterNot { it.index == dragged }

        if (others.isEmpty()) return dragged

        val target = others.firstOrNull { other ->
            val otherCenter = other.offset + other.size / 2f
            draggedCenter < otherCenter
        } ?: others.last()

        return target.index
    }
}
