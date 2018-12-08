package br.com.mobilete

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.view.View
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_cadastro_usuario.*
import android.widget.Toast
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import br.com.mobilete.entities.AppConstants
import br.com.mobilete.entities.AppConstants.REQUEST_CAMERA
import br.com.mobilete.entities.AppConstants.REQUEST_GALERIA
import br.com.mobilete.entities.AppConstants.TAG_AUTH
import br.com.mobilete.entities.AppConstants.TAG_CAD
import br.com.mobilete.entities.AppConstants.TAG_UP
import br.com.mobilete.entities.Usuario
import br.com.mobilete.utils.GlideApp
import br.com.mobilete.utils.Preferencias
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class CadastroUsuarioActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var usuario : Usuario? = null
    private var fotoCamera: Uri? = null
    private var fotoAceita: Uri? = null
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_usuario)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Firebase
        mAuth = FirebaseAuth.getInstance()

        dialog = ProgressDialog(this, R.style.ProgressDialogStyle) //Inicia o Progress Dialog
        dialog.setMessage("Salvando...")

        carregaFoto()

        btnCadastrar.setOnClickListener {
            cadastraUsuaio()
        }

        btnGaleria.setOnClickListener{
            if (readExternalPermission())
                selecionaFoto()
        }

        btnFoto.setOnClickListener{
            if (cameraPermission())
                tiraFoto()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_GALERIA && resultCode == Activity.RESULT_OK) {
            if (data != null){
                fotoAceita = data.data
                carregaFoto()
            }
        }

        if(requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK){
            fotoAceita = fotoCamera
            carregaFoto()
        }
    }

    private fun cadastraUsuaio(){
        progressWheel(true)
        if (validaCampos()) {
            usuario = Usuario(
                edtNome.text.toString(),
                edtEmail.text.toString(),
                edtTelefone.text.toString()
            )
            criaAutenticadorEmailSenha(usuario, edtSenha.text.toString())
        }else
            progressWheel(false)
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

    private fun emailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun criaAutenticadorEmailSenha(usuario: Usuario?, senha: String){
        mAuth!!.createUserWithEmailAndPassword(usuario!!.email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG_AUTH, "Sucesso")
                    user = mAuth!!.currentUser
                    if(fotoAceita != null)
                        uploadFoto(user!!, fotoAceita!!)
                    criaUsuario(user!!, usuario)
                } else {
                    Log.d(TAG_AUTH, "Falhou ${task.exception}")
                    mensagemErro("Email já cadastrado ou invalido!!")
                    progressWheel(false)
                }
            }
    }

    private fun criaUsuario(fireUser: FirebaseUser, usuario: Usuario){
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("usuario").child(fireUser.uid)
        ref.setValue(usuario)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    Log.d(TAG_CAD, "Sucesso")
                    getUsuario()
                }else{
                    Log.d(TAG_CAD, "Falhou ${task.exception}")
                    mensagemErro("Ocorreu um erro ao salvar no banco de dados!!")
                    progressWheel(false)
                }
        }
    }

    private fun goToMainActivity(){
        val goToMain= Intent(this, MainActivity::class.java)
        startActivity(goToMain)
        finish()
    }

    private fun uploadFoto(fireUser: FirebaseUser, fotoURI: Uri) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val ref: StorageReference = storage.getReference("img_usuario").child(fireUser.uid)
        ref.putFile(fotoURI)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG_UP, "Sucesso")
                } else {
                    Log.d(TAG_UP, "Falhou ${task.exception}")
                    mensagemErro("Ocorreu um erro ao fazer upload da imagem!!")
                }
            }
    }

    private fun getUsuario(){
        val preferencias = Preferencias(this)
        if (fotoAceita!= null)
            usuario!!.foto = fotoAceita.toString()
        preferencias.setUsuario(usuario!!)
        goToMainActivity()
    }

    private fun tiraFoto() {
        val tirarFoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (tirarFoto.resolveActivity(packageManager) != null) {
            val arquivoFoto = montaArquivoFoto()
            val uriFoto = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", arquivoFoto)
            tirarFoto.putExtra(MediaStore.EXTRA_OUTPUT, uriFoto)
            startActivityForResult(tirarFoto, REQUEST_CAMERA)
        } else {
            mensagemErro("Impossível tirar foto")
        }
    }

    private fun montaArquivoFoto(): File {
        val nomeArquivo = mAuth!!.uid + "_" +System.currentTimeMillis().toString()
        val diretorioArquivo = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val arquivoFoto = File.createTempFile(nomeArquivo, "jpg", diretorioArquivo)

        fotoCamera = Uri.fromFile(arquivoFoto)

        return arquivoFoto
    }

    private fun selecionaFoto(){
        val selecionaFoto = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        if (selecionaFoto.resolveActivity(packageManager) != null) {
            startActivityForResult(selecionaFoto, REQUEST_GALERIA)
        }
    }

    private fun mensagemErro(mensagem: String, editText: EditText){
        editText.error =mensagem
        editText.requestFocus()
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

    private fun carregaFoto(){
        GlideApp.with(this)
            .load(fotoAceita)
            .placeholder(R.drawable.person)
            .dontAnimate()
            .into(findViewById<View>(R.id.imgUsuario) as ImageView)
    }

    private fun readExternalPermission(): Boolean {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), AppConstants.REQUEST_EXTERNAL)
            return false
        }
        return true
    }

    private fun cameraPermission(): Boolean {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), AppConstants.REQUEST_CAMERA)
            return false
        }
        return true
    }

}
