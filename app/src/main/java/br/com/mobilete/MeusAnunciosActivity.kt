package br.com.mobilete

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_meus_anuncios.*

class MeusAnunciosActivity :  AppCompatActivity(){

    companion object {
        const val TAG_ANUNCIO: String = "FirebaseLog - Recupera Anuncios"
    }

    private var listaAnuncios: MutableList<Anuncio> = mutableListOf()
    private lateinit var swipeAdapter: SwipeAdapter
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
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
                //8 Direita
                if (direction == 8){
                    Log.d("Direcao", "Direita")
                    val adapter = recyclerView.adapter as SwipeAdapter
                }else if (direction == 4){
                    Log.d("Direcao", "Esquerda")
                    val adapter = recyclerView.adapter as SwipeAdapter
                    adapter.removeAt(viewHolder.adapterPosition)
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
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("anuncios").child(user!!.uid)

        ref.addValueEventListener( //Coleta os dados do banco
            object : ValueEventListener  {
                override fun onDataChange(p0: DataSnapshot) {
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

}
