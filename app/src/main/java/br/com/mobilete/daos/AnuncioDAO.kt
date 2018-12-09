package br.com.mobilete.daos

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.util.Log
import br.com.mobilete.callbacks.AnuncioCallback
import br.com.mobilete.callbacks.UsuarioCallback
import br.com.mobilete.entities.Anuncio
import br.com.mobilete.entities.AppConstants
import br.com.mobilete.entities.AppConstants.PATH_ANUNCIO
import br.com.mobilete.entities.AppConstants.PATH_IMG_ANUNCIO
import br.com.mobilete.utils.Mensagens
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AnuncioDAO(var anuncio: Anuncio,
                 val fireUser: FirebaseUser,
                 var fotoUri: Uri) : AppCompatActivity() {

    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseRef: DatabaseReference = database.getReference(PATH_ANUNCIO).child(fireUser.uid)

    fun setKey(){
        val key = databaseRef.push().key
        anuncio.id = key!!
    }


    fun uploadFoto(callback: AnuncioCallback) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val ref: StorageReference = storage.getReference(PATH_IMG_ANUNCIO).child(anuncio.id)
        ref.putFile(fotoUri)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(AppConstants.TAG_UP, "Sucesso")
                    getStorageUrl(callback, ref)
                } else {
                    Log.d(AppConstants.TAG_UP, "Falhou ${task.exception}")
                    callback.onError("Ocorreu um erro ao fazer upload da imagem!!")
                }
            }
    }

    fun getStorageUrl(callback: AnuncioCallback, storageRef: StorageReference){
        storageRef.downloadUrl.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(AppConstants.TAG_UP, "Sucesso")
                anuncio.foto = task.result.toString()
                callback.onCallbackUploadFoto(task.result.toString())
            } else {
                Log.d(AppConstants.TAG_UP, "Falhou ${task.exception}")
            }
        }
    }

    fun salvaAnuncio(callback: AnuncioCallback){
        databaseRef.child(anuncio.id).setValue(anuncio)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    Log.d(AppConstants.TAG_CAD, "Sucesso")
                    callback.onCallbackAnuncioDao()
                }else{
                    Log.d(AppConstants.TAG_CAD, "Falhou ${task.exception}")
                    callback.onError("Ocorreu um erro ao salvar no banco de dados!!")
                }
            }
    }

}
