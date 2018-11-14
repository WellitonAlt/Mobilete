package br.com.mobilete


import android.support.v4.widget.CircularProgressDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.anuncios_lista.view.*
import me.grantland.widget.AutofitHelper

class SwipeAdapter(private val items: MutableList<Anuncio>) : RecyclerView.Adapter<SwipeAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int) :Anuncio {
        val anuncio: Anuncio = items[position]
        return anuncio
    }

    fun addItem(anuncio: Anuncio) {
        items.add(anuncio)
        notifyItemInserted(items.size)
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.anuncios_lista, parent, false)) {

        fun bind(anuncio: Anuncio ) = with(itemView) {
            val circularProgressDrawable = CircularProgressDrawable(itemView.context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            AutofitHelper.create(txtDescricao)
            txtDescricao.text = anuncio.descricao
            txtValidade.text = "Validade: ${anuncio.validade}"
            txtValor.text = "Valor: %.2f".format(anuncio.valor.toFloat())
            GlideApp.with(this)
                .load(anuncio.foto)
                .placeholder(circularProgressDrawable)
                .apply(RequestOptions.circleCropTransform())
                .into(imgFoto)
        }
    }
}