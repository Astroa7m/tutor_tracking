package com.example.tutortracking.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tutortracking.R
import com.example.tutortracking.data.localdata.models.LocalStudent
import com.example.tutortracking.data.remotedata.models.Student
import com.example.tutortracking.databinding.StudentListItemsBinding
import com.example.tutortracking.util.EspressoIdlingResource
import com.example.tutortracking.util.decode
import com.example.tutortracking.util.getImageString

class StudentsAdapter(private inline val moveToFirst : ()->Unit) : ListAdapter<LocalStudent, StudentsAdapter.StudentHolder>(DiffUtilCallback()) {

    class StudentHolder(private val binding: StudentListItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LocalStudent?, context: Context) {
            binding.apply {
                if(item?.studentPic!=null)
                    listItemsImage.setImageBitmap(decode(getImageString(item.studentPic)))
                else
                    listItemsImage.setImageResource(R.drawable.ic_user)
                listItemsName.text = item!!.studentName
                listItemsSubject.text = item.studentSubject.toString()/*context.getString(R.string.student_subject, item.studentSubject.toString())*/
                listItemsYear.text =  item.studentYear.toString() /*context.getString(R.string.student_year, item.studentYear.toString())*/
                listItemsSyncText.text = if(item.isConnected) "synced" else "not synced"
                listItemsSyncView.setBackgroundColor(
                    if(item.isConnected)
                        context.getColor(R.color.synced)
                    else
                        context.getColor(R.color.not_synced)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentHolder {

        return StudentHolder(
            StudentListItemsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private var onClickListener: ((LocalStudent) -> Unit)? = null

    fun setOnClickListener(listener: (LocalStudent)->Unit){
        onClickListener = listener
    }

    override fun onBindViewHolder(holder: StudentHolder, position: Int) {
       holder.bind(getItem(position),holder.itemView.context)
        holder.itemView.setOnClickListener {
            onClickListener?.let {item->
                item(currentList[position])
            }
        }
    }

    override fun submitList(list: MutableList<LocalStudent>?) {
        EspressoIdlingResource.increment()
        val runnableCallback = Runnable {
            moveToFirst()
            EspressoIdlingResource.decrement()
        }
        super.submitList(list, runnableCallback)
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<LocalStudent>(){

        override fun areItemsTheSame(oldItem: LocalStudent, newItem: LocalStudent) = oldItem._id==newItem._id

        override fun areContentsTheSame(oldItem: LocalStudent, newItem: LocalStudent) = oldItem == newItem

    }

}