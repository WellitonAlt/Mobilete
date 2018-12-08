package br.com.mobilete

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
import android.widget.Toast
import br.com.mobilete.AppConstants.ANUNCIO
import br.com.mobilete.AppConstants.DELETA
import br.com.mobilete.AppConstants.EDITA
import br.com.mobilete.AppConstants.TAG_ANUNCIO
import br.com.mobilete.AppConstants.TAG_ANUNCIO_DELETE
import br.com.mobilete.AppConstants.TAG_ANUNCIO_FOTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_meus_anuncios.*
import java.io.Serializable

class MeusAnunciosActivity :  AppCompatActivity(){

    private var listaAnuncios: MutableList<Anuncio> = mutableListOf()
    private var database: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null

    private lateinit var swipeAdapter: SwipeAdapter
    private lateinit var dialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meus_anuncios)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        database = FirebaseDatabase.getInstance()
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
        val ref: DatabaseReference = database!!.getReference("anuncios").child(user!!.uid)

        ref.addValueEventListener( //Coleta os dados do banco
            object : ValueEventListener  {
                override fun onDataChange(p0: DataSnapshot) {
                    listaAnuncios.clear()
                    p0.children.mapNotNullTo(listaAnuncios) {
                        it.getValue<Anuncio>(Anuncio::class.java)
                    }
                    Log.d(TAG_ANUNCIO, "Anuncios recuperados")
                    listaAnuncios()
                }

                override fun onCancelled(dataSnapshot: DatabaseError) {
                    Log.d(TAG_ANUNCIO, "Anuncios não recuperados")
                }
            }
        )
    }

    private fun deletaAnuncio(anuncio: Anuncio){
        val refDatabase: DatabaseReference = database!!.getReference("anuncios").child(user!!.uid).child(anuncio.id)
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val refDataStore: StorageReference = storage.getReference("img_anuncio").child(anuncio.id)
        refDatabase.setValue(null).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG_ANUNCIO_DELETE, "Sucesso")
            } else {
                Log.d(TAG_ANUNCIO_DELETE, "Falhou ${task.exception}")
                mensagemErro("Ocorreu um erro ao excluir o anuncio!!")
            }
        }
        refDataStore.delete().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG_ANUNCIO_FOTO, "Sucesso")
                mensagemErro("Anuncio removido com sucesso!!")
            } else {
                Log.d(TAG_ANUNCIO_FOTO, "Falhou ${task.exception}")
                mensagemErro("Ocorreu um erro ao deletar a imagem!!")
            }
        }
    }

    private fun listaAnuncios(){
        swipeAdapter = SwipeAdapter(listaAnuncios)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = swipeAdapter
        progressWheel(false)
    }

    private fun mensagemErro(mensagem: String){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
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
