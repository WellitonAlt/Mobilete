package br.com.mobilete.scenarios.usuario

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import br.com.mobilete.scenarios.main.MainActivity
import br.com.mobilete.R
import br.com.mobilete.callbacks.UsuarioCallback
import br.com.mobilete.daos.UsuarioDAO
import br.com.mobilete.entities.AppConstants.FACEBOOK
import br.com.mobilete.entities.AppConstants.FIREBASE
import br.com.mobilete.entities.AppConstants.TAG_LOGIN_EMAIL_SENHA
import br.com.mobilete.entities.AppConstants.TAG_LOGIN_FB
import br.com.mobilete.entities.Usuario
import br.com.mobilete.utils.Mensagens
import br.com.mobilete.utils.Preferencias
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.LoginManager
import com.google.firebase.auth.FacebookAuthProvider
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var mCallbackManager: CallbackManager? = null
    private var mAuth: FirebaseAuth? = null
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
                    Mensagens.mensagem(this@LoginActivity, "Autenticação pelo Facebook falhou!!")
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG_LOGIN_FB, "Facebook onError. $error")
                    progressWheel(false)
                    Mensagens.mensagem(this@LoginActivity,"Autenticação pelo Facebook falhou!!")
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
                        Mensagens.mensagem(this@LoginActivity,"Email ou Senha invalidos!!")
                        progressWheel(false)
                    }
                }
        } else {
            progressWheel(false)
        }
    }

    private fun validaCampos(): Boolean {
        if (!emailValido(edtEmail.text.toString())) {
            Mensagens.mensagemFocus(this, "O campo email está fora do padrão!!", edtEmail)
            return false
        } else if (edtEmail.text.isEmpty()) {
            Mensagens.mensagemFocus(this, "O campo email deve conter informação!!", edtEmail)
            return false
        } else if (edtSenha.text.isEmpty()) {
            Mensagens.mensagemFocus(this, "O campo senha não pode ser vazio!!", edtSenha)
            return false
        }
        return true
    }

    private fun emailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun getUsuario(fireUser: FirebaseUser?, provider: String) {
        val preferencias = Preferencias(this)
        val usuarioDao = UsuarioDAO(null, "", null)
        usuarioDao.fireUser = fireUser
        if (provider == FIREBASE) {
            usuarioDao.getUsuario(object: UsuarioCallback{
                override fun onCallbackUsuarioDao() {
                }

                override fun onCallbackgetUsuario(usuario: Usuario) {
                    preferencias.setUsuario(usuario)
                    goToMainActivity()
                }

                override fun onCallbackUploadFoto(fotoUri: String) {}

                override fun onError(men: String) {}

            })
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

