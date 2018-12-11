package br.com.mobilete.scenarios.anuncio

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import br.com.mobilete.R
import br.com.mobilete.callbacks.AnuncioCallback
import br.com.mobilete.daos.AnuncioDAO
import br.com.mobilete.entities.Anuncio
import br.com.mobilete.entities.AppConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_busca.*


class BuscaActivity : AppCompatActivity() {

    private var anuncios: MutableList<Anuncio> = mutableListOf()
    private var buscaAnuncios: MutableList<Anuncio> = mutableListOf()
    private lateinit var anuncioAdapter: AnuncioAdapter
    private lateinit var dialog: ProgressDialog
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_busca)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser


        dialog = ProgressDialog(this, R.style.ProgressDialogStyle) //Inicia o Progress Dialog
        dialog.setMessage("Carregando...")

        progressWheel(true)
        getAnuncios()

        edtBusca.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty())
                    listaAnuncios(anuncios)
                buscaAnuncio()
            }

        })
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
        var anuncioDao = AnuncioDAO(null, user!!, null)
        anuncioDao.getTodosAnuncios(object : AnuncioCallback {

            override fun onCallbackAnuncios(anuncios: MutableList<Anuncio>) {
                this@BuscaActivity.anuncios = anuncios
                listaAnuncios(this@BuscaActivity.anuncios)
            }

            override fun onCallbackAnuncioDao() {}

            override fun onCallbackUploadFoto(fotoUri: String) {}

            override fun onError(men: String) {}

        })
    }

    private fun listaAnuncios(_listaAnuncio: MutableList<Anuncio>){
        anuncioAdapter = AnuncioAdapter(this, _listaAnuncio)
        anuncioAdapter.setOnItemClickListener {position ->
            val anuncio = Intent(this, AnuncioActivity::class.java)
            anuncio.putExtra(AppConstants.ANUNCIO, _listaAnuncio[position])
            startActivity(anuncio)
        }

        rvAnuncios.adapter = anuncioAdapter
        rvAnuncios.layoutManager = LinearLayoutManager(this)
        progressWheel(false)
    }

    private fun progressWheel(enabled: Boolean) {
        if (enabled)
            dialog.show()
        else
            dialog.dismiss()
    }

    private fun buscaAnuncio() {
        buscaAnuncios.clear()
        for (anuncio in anuncios) {
            if (anuncio.descricao.contains(edtBusca.text.toString())) {
                buscaAnuncios.add(anuncio)

            }
        }
        if (buscaAnuncios.size == 0)
            txtResultado.text = "Nenhum item encontrado"
        else
            txtResultado.text = "Resultado"
        listaAnuncios(buscaAnuncios)
    }

}
