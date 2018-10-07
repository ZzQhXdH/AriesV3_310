package util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.*
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import app.App

import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation

import com.bumptech.glide.request.target.SimpleTarget
import com.hontech.icecreamcustomclient.R
import com.wang.avi.AVLoadingIndicatorView


private val MENU_IMAGE_WIDTH = App.AppContext.resources.getDimension(R.dimen.x900).toInt()
private val MENU_IMAGE_HEIGHT = App.AppContext.resources.getDimension(R.dimen.y518).toInt()
private val MENU_IMAGE_RADIUS = App.AppContext.resources.getDimension(R.dimen.x25).toInt()


fun showToast(msg: String, context: Context = App.AppContext)
{
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}


fun convertViewToBitmap(view: View): Bitmap
{
    view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    view.buildDrawingCache()
    return view.drawingCache
}

fun ByteArray.toHexString(): String
{
    val sb = StringBuilder()
    this.forEach {
        sb.append(String.format("%02x ", it))
    }
    return sb.toString()
}

inline fun ByteArray.argInt(index: Int) = this[index + 2].toInt() and 0xFF

inline fun ByteArray.action() = this[2].toInt() and 0xFF

fun ByteArray.isCorrectOfResult(): Int
{
    if (this[1].toInt() != size) {
        return 1
    }
    var c = 0
    for (i in 3 until (size - 2)) { c = c xor this[i].toInt() }
    return if (c != this[size-2].toInt()) 2 else 0
}

fun ImageView.setImageNoSelectorAsync(url: String, loading: AVLoadingIndicatorView)
{
    this.setTag(R.id.imageId, url)
    Glide.with(App.AppContext)
            .load(url)
            .asBitmap()
            .override(MENU_IMAGE_WIDTH, MENU_IMAGE_HEIGHT)
            .into(object: SimpleTarget<Bitmap>() {

                override fun onLoadStarted(placeholder: Drawable?)
                {
                    val info = this@setImageNoSelectorAsync.getTag(R.id.imageId)
                    if ((info != null) && ((info as String)  == url))
                    {
                        this@setImageNoSelectorAsync.visibility = View.GONE
                        loading.show()
                    }
                }

                override fun onLoadFailed(e: java.lang.Exception?, errorDrawable: Drawable?)
                {
                    e?.printStackTrace()
                    val info = this@setImageNoSelectorAsync.getTag(R.id.imageId)
                    if ((info != null) && ((info as String)  == url))
                    {
                        e?.printStackTrace()
                        this@setImageNoSelectorAsync.visibility = View.VISIBLE
                        this@setImageNoSelectorAsync.setImageResource(R.drawable.ic_error)
                        loading.hide()
                    }
                }

                override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>?)
                {
                    val info = this@setImageNoSelectorAsync.getTag(R.id.imageId)
                    if ((info != null) && ((info as String)  == url))
                    {
                        this@setImageNoSelectorAsync.visibility = View.VISIBLE
                        this@setImageNoSelectorAsync.setImageBitmap(resource)
                        loading.hide()
                    }
                }
            })
}

fun ImageView.setImageAsync(url: String, loading: AVLoadingIndicatorView)
{
    this.setTag(R.id.imageId, url)
    Glide.with(App.AppContext)
            .load(url)
            .asBitmap()
            .override(MENU_IMAGE_WIDTH, MENU_IMAGE_HEIGHT)
            .into(object: SimpleTarget<Bitmap>() {

                override fun onLoadStarted(placeholder: Drawable?)
                {
                    val info = this@setImageAsync.getTag(R.id.imageId)
                    if ((info != null) && ((info as String)  == url))
                    {
                        this@setImageAsync.visibility = View.GONE
                        loading.show()
                    }
                }

                override fun onLoadFailed(e: java.lang.Exception?, errorDrawable: Drawable?)
                {
                    e?.printStackTrace()
                    val info = this@setImageAsync.getTag(R.id.imageId)
                    if ((info != null) && ((info as String)  == url))
                    {
                        this@setImageAsync.visibility = View.VISIBLE
                        this@setImageAsync.setImageResource(R.drawable.ic_error)
                        loading.hide()
                    }
                }

                override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>?)
                {
                    val info = this@setImageAsync.getTag(R.id.imageId)
                    if ((info != null) && ((info as String)  == url))
                    {
                        try {
                            val drawable = xCreateStateDrawable(resource, MENU_IMAGE_RADIUS)
                            this@setImageAsync.visibility = View.VISIBLE
                            this@setImageAsync.setImageDrawable(drawable)
                            loading.hide()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
}

fun Int.px(): Int
{
    val scale = App.AppContext.resources.displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

fun Int.dp(): Int
{
    val scale = App.AppContext.resources.displayMetrics.density
    return (this / scale + 0.5f).toInt()
}

fun xCreateStateDrawable(bitmap: Bitmap, radius: Int): Drawable
{
    val gradientDrawable = GradientDrawable()
    gradientDrawable.cornerRadius = radius.toFloat()
    gradientDrawable.setColor(App.AppContext.resources.getColor(R.color.colorShadow))

    val roundedDrawable = xCreateRoundedBitmap(bitmap, radius.dp())
    val layerDrawable = LayerDrawable(arrayOf(roundedDrawable, gradientDrawable))
    val stateDrawable = StateListDrawable()

    stateDrawable.addState(intArrayOf(android.R.attr.state_pressed), layerDrawable)
    stateDrawable.addState(intArrayOf(), roundedDrawable)

    return stateDrawable
}

fun xCreateRoundedBitmap(bitmap: Bitmap, radius: Int): Drawable
{
    val w= bitmap.width
    val h = bitmap.height
    val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bm)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rectF = RectF(0f, 0f, w.toFloat(), h.toFloat())
    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawRoundRect(rectF, radius.toFloat(), radius.toFloat(), paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, 0f, 0f, paint)
    return BitmapDrawable(bm)
}



