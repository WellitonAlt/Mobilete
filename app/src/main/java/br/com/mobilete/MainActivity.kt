package br.com.mobilete

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import br.com.mobilete.AppConstants.ANUNCIO
import br.com.mobilete.AppConstants.FACEBOOK
import br.com.mobilete.AppConstants.LOGIN
import br.com.mobilete.AppConstants.MEUS_ANUNCIO
import br.com.mobilete.AppConstants.SOBRE
import br.com.mobilete.R.style.ProgressDialogStyle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.login.LoginManager
import com.gc.materialdesign.R.layout.dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_meus_anuncios.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CADASTRO: Int = 1
        private const val LISTA = "ListaAnuncuis"
    }

    private var listaAnuncios: MutableList<Anuncio> = mutableListOf()
    private var database: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null

    private lateinit var swipeAdapter: SwipeAdapter
    private lateinit var dialog: ProgressDialog

    private val toggle: ActionBarDrawerToggle by lazy {
        ActionBarDrawerToggle(this, drawerLayout, R.string.abre, R.string.fecha)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser

        dialog = ProgressDialog(this, ProgressDialogStyle) //Inicia o Progress Dialog
        dialog.setMessage("Carregando...")

        progressWheel(true)
        getAnuncios()

        val myToolbar = findViewById<View>(R.id.toolbarApp) as android.support.v7.widget.Toolbar
        setSupportActionBar(myToolbar)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val preferencias = Preferencias(this)

        navigationView.setNavigationItemSelectedListener {
            when {
                it.itemId == R.id.opCriaAnuncio -> {
                    goToActivity(ANUNCIO)
                    true
                }
                it.itemId == R.id.opMeusAnuncios -> {
                    goToActivity(MEUS_ANUNCIO)
                    true
                }
                it.itemId == R.id.opSobre -> {
                    Toast.makeText(this, "Sobre", Toast.LENGTH_SHORT).show()
                    true
                }
                it.itemId == R.id.opSair -> {
                    FirebaseAuth.getInstance().signOut()
                    //Logout pelo Facebook tambem
                    if(preferencias.getProvider() == FACEBOOK)
                        LoginManager.getInstance().logOut()
                    goToActivity(LOGIN)
                    true
                }
                else -> false
            }
        }

        val navView: NavigationView = findViewById(R.id.navigationView)
        val navHeader: View = navView.getHeaderView(0)
        val txtNome: TextView = navHeader.findViewById(R.id.txtNomeNav)
        val txtEmail: TextView = navHeader.findViewById(R.id.txtEmailNav)
        val imgPerfil: ImageView = navHeader.findViewById(R.id.imgPerfil)
        txtNome.text = preferencias.getNome()
        txtEmail.text = preferencias.getEmail()

        if (preferencias.getFoto() != ""){
            Glide.with(this)
              .load(preferencias.getFoto())
              .apply(RequestOptions.circleCropTransform())
              .into(imgPerfil)
        }else {
            Glide.with(this)
               .load(R.drawable.person)
               .apply(RequestOptions.circleCropTransform())
               .into(imgPerfil)
        }
    }

    private fun goToActivity(nome: String){
        when (nome) {
            LOGIN -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            ANUNCIO -> startActivity(Intent(this, CadastroAnuncioActivity::class.java))
            MEUS_ANUNCIO -> startActivity(Intent(this, MeusAnunciosActivity::class.java))
            SOBRE -> startActivity(Intent(this, SobreActivity::class.java))
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun getAnuncios(){
        val ref: DatabaseReference = database!!.getReference().child("anuncios")

        ref.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    listaAnuncios.clear()

                    val newRef: MutableIterable<DataSnapshot> = p0.children

                    for(p in newRef) {
                        p.children.mapNotNullTo(listaAnuncios) {
                            it.getValue<Anuncio>(Anuncio::class.java)
                        }
                        Log.d(AppConstants.TAG_ANUNCIO, p.toString())
                    }
                    listaAnuncios()
                }

                override fun onCancelled(dataSnapshot: DatabaseError) {
                    Log.d(AppConstants.TAG_ANUNCIO, "Anuncios não recuperados")
                }
            }
        )
    }

    private fun progressWheel(enabled: Boolean) {
        if (enabled)
            dialog.show()
        else
            dialog.dismiss()
    }

    private fun listaAnuncios(){
        swipeAdapter = SwipeAdapter(listaAnuncios)
        rvAnuncios.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rvAnuncios.layoutManager = LinearLayoutManager(this)
        rvAnuncios.adapter = swipeAdapter
        progressWheel(false)
    }


}
