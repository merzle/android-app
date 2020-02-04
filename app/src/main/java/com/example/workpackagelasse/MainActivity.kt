package com.example.workpackagelasse


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.github.chrisbanes.photoview.OnMatrixChangedListener
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_TAKE_PHOTO = 1
    var currentPhotoPath: String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createPopupOnImageClick()
        createPictureButton()
    }

    /**
     * Creates a full screen popup window with zooming functionalities of the picture
     * after a user clicked/tapped on it
     */
    private fun createPopupOnImageClick() {
        val image: ImageView = this.findViewById(R.id.picture_view) as ImageView

        image.setOnClickListener{

            val view = layoutInflater.inflate(R.layout.popup_window_layout, null);

            // PhotoView class with implemented zoom functionalities
            // from https://github.com/chrisbanes/PhotoView
            val popupWindowImage: PhotoView = view.findViewById(R.id.popupView) as PhotoView

            // if the user made a picture use it for the popup, otherwise use the default picture
            if(currentPhotoPath != ""){
                val imgFile = File(currentPhotoPath)
                if(imgFile.exists()){
                    val myBitmap: Bitmap  = BitmapFactory.decodeFile(imgFile.absolutePath);
                    popupWindowImage.setImageBitmap(myBitmap)
                } else {
                    popupWindowImage.setImageResource(R.drawable.picture_car)
                }
            } else {
                popupWindowImage.setImageResource(R.drawable.picture_car)
            }


            //negative scaling made the picture smaller than full screen, which showed the layout
            // behind the popup
            popupWindowImage.setOnMatrixChangeListener(OnMatrixChangedListener {
                if (popupWindowImage.scale < 1)
                    popupWindowImage.scale = 1F
            })

            val popupWindow = PopupWindow(
                view, // Custom view to show in popup window
                LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                LinearLayout.LayoutParams.MATCH_PARENT// Window height
            )

            popupWindow.showAtLocation(
                root_layout, // Location to display popup window
                Gravity.CENTER, // Exact position of layout to display popup
                0, // X offset
                0 // Y offset
            )

            // cancel button in the top right corner
            val button: FloatingActionButton = view.findViewById(R.id.cancel_button) as FloatingActionButton
            button.setOnClickListener {
                popupWindow.dismiss()
            }
        }
    }

    /**
     * Creates a Button to open the camera modus in oder for users to take pictures
     */
    private fun createPictureButton() {
        val pictureButton: Button = this.findViewById(R.id.picture_button) as Button
        pictureButton.setOnClickListener {
            // check if user allows the use of the camera and to write into storage
            //otherwise, ask for permission
            if ((ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.CAMERA ) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                dispatchTakePictureIntent()

            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    222)
            }
        }
    }

    /**
     * Checks whether the user granted permission for camera and writing to storage
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 222){
            if(grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, getString(R.string.no_camer_permission_toast),
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Creates the intent to take a picture and store it in the internal app storage
     */
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.d("myTag", "Error occurred while creating the File");
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.workpackagelasse.fileprovider",
                        photoFile
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    //Files you save in the directories provided by getExternalFilesDir() or getFilesDir()
    // are deleted when the user uninstalls your app.
    // https://developer.android.com/training/camera/photobasics
    /**
     * Creates an image file in the internal app storage to save the taken picture
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    /**
     * Reacts to the user saving the picture he took to set it as the new picture
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPicture()
        }
    }

    /**
     * Sets the picture that the user took as new ImageView
     */
    private fun setPicture() {
        val image: ImageView = this.findViewById(R.id.picture_view) as ImageView
        // Get the dimensions of the View
        val targetW: Int = image.width
        val targetH: Int = image.height

        val bMap = BitmapFactory.decodeFile(currentPhotoPath)
        val bMapScaled = Bitmap.createScaledBitmap(bMap, targetW, targetH, true)

        image.setImageBitmap(bMapScaled)
    }
}
