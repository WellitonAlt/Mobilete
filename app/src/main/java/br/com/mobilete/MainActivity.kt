package br.com.mobilete

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import co.zsmb.materialdrawerkt.draweritems.sectionHeader
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var result: Drawer
    private lateinit var headerResult: AccountHeader
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        mAuth = FirebaseAuth.getInstance() // Inicializa o Firebase Auth
        val user = mAuth!!.currentUser

        

        result = drawer {
           toolbar = this@MainActivity.toolbar
           hasStableIds = true
           savedInstance = savedInstanceState
           showOnFirstLaunch = true

            headerResult = accountHeader {
                background = R.mipmap.fundo
                savedInstance = savedInstanceState
                translucentStatusBar = true

                profile("Mike Penz", "mikepenz@gmail.com") {
                    iconUrl = "https://avatars3.githubusercontent.com/u/1476232?v=3&s=460"
                    identifier = 100
                }
            }
       }

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        result.saveInstanceState(outState)
        headerResult.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (result.isDrawerOpen)
            result.closeDrawer()
        else
            super.onBackPressed()
    }
}
