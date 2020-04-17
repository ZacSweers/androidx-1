/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.ui.core

import androidx.ui.graphics.Canvas
import androidx.ui.graphics.Color
import androidx.ui.graphics.Paint
import androidx.ui.graphics.PaintingStyle
import androidx.ui.unit.IntPx
import androidx.ui.unit.IntPxSize

internal class ModifiedLayoutNode2(
    wrapped: LayoutNodeWrapper,
    val layoutModifier: LayoutModifier2
) : DelegatingLayoutNodeWrapper<LayoutModifier2>(wrapped, layoutModifier) {

    override fun performMeasure(constraints: Constraints): Placeable = with(modifier) {
        updateLayoutDirection()
        measureResult =
            layoutNode.measureScope.measure(wrapped, constraints, layoutNode.layoutDirection!!)
        this@ModifiedLayoutNode2
    }

    override fun minIntrinsicWidth(height: IntPx): IntPx = with(modifier) {
        updateLayoutDirection()
        layoutNode.measureScope.minIntrinsicWidth(wrapped, height, layoutNode.layoutDirection!!)
    }

    override fun maxIntrinsicWidth(height: IntPx): IntPx = with(modifier) {
        updateLayoutDirection()
        layoutNode.measureScope.maxIntrinsicWidth(wrapped, height, layoutNode.layoutDirection!!)
    }

    override fun minIntrinsicHeight(width: IntPx): IntPx = with(modifier) {
        updateLayoutDirection()
        layoutNode.measureScope.minIntrinsicHeight(wrapped, width, layoutNode.layoutDirection!!)
    }

    override fun maxIntrinsicHeight(width: IntPx): IntPx = with(modifier) {
        updateLayoutDirection()
        layoutNode.measureScope.maxIntrinsicHeight(wrapped, width, layoutNode.layoutDirection!!)
    }

    override operator fun get(line: AlignmentLine): IntPx? =
        measureResult.alignmentLines.getOrElse(line, { wrapped[line] })

    override fun draw(canvas: Canvas) {
        withPositionTranslation(canvas) {
            wrapped.draw(canvas)
            if (layoutNode.requireOwner().showLayoutBounds) {
                drawBorder(canvas, modifierBoundsPaint)
            }
        }
    }

    internal companion object {
        val modifierBoundsPaint = Paint().also { paint ->
            paint.color = Color.Blue
            paint.strokeWidth = 1f
            paint.style = PaintingStyle.stroke
        }
    }

    private fun updateLayoutDirection() {
        // TODO(popam): add support to change layout direction in the layout DSL
    }
}

@Suppress("Deprecation")
internal class ModifiedLayoutNode(
    wrapped: LayoutNodeWrapper,
    layoutModifier: LayoutModifier
) : DelegatingLayoutNodeWrapper<LayoutModifier>(wrapped, layoutModifier) {
    override fun performMeasure(constraints: Constraints): Placeable = with(modifier) {
        updateLayoutDirection()
        val placeable = wrapped.measure(
            layoutNode.measureScope.modifyConstraints(constraints, layoutDirection)
        )
        val size = layoutNode.measureScope.modifySize(
            constraints,
            layoutDirection,
            IntPxSize(placeable.width, placeable.height)
        )
        val wrappedPosition = with(modifier) {
            layoutNode.measureScope.modifyPosition(
                IntPxSize(placeable.width, placeable.height),
                size,
                layoutDirection
            )
        }
        measureResult = object : MeasureScope.MeasureResult {
            override val width: IntPx = size.width
            override val height: IntPx = size.height
            override val alignmentLines: Map<AlignmentLine, IntPx> = emptyMap()
            override fun placeChildren(layoutDirection: LayoutDirection) {
                placeable.placeAbsolute(wrappedPosition)
            }
        }
        this@ModifiedLayoutNode
    }

    override fun minIntrinsicWidth(height: IntPx): IntPx = with(modifier) {
        updateLayoutDirection()
        layoutNode.measureScope.minIntrinsicWidthOf(wrapped, height, layoutDirection)
    }

    override fun maxIntrinsicWidth(height: IntPx): IntPx = with(modifier) {
        updateLayoutDirection()
        layoutNode.measureScope.maxIntrinsicWidthOf(wrapped, height, layoutDirection)
    }

    override fun minIntrinsicHeight(width: IntPx): IntPx = with(modifier) {
        updateLayoutDirection()
        layoutNode.measureScope.minIntrinsicHeightOf(wrapped, width, layoutDirection)
    }

    override fun maxIntrinsicHeight(width: IntPx): IntPx = with(modifier) {
        updateLayoutDirection()
        layoutNode.measureScope.maxIntrinsicHeightOf(wrapped, width, layoutDirection)
    }

    override operator fun get(line: AlignmentLine): IntPx? = with(modifier) {
        return layoutNode.measureScope.modifyAlignmentLine(
            line,
            super.get(line),
            layoutDirection
        )
    }

    override fun draw(canvas: Canvas) {
        withPositionTranslation(canvas) {
            wrapped.draw(canvas)
            if (layoutNode.requireOwner().showLayoutBounds) {
                drawBorder(canvas, modifierBoundsPaint)
            }
        }
    }

    internal companion object {
        val modifierBoundsPaint = Paint().also { paint ->
            paint.color = Color.Blue
            paint.strokeWidth = 1f
            paint.style = PaintingStyle.stroke
        }
    }

    override lateinit var layoutDirection: LayoutDirection

    private fun updateLayoutDirection() = with(modifier) {
        val modifiedLayoutDirection =
            layoutNode.measureScope.modifyLayoutDirection(layoutNode.layoutDirection!!)
        layoutNode.layoutDirection = modifiedLayoutDirection
        layoutDirection = modifiedLayoutDirection
    }
}