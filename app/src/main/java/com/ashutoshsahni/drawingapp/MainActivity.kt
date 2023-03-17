package com.ashutoshsahni.drawingapp

import android.Manifest
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {

    private var drawingView : DrawingView? = null
    private var mImageButtonColorPaint : ImageButton? = null

    val openGalleryLauncher : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if(result.resultCode == RESULT_OK && result.data != null) {
                val imageBackground : ImageView = findViewById(R.id.iv_background)
                imageBackground.setImageURI(result.data?.data)
            }
        }

    private var requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions -> permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value

                if (isGranted) {
                    Toast.makeText(this, "Permission Granted for Memory Read", Toast.LENGTH_SHORT).show()

                    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                } else
                    if(permissionName == Manifest.permission.READ_EXTERNAL_STORAGE)
                        Toast.makeText(this, "Permission denied for storage", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val linearLayoutColorPalette = findViewById<LinearLayout>(R.id.ll_color_palette)

        mImageButtonColorPaint = linearLayoutColorPalette[1] as ImageButton
        mImageButtonColorPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.palette_selected)
        )

        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setBrushSize(20.toFloat())

        val btnBrushSize : ImageButton = findViewById(R.id.ib_btn_brush)
        btnBrushSize.setOnClickListener {
            showBrushSizeSelectorDialog()
        }

        val ibGallery : ImageButton = findViewById(R.id.ib_gallery)
        ibGallery.setOnClickListener {
            requestStoragePermission()
        }

        val ibUndo : ImageButton = findViewById(R.id.ib_btn_undo)
        ibUndo.setOnClickListener {
            drawingView?.undoPath()
        }

        val ibRedo : ImageButton = findViewById(R.id.ib_btn_redo)
        ibRedo.setOnClickListener {
            drawingView?.redoPath()
        }
    }

    private fun requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
            rationaleDialog("Drawing App", "Drawing App needs your External Storage Permission")
        else {
            requestPermission.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
                //TODO: external storage permission
            ))
        }
    }

    private fun showBrushSizeSelectorDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dailog_brush_size)
        brushDialog.setTitle("Brush Size : ")

        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        smallBtn.setOnClickListener {
            drawingView?.setBrushSize(10.toFloat())
            brushDialog.dismiss()
        }

        val mediumBtn = brushDialog.findViewById<ImageView>(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener {
            drawingView?.setBrushSize(20.toFloat())
            brushDialog.dismiss()
        }

        val largeBtn = brushDialog.findViewById<ImageView>(R.id.ib_large_brush)
        largeBtn.setOnClickListener {
            drawingView?.setBrushSize(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    fun paintClicked(view : View) {
        if(view !== mImageButtonColorPaint) {
            val imageButton = view as ImageButton
            val imageButtonTag = imageButton.tag.toString()
            drawingView?.setColor(imageButtonTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.palette_selected)
            )

            mImageButtonColorPaint?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.palette_normal)
            )

            mImageButtonColorPaint = view
        }
    }

    //show rationale dialog
    private fun rationaleDialog(title: String, message: String) {
        val mDialog : AlertDialog.Builder = AlertDialog.Builder(this)
        mDialog.setTitle(title).setMessage(message).setPositiveButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        mDialog.create().show()
    }
}