package com.hooloovoochimico.badpiccollage

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.manzo.slang.extensions.color
import com.manzo.slang.extensions.goneIf
import com.manzo.slang.extensions.invisibleIf
import com.manzo.slang.navigation.toAdapter
import com.orhanobut.logger.Logger
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.meme_activity.*
import org.koin.android.ext.android.inject
import java.lang.Exception


class MemeActivity : AppCompatActivity(),IMemeView{
    override fun showList(memes: List<MemesItem?>?) {
       memesAdapter.dataset = memes?.map {
           it!!
       }?.toMutableList()?: mutableListOf()

        memesAdapter.notifyDataSetChanged()
    }

    override fun showError() {
        Toast.makeText(this,R.string.network_error,Toast.LENGTH_LONG).show()
    }

    private val imgVolatileStorage: ImageVolatileStorage by inject()

    private val memes = mutableListOf<MemesItem>()

    private val presenter : MemePresenter by inject()

    private val memesAdapter by lazy {
        memes.toAdapter(
            rowLayout = R.layout.meme_row_item,
            emptyLayout = R.layout.empty_meme_list,
            onBindContent = {holder,_, element ->

                holder.itemView.setOnClickListener {
                    downloadImage(element.url)
                }

                (holder[R.id.meme_description] as TextView).text = element.name

                Picasso.get().load(element.url).into((holder[R.id.meme_imgview]) as ImageView)

            }
        )
    }

    override fun onStart() {
        super.onStart()
        presenter.subscribe(this)
    }

    override fun onStop() {
        super.onStop()
        presenter.unsubscribe()
    }

    override fun onResume() {
        super.onResume()

        if(memes.isEmpty()) presenter.getMemes()
    }

    private fun downloadImage(url: String?) {
        Picasso.get().load(url).into(object: Target{
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                imgVolatileStorage.memeSelected = bitmap
                setResult(Activity.RESULT_OK)
                finish()
            }

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.meme_activity)

        actionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        meme_list.apply {
            layoutManager = LinearLayoutManager(this@MemeActivity)
            adapter = memesAdapter
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if(item?.itemId == android.R.id.home ){
            super.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showLoader(toShow: Boolean) {

        progress_wheel.barColor = color(R.color.colorPrimary)
        progress_wheel.goneIf {

            if(toShow) progress_wheel.spin() else progress_wheel.stopSpinning()

            !toShow


        }
    }
}

interface IMemeView{
    fun showList(memes: List<MemesItem?>?)
    fun showError()

    fun showLoader(toShow: Boolean = true)

}

class MemePresenter(private val logic:MemeLogic){


    private var disposable: Disposable? = null

    private var view: IMemeView? = null

    fun getMemes() {

        view?.showLoader()
        disposable = logic.getMemes().subscribe { bean, ex ->

            view?.showLoader(false)
            if(bean!= null){
            view?.showList(bean.data?.memes)
            }else {
                Logger.t(this::class.java.name).e(ex, "error")
                view?.showError()
            }
        }
    }

    fun subscribe(view: IMemeView){
        this.view = view
    }

    fun unsubscribe() {
        this.view = null
        try {
            disposable?.dispose()
        }catch (e:Exception){
            e.printStackTrace()
            Logger.t(this::class.java.name).e(e, "error")
        }
    }

}


class MemeLogic {


    fun getMemes(): Single<ImageBean>{
        return Single.defer {
            val cc = ImageServiceManager.getImages().execute()

            if(cc.isSuccessful){
                return@defer Single.just(cc.body()!!)
            }else {
                return@defer Single.error<ImageBean>(RuntimeException(cc.message()))
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }
}