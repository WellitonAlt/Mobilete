package br.com.mobilete

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.LoginManager
import com.google.firebase.auth.FacebookAuthProvider
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    companion object {
        const val TAG_FB: String = "LoginLog - Facebook"
        const val TAG_EMAIL_SENHA: String = "LoginLog - Email Senha"
        const val TAG_USUARIO: String = "FirebaseLog - Recupera Usuario"
        const val TAG_PHOTO: String = "FirebaseLog - Recupera Foto"
        const val FIREBASE: Int = 1
        const val FACEBOOK: Int = 2
    }

    private var mCallbackManager: CallbackManager? = null
    private var mAuth: FirebaseAuth? = null
    private var usuario : Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        setContentView(R.layout.activity_login)


        mAuth = FirebaseAuth.getInstance() // Inicializa o Firebase Auth
        mCallbackManager = CallbackManager.Factory.create() // Inicio o CallBack

        btnFbLogin.setOnClickListener {
            progressWheel(true)
            logaFb()
        }

        btnLogin.setOnClickListener{
            progressWheel(true)
            logaEmailSenha()
        }

        txtCadastrar.setOnClickListener{
            progressWheel(true)
            val goToCadastro= Intent(this, CadastroUsuarioActivity::class.java)
            startActivity(goToCadastro)
            finish()
        }
    }

    public override fun onStart() {
        super.onStart()
        if (mAuth?.currentUser != null) {  //Se já existir um usuario logado
            goToMainActivity()
        }
    }

    private fun goToMainActivity(){
        val goToMain= Intent(this, MainActivity::class.java)
        startActivity(goToMain)
        finish()
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG_EMAIL_SENHA, "handleFacebookAccessToken: $token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG_FB, "Sucesso")
                    getUsuario(mAuth!!.currentUser, FACEBOOK)
                } else {
                    Log.d(TAG_EMAIL_SENHA, "Falhou", task.exception)
                    progressWheel(false)
                }
            }
    }

    private fun logaFb() {
        mCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logOut()
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends"))
        LoginManager.getInstance().registerCallback(mCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG_FB, "Facebook token: $loginResult.accessToken.token")
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG_FB, "Facebook onCancel.")
                    progressWheel(false)
                    Toast.makeText(this@LoginActivity, "Autenticação pelo Facebook falhou!!",
                        Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG_FB, "Facebook onError. $error")
                    progressWheel(false)
                    Toast.makeText(this@LoginActivity, "Autenticação pelo Facebook falhou!!",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun logaEmailSenha(){
        if(validaCampos()){
            mAuth!!.signInWithEmailAndPassword(edtEmail.text.toString(), edtSenha.text.toString())
                .addOnCompleteListener(this){ task ->
                    if(task.isSuccessful){
                        Log.d(TAG_EMAIL_SENHA, "Sucesso")
                        getUsuario(mAuth!!.currentUser, FIREBASE)
                    }else{
                        Log.d(TAG_EMAIL_SENHA, "Falhou ${task.exception}")
                        mensagemErro("Email ou Senha invalidos!!")
                        progressWheel(false)
                    }
                }
        }
    }

    private fun validaCampos(): Boolean {
        if (!emailValido(edtEmail.text.toString())) {
            mensagemErro("O campo email está fora do padrão!!", edtEmail)
            return false
        }else if (edtEmail.text.isEmpty()) {
            mensagemErro("O campo email deve conter informação!!", edtEmail)
            return false
        } else if (edtSenha.text.isEmpty()) {
            mensagemErro("O campo senha deve conter informação!!", edtSenha)
            return false
        }
        return true
    }

    private fun emailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun mensagemErro(mensagem: String, viewFocus: View){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
        viewFocus.requestFocus()
    }

    private fun mensagemErro(mensagem: String){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun getUsuario(fireUser: FirebaseUser?, provider: Int){
        //Todo CRUD
        val preferencias = Preferencias(this)
        if(provider == FIREBASE) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val ref: DatabaseReference = database.getReference("usuario")

            ref.child(fireUser!!.uid).addListenerForSingleValueEvent( //Coleta os dados do banco
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            usuario = dataSnapshot.getValue(Usuario::class.java)
                            Log.d(TAG_USUARIO, usuario.toString())
                            preferencias.setUsuario(usuario!!)
                            getFotoUri(fireUser.uid, preferencias)
                        }
                    }
                    override fun onCancelled(dataSnapshot: DatabaseError) {
                        Log.d(TAG_USUARIO, "Usuario não recuperado")
                    }
                }
            )
        }else if(provider == FACEBOOK){
            var facebookUserId = ""
            val fotoUrl: String
            val usuario : Usuario
            for (profile in mAuth!!.currentUser!!.providerData) { //Pega o Id do facebook
                if (FacebookAuthProvider.PROVIDER_ID == profile.providerId) {
                    facebookUserId = profile.uid
                }
            }
            fotoUrl = "https://graph.facebook.com/$facebookUserId/picture?height=500"
            usuario = Usuario(fireUser!!.displayName!!, fireUser.email!!, "", fotoUrl)
            preferencias.setUsuario(usuario)
            goToMainActivity()
        }
    }

    private fun getFotoUri(userID: String, preferencias: Preferencias){
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val ref: StorageReference = storage.getReference("img_usuario").child(userID)
        ref.downloadUrl.addOnCompleteListener{task ->
            if (task.isSuccessful) {
                preferencias.setFoto(task.result.toString())
                Log.d(TAG_PHOTO, "Deu Bom ${task.result.toString()}")
                goToMainActivity()
            } else {
                Log.d(TAG_PHOTO, "Falhou ${task.exception}")
                goToMainActivity()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun progressWheel(enabled: Boolean){
        enableDisableView(linearForm, !enabled)
        btnFbLogin.isEnabled = !enabled
        if (enabled) {
            linearProgress.visibility = View.VISIBLE
            linearProgress.bringToFront()
        }else{
            linearProgress.visibility = View.GONE
        }
    }

    private fun enableDisableView(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (idx in 0 until view.childCount) {
                enableDisableView(view.getChildAt(idx), enabled)
            }
        }
    }
}

