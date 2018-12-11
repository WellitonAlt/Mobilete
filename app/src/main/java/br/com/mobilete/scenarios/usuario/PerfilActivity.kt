package br.com.mobilete.scenarios.usuario

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
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import br.com.mobilete.BuildConfig
import br.com.mobilete.R
import br.com.mobilete.callbacks.TagsCallback
import br.com.mobilete.callbacks.UsuarioCallback
import br.com.mobilete.daos.TagDAO
import br.com.mobilete.daos.UsuarioDAO
import br.com.mobilete.entities.AppConstants
import br.com.mobilete.entities.Tag
import br.com.mobilete.entities.Usuario
import br.com.mobilete.utils.GlideApp
import br.com.mobilete.utils.Mensagens
import br.com.mobilete.utils.Preferencias
import br.com.mobilete.utils.TagHandle
import com.anton46.collectionitempicker.Item
import com.anton46.collectionitempicker.CollectionPicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_perfil.*
import java.io.File


class PerfilActivity : AppCompatActivity() {

    private var minhasTags: String = ""
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var usuario : Usuario? = null
    private var provider: String? = null
    private var fotoCamera: Uri? = null
    private var fotoAceita: Uri? = null
    private var edicao: Boolean = false
    private var mudouFoto: Boolean = false
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser

        dialog = ProgressDialog(this, R.style.ProgressDialogStyle) //Inicia o Progress Dialog
        dialog.setMessage("Salvando...")

        btnFoto.setOnClickListener{
            if (cameraPermission())
                tiraFoto()
        }

        btnGaleria.setOnClickListener{
            if (readExternalPermission())
                selecionaFoto()
        }

        btnAtualizar.setOnClickListener {
           atualizaUsuario()
        }


        carregaDados()
        carregaFoto()
        listaTags()
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

        if(requestCode == AppConstants.REQUEST_CAMERA && resultCode == Activity.RESULT_OK){
            mudouFoto = true
            fotoAceita = fotoCamera
            carregaFoto()
        }

        if (requestCode == AppConstants.REQUEST_GALERIA && resultCode == Activity.RESULT_OK) {
            if (data != null){
                mudouFoto = true
                fotoAceita = data.data
                carregaFoto()
            }
        }

    }

    private fun tiraFoto() {
        val tirarFoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (tirarFoto.resolveActivity(packageManager) != null) {
            val arquivoFoto = montaArquivoFoto()
            val uriFoto = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", arquivoFoto)
            tirarFoto.putExtra(MediaStore.EXTRA_OUTPUT, uriFoto)
            startActivityForResult(tirarFoto, AppConstants.REQUEST_CAMERA)
        } else {
            Mensagens.mensagem(this, "Impossível tirar foto")
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
        val selecionaFoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        if (selecionaFoto.resolveActivity(packageManager) != null) {
            startActivityForResult(selecionaFoto, AppConstants.REQUEST_GALERIA)
        }
    }

    private fun getTags(){
        val tagDao = TagDAO()
        tagDao.getTags(object: TagsCallback {
            override fun onCallbackTagsDao() {}

            override fun onCallbackTags(tags: MutableList<Tag>) {
            }

            override fun onError(men: String) {}

        })
    }

    private fun listaTags(){
        val picker = findViewById<CollectionPicker>(R.id.collection_picker)
        picker.items = generateItems()
        picker.setOnItemClickListener { item, position ->
            if (item.isSelected) {
                minhasTags = TagHandle.tagAdd(minhasTags, item.id)
                Log.d("DEUS ADD", item.id)
            } else {
                minhasTags = TagHandle.tagRemove(minhasTags, item.id)
                Log.d("DEUS Remove", item.id)
            }
        }
    }



    private fun generateItems(): List<Item> {
        val items = ArrayList<Item>()
        items.add(Item("1", "Para vencer"))
        items.add(Item("2", "Caseira"))
        items.add(Item("3", "Pizza"))
        items.add(Item("4", "Japonesa"))
        return items
    }

    private fun carregaDados(){
        var preferencias = Preferencias(this)
        usuario = preferencias.getUsuario()
        provider = preferencias.getProvider()
        if (usuario!!.foto != "")
            fotoAceita = Uri.parse(usuario!!.foto)
        edtNome.setText(usuario!!.nome)
        edtTelefone.setText(usuario!!.telefone)
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


    private fun atualizaUsuario(){
        val usuarioDao = UsuarioDAO(usuario)
        progressWheel(true)
        if (validaCampos()) {
            usuario!!.nome = edtNome.text.toString()
            usuario!!.telefone = edtTelefone.text.toString()
            if(mudouFoto) {
                usuarioDao.uploadFoto(object: UsuarioCallback {
                    override fun onCallbackUsuarioDao() {}

                    override fun onCallbackgetUsuario(usuario: Usuario) {}

                    override fun onCallbackUploadFoto(fotoUri: String) {
                        salvaUsuario(usuarioDao)
                    }
                    override fun onError(men: String) {
                        Mensagens.mensagem(this@PerfilActivity, men)
                    }
                })
            } else
                salvaUsuario(usuarioDao)
        }else
            progressWheel(false)
    }


    private fun salvaUsuario(usuarioDao: UsuarioDAO) {
        usuarioDao.fireUser = user
        usuarioDao.editaUsuario(object: UsuarioCallback {
            override fun onCallbackUsuarioDao() {
                var preferencias = Preferencias(this@PerfilActivity)
                preferencias.setUsuario(usuarioDao.usuario!!)
                Mensagens.mensagem(this@PerfilActivity, "Usuario salvo com sucesso")
                salvaTag()
            }

            override fun onCallbackgetUsuario(usuario: Usuario) {}

            override fun onError(men: String) {
                Mensagens.mensagem(this@PerfilActivity, men)
                progressWheel(false)
            }
            override fun onCallbackUploadFoto(fotoUri: String) {}

        })

    }

    private fun salvaTag() {
        var tagDao = TagDAO()
        tagDao.fireUser = user
        tagDao.tags = TagHandle.tagSort(minhasTags)
        tagDao.salvaTagsUsuario(object : TagsCallback {
            override fun onCallbackTagsDao() {
                Mensagens.mensagem(this@PerfilActivity, "Salvou as Tags")
                progressWheel(false)
            }

            override fun onCallbackTags(tags: MutableList<Tag>) {
            }

            override fun onError(men: String) {
                progressWheel(false)
            }

        })
    }

    private fun validaCampos(): Boolean {
        if (edtNome.text.isEmpty()) {
            Mensagens.mensagemFocus(this, "O campo nome deve conter informação!!", edtNome)
            return false
        } else if (edtTelefone.text.isEmpty()) {
            Mensagens.mensagemFocus(this, "O campo Telefone deve conter informação!!", edtTelefone)
            return false
        }
        return true
    }

    private fun progressWheel(enabled: Boolean) {
        if (enabled)
            dialog.show()
        else
            dialog.dismiss()
    }
}
