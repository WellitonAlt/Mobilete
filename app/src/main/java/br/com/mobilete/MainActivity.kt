package br.com.mobilete

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val toggle: ActionBarDrawerToggle by lazy {
        ActionBarDrawerToggle(this, drawerLayout, R.string.abre, R.string.fecha)
    }

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                    goToActivity(SOBRE)
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

}
