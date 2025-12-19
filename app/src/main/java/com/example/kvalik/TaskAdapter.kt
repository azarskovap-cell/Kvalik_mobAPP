package com.example.kvalik

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kvalik.data.TaskEntity
import com.example.kvalik.databinding.ItemTaskBinding

class TaskAdapter(
    private val onChecked: (TaskEntity, Boolean) -> Unit,
    private val onClick: (TaskEntity) -> Unit
) : RecyclerView.Adapter<TaskAdapter.VH>() {

    private val items = mutableListOf<TaskEntity>()

    fun submit(list: List<TaskEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun getItem(pos: Int): TaskEntity = items[pos]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(private val b: ItemTaskBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind(task: TaskEntity) {
            b.tvTaskTitle.text = task.title
            b.tvTaskDesc.text = task.description

            // ---- Цвет приоритета (полоска слева) ----
            val colorRes = when (task.priority) {
                2 -> R.color.priority_high
                1 -> R.color.priority_medium
                else -> R.color.priority_low
            }
            b.vPriority.setBackgroundColor(ContextCompat.getColor(b.root.context, colorRes))

            // ---- чекбокс ----
            b.cbDone.setOnCheckedChangeListener(null)
            b.cbDone.isChecked = task.isDone

            // ---- визуально "выполнено" ----
            if (task.isDone) {
                b.tvTaskTitle.paintFlags = b.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                b.tvTaskTitle.alpha = 0.5f
                b.tvTaskDesc.alpha = 0.4f
                b.cardTask.alpha = 0.85f
            } else {
                b.tvTaskTitle.paintFlags = b.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                b.tvTaskTitle.alpha = 1f
                b.tvTaskDesc.alpha = 0.75f
                b.cardTask.alpha = 1f
            }

            b.cbDone.setOnCheckedChangeListener { _, checked ->
                onChecked(task, checked)
            }

            b.root.setOnClickListener {
                onClick(task)
            }
        }
    }
}
