package com.example.safecross

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.safecross.databinding.ActivityMainBinding
import com.example.safecross.fragment.*


class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

         drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        //notification



        setSupportActionBar(toolbar)
        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null){
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,HomeFragment()).commit()
            binding.navigationView.setCheckedItem(R.id.optHome)
        }

        binding.navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.optHome -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container,HomeFragment()).commit()
                R.id.optNotes -> startActivity(Intent(this,NotesActivity::class.java))
                R.id.optSettings -> startActivity(Intent(this,SettingActivity::class.java))
                R.id.incidentReport -> startActivity(Intent(this,IncidentReport::class.java))

            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            if (toggle.onOptionsItemSelected(item)){
                return true }

        when(item.itemId){
            R.id.miAddContact -> startActivity(Intent(this,AddContactActivity::class.java))
            R.id.mifavorite -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container,HomeFragment()).commit()
            R.id.miSetting -> startActivity(Intent(this,SettingActivity::class.java))
            R.id.mifeedback ->startActivity(Intent(this,FeedBackActivity::class.java))
            R.id.miclose -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
           onBackPressedDispatcher.onBackPressed()
        }
    }




}