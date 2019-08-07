package com.hooloovoochimico.badpiccollage

import `in`.myinnos.savebitmapandsharelib.SaveAndShare
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDexApplication
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hooloovoochimico.badpiccollageimageview.*
import com.hooloovoochimico.genericlistbottomsheet.GenericBottomSheet
import com.hooloovoochimico.genericlistbottomsheet.getGenericBottomSheet
import com.manzo.slang.extensions.goneIf
import com.manzo.slang.extensions.string
import com.manzo.slang.navigation.toAdapter
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.yalantis.ucrop.UCrop
import dev.jai.genericdialog2.GenericDialog
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module


val appModule = module {
    single { ImageVolatileStorage() }
    factory { MemeLogic() }
    factory { MemePresenter(get()) }
}


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

        startKoin {
            androidContext(this@BadPicCollageApp)
            modules(appModule)
        }
    }
}


class MainActivity : AppCompatActivity(), TextEditorDialogFragment.OnTextLayerCallback {

    private val imgVolatileStorage: ImageVolatileStorage by inject()


    private val textPanelAdapter by lazy {
        getTextActions().toMutableList().toAdapter(
            rowLayout = R.layout.action_text_panel_item,
            onBindContent = {holder, _, element ->
                with(holder[R.id.imvaction]){
                    (this as? ImageView)?.setImageResource(
                        when(element.action){
                            ActionModelsEnum.INCREASE_TEXT -> R.drawable.ic_add_red
                            ActionModelsEnum.DECREASE_TEXT -> R.drawable.ic_decrease_text_red
                            ActionModelsEnum.CHOOSE_TEXT_COLOR -> R.drawable.ic_change_color_red
                            ActionModelsEnum.CHOOSE_BG_TEXT_COLOR -> R.drawable.ic_change_bgcolor_red
                            ActionModelsEnum.CHOOSE_TEXT_FONT -> R.drawable.ic_change_font_red
                            ActionModelsEnum.EDIT_TEXT -> R.drawable.ic_pencil
                            ActionModelsEnum.FLIP -> R.drawable.ic_flip
                            ActionModelsEnum.DELETE -> R.drawable.ic_delete
                            ActionModelsEnum.CANCEL -> R.drawable.ic_cancel_red
                            else -> R.drawable.ic_add_red

                        }
                    )
                }

                holder.itemView.setOnClickListener {
                    handleTextEditAction(element.action)
                }
            }
        )
    }
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

                    ActionModelsEnum.PICK_FROM_IMGFLIP -> {
                        text.text = getString(R.string.pick_from_meme)
                        imgView.setImageResource(R.drawable.ic_memes)
                    }

                    ActionModelsEnum.ADD_BLANK_IMAGE -> {
                        text.text = string(R.string.blank_image)
                        imgView.setImageResource(R.drawable.ic_blank_image)
                    }
                    else -> {}

                }

            }
            listItem = getAddAction(!imageView.isBaseImageLoaded)

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

    private var editTextPanel : RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextPanel = findViewById<RecyclerView>(R.id.text_action_panel).apply {
            layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
            adapter = textPanelAdapter
        }


        imageView.motionViewCallback = object : BadPicCollageImageView.MotionViewCallback{
            override fun onEntitySelected(entity: MotionEntity?) {
                openEditPanel(true, entity is TextEntity)
            }

            override fun onEntityDoubleTap(entity: MotionEntity) {
               openTextEditorDialog(entity)
            }

        }

        add_image_hint.setOnClickListener {
            openChoosePhoto()
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

    override fun onResume() {
        super.onResume()
        add_image_hint.goneIf {
            imageView.isBaseImageLoaded
        }
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.action_add -> {
                openChoosePhoto()
                true
            }

            R.id.action_finish -> {
                saveImageAndShare()


                true
            }
            else ->{
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun openChoosePhoto(){
        pickBottomSheet.updateAll(getAddAction(!imageView.isBaseImageLoaded))
       pickBottomSheet.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == MEME_REQ && resultCode ==  Activity.RESULT_OK){
            addImageToImageView(imgVolatileStorage.memeSelected!!)
        }
    }

    override fun onBackPressed() {

        if(imageView.isBaseImageLoaded){

            GenericDialog.Builder(this)
                .setDialogTheme(R.style.v_i_dialog_style)
                .setTitle(getString(R.string.hey_oh)).setTitleAppearance(android.R.color.white,23f)
                .setMessage(getString(R.string.do_you_want_to_lose)).setMessageAppearance(R.color.light_cyan,16f)

                .addNewButton(R.style.close_app_button) {
                    //openChoosePhoto()
                    super.onBackPressed()
                }

                .addNewButton(R.style.no_button) {
                    //openChoosePhoto()

                }

                .addNewButton(R.style.yes_button) {
                    //openChoosePhoto()
                    clearImage()
                }

                .setButtonOrientation(LinearLayout.HORIZONTAL)
                .setCancelable(true)
                .generate()

        }else {
            super.onBackPressed()
        }

    }
    private fun openImagePicker(action:ActionModelsEnum){

        when (action) {
            ActionModelsEnum.ADD_BLANK_IMAGE -> ColorPickerDialog.Builder(this@MainActivity)
                .setTitle(string(R.string.choose_background_of_image))
                .setPositiveButton("Select", object : ColorEnvelopeListener {
                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {

                        addImageToImageView(Bitmap.createBitmap(1024, 1536, Bitmap.Config.ARGB_8888).apply {
                            eraseColor(envelope?.color?:Color.WHITE)
                            //recycle()
                        })
                    }

                })
                .setNegativeButton("Cancel") { p0, _ -> p0?.dismiss() }
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(false)
                .show()
            ActionModelsEnum.PICK_FROM_IMGFLIP -> startActivityForResult(Intent(this, MemeActivity::class.java), MEME_REQ)
            else ->{

                with(RxPaparazzo.single(this)
                    .crop(UCrop.Options().apply {
                        setToolbarColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
                        setActiveWidgetColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
                        setFreeStyleCropEnabled(true)
                        setToolbarTitle(getCropImageTitle(this@MainActivity))
                    })
                ) {
                    when (action) {
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
                    .subscribe({ response ->

                        //response.targetUI().showImage(response.data())

                        response.targetUI().loadImage(response.data())

                    }, { throwable ->
                        throwable.printStackTrace()
                        Toast.makeText(applicationContext, "ERROR ", Toast.LENGTH_SHORT).show()
                    })

            }

        }


    }

    private fun loadImage(data: FileData?) {
        val d = Single.defer {
            Single.just(BitmapFactory.decodeFile(data?.file?.absolutePath,BitmapFactory.Options()))
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { bitmap, error ->

                if(error!= null){
                    //Toast.makeText(this,"ERROR RORJRIR",Toast.LENGTH_LONG).show()
                }else {
                    addImageToImageView(bitmap)
                }
            }
    }

    private fun addImageToImageView(bitmap: Bitmap){
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

            add_image_hint.goneIf {
                imageView.isBaseImageLoaded
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

            openEditPanel(true)
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
            .attachBrightnessSlideBar(true)
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


    private fun handleTextEditAction(action: ActionModelsEnum) {
        when (action) {
            ActionModelsEnum.INCREASE_TEXT -> imageView.increaseSelectedTextEntitySize()
            ActionModelsEnum.DECREASE_TEXT -> imageView.decreaseSelectedTextEntitySize()
            ActionModelsEnum.CHOOSE_TEXT_COLOR -> openColorPicker(true)
            ActionModelsEnum.CHOOSE_BG_TEXT_COLOR -> openColorPicker(false)
            ActionModelsEnum.CHOOSE_TEXT_FONT -> openFontDialog()
            ActionModelsEnum.DELETE -> {
                imageView.deleteSelectedEntity()
                openEditPanel(false)
            }
            ActionModelsEnum.FLIP -> imageView.flipSelectedEntity()
            ActionModelsEnum.EDIT_TEXT -> {
                openTextEditorDialog(imageView.selectedEntity)
            }
            ActionModelsEnum.CANCEL -> {
                imageView.unselectEntity()
                openEditPanel(false)
            }
            else -> {
            }
        }
    }


    private fun openTextEditorDialog(entity: MotionEntity?){
        if(entity is TextEntity){
            TextEditorDialogFragment.getInstance(entity.getTextLayer().text?:"")
                .show(supportFragmentManager,TextEditorDialogFragment::class.java.name)
        }
    }


    private fun openEditPanel(toShow: Boolean, textEditing:Boolean = true) {

        if(toShow) textPanelAdapter.dataset = (if(textEditing) getTextActions() else getImageActions()).toMutableList()
        editTextPanel?.visibility = if(toShow)View.VISIBLE else View.GONE
    }

    private fun saveImageAndShare() {
        if(imageView.isBaseImageLoaded){
            SaveAndShare.save(this,
                imageView.getThumbnailImage(),
                getImgName(),null,null)
        }else {
            GenericDialog.Builder(this)
                .setDialogTheme(R.style.v_i_dialog_style)
                .setTitle(getString(R.string.to_fast_string)).setTitleAppearance(android.R.color.white,23f)
                .setMessage(getString(R.string.to_fast_message)).setMessageAppearance(R.color.light_cyan,16f)
                .addNewButton(R.style.add_image_button_style) {
                    openChoosePhoto()
                }
                .setButtonOrientation(LinearLayout.HORIZONTAL)
                .setCancelable(true)
                .generate()
        }
    }

    private fun clearImage(){
        imageView.clear()
        add_image_hint.goneIf {
            imageView.isBaseImageLoaded
        }
    }


    companion object{
        const val MEME_REQ = 2738
    }




}

