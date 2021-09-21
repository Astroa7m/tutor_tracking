package com.example.tutortracking.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tutortracking.R
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.databinding.StudentListItemsBinding
import com.example.tutortracking.util.decode
import com.example.tutortracking.util.getImageString

class StudentsAdapter(private val clickListener: (LocalStudent)->Unit) : ListAdapter<LocalStudent, StudentsAdapter.StudentHolder>(DiffUtilCallback()) {

    class StudentHolder(private val binding: StudentListItemsBinding, getItemAtPos: (Int)->Unit?) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LocalStudent?, context: Context) {
            binding.apply {
                listItemsImage.setImageBitmap(decode(getImageString(item?.studentPic)))
                listItemsName.text = item!!.studentName
                listItemsSubject.text = context.getString(R.string.student_subject, item.studentYear.toString())
                listItemsYear.text = context.getString(R.string.student_year, item.studentYear.toString())
                listItemsSyncText.text = if(item.isConnected) "synced" else "not synced"
                listItemsSyncView.setBackgroundColor(
                    if(item.isConnected)
                        context.getColor(R.color.synced)
                    else
                        context.getColor(R.color.not_synced)
                )
            }
        }
        init {
            getItemAtPos(bindingAdapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentHolder {
        val view = StudentHolder(
            StudentListItemsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false)){
            clickListener(currentList[it])
        }

        return view
    }

    override fun onBindViewHolder(holder: StudentHolder, position: Int) {
       holder.bind(getItem(position),holder.itemView.context)
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<LocalStudent>(){

        override fun areItemsTheSame(oldItem: LocalStudent, newItem: LocalStudent) = oldItem._id==newItem._id

        override fun areContentsTheSame(oldItem: LocalStudent, newItem: LocalStudent) = oldItem == newItem

    }

}