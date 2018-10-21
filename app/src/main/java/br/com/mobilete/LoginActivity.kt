package br.com.mobilete

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.facebook.login.LoginManager
import com.google.firebase.auth.FacebookAuthProvider
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    companion object {
        const val TAG_FB: String = "Login - Facebook"
        const val TAG_EMAIL_SENHA: String = "Login - Email Senha"
        const val USUARIO: String = "Usuario"
    }

    private var mCallbackManager: CallbackManager? = null
    private var mAuth: FirebaseAuth? = null

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
        //Se já existir um usuario logado
        if (mAuth?.currentUser != null) {
            goToMainActivity(mAuth?.currentUser)
            finish()
        }
    }

    private fun goToMainActivity(fireUser: FirebaseUser?){
        val goToMain= Intent(this, MainActivity::class.java)
        goToMain.putExtra(USUARIO, fireUser)
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
                    goToMainActivity(mAuth!!.currentUser)
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
                        goToMainActivity(mAuth!!.currentUser)
                    }else{
                        Log.d(TAG_EMAIL_SENHA, "Falhou ${task.exception}")
                        mensagemErro("Email ou Senha invalidos!!")
                        progressWheel(true)
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

