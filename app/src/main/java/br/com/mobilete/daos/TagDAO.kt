package br.com.mobilete.daos

import android.support.v7.app.AppCompatActivity
import android.util.Log
import br.com.mobilete.callbacks.TagsCallback
import br.com.mobilete.entities.AppConstants.PATH_TAG
import br.com.mobilete.entities.AppConstants.PATH_USUARIO_TAG
import br.com.mobilete.entities.AppConstants.TAG_CAD
import br.com.mobilete.entities.AppConstants.TAG_TAG
import br.com.mobilete.entities.Tag
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class TagDAO : AppCompatActivity() {

    var fireUser: FirebaseUser? = null
    var tags: String = ""
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseRef: DatabaseReference = database.getReference(PATH_TAG)
    private var listaTags: MutableList<Tag> = mutableListOf()

    fun getTags(callback: TagsCallback){
        databaseRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    listaTags.clear()
                    p0.children.mapNotNullTo(listaTags) {
                        it.getValue<Tag>(Tag::class.java)
                    }
                    Log.d(TAG_TAG, "Tags recuperadass")
                    callback.onCallbackTags(listaTags)
                }

                override fun onCancelled(dataSnapshot: DatabaseError) {
                    Log.d(TAG_TAG, "TAGS não recuperadas")
                }
            }
        )
    }

    fun salvaTagsUsuario(callback : TagsCallback) {
        val databaseRef: DatabaseReference = database.getReference(PATH_USUARIO_TAG)
        databaseRef.child(fireUser!!.uid).setValue(tags)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    Log.d(TAG_CAD, "Sucesso")
                    callback.onCallbackTagsDao()
                }else{
                    Log.d(TAG_CAD, "Falhou ${task.exception}")
                    callback.onError("Ocorreu um erro ao salvar no banco de dados!!")
                }
            }
    }
}