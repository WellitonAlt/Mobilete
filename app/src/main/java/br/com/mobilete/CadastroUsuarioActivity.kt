package br.com.mobilete

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_cadastro_usuario.*
import android.widget.Toast
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CadastroUsuarioActivity : AppCompatActivity() {

    companion object {
        const val TAG_AUTH: String = "Cria Autenticação"
        const val TAG_CAD: String = "Salva no Banco"
    }

    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_usuario)

        //Firebase
        mAuth = FirebaseAuth.getInstance()

        Glide.with(this)
            .load(R.drawable.person)
            .apply(RequestOptions.circleCropTransform())
            .into(findViewById<View>(R.id.imgUsuario) as ImageView)

        btnCadastrar.setOnClickListener {
            progressWheel(true)
            cadastraUsuaio()
            progressWheel(false)
        }
    }

    private fun cadastraUsuaio(){
        if (validaCampos()) {
            val usuario = Usuario(edtNome.text.toString(),
                edtEmail.text.toString(),
                edtTelefone.text.toString(),
                edtSenha.text.toString())
            criaAutenticadorEmailSenha(usuario)
        }
    }

    private fun validaCampos(): Boolean {

        if (edtNome.text.isEmpty()) {
            mensagemErro("O campo nome deve conter informação!!", edtNome)
            return false
        }else if (!emailValido(edtEmail.text.toString())) {
            mensagemErro("O campo email está fora do padrão!!", edtEmail)
            return false
        }else if (edtEmail.text.isEmpty()) {
            mensagemErro("O campo email deve conter informação!!", edtEmail)
            return false
        } else if (edtTelefone.text.isEmpty()) {
            mensagemErro("O campo telefone deve conter informação!!", edtTelefone)
            return false
        } else if (edtSenha.text.isEmpty()) {
            mensagemErro("O campo senha deve conter informação!!", edtSenha)
            return false
        } else if (edtSenha.text.length < 6) {
            mensagemErro("O campo senha deve conter mais de 6 carateres!!", edtSenha)
            return false
        }
        return true
    }

    fun emailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    fun mensagemErro(mensagem: String, viewFocus: View){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
        viewFocus.requestFocus()
    }

    fun mensagemErro(mensagem: String){
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    private fun criaAutenticadorEmailSenha(usuario: Usuario){
        mAuth!!.createUserWithEmailAndPassword(usuario.email, usuario.senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful()) {
                    Log.d(TAG_AUTH, "Sucesso")
                    user = mAuth!!.currentUser
                    //TODO UPLOAD FOTO
                    criaUsuario(user!!.uid, usuario)
                } else {
                    Log.d(TAG_AUTH, "Falhou ${task.exception}")
                    mensagemErro("Email já cadastrado ou invalido!!")
                }
            }
    }

    private fun criaUsuario(id: String, usuario: Usuario){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("usuario").child(id)
        ref.setValue(usuario)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful()){
                    Log.d(TAG_CAD, "Sucesso")
                    //TODO GOTO MAINACTIVITY
                }else{
                    Log.d(TAG_CAD, "Falhou ${task.exception}")
                    mensagemErro("Ocorreu um erro ao salvar no banco de dados!!")
                }
        }
    }

    private fun progressWheel(habilita: Boolean){
        if (habilita) {
            //TODO DESABILITA FORM
            btnCadastrar.visibility = View.GONE
            progress_wheel.visibility = View.VISIBLE
        }else{
            //TODO HABILITA FORM
            btnCadastrar.visibility = View.VISIBLE
            progress_wheel.visibility = View.GONE
        }

    }

}
