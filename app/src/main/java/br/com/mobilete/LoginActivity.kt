package br.com.mobilete

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import br.com.mobilete.AppConstants.FACEBOOK
import br.com.mobilete.AppConstants.FIREBASE
import br.com.mobilete.AppConstants.TAG_LOGIN_EMAIL_SENHA
import br.com.mobilete.AppConstants.TAG_LOGIN_FB
import br.com.mobilete.AppConstants.TAG_PHOTO
import br.com.mobilete.AppConstants.TAG_USUARIO
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.LoginManager
import com.google.firebase.auth.FacebookAuthProvider
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var mCallbackManager: CallbackManager? = null
    private var mAuth: FirebaseAuth? = null
    private var usuario: Usuario? = null
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        setContentView(R.layout.activity_login)


        mAuth = FirebaseAuth.getInstance() // Inicia o Firebase Auth
        mCallbackManager = CallbackManager.Factory.create() // Inicio o CallBack

        dialog = ProgressDialog(this, R.style.ProgressDialogStyle) //Inicia o Progress Dialog
        dialog.setMessage("Carregando...")

        btnFbLogin.setOnClickListener {
            progressWheel(true)
            logaFb()
        }

        btnLogin.setOnClickListener {
            progressWheel(true)
            logaEmailSenha()
        }

        txtCadastrar.setOnClickListener {
            val goToCadastro = Intent(this, CadastroUsuarioActivity::class.java)
            startActivity(goToCadastro)
        }
    }

    public override fun onStart() {
        super.onStart()
        if (mAuth?.currentUser != null) {  //Se já existir um usuario logado
            goToMainActivity()
        }
    }

    private fun goToMainActivity() {
        val goToMain = Intent(this, MainActivity::class.java)
        startActivity(goToMain)
        finish()
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG_LOGIN_FB, "handleFacebookAccessToken: $token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG_LOGIN_FB, "Sucesso")
                    getUsuario(mAuth!!.currentUser, FACEBOOK)
                } else {
                    Log.d(TAG_LOGIN_FB, "Falhou", task.exception)
                    progressWheel(false)
                }
            }
    }

    private fun logaFb() {
        mCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends"))
        LoginManager.getInstance().registerCallback(mCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG_LOGIN_FB, "Facebook token: $loginResult.accessToken.token")
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG_LOGIN_FB, "Facebook onCancel.")
                    progressWheel(false)
                    mensagemErro("Autenticação pelo Facebook falhou!!")
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG_LOGIN_FB, "Facebook onError. $error")
                    progressWheel(false)
                    mensagemErro("Autenticação pelo Facebook falhou!!")
                }
            })
    }

    private fun logaEmailSenha() {
        if (validaCampos()) {
            mAuth!!.signInWithEmailAndPassword(edtEmail.text.toString(), edtSenha.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG_LOGIN_EMAIL_SENHA, "Sucesso")
                        getUsuario(mAuth!!.currentUser, FIREBASE)
                    } else {
                        Log.d(TAG_LOGIN_EMAIL_SENHA, "Falhou ${task.exception}")
                        mensagemErro("Email ou Senha invalidos!!")
                        progressWheel(false)
                    }
                }
        } else {
            progressWheel(false)
        }
    }

    private fun validaCampos(): Boolean {
        if (!emailValido(edtEmail.text.toString())) {
            mensagemErro("O campo email está fora do padrão!!", edtEmail)
            return false
        } else if (edtEmail.text.isEmpty()) {
            mensagemErro("O campo email deve conter informação!!", edtEmail)
            return false
        } else if (edtSenha.text.isEmpty()) {
            mensagemErro("O campo senha não pode ser vazio!!", edtSenha)
            return false
        }
        return true
    }

    private fun emailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun mensagemErro(mensagem: String, edtText: EditText) {
        edtText.error = mensagem
        edtText.requestFocus()
    }

    private fun mensagemErro(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun getUsuario(fireUser: FirebaseUser?, provider: String) {
        val preferencias = Preferencias(this)
        if (provider == FIREBASE) {
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
        } else if (provider == FACEBOOK) {
            var facebookUserId = ""
            val fotoUrl: String
            val usuario: Usuario
            for (profile in mAuth!!.currentUser!!.providerData) { //Pega o Id do facebook
                if (FacebookAuthProvider.PROVIDER_ID == profile.providerId) {
                    facebookUserId = profile.uid
                }
            }
            fotoUrl = "https://graph.facebook.com/$facebookUserId/picture?height=500"
            usuario = Usuario(fireUser!!.displayName!!, fireUser.email!!, "", fotoUrl)
            preferencias.setUsuario(usuario)
            preferencias.setProvider(FACEBOOK)
            goToMainActivity()
        }
    }

    private fun getFotoUri(userID: String, preferencias: Preferencias) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val ref: StorageReference = storage.getReference("img_usuario").child(userID)
        ref.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                preferencias.setFoto(task.result.toString())
                Log.d(TAG_PHOTO, "Sucesso ${task.result.toString()}")
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

    private fun progressWheel(enabled: Boolean) {
        if (enabled)
            dialog.show()
        else
            dialog.dismiss()
    }
}

