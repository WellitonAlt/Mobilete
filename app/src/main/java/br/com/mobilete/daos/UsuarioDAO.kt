package br.com.mobilete.daos

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.util.Log
import br.com.mobilete.entities.AppConstants
import br.com.mobilete.entities.AppConstants.PATH_IMG_USUARIO
import br.com.mobilete.entities.AppConstants.PATH_USUARIO
import br.com.mobilete.entities.Usuario
import br.com.mobilete.callbacks.UsuarioCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class UsuarioDAO(var usuario: Usuario? = null,
                 val senha: String = "",
                 var fotoUri: Uri? = null) : AppCompatActivity() {

    var fireUser: FirebaseUser? = null
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseRef: DatabaseReference = database.getReference(PATH_USUARIO)

    fun criaAutenticadorEmailSenha(callback : UsuarioCallback) {
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        mAuth.createUserWithEmailAndPassword(usuario!!.email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(AppConstants.TAG_AUTH, "Auth criada com sucesso")
                    fireUser = mAuth.currentUser!!
                    criaUsuario(callback, mAuth.currentUser!!)
                } else {
                    Log.d(AppConstants.TAG_AUTH, "Falhou ${task.exception}")
                    callback.onError("Email já cadastrado ou invalido!!")
                }
            }
    }

    fun criaUsuario(callback : UsuarioCallback, fireUser: FirebaseUser) {
        databaseRef.child(fireUser.uid).setValue(usuario)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    Log.d(AppConstants.TAG_CAD, "Sucesso")
                    callback.onCallbackUsuarioDao()
                }else{
                    Log.d(AppConstants.TAG_CAD, "Falhou ${task.exception}")
                    callback.onError("Ocorreu um erro ao salvar no banco de dados!!")
                }
            }
    }

    fun uploadFoto(callback : UsuarioCallback) {
        if (fotoUri != null && fireUser != null) {
            val storage: FirebaseStorage = FirebaseStorage.getInstance()
            val ref: StorageReference = storage.getReference(PATH_IMG_USUARIO).child(fireUser!!.uid)
            ref.putFile(fotoUri!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(AppConstants.TAG_UP, "Upload foto feito sucesso")
                        getFotoStorageUrl(callback, ref)
                    } else {
                        Log.d(AppConstants.TAG_UP, "Falhou ${task.exception}")
                        callback.onError("Ocorreu um erro ao fazer upload da imagem!!")
                    }
                }
        }
    }

    fun getFotoStorageUrl(callback: UsuarioCallback, storageRef: StorageReference){
        storageRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(AppConstants.TAG_UP, "Pegou a url co sucesso")
                usuario!!.foto = task.result.toString()
                callback.onCallbackUploadFoto(task.result.toString())
            } else {
                Log.d(AppConstants.TAG_UP, "Falhou ${task.exception}")
                callback.onError("Ocorreu um erro ao fazer upload da imagem!!")
            }
        }
    }

    fun editaUsuario(callback: UsuarioCallback){
        if (fireUser != null) {
            databaseRef.child(fireUser!!.uid).setValue(usuario)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(AppConstants.TAG_CAD, "Sucesso")
                        callback.onCallbackUsuarioDao()
                    } else {
                        Log.d(AppConstants.TAG_CAD, "Falhou ${task.exception}")
                        callback.onError("Ocorreu um erro ao atualizar o usuario!!")
                    }
                }
        }
    }

    fun getUsuario(callback: UsuarioCallback){
        databaseRef.child(fireUser!!.uid).addListenerForSingleValueEvent( //Coleta os dados do banco
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d(AppConstants.TAG_USUARIO, usuario.toString())
                        usuario = dataSnapshot.getValue(Usuario::class.java)
                        callback.onCallbackgetUsuario(usuario!!)
                    }
                }

                override fun onCancelled(dataSnapshot: DatabaseError) {
                    Log.d(AppConstants.TAG_USUARIO, "Usuario não recuperado")
                }
            }
        )
    }

}