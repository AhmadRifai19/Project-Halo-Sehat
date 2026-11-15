package com.example.appsdokter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class AttachmentAdapter(
    private val attachments: MutableList<Uri>,
    private val onRemove: (Uri) -> Unit
) : RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.iv_attachment)
        val btnRemove: ImageButton = view.findViewById(R.id.btn_remove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attachment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = attachments[position]
        holder.image.setImageURI(uri)
        holder.btnRemove.setOnClickListener { onRemove(uri) }
    }

    override fun getItemCount() = attachments.size
}