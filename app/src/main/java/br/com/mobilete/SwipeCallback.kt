package br.com.mobilete

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

abstract class SwipeCallback(context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT ) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24dp)
    private val editIcon = ContextCompat.getDrawable(context, R.drawable.ic_edit_white_24dp)
    private val intrinsicEditWidth = editIcon!!.intrinsicWidth
    private val intrinsicEditHeight = editIcon!!.intrinsicHeight
    private val intrinsicDeleteWidth = deleteIcon!!.intrinsicWidth
    private val intrinsicDeleteHeight = deleteIcon!!.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColorRed = Color.parseColor("#F44336")
    private val backgroundColorGreen = Color.parseColor("#209228")
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }


    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (viewHolder?.adapterPosition == 10) return 0
        return super.getMovementFlags(recyclerView!!, viewHolder!!)
    }

    override fun onMove(p0: RecyclerView, p1: RecyclerView.ViewHolder, p2: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c!!, recyclerView!!, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            //Direita
            if (dX > 0) {
                background.color = backgroundColorGreen
                background.setBounds(itemView.left + dX.toInt(), itemView.top, itemView.left, itemView.bottom)
                background.draw(c)

                val editIconTop = itemView.top + (itemHeight - intrinsicEditHeight) / 2
                val editIconMargin = (itemHeight - intrinsicEditHeight) / 2
                val editIconLeft = itemView.left + editIconMargin
                val editIconRight = itemView.left + editIconMargin + intrinsicEditWidth
                val editIconBottom = editIconTop + intrinsicEditHeight

                editIcon!!.setBounds(editIconLeft, editIconTop, editIconRight, editIconBottom)
                editIcon!!.draw(c)

            }else{ //Esquerda
                background.color = backgroundColorRed
                background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                background.draw(c)

                val deleteIconTop = itemView.top + (itemHeight - intrinsicDeleteHeight) / 2
                val deleteIconMargin = (itemHeight - intrinsicDeleteHeight) / 2
                val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicDeleteWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + intrinsicDeleteHeight

                deleteIcon!!.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                deleteIcon!!.draw(c)
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }


    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}