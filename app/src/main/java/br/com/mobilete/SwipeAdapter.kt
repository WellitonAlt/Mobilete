package br.com.mobilete

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.meus_anuncios_lista.view.*

class SwipeAdapter(private val items: MutableList<Anuncio>) : RecyclerView.Adapter<SwipeAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun addItem(anuncio: Anuncio) {
        items.add(anuncio)
        notifyItemInserted(items.size)
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.meus_anuncios_lista, parent, false)) {

        fun bind(anuncio: Anuncio ) = with(itemView) {
            txtDescricao.text = anuncio.descricao
            txtValidade.text = anuncio.validade
            Glide.with(this)
                .load(anuncio.foto)
                .apply(RequestOptions.circleCropTransform())
                .into(imgFoto)
        }
    }
}