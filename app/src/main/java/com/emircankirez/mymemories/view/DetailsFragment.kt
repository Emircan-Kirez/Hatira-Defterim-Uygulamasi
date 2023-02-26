package com.emircankirez.mymemories.view

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.emircankirez.mymemories.R
import com.emircankirez.mymemories.databinding.FragmentDetailsBinding
import com.emircankirez.mymemories.model.Memory
import com.emircankirez.mymemories.roomdb.MemoryDao
import com.emircankirez.mymemories.roomdb.MemoryDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar

class DetailsFragment : Fragment(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: FragmentDetailsBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedBitmap : Bitmap? = null
    private lateinit var memoryDatabase: MemoryDatabase
    private lateinit var memoryDao: MemoryDao
    private val compositeDisposable = CompositeDisposable()
    private var memoryFromMain : Memory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        registerLauncher()

        memoryDatabase = Room.databaseBuilder(requireContext(), MemoryDatabase::class.java, "Memories").build()
        memoryDao = memoryDatabase.memoryDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // menu'yu gizle
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.add_new_memory).isVisible = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtDate.text = SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().time)
        binding.btnDate.setOnClickListener { pickDate(view) }
        binding.imageView.setOnClickListener { selectImage(view) }
        binding.btnSave.setOnClickListener { saveImage(view) }
        binding.btnDelete.setOnClickListener { deleteImage(view) }

        arguments?.let {
            val info = DetailsFragmentArgs.fromBundle(it).info
            if(info == "new"){
                binding.btnDelete.visibility = View.GONE
            }else{
                // old
                binding.btnSave.visibility = View.GONE
                binding.btnDate.visibility = View.GONE
                binding.imageView.isClickable = false
                binding.txtMemoryName.isFocusable = false
                binding.txtComment.isFocusable = false
                val id = DetailsFragmentArgs.fromBundle(it).id
                compositeDisposable.add(
                    memoryDao.getMemoryById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponseForOldMemory)
                )
            }
        }
    }

    private fun handleResponseForOldMemory(memory: Memory){
        memoryFromMain = memory
        binding.txtMemoryName.setText(memory.memoryName)
        binding.txtComment.setText(memory.comment)
        binding.txtDate.setText(memory.date)

        val bitmap = BitmapFactory.decodeByteArray(memory.image, 0, memory.image.size)
        binding.imageView.setImageBitmap(bitmap)
    }

    // tarih seçimi
    private fun pickDate(view: View){
        val datePicker =  DatePickerDialog(requireContext(), this,
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        binding.txtDate.text = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK){ // resim seçildiyse yerine koy
                val intentFromResult = result.data
                if(intentFromResult != null){
                    val selectedUri = intentFromResult.data
                    if(selectedUri != null){
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, selectedUri)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                        }else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedUri)
                        }
                        binding.imageView.setImageBitmap(selectedBitmap)
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if(result){
                // permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                // permissin denied
                Toast.makeText(requireContext(), "Devam edebilmek için izin gerekli!!", Toast.LENGTH_LONG).show()
            }
        }
    }

    // galeriden resim seçme ve izinler
    private fun selectImage(view: View){
        activity?.let {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if(ContextCompat.checkSelfPermission(requireActivity().applicationContext, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                        Snackbar.make(view, "Galeri için izin lazım", Snackbar.LENGTH_INDEFINITE).setAction("İzin ver"){
                            // request permission
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }.show()
                    } else {
                        // request permission
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                } else {
                    // permission granted
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                }
            }else{
                if(ContextCompat.checkSelfPermission(requireActivity().applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Snackbar.make(view, "Galeri için izin lazım", Snackbar.LENGTH_INDEFINITE).setAction("İzin ver"){
                                // request permission
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }.show()
                    } else {
                        // request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    // permission granted
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                }
            }
        }
    }

    private fun saveImage(view : View){
        if(selectedBitmap != null){ // resim seçiliyse
            val memoryName = binding.txtMemoryName.text.toString()
            val comment = binding.txtComment.text.toString()
            val date = binding.txtDate.text.toString()

            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 75, outputStream)
            val byteArray = outputStream.toByteArray()

            val memory = Memory(memoryName, comment, date, byteArray)
            compositeDisposable.add(
                memoryDao.insert(memory)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForSaveAndDelete)
            )
        }else{
            Toast.makeText(requireContext(), "Resim seçiniz!!", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleResponseForSaveAndDelete(){
        val action = DetailsFragmentDirections.actionDetailsFragmentToMemoryListFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    private fun deleteImage(view: View){
        memoryFromMain?.let {
            compositeDisposable.add(
                memoryDao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForSaveAndDelete)
            )
        }
    }

    private fun makeSmallerBitmap(image : Bitmap, max : Int) : Bitmap{
        var width = image.width
        var height = image.height
        val ratio = width.toDouble() / height.toDouble()

        if(ratio > 1){
            // landscape
            width = max
            val scaledHeight = width / ratio
            height = scaledHeight.toInt()
        }else{
            // portrait
            height = max
            val scaledWidth = height * ratio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}