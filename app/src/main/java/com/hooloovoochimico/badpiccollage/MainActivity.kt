package com.hooloovoochimico.badpiccollage

import `in`.myinnos.savebitmapandsharelib.SaveAndShare
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDexApplication
import com.hooloovoochimico.badpiccollageimageview.*
import com.hooloovoochimico.genericlistbottomsheet.GenericBottomSheet
import com.hooloovoochimico.genericlistbottomsheet.getGenericBottomSheet
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.yalantis.ucrop.UCrop
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers


class BadPicCollageApp: MultiDexApplication(){

    override fun onCreate() {
        super.onCreate()
        RxPaparazzo.register(this)
            .withFileProviderPath("badpiccollage_images")
        Logger.addLogAdapter(AndroidLogAdapter(PrettyFormatStrategy.newBuilder()
            .tag(this.javaClass.name).build()))

        RxJavaPlugins.setErrorHandler {
            //Toast.makeText(this, "Errore Generale RX",Toast.LENGTH_LONG).show()
            Logger.e(it,"errore ")
        }

        BPCFontProvider.init(this) {
            hashMapOf("Montserrat" to "Montserrat-Regular.ttf","Nunito" to "Nunito-Regular.ttf",
                "OpenSans Condensed" to "OpenSansCondensed-Light.ttf", "Roboto Mono" to "RobotoMono-Regular.ttf")
        }
    }
}


class MainActivity : AppCompatActivity(), TextEditorDialogFragment.OnTextLayerCallback {


    private val fontsAdapter by lazy {
        FontsAdapter(this,BPCFontProvider.getFontProvider(this))
    }

    private val pickBottomSheet: GenericBottomSheet by lazy {
        getGenericBottomSheet {
            context = this@MainActivity
            rowLayout = R.layout.pic_action_row
            bind = { view: View, item:Any ->
                val action = item as ActionModels
                val imgView = view.findViewById<ImageView>(R.id.image_row)
                val text = view.findViewById<TextView>(R.id.text_row)

                when(action.action){
                    ActionModelsEnum.PICK_FROM_CAMERA -> {
                        text.text = getString(R.string.pick_from_camera)
                        imgView.setImageResource(R.drawable.ic_camera)

                    }
                    ActionModelsEnum.PICK_FROM_GALLERY -> {
                        text.text = getString(R.string.pick_from_gallery)
                        imgView.setImageResource(R.drawable.ic_gallery)
                    }
                    ActionModelsEnum.ADD_TEXT -> {
                        text.text = getString(R.string.add_text)
                        imgView.setImageResource(R.drawable.ic_pencil)
                    }

                }

            }
            listItem = mutableListOf(ActionModels(ActionModelsEnum.PICK_FROM_CAMERA),
                ActionModels(ActionModelsEnum.PICK_FROM_GALLERY), ActionModels(ActionModelsEnum.ADD_TEXT)
            )

            listener = { _, item:Any ->

                val action = item as ActionModels
                if(action.action == ActionModelsEnum.ADD_TEXT){
                    addTextDefault()
                }else {
                    openImagePicker(action = action.action)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        imageView.motionViewCallback = object : BadPicCollageImageView.MotionViewCallback{
            override fun onEntitySelected(entity: MotionEntity?) {
            }

            override fun onEntityDoubleTap(entity: MotionEntity) {
                if(entity is TextEntity){
                    TextEditorDialogFragment.getInstance(entity.getTextLayer().text?:"")
                        .show(supportFragmentManager,TextEditorDialogFragment::class.java.name)
                }
            }

        }

        if(savedInstanceState!=null){
            imageView.restoreInstanceState(savedInstanceState)
        }else {
            imageView.setEmptyState()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        imageView.saveInstanceState()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun textChanged(text: String) {
        imageView.changeSelectedTextEntityText(text)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.action_add -> {
                openChoosePhoto()
                true
            }

            R.id.action_finish -> {
                SaveAndShare.save(this,
                    imageView.getThumbnailImage(),
                    getImgName(),null,null)

                true
            }
            else ->{
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun openChoosePhoto(){
       pickBottomSheet.show()
    }

    private fun openImagePicker(action:ActionModelsEnum){


        val df = with(RxPaparazzo.single(this)
            .crop(UCrop.Options().apply {
                setToolbarColor(ContextCompat.getColor(this@MainActivity,R.color.colorPrimary))
                setActiveWidgetColor(ContextCompat.getColor(this@MainActivity,R.color.colorPrimary))
            })){
            when(action){
                ActionModelsEnum.PICK_FROM_GALLERY -> {
                    usingGallery()
                }
                 else -> {
                    usingCamera()
                }

            }

        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({ response ->

                //response.targetUI().showImage(response.data())

                response.targetUI().loadImage(response.data())

            },{throwable ->
                throwable.printStackTrace()
                Toast.makeText(applicationContext, "ERROR ", Toast.LENGTH_SHORT).show()
            })



    }

    private fun loadImage(data: FileData?) {
        val d = Single.defer {
            Single.just(BitmapFactory.decodeFile(data?.file?.absolutePath,BitmapFactory.Options()))
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { bitmap, error ->

                if(error!= null){
                    Toast.makeText(this,"ERROR RORJRIR",Toast.LENGTH_LONG).show()
                }else {
                    imageView.post {
                        if (imageView.isBaseImageLoaded) {
                            imageView.addEntityAndPosition(
                                ImageEntity(
                                    bitmap = bitmap,
                                    canvasWidth = imageView.width, canvasHeight = imageView.height
                                )
                            )
                        } else {
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                }
            }
    }

    private fun addTextDefault() {
        imageView.post {
            imageView.addEntityAndPosition(
                TextEntity(layer = TextLayer().apply {
                    font = getFont { typeface = "Roboto Mono" }
                    text = getString(R.string.double_tap_to_edit)
                },canvasWidth = imageView.width,
                    canvasHeight = imageView.height,
                    fontProvider = BPCFontProvider.getFontProvider(this))
            )
        }
    }

    private fun openColorPicker(textColor:Boolean = true){
        ColorPickerDialog.Builder(this@MainActivity)
            .setTitle("Choose Color")
            .setPositiveButton("select", object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                    if(textColor){
                        imageView.changeSelectedTextEntityColor(envelope?.color?: Color.BLACK)
                    }else{
                        imageView.changeSelectedTextEntityBGColor(envelope?.color?: Color.TRANSPARENT)
                    }
                }

            })
            .setNegativeButton("Cancel") { p0, _ -> p0?.dismiss() }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(false)
            .show()
    }

    private fun openFontDialog(){


        AlertDialog.Builder(this)
            .setTitle("Choose Font")
            .setAdapter(fontsAdapter) { p0, p1 ->
                imageView.changeSelectedTextEntityFont(fontsAdapter.getFontName(p1))
                p0?.dismiss()
            }
            .show()
    }



}

