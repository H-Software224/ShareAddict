package com.example.loginmeta2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    companion object {
        const val IMAGE_PICK_CODE = 1001
    }

    private lateinit var pickImageButton: Button
    private lateinit var selectImageButton: Button
    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pickImageButton = findViewById(R.id.pickImageButton)
        selectImageButton = findViewById(R.id.selectImageButton)

        pickImageButton.setOnClickListener {
            pickImageFromGallery()
        }

        selectImageButton.setOnClickListener {
            imageFile?.let {
                shareImageToInstagramStory(it)
            } ?: Toast.makeText(this, "이미지를 먼저 선택하세요!", Toast.LENGTH_SHORT).show()
        }

        // 이미지 파일 참조 설정 (앱 재실행 대비)
        imageFile = File(getExternalFilesDir(null), "shared_image.png")
        if (!imageFile!!.exists()) {
            imageFile = null
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data ?: return

            try {
                val inputStream = contentResolver.openInputStream(selectedImageUri)
                val file = File(getExternalFilesDir(null), "shared_image.png")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                imageFile = file

                Toast.makeText(this, "이미지 저장 완료", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "이미지 저장 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareImageToInstagramStory(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            file
        )

        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra("source_application", packageName)
        }

        grantUriPermission("com.instagram.android", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (packageManager.resolveActivity(intent, 0) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Instagram 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
