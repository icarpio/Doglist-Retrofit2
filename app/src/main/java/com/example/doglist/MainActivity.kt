package com.example.doglist

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doglist.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class MainActivity : AppCompatActivity(), androidx.appcompat.widget.SearchView.OnQueryTextListener {

    private lateinit var binding:ActivityMainBinding
    private lateinit var adapter: DogsAdapter
    private val dogImages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.svDogs.setOnQueryTextListener(this)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = DogsAdapter(dogImages)
        binding.recyclerViewDogs.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewDogs.adapter = adapter
    }

    private fun getRetrofit():Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/breed/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun searchByName(query:String){
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(ApiService::class.java).getDogsByBreeds("$query/images")
            val puppies:DogsResponse? = call.body()
            runOnUiThread {
                if(call.isSuccessful){
                   val images:List<String> = puppies?.images ?: emptyList()
                    dogImages.clear()
                    dogImages.addAll(images)
                    adapter.notifyDataSetChanged()
                }else{
                   showError()
                }
                hideKeyboard()
            }

        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.viewRoot.windowToken, 0)
    }

    private fun showError() {
        Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(!query.isNullOrEmpty()){
            searchByName(query.lowercase(Locale.ROOT))
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }
}