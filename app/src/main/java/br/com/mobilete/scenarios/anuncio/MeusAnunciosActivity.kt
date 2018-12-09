package br.com.mobilete.scenarios.anuncio

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.MenuItem
import br.com.mobilete.R
import br.com.mobilete.callbacks.AnuncioCallback
import br.com.mobilete.daos.AnuncioDAO
import br.com.mobilete.entities.AppConstants.ANUNCIO
import br.com.mobilete.entities.AppConstants.DELETA
import br.com.mobilete.entities.AppConstants.EDITA
import br.com.mobilete.entities.Anuncio
import br.com.mobilete.utils.Mensagens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_meus_anuncios.*

class MeusAnunciosActivity :  AppCompatActivity(){

    private var listaAnuncios: MutableList<Anuncio> = mutableListOf()
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null

    private lateinit var swipeAdapter: SwipeAdapter
    private lateinit var dialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meus_anuncios)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser

        dialog = ProgressDialog(this, R.style.ProgressDialogStyle) //Inicia o Progress Dialog
        dialog.setMessage("Carregando...")

        progressWheel(true)
        getAnuncios()

        val swipeHandler = object : SwipeCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = recyclerView.adapter as SwipeAdapter
                //8 Direita
                if (direction == 8){
                    Log.d("Direcao", "Direita")
                    alerta(adapter.getItem(viewHolder.adapterPosition), viewHolder.adapterPosition, EDITA)
                //4 Esquerda
                }else if (direction == 4){
                    Log.d("Direcao", "Esquerda")
                    alerta(adapter.getItem(viewHolder.adapterPosition), viewHolder.adapterPosition, DELETA)
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun getAnuncios(){
        val anuncioDao = AnuncioDAO(null, user!!, null)
        anuncioDao.getAnuncios(object: AnuncioCallback {
            override fun onCallbackAnuncios(anuncios: MutableList<Anuncio>) {
                listaAnuncios = anuncios
                listaAnuncios()
            }

            override fun onCallbackAnuncioDao() {}

            override fun onError(men: String) {
                Mensagens.mensagem(this@MeusAnunciosActivity, men)
                progressWheel(false)
            }
            override fun onCallbackUploadFoto(fotoUri: String) {}
        })
    }

    private fun deletaAnuncio(anuncio: Anuncio){
        val anuncioDao = AnuncioDAO(anuncio, user!!, null)
        anuncioDao.deletaAnuncio(object : AnuncioCallback {

            override fun onError(men: String) {
                Mensagens.mensagem(this@MeusAnunciosActivity, men)
                progressWheel(false)
            }

            override fun onCallbackAnuncioDao() {}

            override fun onCallbackAnuncios(anuncios: MutableList<Anuncio>) {}

            override fun onCallbackUploadFoto(fotoUri: String) {}

        })
    }

    private fun listaAnuncios(){
        swipeAdapter = SwipeAdapter(listaAnuncios)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = swipeAdapter
        progressWheel(false)
    }

    private fun progressWheel(enabled: Boolean) {
        if (enabled)
            dialog.show()
        else
            dialog.dismiss()
    }

    private fun alerta(anuncio: Anuncio, possicao: Int, op: Int){
        val builder = AlertDialog.Builder(this@MeusAnunciosActivity)
        val adapter = recyclerView.adapter as SwipeAdapter
        val mensagem: String

        if (op == DELETA)
            mensagem = "Deseja Deletar esse Anuncio?"
        else
            mensagem = "Deseja Editar esse Anuncio?"

        builder.setTitle(mensagem)
        builder.setMessage(anuncio.descricao)
        builder.setPositiveButton("Sim"){dialog, which ->
            if(op == DELETA) {
                adapter.removeAt(possicao)
                deletaAnuncio(anuncio)
            }else{
                val goToEdita = Intent(this, CadastroAnuncioActivity::class.java)
                goToEdita.putExtra(ANUNCIO, anuncio)
                startActivity(goToEdita)
                listaAnuncios()
            }
        }
        builder.setNegativeButton("Não"){dialog, which ->
            listaAnuncios()
        }
        builder.create().show()
    }

}
