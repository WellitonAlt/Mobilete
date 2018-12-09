package br.com.mobilete.scenarios.anuncio

import android.content.Context
import android.support.v4.widget.CircularProgressDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.mobilete.R
import br.com.mobilete.entities.Anuncio
import br.com.mobilete.utils.GlideApp
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.anuncios_lista.view.*
import me.grantland.widget.AutofitHelper

class AnuncioAdapter(val context: Context, val items: MutableList<Anuncio>)
    : RecyclerView.Adapter<AnuncioAdapter.ViewHolder>() {

    var itemClickListener: ((index: Int) -> Unit)? = null

    fun setOnItemClickListener(clique: ((index: Int) -> Unit)) {
        this.itemClickListener = clique
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.anuncios_lista, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(context, items[position], itemClickListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(context: Context, anuncio: Anuncio, itemClickListener: ((index: Int) -> Unit)?) {
            val circularProgressDrawable = CircularProgressDrawable(itemView.context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            AutofitHelper.create(itemView.txtDescricao)
            itemView.txtDescricao.text = anuncio.descricao
            itemView.txtValidade.text = "Validade: ${anuncio.validade}"
            if (anuncio.valor.isEmpty()) {
                itemView.txtValor.text = "Valor: 0,00"
            } else {
                itemView.txtValor.text = "Valor: %.2f".format(anuncio.valor.toFloat())
            }
            GlideApp.with(context)
                .load(anuncio.foto)
                .placeholder(circularProgressDrawable)
                .apply(RequestOptions.circleCropTransform())
                .into(itemView.imgFoto)

            if (itemClickListener != null) {
                itemView.setOnClickListener {
                    itemClickListener.invoke(adapterPosition)
                }
            }

        }

    }
}