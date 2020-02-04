package com.example.workpackagelasse


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 *
 * Class to represent the bottom of the application. It consists of picture.
 */
class PictureBottomFragment : Fragment() {

    val MAX_HEIGHT_PIXELS: Int = 800
    val MAX_WIDTH_PIXELS: Int = 500

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.picture_bottom_fragment, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load a bitmap from the drawable folder
        val bMap = BitmapFactory.decodeResource(resources, R.drawable.picture_car)

        // Resize the bitmap to MAX_WIDTH_PIXELS x MAX_HEIGHT_PIXELS
        val bMapScaled = Bitmap.createScaledBitmap(bMap, MAX_WIDTH_PIXELS, MAX_HEIGHT_PIXELS, true)

        // Loads the resized Bitmap into an ImageView
        val image: ImageView = view.findViewById(R.id.picture_view) as ImageView
        image.setImageBitmap(bMapScaled)
    }
}

